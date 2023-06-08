#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>
#include <Wire.h>
#include <WiFi.h>
#include <PubSubClient.h>

WiFiClient espClient;
PubSubClient mqttClient(espClient);
Adafruit_MPU6050 mpu;

int led_red = 26;
int led_green = 14;
int led_yellow = 27;
int brightness = 0;      // how bright the LED is
int fadeAmount = 5;      // how many points to fade the LED by

#define IC2_SDA 33
#define I2C_SCL 32

const char* ssid = "MaraudersMap";
const char* password = "Page394%";
const char* mqttBroker = "192.168.0.89";
const int mqttPort = 1883;
const char* mpuTopic = "mpu/K05";
const char* tempTopic = "temp/K05";
const char* finishedTopic = "finished/K05";

hw_timer_t* timer = NULL;
portMUX_TYPE timerMux = portMUX_INITIALIZER_UNLOCKED;
volatile unsigned long previousMillis = 0;
const unsigned long interval = 1000; // Interval in milliseconds

void IRAM_ATTR onTimer() {
  portENTER_CRITICAL_ISR(&timerMux);
  previousMillis += interval;
  portEXIT_CRITICAL_ISR(&timerMux);
}

void onMqttMessageReceived(char* topic, byte* payload, unsigned int length) {
  // Handle MQTT message received
  // Convert payload to a string
  char message[length + 1];
  memcpy(message, payload, length);
  message[length] = '\0';

  // Display the received message in the console
  Serial.print("Received message on topic: ");
  Serial.print(topic);
  Serial.print(", payload: ");
  Serial.println(message);
}

void connectToWifi() {
  WiFi.begin(ssid, password);
  Serial.print("Connecting to Wi-Fi...");
  while (WiFi.status() != WL_CONNECTED) {
    delay(100);
  }
  Serial.println("connected!");

  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
}

void setupMqtt() {
  mqttClient.setServer(mqttBroker, mqttPort);
  mqttClient.setCallback(onMqttMessageReceived);

  Serial.print("Connecting to MQTT broker...");
  while (!mqttClient.connected()) {
    if (mqttClient.connect("ESP32Client")) {
      Serial.println("connected!");
      mqttClient.subscribe(finishedTopic);
    } else {
      Serial.print("failed, retrying in 5 seconds...");
      delay(5000);
    }
  }
}

void setup() {
  pinMode(led_green, OUTPUT);
  pinMode(led_red, OUTPUT);
  pinMode(led_yellow, OUTPUT);

  Wire.begin(33, 32);

  Serial.begin(115200);
  while (!Serial)
    delay(10);

  Serial.println("Adafruit MPU6050 test!");

  if (!mpu.begin()) {
    Serial.println("Failed to find MPU6050 chip");
    while (1) {
      delay(10);
    }
  }
  Serial.println("MPU6050 Found!");

  mpu.setAccelerometerRange(MPU6050_RANGE_8_G);
  Serial.print("Accelerometer range set to: +-8G");

  mpu.setGyroRange(MPU6050_RANGE_500_DEG);
  Serial.print("Gyro range set to: +- 1000 deg/s");

  mpu.setFilterBandwidth(MPU6050_BAND_21_HZ);
  Serial.print("Filter bandwidth set to: 44hz");
  Serial.println("");
  delay(3000);

  connectToWifi();

  setupMqtt();

  // Set up the hardware timer
  timer = timerBegin(0, 80, true);              // Timer 0, prescaler 80 (1MHz tick), count up
  timerAttachInterrupt(timer, &onTimer, true);  // Attach the timer ISR
  timerAlarmWrite(timer, interval * 1000, true); // Set the alarm to trigger every interval (in microseconds)
  timerAlarmEnable(timer);                       // Enable the alarm
}

void blink() {
  digitalWrite(led_green, HIGH);
  digitalWrite(led_red, HIGH);
  digitalWrite(led_yellow, HIGH);

  delay(30);

  digitalWrite(led_green, LOW);
  digitalWrite(led_red, LOW);
  digitalWrite(led_yellow, LOW);
}

void loop() {
  static unsigned long previousTempMillis = 0;
  static char tempValue[8]; // Buffer to store temperature value

  portENTER_CRITICAL(&timerMux);
  unsigned long currentMillis = previousMillis;
  portEXIT_CRITICAL(&timerMux);

  sensors_event_t a, g, temp;
  mpu.getEvent(&a, &g, &temp);

  // Store sensor values in a list
  String sensorValues = String(a.acceleration.x) + "," +
                        String(a.acceleration.y) + "," +
                        String(a.acceleration.z) + "," +
                        String(g.gyro.x) + "," +
                        String(g.gyro.y) + "," +
                        String(g.gyro.z);

  // Publish the list of sensor values
  mqttClient.publish(mpuTopic, sensorValues.c_str());

  // Publish gyro X value
  char gyroXValue[8]; // Buffer to store gyro X value
  snprintf(gyroXValue, sizeof(gyroXValue), "%f", g.gyro.x);
  mqttClient.publish(mpuTopic, gyroXValue);

  blink();

  Serial.print("Sensor Values Tuple: ");
  Serial.print(sensorValues);
  Serial.println("Acceleration X: ");
  Serial.print(a.acceleration.x);
  Serial.print(", Y: ");
  Serial.print(a.acceleration.y);
  Serial.print(", Z: ");
  Serial.print(a.acceleration.z);
  Serial.println(" m/s^2");

  Serial.print("Rotation X: ");
  Serial.print(g.gyro.x);
  Serial.print(", Y: ");
  Serial.print(g.gyro.y);
  Serial.print(", Z: ");
  Serial.print(g.gyro.z);
  Serial.println(" rad/s");

  if (currentMillis - previousTempMillis >= interval) {
    previousTempMillis = currentMillis;

    // Publish temperature value
    snprintf(tempValue, sizeof(tempValue), "%f", temp.temperature);
    mqttClient.publish(tempTopic, tempValue);
  }

  Serial.print("Temperature: ");
  Serial.print(temp.temperature);
  Serial.println(" degC");

  Serial.println("");
  delay(100);

  mqttClient.loop();
}

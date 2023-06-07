
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
int brightness = 0;  // how bright the LED is
int fadeAmount = 5;  // how many points to fade the LED by

#define IC2_SDA 33
#define I2C_SCL 32



const char* ssid = "Keine Ahnung";
const char* password = "Fragdeinemutter";
//bei public broker = "broker" teiweise
const char* mqttBroker = "192.168.0.188";
const int mqttPort = 1883;
const char* mqttTopic = "gyro_x";

void onMqttMessageReceived(char* topic, byte* payload, unsigned int length) {
  // Handle MQTT message received
  // Convert payload to a string
  String message;
  for (unsigned int i = 0; i < length; i++) {
    message += (char)payload[i];
  }

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
      mqttClient.subscribe(mqttTopic);
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

  //sets the pins for the MPU6050 chip 
  Wire.begin(33,32);
  
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

  // Connect to Wi-Fi
  connectToWifi();

  // Setup MQTT
  setupMqtt();
}


void fade(){
   // set the brightness of pin 9:
  analogWrite(led_green, brightness);
  analogWrite(led_red, brightness);
  analogWrite(led_yellow, brightness);


  // change the brightness for next time through the loop:
  brightness = brightness + fadeAmount;

  // reverse the direction of the fading at the ends of the fade:
  if (brightness <= 0 || brightness >= 255) {
    fadeAmount = -fadeAmount;
  }
  // wait for 30 milliseconds to see the dimming effect
  delay(30);
}

void blink(){
  digitalWrite(led_green, HIGH);
  digitalWrite(led_red, HIGH);
  digitalWrite(led_yellow, HIGH);

  delay(30);

  digitalWrite(led_green, LOW);
  digitalWrite(led_red, LOW);
  digitalWrite(led_yellow, LOW);
}

void loop() {
   /* Get new sensor events with the readings */
  sensors_event_t a, g, temp;
  mpu.getEvent(&a, &g, &temp);

  // Publish the g.gyro.x value
  String gyroXValue = String(g.gyro.x);
  mqttClient.publish(mqttTopic, gyroXValue.c_str());
  blink();

  /* Print out the values */
  Serial.print("Acceleration X: ");
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

  Serial.print("Temperature: ");
  Serial.print(temp.temperature);
  Serial.println(" degC");

  Serial.println("");
  delay(500);

  // Process MQTT messages
  mqttClient.loop();
}


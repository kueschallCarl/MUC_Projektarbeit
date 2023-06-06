
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>
#include <Wire.h>
#include <WiFi.h>
#include <PubSubClient.h>

WiFiClient espClient;
PubSubClient mqttClient(espClient);
Adafruit_MPU6050 mpu; 

#define I2C_SDA 0
#define I2C_SCL 4
const char* ssid = "MaraudersMap";
const char* password = "Page394%";
//bei public broker = "broker" teiweise
const char* mqttBroker = "192.168.0.89";
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
  //sets the pins for the MPU6050 chip IMPORTANT!!!!!!!!!!!!!!!!!!!!!!!!!
  Wire.begin(0,4);
  
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
  Serial.print("Gyro range set to: +- 500 deg/s");

  mpu.setFilterBandwidth(MPU6050_BAND_5_HZ);
  Serial.print("Filter bandwidth set to: 44hz");
  Serial.println("");
  delay(2000);

  // Connect to Wi-Fi
  connectToWifi();

  // Setup MQTT
  setupMqtt();
}




void loop() {
   /* Get new sensor events with the readings */
  sensors_event_t a, g, temp;
  mpu.getEvent(&a, &g, &temp);

  // Publish the g.gyro.x value
  String gyroXValue = String(g.gyro.x);
  mqttClient.publish(mqttTopic, gyroXValue.c_str());

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


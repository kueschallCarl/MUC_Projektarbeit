package com.example.menu_template;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * This class handles MQTT connection and handles MQTT events
 */
public class MqttManager {
    private static MqttManager instance;

    // tcp://192.168.0.89:1883
    public String MQTT_BROKER_IP = "198.162.0.89";
    public String MQTT_BROKER_PORT = "1883";
    public String MQTT_BROKER_METHOD = "tcp";
    private static final String MQTT_CLIENT_ID = "mosquitto_id";
    private static final String MPU_TOPIC = "mpu/K05";
    private MqttClient mqttClient;
    private MqttCallbackListener callbackListener;

    private MqttManager() {
        // Private constructor to prevent instantiation outside the class
    }

    /**
     * This method returns the singleton instance of MqttManager
     *
     * @return the MqttManager instance
     */
    public static MqttManager getInstance() {
        if (instance == null) {
            instance = new MqttManager();
        }
        return instance;
    }

    /**
     * This method sets the CallbackListener
     * The listener should implement the necessary methods defined in the MqttCallbackListener interface
     *
     * @param listener the CallbackListener
     * @see MqttCallbackListener
     */
    public void setCallbackListener(MqttCallbackListener listener) {
        this.callbackListener = listener;
    }

    /**
     * Publishes a message to a topic
     *
     * @param message the message to publish
     * @param topic   the topic to publish the message to
     */
    public void publishToTopic(String message, String topic) {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                mqttClient.publish(topic, mqttMessage);
                Log.d("MqttManager", "Published message: " + message + " on topic: " + topic);
            } else {
                Log.d("MqttManager", "Failed to publish message. MQTT client is not connected.");
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Subscribes to a topic
     *
     * @param topic the topic to subscribe
     */
    public void subscribeToTopic(String topic) {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.subscribe(topic);
                Log.d("MqttManager", "Subscribed to topic: " + topic);
            } else {
                Log.d("MqttManager", "Failed to subscribe to topic. MQTT client is not connected.");
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method attempts to connect to an MQTT broker
     */
    public void connect() {
        try {
            mqttClient = new MqttClient(MQTT_BROKER_METHOD + "://" + MQTT_BROKER_IP + ":" + MQTT_BROKER_PORT, MQTT_CLIENT_ID, new MemoryPersistence());
            mqttClient.setCallback(new MqttCallback() {

                @Override
                public void connectionLost(Throwable cause) {
                    if (callbackListener != null) {
                        callbackListener.onConnectionLost();
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload = new String(message.getPayload());
                    Log.d("mpu_message", payload);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            mqttClient.connect(options);

            if (mqttClient.isConnected()) {
                Log.d("MqttManager", "Connected to MQTT broker");
            } else {
                Log.d("MqttManager", "Failed to connect to MQTT broker");
            }

        } catch (MqttException e) {
            e.printStackTrace();
            if (callbackListener != null) {
                callbackListener.onConnectionError(e.getMessage());
            }
        }
    }

    /**
     * Handles disconnecting from the broker
     */
    public void disconnect() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.unsubscribe(MPU_TOPIC);
                mqttClient.disconnect();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}

package com.example.menu_template;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


/**
 * This class handles MQTT connection and handles MQTT events
 */
public class MqttManager {


        private static final String MQTT_BROKER_URL = "tcp://192.168.0.89:1883";
        private static final String MQTT_CLIENT_ID = "mosquitto_id";
        private static final String MPU_TOPIC = "mpu/K05";
        private MqttClient mqttClient;
        private MqttCallbackListener callbackListener;


    /**
     * This method sets the CallbackListener
     * The listener should implement the necessary methods defined in the MqttCallback interface
     * @param listener the CallbackListener
     * @see MqttCallbackListener
     */
    public void setCallbackListener(MqttCallbackListener listener) {
        this.callbackListener = listener;
    }

    /**
     * This method attempts to connect to an MQTT broker
     */
    public void connect() {
            try {
                mqttClient = new MqttClient(MQTT_BROKER_URL, MQTT_CLIENT_ID, new MemoryPersistence());
                mqttClient.setCallback(new MqttCallback() {

                    /**
                     * Handles connection-loss
                     * @param cause the reason behind the loss of connection.
                     */
                    @Override
                    public void connectionLost(Throwable cause) {
                        // Handle connection lost
                    }

                    /**
                     *
                     * @param topic name of the topic on the message was published to
                     * @param message the actual message.
                     * @throws Exception is caught in the catch below
                     */
                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        // Handle received message
                        String payload = new String(message.getPayload());
                        // Process the payload as per your game logic
                        Log.d("mpu_message", payload);

                    }

                    /**
                     * Handles delivery status information
                     * @param token the delivery token associated with the message. Provides information about the delivery status.
                     */
                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        // Handle message delivery complete
                    }
                });

                //Create options instance
                MqttConnectOptions options = new MqttConnectOptions();
                //set CleanSessions to true the broker won't queue any messages for the client while it was disconnected.
                options.setCleanSession(true);

                //Finally attempt to connect to the broker
                mqttClient.connect(options);

                //Check if connection was successful or not and log that information
                if (mqttClient.isConnected()) {
                    Log.d("MqttManager", "Connected to MQTT broker");
                } else {
                    Log.d("MqttManager", "Failed to connect to MQTT broker");
                }

                //subscribes to certain topics
                mqttClient.subscribe(MPU_TOPIC);
                Log.d("MqttManager", "Subscribed to topic: " + MPU_TOPIC);
            } catch (MqttException e) {
                e.printStackTrace();
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


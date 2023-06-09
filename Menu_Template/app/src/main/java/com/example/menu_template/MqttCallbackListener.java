package com.example.menu_template;

/**
 * provides interface to handle MQTT events for clients to implement
 * @see MqttManager
 * @see SecondFragment
 */
public interface MqttCallbackListener {
    void onMessageReceived(String topic, String message);

    void onConnectionLost();

    void onConnectionError(String message);
}

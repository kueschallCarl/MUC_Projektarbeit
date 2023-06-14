package com.example.menu_template;

import android.content.Context;
import android.util.Log;

public class SecondListener implements MqttCallbackListener {
    private MqttManager mqttManager;
    private SettingsDatabase settingsDatabase;
    public SecondListener(Context context){
        this.mqttManager = new MqttManager("second_listener");
        this.settingsDatabase = SettingsDatabase.getInstance(context);
        mqttManager.connect(settingsDatabase, "second_listener");
        mqttManager.setCallbackListener(this);
        mqttManager.subscribeToTopic(Constants.MPU_TOPIC);
    }
    @Override
    public void onMessageReceived(String topic, String message) {
        if (topic.equals(Constants.MPU_TOPIC)) {
            Log.d("MPUFirstListener", "In first listener" + message);
        }    }

    @Override
    public void onConnectionLost() {
        // Handle the connection loss in the second listener
    }

    @Override
    public void onConnectionError(String message) {
        // Handle the connection error in the second listener
    }
}
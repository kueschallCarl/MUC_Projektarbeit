package com.example.menu_template;

import android.content.Context;
import android.mtp.MtpConstants;
import android.util.Log;

public class FirstListener implements MqttCallbackListener {
    private MqttManager mqttManager;
    private SettingsDatabase settingsDatabase;
    public FirstListener(Context context){
        this.mqttManager = new MqttManager("first_listener");
        this.settingsDatabase = SettingsDatabase.getInstance(context);
        mqttManager.connect(settingsDatabase, "first_listener");
        mqttManager.setCallbackListener(this);
        mqttManager.subscribeToTopic(Constants.TEMP_TOPIC);
    }
    @Override
    public void onMessageReceived(String topic, String message) {
        if (topic.equals(Constants.TEMP_TOPIC)) {
            Log.d("tempFirstListener", "In first listener" + message);
        }
    }

    @Override
    public void onConnectionLost() {
        // Handle the connection loss in the first listener
    }

    @Override
    public void onConnectionError(String message) {
        // Handle the connection error in the first listener
    }
}
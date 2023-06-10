package com.example.menu_template;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.menu_template.MqttManager;
import com.example.menu_template.MqttCallbackListener;
import com.example.menu_template.Constants.*;

public class GameLogic implements MqttCallbackListener{

    private MqttManager mqttManager;
    private Context context;

    public GameLogic(Context context) {
        this.context = context;
        mqttManager = MqttManager.getInstance();
        mqttManager.setCallbackListener(this);
        mqttManager.connect();

        mqttManager.publishToTopic("0", Constants.FINISHED_TOPIC);
        mqttManager.subscribeToTopic(Constants.TEMP_TOPIC);
    }

    /**
     * This method implements the MqttCallbackListener interface for onMessageReceived()
     * @param topic the MQTT topic
     * @param message the current message received for that MQTT topic
     */
    @Override
    public void onMessageReceived(String topic, String message) {
        if (topic.equals(Constants.TEMP_TOPIC)) {
            // Handle received message
            String payload = new String(message);
            // Process the payload as per your game logic
            Log.d(Constants.TEMP_TOPIC, payload);
        }
    }

    @Override
    public void onConnectionLost() {
        // Handle connection lost
        // Show alert to the user
        showAlert("Connection Lost", "The MQTT connection to "+
                mqttManager.MQTT_BROKER_METHOD+"://"+mqttManager.MQTT_BROKER_IP+":"+mqttManager.MQTT_BROKER_PORT + "was lost.");
    }
    @Override
    public void onConnectionError(String errorMessage) {
        // Handle connection error
        // Show alert to the user with the error message
        showAlert("Connection Error", "Failed to connect to the MQTT broker at: " +
                mqttManager.MQTT_BROKER_METHOD+"://"+mqttManager.MQTT_BROKER_IP+":"+mqttManager.MQTT_BROKER_PORT);
    }
    private void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}

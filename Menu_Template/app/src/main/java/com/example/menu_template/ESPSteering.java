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

/**
 * This class parses the ESP32's Accelerometer/Gyro-Value-Strings received through the MPU_TOPIC,
 * so that the GameLogic Class can access that data to calculate game-physics etc.
 */
public class ESPSteering implements MqttCallbackListener{

    private MqttManager mqttManager;
    private Context context;
    private float acc_x;
    private float acc_y;
    private float acc_z;
    private float gyro_x;
    private float gyro_y;
    private float gyro_z;
    public ESPSteering(Context context) {
        this.context = context;
        mqttManager = MqttManager.getInstance();
        mqttManager.setCallbackListener(this);
        mqttManager.subscribeToTopic(Constants.MPU_TOPIC);

    }

    /**
     * This method implements the MqttCallbackListener interface for onMessageReceived()
     * @param topic the MQTT topic
     * @param message the current message received for that MQTT topic
     */
    @Override
    public void onMessageReceived(String topic, String message) {
        if (topic.equals(Constants.MPU_TOPIC)) {
            parseAndAssignValues(message);
            // Handle received message
            String payload = new String(message);
            // Process the payload as per your game logic
            Log.d(Constants.MPU_TOPIC, payload);
        }


    }

    private void parseAndAssignValues(String message) {
        String[] values = message.replaceAll("[()]", "").split(",");
        if (values.length == 6) {
            acc_x = Float.parseFloat(values[0]);
            acc_y = Float.parseFloat(values[1]);
            acc_z = Float.parseFloat(values[2]);
            gyro_x = Float.parseFloat(values[3]);
            gyro_y = Float.parseFloat(values[4]);
            gyro_z = Float.parseFloat(values[5]);
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

    public float getAccX() {
        return acc_x;
    }

    public float getAccY() {
        return acc_y;
    }

    public float getAccZ() {
        return acc_z;
    }

    public float getGyroX() {
        return gyro_x;
    }

    public float getGyroY() {
        return gyro_y;
    }

    public float getGyroZ() {
        return gyro_z;
    }
}

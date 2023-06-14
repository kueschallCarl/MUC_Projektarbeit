package com.example.menu_template;
import android.app.AlertDialog;
import android.content.Context;
import android.hardware.SensorManager;
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

import java.util.Arrays;

/**
 * This class parses the ESP32's Accelerometer/Gyro-Value-Strings received through the MPU_TOPIC,
 * so that the GameLogic Class can access that data to calculate game-physics etc.
 */
public class ESPSteering{

    private MqttManager mqttManager;
    private Context context;
    private float acc_x;
    private float acc_y;
    private float acc_z;
    private float gyro_x;
    private float gyro_y;
    private float gyro_z;

    private FirstListener firstListener;

    public ESPSteering(Context context) {
        this.context = context;
        this.mqttManager = new MqttManager("esp_steering");

        firstListener = new FirstListener();
        mqttManager.setCallbackListener(firstListener);
    }


    public void startSensors() {
        mqttManager.subscribeToTopic(Constants.MPU_TOPIC);
    }

    public void stopSensors() {
        mqttManager.unsubscribeFromTopic(Constants.MPU_TOPIC);

    }

    private class FirstListener implements MqttCallbackListener {
        @Override
        public void onMessageReceived(String topic, String message) {
            if (topic.equals(Constants.MPU_TOPIC)) {
                parseAndAssignValues(message);
                Log.d(Constants.MPU_TOPIC, message);
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
        public void onConnectionError(String message) {
            // Handle connection error
            // Show alert to the user with the error message
            showAlert("Connection Error", "Failed to connect to the MQTT broker at: " +
                    mqttManager.MQTT_BROKER_METHOD+"://"+mqttManager.MQTT_BROKER_IP+":"+mqttManager.MQTT_BROKER_PORT);
        }
    }



    private void parseAndAssignValues(String message) {
        String[] values = message.replaceAll("[()]", "").split(",");
        if (values.length == 6) {
            try {
                acc_x = Float.parseFloat(values[0]);
                acc_y = Float.parseFloat(values[1]);
                acc_z = Float.parseFloat(values[2]);
                gyro_x = Float.parseFloat(values[3]);
                gyro_y = Float.parseFloat(values[4]);
                gyro_z = Float.parseFloat(values[5]);

                Log.d("ParsedValues", "acc_x: " + acc_x + ", acc_y: " + acc_y + ", acc_z: " + acc_z
                        + ", gyro_x: " + gyro_x + ", gyro_y: " + gyro_y + ", gyro_z: " + gyro_z);
            } catch (NumberFormatException e) {
                Log.e("ParseError", "Error parsing values: " + e.getMessage());
            }
        } else {
            Log.d("NumberOfValues", "Invalid number of values: " + values.length);
        }
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

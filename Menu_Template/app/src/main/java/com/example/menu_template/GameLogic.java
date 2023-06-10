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

import java.util.Random;

public class GameLogic implements MqttCallbackListener {

    public MqttManager mqttManager;
    private Context context;
    private final ESPSteering espSteering;
    private final PhoneSteering phoneSteering;

    private int[][] labyrinth;

    public GameLogic(Context context) {
        this.context = context;

        this.mqttManager = MqttManager.getInstance();
        mqttManager.setCallbackListener(this);
        mqttManager.connect();

        this.espSteering = new ESPSteering(context);
        this.phoneSteering = new PhoneSteering();

        mqttManager.publishToTopic("0", Constants.FINISHED_TOPIC);
        mqttManager.subscribeToTopic(Constants.TEMP_TOPIC);

        generateLabyrinth();
    }

    private float[] getValuesFromESP() {
        // Replace with your implementation of getting values from ESP steering
        float accX = espSteering.getAccX();
        float accY = espSteering.getAccY();
        float accZ = espSteering.getAccZ();
        float gyroX = espSteering.getGyroX();
        float gyroY = espSteering.getGyroY();
        float gyroZ = espSteering.getGyroZ();

        return new float[]{accX, accY, accZ, gyroX, gyroY, gyroZ};
    }

    /**
     * This method generates a randomly generated 10x10 labyrinth.
     * The labyrinth is represented as a 2D array of integers.
     * 0 represents a path, and 1 represents a wall.
     */
    private void generateLabyrinth() {
        Random random = new Random();
        labyrinth = new int[10][10];

        // Generate the labyrinth
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                // Randomly assign 1 (wall) or 0 (path)
                labyrinth[i][j] = random.nextInt(2);
            }
        }
    }

    /**
     * This method implements the MqttCallbackListener interface for onMessageReceived()
     *
     * @param topic   the MQTT topic
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
        showAlert("Connection Lost", "The MQTT connection to " +
                mqttManager.MQTT_BROKER_METHOD + "://" + mqttManager.MQTT_BROKER_IP + ":" + mqttManager.MQTT_BROKER_PORT + " was lost.");
    }

    @Override
    public void onConnectionError(String errorMessage) {
        // Handle connection error
        // Show alert to the user with the error message
        showAlert("Connection Error", "Failed to connect to the MQTT broker at: " +
                mqttManager.MQTT_BROKER_METHOD + "://" + mqttManager.MQTT_BROKER_IP + ":" + mqttManager.MQTT_BROKER_PORT);
    }

    private void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    public ESPSteering getEspSteering() {
        return espSteering;
    }

    public PhoneSteering getPhoneSteering() {
        return phoneSteering;
    }

    public int[][] getLabyrinth() {
        return labyrinth;
    }
}

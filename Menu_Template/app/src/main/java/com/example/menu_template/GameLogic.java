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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

        try {
            generateLabyrinth();
            Log.d("Labyrinth", "Labyrinth: "+ Arrays.deepToString(this.labyrinth));
        }
        catch(Exception e){
            Log.d("Labyrinth", "Problem generating Labyrinth in GameLogic.java", e);
        }
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

    private void generateLabyrinth() {
        this.labyrinth = new int[10][10];

        // Set the boundary walls
        for (int i = 0; i < 10; i++) {
            labyrinth[0][i] = 1;         // Top boundary
            labyrinth[9][i] = 1;         // Bottom boundary
            labyrinth[i][0] = 1;         // Left boundary
            labyrinth[i][9] = 1;         // Right boundary
        }

        // Set the start and end blocks
        labyrinth[0][getRandomNumber(1, 8)] = 2;    // Start block on the top row (excluding the corners)
        labyrinth[9][getRandomNumber(1, 8)] = 3;    // End block on the bottom row (excluding the corners)

        // Generate a random path
        generatePath(0, getRandomNumber(1, 8), getRandomNumber(1, 8), 9);

        // Set remaining walls and free spaces
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                if (labyrinth[i][j] == 0)
                    labyrinth[i][j] = 1;     // Set remaining free spaces to walls
                else if (labyrinth[i][j] == -1)
                    labyrinth[i][j] = 0;     // Set path markers to free spaces
            }
        }
    }

    private void generatePath(int startX, int startY, int endX, int endY) {
        labyrinth[startX][startY] = -1;    // Mark the starting point as part of the path

        int currentX = startX;
        int currentY = startY;

        while (currentX != endX || currentY != endY) {
            List<int[]> neighbors = getNeighbors(currentX, currentY);
            Collections.shuffle(neighbors);     // Randomly shuffle the neighbor list

            boolean pathFound = false;
            for (int[] neighbor : neighbors) {
                int neighborX = neighbor[0];
                int neighborY = neighbor[1];

                if (labyrinth[neighborX][neighborY] == 0) {
                    labyrinth[neighborX][neighborY] = -1;    // Mark the neighbor as part of the path
                    currentX = neighborX;
                    currentY = neighborY;
                    pathFound = true;
                    break;
                }
            }

            if (!pathFound) {
                // No available neighbors, backtrack to find a new path
                boolean backtrack = true;
                while (backtrack) {
                    int[] previous = findPrevious(currentX, currentY);
                    currentX = previous[0];
                    currentY = previous[1];
                    if (labyrinth[currentX][currentY] == -1)
                        labyrinth[currentX][currentY] = 0;    // Reset the previous path marker
                    else
                        backtrack = false;
                }
            }
        }
    }

    private List<int[]> getNeighbors(int x, int y) {
        List<int[]> neighbors = new ArrayList<>();

        // Add the neighbors above, below, left, and right
        if (x > 0) neighbors.add(new int[]{x - 1, y});
        if (x < 9) neighbors.add(new int[]{x + 1, y});
        if (y > 0) neighbors.add(new int[]{x, y - 1});
        if (y < 9) neighbors.add(new int[]{x, y + 1});

        return neighbors;
    }

    private int[] findPrevious(int x, int y) {
        for (int i = x - 1; i >= 0; i--) {
            for (int j = y - 1; j >= 0; j--) {
                if (labyrinth[i][j] == -1)
                    return new int[]{i, j};
            }
        }
        return new int[]{-1, -1};   // Invalid previous position
    }

    private int getRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
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

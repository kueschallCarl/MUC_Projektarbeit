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
import java.util.Stack;

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

        // Set all cells as walls
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                labyrinth[i][j] = 1;
            }
        }

        // Choose a random starting point
        int startX = getRandomNumber(0, 9);
        int startY = getRandomNumber(0, 9);

        labyrinth[startX][startY] = 0; // Mark the starting point as part of the maze

        // Create a stack to keep track of visited cells
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{startX, startY});

        while (!stack.isEmpty()) {
            int[] currentCell = stack.peek();
            int currentX = currentCell[0];
            int currentY = currentCell[1];

            // Get the unvisited neighbors of the current cell
            List<int[]> unvisitedNeighbors = getUnvisitedNeighbors(currentX, currentY);
            if (!unvisitedNeighbors.isEmpty()) {
                // Choose a random unvisited neighbor
                int[] randomNeighbor = unvisitedNeighbors.get(getRandomNumber(0, unvisitedNeighbors.size() - 1));
                int neighborX = randomNeighbor[0];
                int neighborY = randomNeighbor[1];

                // Remove the wall between the current cell and the chosen neighbor
                int wallX = (currentX + neighborX) / 2;
                int wallY = (currentY + neighborY) / 2;
                labyrinth[wallX][wallY] = 0;

                labyrinth[neighborX][neighborY] = 0; // Mark the neighbor as part of the maze
                stack.push(new int[]{neighborX, neighborY});
            } else {
                // All neighbors visited, backtrack
                stack.pop();
            }
        }
    }

    private List<int[]> getUnvisitedNeighbors(int x, int y) {
        List<int[]> unvisitedNeighbors = new ArrayList<>();

        // Check the four cardinal directions
        if (x > 1 && labyrinth[x - 2][y] == 1) {
            unvisitedNeighbors.add(new int[]{x - 2, y});
        }
        if (x < 8 && labyrinth[x + 2][y] == 1) {
            unvisitedNeighbors.add(new int[]{x + 2, y});
        }
        if (y > 1 && labyrinth[x][y - 2] == 1) {
            unvisitedNeighbors.add(new int[]{x, y - 2});
        }
        if (y < 8 && labyrinth[x][y + 2] == 1) {
            unvisitedNeighbors.add(new int[]{x, y + 2});
        }

        return unvisitedNeighbors;
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

}

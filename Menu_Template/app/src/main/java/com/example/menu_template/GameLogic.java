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

public class GameLogic implements MqttCallbackListener{

    public MqttManager mqttManager;
    private Context context;

    private ESPSteering espSteering;
    private PhoneSteering phoneSteering;
    private static final int MAZE_SIZE = 10;
    public static final int WALL = 0;
    private static final int PATH = 1;

    private int[][] labyrinth;

    public GameLogic(Context context) {
        this.context = context;
        mqttManager = MqttManager.getInstance();
        mqttManager.setCallbackListener(this);
        mqttManager.connect();
        espSteering = new ESPSteering(context);
        phoneSteering = new PhoneSteering();
        mqttManager.publishToTopic("0", Constants.FINISHED_TOPIC);
        mqttManager.subscribeToTopic(Constants.TEMP_TOPIC);

        generateLabyrinth();

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


    public float[] getSensorDataFromESPSteering(){
        float[] sensorData = new float[6];
        sensorData[0] = espSteering.getAccX();
        sensorData[1] = espSteering.getAccY();
        sensorData[2] = espSteering.getAccZ();
        sensorData[3] = espSteering.getGyroX();
        sensorData[4] = espSteering.getGyroY();
        sensorData[5] = espSteering.getGyroZ();
        return sensorData;
    }

    private void generateLabyrinth() {
        labyrinth = new int[MAZE_SIZE][MAZE_SIZE];

        // Initialize all cells as walls
        for (int i = 0; i < MAZE_SIZE; i++) {
            for (int j = 0; j < MAZE_SIZE; j++) {
                labyrinth[i][j] = WALL;
            }
        }

        // Generate a random path through the labyrinth
        // For simplicity, let's start at the top-left corner (0, 0) and end at the bottom-right corner (MAZE_SIZE-1, MAZE_SIZE-1)
        labyrinth[0][0] = PATH; // Start cell

        // Perform a random walk to create the path
        int currentX = 0;
        int currentY = 0;
        Random random = new Random();

        while (currentX != MAZE_SIZE - 1 || currentY != MAZE_SIZE - 1) {
            int direction = random.nextInt(4); // Randomly choose a direction (up, down, left, or right)

            // Move in the chosen direction if it's a valid move
            switch (direction) {
                case 0: // Up
                    if (currentY > 0 && labyrinth[currentX][currentY - 1] != PATH) {
                        currentY--;
                        labyrinth[currentX][currentY] = PATH;
                    }
                    break;
                case 1: // Down
                    if (currentY < MAZE_SIZE - 1 && labyrinth[currentX][currentY + 1] != PATH) {
                        currentY++;
                        labyrinth[currentX][currentY] = PATH;
                    }
                    break;
                case 2: // Left
                    if (currentX > 0 && labyrinth[currentX - 1][currentY] != PATH) {
                        currentX--;
                        labyrinth[currentX][currentY] = PATH;
                    }
                    break;
                case 3: // Right
                    if (currentX < MAZE_SIZE - 1 && labyrinth[currentX + 1][currentY] != PATH) {
                        currentX++;
                        labyrinth[currentX][currentY] = PATH;
                    }
                    break;
            }
        }
    }
    public int[][] getLabyrinth() {
        return labyrinth;
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

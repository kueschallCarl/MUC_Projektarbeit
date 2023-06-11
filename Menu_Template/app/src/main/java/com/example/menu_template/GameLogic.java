package com.example.menu_template;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class GameLogic implements MqttCallbackListener {

    public MqttManager mqttManager;
    private Context context;
    public final ESPSteering espSteering;
    public final PhoneSteering phoneSteering;
    private int[][] labyrinth;
    private final int size = 28;

    public GameLogic(Context context) {
        this.context = context;

        this.mqttManager = MqttManager.getInstance();
        mqttManager.setCallbackListener(this);
        mqttManager.connect();

        this.espSteering = new ESPSteering(context);
        this.phoneSteering = new PhoneSteering(context);

        mqttManager.publishToTopic("0", Constants.FINISHED_TOPIC);
        mqttManager.subscribeToTopic(Constants.TEMP_TOPIC);

        try {
            generateLabyrinth();
            Log.d("Labyrinth", "Labyrinth: " + Arrays.deepToString(this.labyrinth));
        } catch (Exception e) {
            Log.d("Labyrinth", "Problem generating Labyrinth in GameLogic.java", e);
        }
    }

    private float[] getValuesFromESPSensor() {
        // Replace with your implementation of getting values from ESP steering
        float accX = espSteering.getAccX();
        float accY = espSteering.getAccY();
        float accZ = espSteering.getAccZ();
        float gyroX = espSteering.getGyroX();
        float gyroY = espSteering.getGyroY();
        float gyroZ = espSteering.getGyroZ();

        return new float[]{accX, accY, accZ, gyroX, gyroY, gyroZ};
    }

    private float[] getValuesFromPhoneSensor() {
        // Replace with your implementation of getting values from ESP steering
        float accX = phoneSteering.getAccX();
        float accY = phoneSteering.getAccY();
        float accZ = phoneSteering.getAccZ();
        float gyroX = phoneSteering.getGyroX();
        float gyroY = phoneSteering.getGyroY();
        float gyroZ = phoneSteering.getGyroZ();

        return new float[]{accX, accY, accZ, gyroX, gyroY, gyroZ};
    }


    private int parsePlayerDirection(float[] sensorData) {
        // Assuming the gyro values determine the direction
        float gyroX = sensorData[3];
        float gyroY = sensorData[4];
        float gyroZ = sensorData[5];

        // Adjust the thresholds based on your specific requirements
        float threshold = 0.5f;

        // Check the absolute values of gyroX and gyroY to determine the direction
        if (Math.abs(gyroX) > Math.abs(gyroY)) {
            if (gyroX > threshold) {
                // Player is tilting the phone or ESP to the right
                int direction = 0;
                Log.d("direction", "Returned direction: " + direction);
                return direction; // Right direction
            } else if (gyroX < -threshold) {
                // Player is tilting the phone or ESP to the left
                int direction = 1;
                Log.d("direction", "Returned direction: " + direction);
                return direction; // Left direction
            }
        } else {
            if (gyroY > threshold) {
                // Player is tilting the phone or ESP forward
                int direction = 2;
                Log.d("direction", "Returned direction: " + direction);
                return direction; // Forward direction
            } else if (gyroY < -threshold) {
                // Player is tilting the phone or ESP backward
                int direction = 3;
                Log.d("direction", "Returned direction: " + direction);
                return direction; // Backward direction
            }
        }

        // If none of the conditions match, return a default direction
        int defaultDirection = -1;
        Log.d("direction", "Returned default direction: " + defaultDirection);
        return defaultDirection;
    }


    public int getPlayerDirection(String steeringType){
        float[] sensor_data = new float[6];
        switch (steeringType) {
            case "ESP":
                this.espSteering.startSensors();
                sensor_data = getValuesFromESPSensor();
                break;
            case "Phone":
                this.phoneSteering.startSensors();
                sensor_data = getValuesFromPhoneSensor();
                break;
        }
        return parsePlayerDirection(sensor_data);
    }


    private void generateLabyrinth() {
        this.labyrinth = new int[size][size];

        // Set all cells as walls
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                labyrinth[i][j] = 1;
            }
        }

        // Choose a random starting point on the top boundary
        int startX = 0;
        int startY = getRandomNumber(1, size - 2);
        labyrinth[startX][startY] = 2; // Mark the starting point as 2

        // Choose a random ending point on the bottom boundary
        int endX = size - 1;
        int endY = getRandomNumber(1, size - 2);
        labyrinth[endX][endY] = 3; // Mark the ending point as 3

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
                // Check if the end point has adjacent 0's, if not, regenerate the labyrinth
                if (!hasAdjacentZeros(endX, endY)) {
                    Log.d("Labyrinth", "Labyrinth before regen: " + Arrays.deepToString(this.labyrinth));
                    generateLabyrinth();
                }
            }

    private boolean hasAdjacentZeros(int x, int y) {
        // Check the four cardinal directions
        if (x > 0 && labyrinth[x - 1][y] == 0) {
            return true; // There is a 0 to the north
        }
        if (x < size - 1 && labyrinth[x + 1][y] == 0) {
            return true; // There is a 0 to the south
        }
        if (y > 0 && labyrinth[x][y - 1] == 0) {
            return true; // There is a 0 to the west
        }
        if (y < size - 1 && labyrinth[x][y + 1] == 0) {
            return true; // There is a 0 to the east
        }

        return false; // No adjacent 0's
    }


    private List<int[]> getUnvisitedNeighbors(int x, int y) {
        List<int[]> unvisitedNeighbors = new ArrayList<>();

        // Check the four cardinal directions
        if (x > 1 && labyrinth[x - 2][y] == 1) {
            unvisitedNeighbors.add(new int[]{x - 2, y});
        }
        if (x < size - 2 && labyrinth[x + 2][y] == 1) {
            unvisitedNeighbors.add(new int[]{x + 2, y});
        }
        if (y > 1 && labyrinth[x][y - 2] == 1) {
            unvisitedNeighbors.add(new int[]{x, y - 2});
        }
        if (y < size - 2 && labyrinth[x][y + 2] == 1) {
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

    public int[][] getLabyrinth() {
        return labyrinth;
    }
}
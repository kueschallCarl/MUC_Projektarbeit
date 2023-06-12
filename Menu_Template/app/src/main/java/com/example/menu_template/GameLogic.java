package com.example.menu_template;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
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
    public int[][] labyrinth;
    private final int size = 10;
    private Handler handler; // Handler to run code on the main thread

    private int lastValidDirection = -1; // Store the last valid direction

    public GameLogic(Context context, SettingsDatabase settingsDatabase) {
        this.context = context;
        handler = new Handler(); // Initialize the handler
        this.mqttManager = MqttManager.getInstance();
        mqttManager.setCallbackListener(this);
        mqttManager.connect(settingsDatabase);

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


    public boolean gameStep(String steeringType) {
        Log.d("gameLoop", "Game Loop started");
        startSensors(steeringType);
        Log.d("gameLoop", "Sensors started");

        int playerDirection = getPlayerDirection(steeringType);
        Log.d("playerDirection", String.valueOf(playerDirection));
        labyrinth = movePlayer(labyrinth, playerDirection);

        if (isLabyrinthEmpty(labyrinth)) {

            showAlert("YOU WIN!", "You have successfully completed the labyrinth!");
            return true;
        }

        else{
            try {
                Thread.sleep(100); // Add a 100ms delay
            } catch (InterruptedException e) {
                e.printStackTrace();

            }
            return false;
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
        Log.d("ESPSensor", "ESPSensorValue: "+ Arrays.toString(new float[]{accX, accY, accZ, gyroX, gyroY, gyroZ}));

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

        Log.d("PhoneSensor", "PhoneSensorValue: "+ Arrays.toString(new float[]{accX, accY, accZ, gyroX, gyroY, gyroZ}));

        return new float[]{accX, accY, accZ, gyroX, gyroY, gyroZ};
    }



    private int parsePlayerDirection(float[] sensorData) {
        // Assuming the gyro values determine the direction
        float gyroX = sensorData[3];
        float gyroY = sensorData[4];
        float gyroZ = sensorData[5];

        // Adjust the thresholds based on your specific requirements
        float threshold = 0.6f;

        // Check the absolute values of gyroX and gyroY to determine the direction
        if (Math.abs(gyroX) > Math.abs(gyroY)) {
            if (gyroX > threshold) {
                // Player is tilting the phone or ESP to the right
                lastValidDirection = 1;
            } else if (gyroX < -threshold) {
                // Player is tilting the phone or ESP to the left
                lastValidDirection = 0;
            }
        } else {
            if (gyroY > threshold) {
                // Player is tilting the phone or ESP forward
                lastValidDirection = 2;
            } else if (gyroY < -threshold) {
                // Player is tilting the phone or ESP backward
                lastValidDirection = 3;
            }
        }

        Log.d("direction", "Returned direction: " + lastValidDirection);
        return lastValidDirection;
    }



    public int getPlayerDirection(String steeringType){
        float[] sensor_data = new float[6];
        switch (steeringType) {
            case "ESP32":
                sensor_data = getValuesFromESPSensor();
                break;
            case "Phone":
                sensor_data = getValuesFromPhoneSensor();
                break;
        }
        return parsePlayerDirection(sensor_data);
    }

    public void startSensors(String steeringType) {
        switch (steeringType) {
            case "ESP32":
                this.espSteering.startSensors();
                Log.d("gameLoop", "ESPSensor started");
                break;
            case "Phone":
                this.phoneSteering.startSensors();
                Log.d("gameLoop", "PhoneSensor started");
                break;
            default:
                Log.d("gameLoop", "Steering Type unknown " + steeringType);
        }
    }

    public void stopSensors(String steeringType) {
        switch (steeringType) {
            case "ESP32":
                this.espSteering.stopSensors();
                break;
            case "Phone":
                this.phoneSteering.stopSensors();
                break;
        }
    }

    public int[][] movePlayer(int[][] labyrinth, int playerDirection) {
        int playerX = -1;
        int playerY = -1;

        // Find the current position of the player (value 2) in the labyrinth
        for (int i = 0; i < labyrinth.length; i++) {
            for (int j = 0; j < labyrinth[i].length; j++) {
                if (labyrinth[i][j] == 2) {
                    playerX = i;
                    playerY = j;
                    break;
                }
            }
        }

        if (playerX == -1 || playerY == -1) {
            // Player not found in the labyrinth
            Log.d("movePlayer", "Player not found in the labyrinth");
            return labyrinth;
        }

        // Determine the new position based on the player's direction
        int newPlayerX = playerX;
        int newPlayerY = playerY;

        switch (playerDirection) {
            case 0: // Right direction
                newPlayerY++;
                break;
            case 1: // Left direction
                newPlayerY--;
                break;
            case 2: // Forward direction
                newPlayerX--;
                break;
            case 3: // Backward direction
                newPlayerX++;
                break;
            default:
                // Invalid direction
                Log.d("movePlayer", "Invalid direction: " + playerDirection);
                return labyrinth;
        }

        // Check if the new position is within the bounds of the labyrinth
        if (newPlayerX < 0 || newPlayerX >= labyrinth.length || newPlayerY < 0 || newPlayerY >= labyrinth[0].length) {
            // Player is out of bounds
            Log.d("movePlayer", "Player is out of bounds");
            return labyrinth;
        }

        // Check if the new position is a wall (value 1)
        if (labyrinth[newPlayerX][newPlayerY] == 1) {
            // Player cannot move to a wall
            Log.d("movePlayer", "Player cannot move to a wall");
            return labyrinth;
        }



        // Check if the new position is the winning position (value 3)
        if (labyrinth[newPlayerX][newPlayerY] == 3) {
            Log.d("movePlayer", "Player won the game!");
            // Set all elements in the labyrinth to 0 (empty space)
            for (int[] ints : labyrinth) {
                Arrays.fill(ints, 0);
            }
            return labyrinth;
        }
        // Move the player to the new position
        labyrinth[playerX][playerY] = 0; // Set the current position to 0 (empty space)
        labyrinth[newPlayerX][newPlayerY] = 2; // Set the new position to 2 (player)

        return labyrinth;
    }

    public boolean isLabyrinthEmpty(int[][] labyrinth) {
        for (int i = 0; i < labyrinth.length; i++) {
            for (int j = 0; j < labyrinth[i].length; j++) {
                if (labyrinth[i][j] != 0) {
                    return false; // Found a non-zero element, labyrinth is not empty
                }
            }
        }
        return true; // All elements are zeros, labyrinth is empty
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
        handler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
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
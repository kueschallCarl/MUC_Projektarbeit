package com.example.menu_template;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.Manifest;
import android.util.Log;
import androidx.core.content.ContextCompat;


/**
 * This class utilizes the sensors inside the Smartphone to parse accelerometer/gyro values, so that the GameLogic class can
 * use that data to calculate game-physics etc.
 */
public class PhoneSteering implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private float acc_x, acc_y, acc_z;
    private float gyro_x, gyro_y, gyro_z;

    public PhoneSteering(Context context) {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            Log.d("Sensor_Listener","Permissions granted");
        } else {
            Log.d("Sensor_Listener","Permissions not granted");

        }

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    public void startSensors() {
        if (accelerometer != null) {
            Log.d("Sensor_Listener","Accelerometer listener activated");
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        if (gyroscope != null) {
            Log.d("Sensors_Listener","Gyro listener activated");
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void stopSensors() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            acc_x = event.values[0];
            acc_y = event.values[1];
            acc_z = event.values[2];
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyro_x = event.values[0];
            gyro_y = event.values[1];
            gyro_z = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
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

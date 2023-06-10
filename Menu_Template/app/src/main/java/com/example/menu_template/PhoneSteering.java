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
 * This class utilizes the sensors inside the Smartphone to parse accelerometer/gyro values, so that the GameLogic class can
 * use that data to calculate game-physics etc.
 */
public class PhoneSteering{

    public PhoneSteering() {
        //implement Smartphone accelerometer code and make sure, that whatever is returned matches the format of ESPSteering
    }
}

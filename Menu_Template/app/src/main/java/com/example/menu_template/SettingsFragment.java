package com.example.menu_template;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.menu_template.databinding.FragmentFirstBinding;
import com.example.menu_template.databinding.FragmentSecondBinding;
import org.eclipse.paho.client.mqttv3.*;
import com.example.menu_template.MqttManager;
import com.example.menu_template.MqttCallbackListener;
import com.example.menu_template.Constants.*;
import com.example.menu_template.databinding.FragmentSettingsBinding;

import java.util.Set;

import android.widget.ToggleButton;
import android.widget.CompoundButton;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private MqttManager mqttManager;
    private String SteeringMethod;
    private SettingsDatabase settingsDatabase;

    private boolean isAudioEnabled;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.settingsDatabase = SettingsDatabase.getInstance(requireContext());

//01
        ToggleButton toggleButtonAudio = view.findViewById(R.id.toggle_button_audio);
        isAudioEnabled = true; // Set the initial state

//1
        // Retrieve audio setting from the database
        boolean containsAudioSetting = settingsDatabase.containsSetting(SettingsDatabase.COLUMN_AUDIO_ENABLED);

// Set the toggle button state accordingly
        toggleButtonAudio.setChecked(containsAudioSetting);


// Retrieve audio setting from the database
        isAudioEnabled = settingsDatabase.isAudioEnabled();

// Set the toggle button state accordingly
        toggleButtonAudio.setChecked(isAudioEnabled);

        toggleButtonAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isAudioEnabled = isChecked;
                settingsDatabase.updateAudioEnabled(isAudioEnabled);
                // Update the audio state according to the isChecked value
            }
        });
//1

        // Get the singleton instance of MqttManager
        mqttManager = MqttManager.getInstance();

        // Handle connect button click
        binding.buttonSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String brokerIP = binding.brokerAddressTextField.getText().toString();
                mqttManager.MQTT_BROKER_IP = brokerIP;
                settingsDatabase.updateLastSetting(brokerIP, SettingsDatabase.COLUMN_BROKER_IP);
                Log.d("MqttManager", "brokerIP: " + mqttManager.MQTT_BROKER_IP);
            }
        });

        // Set a listener for radio button changes
        binding.radioBtnGroupSteeringMethod.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkSelectedSteeringMethod(settingsDatabase);
            }
        });
        // Call checkSelectedSteeringMethod initially to handle any pre-selected radio button
        checkSelectedSteeringMethod(settingsDatabase);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsDatabase = SettingsDatabase.getInstance(requireContext());
        setHasOptionsMenu(true);


        // Initialize audio setting if it doesn't exist in the database
        if (!settingsDatabase.containsSetting(SettingsDatabase.COLUMN_AUDIO_ENABLED)) {
            settingsDatabase.updateAudioEnabled(true);
        }
    }




    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Navigate to the SettingsFragment
            NavHostFragment.findNavController(this).navigate(R.id.action_FirstFragment_to_SettingsFragment);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void checkSelectedSteeringMethod(SettingsDatabase settingsDatabase) {
        try {
            // Find the RadioGroup within the current fragment's view
            RadioGroup radioGroup = getView().findViewById(R.id.radio_btn_group_steering_method);

            // Check if any radio button is checked
            if (radioGroup.getCheckedRadioButtonId() != -1) {
                // At least one radio button is checked

                // Get the selected radio button's ID
                int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();

                // Check if the selected radio button matches a specific radio button
                if (selectedRadioButtonId == R.id.radio_btn_esp32_steering) {
                    // ESP32 steering method is selected
                    SteeringMethod = "ESP32";
                } else if (selectedRadioButtonId == R.id.radio_btn_phone_steering) {
                    // Phone steering method is selected
                    SteeringMethod = "Phone";
                }

                // Save the selected steering method to the database
                settingsDatabase.updateLastSetting(SteeringMethod, SettingsDatabase.COLUMN_STEERING_METHOD);
                Log.d("Database", "Steering method saved: " + SteeringMethod);

                // Do something with the selected steering method
                // For example, log it or use it in your game logic
                Log.d("SteeringMethod", "Selected steering method: " + SteeringMethod);
            } else {
                Log.d("SteeringMethod", "No SteeringMethod selected");
            }
        } catch (Exception e) {
            showAlert("Radio Button Issue", "No Steering Method selected in Settings");
            Log.e("SteeringMethod", "Error saving steering method: " + e.getMessage());
        }
    }

    public String getSteeringMethod(SettingsDatabase settingsDatabase) {
        String steeringMethod = settingsDatabase.getSetting(SettingsDatabase.COLUMN_STEERING_METHOD);
        Log.d("Database", "Retrieved steering method: " + steeringMethod);
        return steeringMethod;
    }

    private void showAlert(String title, String message) {
        if (getContext() != null) {
            new AlertDialog.Builder(getContext())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}

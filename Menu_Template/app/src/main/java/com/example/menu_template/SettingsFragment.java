package com.example.menu_template;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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


public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private MqttManager mqttManager;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the singleton instance of MqttManager
        mqttManager = MqttManager.getInstance();

        // Handle connect button click
        binding.buttonSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String brokerIP = binding.brokerAddressTextField.getText().toString();
                mqttManager.MQTT_BROKER_IP = brokerIP;
                Log.d("MqttManager", "brokerIP: " + mqttManager.MQTT_BROKER_IP);
            }
        });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
}
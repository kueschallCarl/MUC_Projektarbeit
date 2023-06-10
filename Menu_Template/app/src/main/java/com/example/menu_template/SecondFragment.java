package com.example.menu_template;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.menu_template.databinding.FragmentSecondBinding;
import org.eclipse.paho.client.mqttv3.*;
import com.example.menu_template.MqttManager;
import com.example.menu_template.MqttCallbackListener;
import com.example.menu_template.Constants.*;
import com.google.android.material.snackbar.Snackbar;

/**
 * This fragment hosts the codebase for the visualization of the labyrinth, and with that the entire game-loop
 * This fragment implements the MqttCallbackListener interface to receive MQTT message callbacks
 */
public class SecondFragment extends Fragment {

    private static final String TAG = "SecondFragment";
    private FragmentSecondBinding binding;
    private GameLogic gameLogic;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.gameLogic = new GameLogic(requireContext());

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        return rootView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "SecondFragment opened successfully");

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            try {
                // Get to the Settings Fragment
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.action_SecondFragment_to_SettingsFragment);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error while navigating to Settings Fragment: " + e.getMessage());
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        try {
            gameLogic.mqttManager.publishToTopic("1", Constants.FINISHED_TOPIC);
            binding = null;
            gameLogic.mqttManager.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "Error during onDestroyView: " + e.getMessage());
        }
    }
}

package com.example.menu_template;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.menu_template.databinding.FragmentSecondBinding;
import org.eclipse.paho.client.mqttv3.*;
import com.example.menu_template.MqttManager;
import com.example.menu_template.MqttCallbackListener;

/**
 * This fragment hosts the codebase for the visualization of the labyrinth, and with that the entire game-loop
 * This fragment implements the MqttCallbackListener interface to receive MQTT message callbacks
 */
public class SecondFragment extends Fragment implements MqttCallbackListener{

    private FragmentSecondBinding binding;
    private MqttManager mqttManager;

    /**
     * This method implements the MqttCallbackListener interface for onMessageReceived()
     * @param topic the MQTT topic
     * @param message the current message received for that MQTT topic
     */
    @Override
    public void onMessageReceived(String topic, String message) {
        if (topic.equals("mpu/K05")) {
            // Handle received message
            String payload = new String(message);
            // Process the payload as per your game logic
            Log.d("mpu_message", payload);
        }
    }

    /**
     * This method overrides the implementation of creating the View
     * In this case, an MQTT connection is established and utilized and a binding object is created through inflation
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * ChatGPT explanation of "inflating":
         * "Inflating" refers to the process of creating a View object from a layout XML file.
         * In the context of Android development, when we say a layout is inflated,
         * it means that the XML layout file is parsed and converted into a hierarchy of View objects that represent the user interface components specified in the XML.
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The root view of the fragment.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        mqttManager = new MqttManager();
        mqttManager.setCallbackListener(this); // Set the fragment as the callback listener
        mqttManager.connect();

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }


    /**
     * This method implements what should happen once the View has been created
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {

            /**
             * This method overrides what should happen, when the specified Element is clicked
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
    }

    /**
     * This method overrides what should happen, whenever this View is destroyed
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mqttManager.disconnect();
    }

}
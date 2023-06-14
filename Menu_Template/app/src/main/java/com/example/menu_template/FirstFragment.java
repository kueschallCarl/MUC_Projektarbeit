package com.example.menu_template;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.menu_template.databinding.FragmentFirstBinding;
import com.google.android.material.snackbar.Snackbar;
import org.eclipse.paho.client.mqttv3.*;
import com.example.menu_template.MqttManager;
import com.example.menu_template.MqttCallbackListener;
import com.example.menu_template.Constants.*;
public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private MqttManager mqttManager;

    /**
     * This method overrides the implementation of creating the View
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
        mqttManager = MqttManager.getInstance();
        binding = FragmentFirstBinding.inflate(inflater, container, false);
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

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            /**
             * This method overrides what should happen, when the specified Element is clicked
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {

                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_SecondFragment);

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Get to the Settings Fragment
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(
                    R.id.action_FirstFragment_to_SettingsFragment,
                    null,
                    new NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .setPopUpTo(R.id.FirstFragment, false)
                            .build()
            );            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    /**
     * This method overrides what should happen, whenever this View is destroyed
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
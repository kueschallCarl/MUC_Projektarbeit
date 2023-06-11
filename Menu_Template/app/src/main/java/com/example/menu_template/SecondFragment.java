package com.example.menu_template;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.menu_template.Constants;
import com.example.menu_template.GameLogic;
import com.example.menu_template.databinding.FragmentSecondBinding;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private GameLogic gameLogic;
    private String steeringMethod;
    private SettingsDatabase settingsDatabase;
    private ImageView labyrinthImageView; // Declare the ImageView as a class-level variable
    private View fragmentView; // Declare the view as a class-level variable


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fragmentView = view; // Assign the view to the class-level variable

        SettingsFragment settingsFragment = new SettingsFragment();
        try {
            this.settingsDatabase = SettingsDatabase.getInstance(requireContext());
            steeringMethod = settingsFragment.getSteeringMethod(settingsDatabase);
            Log.d("SteeringMethod", "Method: " + steeringMethod);
        }
        catch (Exception e){
            Log.d("SteeringMethod", "Issue calling the getSteeringMethod(): "+ e);
        }

        gameLogic = new GameLogic(requireContext(), settingsDatabase);
        drawLabyrinth();
    }

    private void drawLabyrinth() {
        int[][] labyrinth = gameLogic.getLabyrinth();

        // Create a new bitmap to draw the labyrinth
        int cellSize = 50;
        int width = labyrinth.length * cellSize;
        int height = labyrinth[0].length * cellSize;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Clear the canvas
        canvas.drawColor(Color.WHITE);

        // Create the paint objects for different cell colors
        Paint emptyCellPaint = new Paint();
        emptyCellPaint.setColor(ContextCompat.getColor(requireContext(), R.color.colorEmptyCell));

        Paint wallPaint = new Paint();
        wallPaint.setColor(ContextCompat.getColor(requireContext(), R.color.colorWall));

        Paint startPaint = new Paint();
        startPaint.setColor(ContextCompat.getColor(requireContext(), R.color.colorStart));

        Paint endPaint = new Paint();
        endPaint.setColor(ContextCompat.getColor(requireContext(), R.color.colorEnd));

        // Draw the labyrinth on the canvas
        for (int i = 0; i < labyrinth.length; i++) {
            for (int j = 0; j < labyrinth[i].length; j++) {
                int cellValue = labyrinth[i][j];
                float left = i * cellSize;
                float top = j * cellSize;
                float right = left + cellSize;
                float bottom = top + cellSize;

                switch (cellValue) {
                    case 0:
                        canvas.drawRect(left, top, right, bottom, emptyCellPaint);
                        break;
                    case 1:
                        canvas.drawRect(left, top, right, bottom, wallPaint);
                        break;
                    case 2:
                        canvas.drawRect(left, top, right, bottom, startPaint);
                        break;
                    case 3:
                        canvas.drawRect(left, top, right, bottom, endPaint);
                        break;
                }
            }
        }

        labyrinthImageView = fragmentView.findViewById(R.id.labyrinthImageView);
        labyrinthImageView.setImageBitmap(bitmap);
    }

    private void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
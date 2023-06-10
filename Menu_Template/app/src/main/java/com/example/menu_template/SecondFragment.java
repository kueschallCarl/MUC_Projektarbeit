package com.example.menu_template;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.menu_template.Constants;
import com.example.menu_template.GameLogic;
import com.example.menu_template.databinding.FragmentSecondBinding;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private GameLogic gameLogic;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gameLogic = new GameLogic(requireContext());
        int[][] labyrinth = gameLogic.getLabyrinth();

        TableLayout tableLayout = view.findViewById(R.id.tableLayout);

        // Clear the existing views from the TableLayout
        tableLayout.removeAllViews();

        // Get the color resources using ContextCompat
        int colorEmptyCell = ContextCompat.getColor(requireContext(), R.color.colorEmptyCell);
        int colorWall = ContextCompat.getColor(requireContext(), R.color.colorWall);
        int colorStart = ContextCompat.getColor(requireContext(), R.color.colorStart);
        int colorEnd = ContextCompat.getColor(requireContext(), R.color.colorEnd);

        // Iterate over the labyrinth and create TableRow and TextView for each cell
        for (int i = 0; i < labyrinth.length; i++) {
            TableRow tableRow = new TableRow(requireContext());

            for (int j = 0; j < labyrinth[i].length; j++) {
                TextView textView = new TextView(requireContext());
                textView.setText(String.valueOf(labyrinth[i][j]));
                textView.setPadding(10, 10, 10, 10);

                // Customize the appearance based on the cell value
                switch (labyrinth[i][j]) {
                    case 0:
                        textView.setBackgroundColor(colorEmptyCell);
                        break;
                    case 1:
                        textView.setBackgroundColor(colorWall);
                        break;
                    case 2:
                        textView.setBackgroundColor(colorStart);
                        break;
                    case 3:
                        textView.setBackgroundColor(colorEnd);
                        break;
                }

                tableRow.addView(textView);
            }

            tableLayout.addView(tableRow);
        }
    }

    @Override
    public void onDestroyView() {
        gameLogic.mqttManager.publishToTopic("1", Constants.FINISHED_TOPIC);
        super.onDestroyView();
        binding = null;
        gameLogic.mqttManager.disconnect();
    }
}

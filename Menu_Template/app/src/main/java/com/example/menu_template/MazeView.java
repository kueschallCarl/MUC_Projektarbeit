package com.example.menu_template;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import com.example.menu_template.GameLogic;

// Custom view to visualize the labyrinth
public class MazeView extends View {
    private static final String TAG = "MazeView";
    private int[][] labyrinth;

    public MazeView(Context context) {
        super(context);
    }

    public void setLabyrinth(int[][] labyrinth) {
        this.labyrinth = labyrinth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        try {
            if (labyrinth == null) {
                Log.e(TAG, "Labyrinth is null. Unable to draw maze.");
                return;
            }

            // Calculate the size of each block based on the view dimensions and maze size
            int blockSize = Math.min(getWidth(), getHeight()) / labyrinth.length;

            // Create paint objects for walls and paths
            Paint wallPaint = new Paint();
            wallPaint.setColor(Color.BLACK);
            wallPaint.setStyle(Paint.Style.FILL);

            Paint pathPaint = new Paint();
            pathPaint.setColor(Color.WHITE);
            pathPaint.setStyle(Paint.Style.FILL);

            // Draw the labyrinth
            for (int i = 0; i < labyrinth.length; i++) {
                for (int j = 0; j < labyrinth[i].length; j++) {
                    float left = j * blockSize;
                    float top = i * blockSize;
                    float right = left + blockSize;
                    float bottom = top + blockSize;

                    if (labyrinth[i][j] == GameLogic.WALL) {
                        canvas.drawRect(left, top, right, bottom, wallPaint);
                    } else {
                        canvas.drawRect(left, top, right, bottom, pathPaint);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while drawing maze: " + e.getMessage());
        }
    }
}

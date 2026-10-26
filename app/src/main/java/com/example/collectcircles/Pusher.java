package com.example.collectcircles;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Pusher {

    private static final float WIDTH_PART_OF_HEIGHT = 0.50f;
    private static final float SPEED_IN_HEIGHTS_PER_SECOND = 0.80f;

    private float centerX;
    private final float centerY;
    private final float height;
    private final int color;
    private final long animationOffsetMillis;
    private int direction = 1;

    /**
     * Creates one animated walker at the requested board position.
     *
     * @param centerX horizontal center on the board, in pixels
     * @param centerY vertical center on the board, in pixels
     * @param height character height, in pixels
     * @param color color used to draw the character
     * @param animationOffsetMillis phase offset that separates this animation
     *                              from the other Pushers
     */
    public Pusher(float centerX, float centerY, float height,
                  int color, long animationOffsetMillis) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.height = height;
        this.color = color;
        this.animationOffsetMillis = animationOffsetMillis;
    }


    /**
            * Advances the walker and turns it around before it leaves the board.
            *
            * @param elapsedSeconds time since the previous frame, in seconds
 * @param boardWidth current board width, in pixels
 */
    public void update(double elapsedSeconds, float boardWidth) {
        float speed = height * SPEED_IN_HEIGHTS_PER_SECOND;
        centerX += direction * speed * elapsedSeconds;

        // Clamp the center so the whole character remains inside the board.
        float halfWidth = height * WIDTH_PART_OF_HEIGHT / 2f;
        if (centerX > boardWidth - halfWidth) {
            centerX = boardWidth - halfWidth;
            direction = -1;
        } else if (centerX < halfWidth) {
            centerX = halfWidth;
            direction = 1;
        }
    }




    /**
     * Returns the current horizontal center for package-level unit tests.
     *
     * @return horizontal center on the board, in pixels
     */
    float getCenterX() {
        return centerX;
    }

    /**
     * Returns the current walking direction for package-level unit tests.
     *
     * @return {@code 1} when walking right, or {@code -1} when walking left
     */
    int getDirection() {
        return direction;
    }


    /**
     * Draws one animation frame of the stick figure.
     *
     * @param canvas destination on which the character is drawn
     * @param paint reusable Paint configured by this method before drawing
     * @param animationTimeMillis monotonic animation time, in milliseconds
     */
    public void draw(Canvas canvas, Paint paint, long animationTimeMillis) {
        // Configure the shared Paint for an outlined figure with rounded joints.
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, height * 0.06f));
        paint.setStrokeCap(Paint.Cap.ROUND);

        // Derive every body landmark from height so the figure scales uniformly.
        float headRadius = height * 0.12f;
        float headY = centerY - height * 0.30f;
        float shouldersY = centerY - height * 0.12f;
        float hipsY = centerY + height * 0.14f;
        float footY = centerY + height * 0.42f;

        // Convert time to a repeating offset; the phase shift keeps Pushers out of sync.
        double phase = (animationTimeMillis + animationOffsetMillis) / 140.0;
        float swing = (float) Math.sin(phase) * height * 0.12f;

        // Draw the head and a short nose that points in the walking direction.
        canvas.drawCircle(centerX, headY, headRadius, paint);
        canvas.drawLine(
                centerX + direction * headRadius,
                headY,
                centerX + direction * headRadius * 1.55f,
                headY,
                paint
        );

        // The torso joins the bottom of the head to the hips.
        canvas.drawLine(centerX, headY + headRadius, centerX, hipsY, paint);

        // Opposite swing signs make the arms alternate instead of moving together.
        canvas.drawLine(centerX, shouldersY,
                centerX + direction * swing, centerY + height * 0.05f, paint);
        canvas.drawLine(centerX, shouldersY,
                centerX - direction * swing, centerY + height * 0.05f, paint);

        // The legs use the same phase in opposite directions to create a walking step.
        canvas.drawLine(centerX, hipsY,
                centerX + swing, footY, paint);
        canvas.drawLine(centerX, hipsY,
                centerX - swing, footY, paint);
    }
}
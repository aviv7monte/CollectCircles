package com.example.collectcircles;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Pusher {

    private static final float WIDTH_PART_OF_HEIGHT = 0.50f;
    private static final float SPEED_IN_HEIGHTS_PER_SECOND = 0.80f;

    private float centerX;
    private float centerY;
    private State state = State.IDLE;
    private Circle assignedCircle;
    private double taskElapsedSeconds;
    private final float height;
    private final int color;
    private final long animationOffsetMillis;
    private int direction = 1;


    // Positions captured at assignment time and reused throughout the task.
    private float pusherStartX;
    private float pusherStartY;
    private float circleStartX;
    private float circleStartY;
    private float targetX;
    private float targetY;
    private float attachX;
    private float attachY;
    private float finishX;
    private float finishY;

    /** Known production rate of one Pusher while work is available. */
    static final double CIRCLES_PER_SECOND = 0.20;
    // A complete task lasts five seconds: two walking and three pushing.
    private static final double TASK_DURATION_SECONDS = 1.0 / CIRCLES_PER_SECOND;
    private static final double WALK_DURATION_SECONDS = 2.0;


    /** Phases of one Pusher's work cycle. */
    private enum State {
        IDLE,
        WALKING,
        PUSHING
    }

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
     * Advances the current work assignment by one frame.
     *
     * @param elapsedSeconds seconds since the previous frame
     * @return the completed circle, or null while the task is still running
     */
    public Circle update(double elapsedSeconds) {
        if (assignedCircle == null) {
            return null;
        }

        taskElapsedSeconds += elapsedSeconds;
        if (taskElapsedSeconds < WALK_DURATION_SECONDS) {
            updateWalkingPosition();
            return null;
        }

        state = State.PUSHING;
        updatePushingPosition();
        if (taskElapsedSeconds < TASK_DURATION_SECONDS) {
            return null;
        }

        return finishTask();
    }

    /**
     * Assigns a circle and calculates the path from the Pusher to the target.
     *
     * @param circle circle to push
     * @param target destination target
     * @return true when the assignment is accepted; false when already busy
     */
    public boolean assign(Circle circle, Target target) {
        if (assignedCircle != null) {
            return false;
        }

        assignedCircle = circle;
        state = State.WALKING;
        pusherStartX = centerX;
        pusherStartY = centerY;
        circleStartX = circle.getCenterX();
        circleStartY = circle.getCenterY();
        targetX = target.getCenterX();
        targetY = target.getCenterY();

        // Normalize the direction from the circle to the target.
        float dx = targetX - circleStartX;
        float dy = targetY - circleStartY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        // Use a stable default direction when the circle is already at the target.
        float unitX = distance == 0 ? 1f : dx / distance;
        float unitY = distance == 0 ? 0f : dy / distance;
        float behindDistance = circle.getRadius() + height * 0.15f;

        attachX = circleStartX - unitX * behindDistance;
        attachY = circleStartY - unitY * behindDistance;
        finishX = targetX - unitX * behindDistance;
        finishY = targetY - unitY * behindDistance;
        updateDirection(attachX - centerX);
        return true;
    }


    /**
     * Updates the direction only when horizontal movement is meaningful.
     *
     * @param horizontalMovement signed horizontal movement; positive means right
     */
    private void updateDirection(float horizontalMovement) {
        if (horizontalMovement > 0.001f) {
            direction = 1;
        } else if (horizontalMovement < -0.001f) {
            direction = -1;
        }
    }

    /**
     * Finds a position between two endpoints.
     *
     * @param start position at progress 0
     * @param end position at progress 1
     * @param progress fraction of the movement already completed
     * @return interpolated position
     */
    private float interpolate(float start, float end, float progress) {
        return start + (end - start) * progress;
    }



    /** Moves the Pusher from its starting point to the attachment point. */
    private void updateWalkingPosition() {
        float progress = (float) (taskElapsedSeconds / WALK_DURATION_SECONDS);
        centerX = interpolate(pusherStartX, attachX, progress);
        centerY = interpolate(pusherStartY, attachY, progress);
    }

    /** Moves the assigned circle and its Pusher toward the target together. */
    private void updatePushingPosition() {
        double pushingSeconds = taskElapsedSeconds - WALK_DURATION_SECONDS;
        double pushingDuration = TASK_DURATION_SECONDS - WALK_DURATION_SECONDS;
        float progress = (float) Math.min(1.0, pushingSeconds / pushingDuration);

        assignedCircle.setCenter(
                interpolate(circleStartX, targetX, progress),
                interpolate(circleStartY, targetY, progress)
        );
        centerX = interpolate(attachX, finishX, progress);
        centerY = interpolate(attachY, finishY, progress);
        updateDirection(targetX - circleStartX);
    }

    /**
     * Releases the completed assignment while preserving fractional extra time.
     *
     * @return circle that reached the target
     */
    private Circle finishTask() {
        Circle completedCircle = assignedCircle;
        assignedCircle = null;
        state = State.IDLE;
        taskElapsedSeconds -= TASK_DURATION_SECONDS;
        return completedCircle;
    }



    /**
     * Checks whether this Pusher owns a particular assignment.
     *
     * @param circle circle to check by object identity
     * @return true when this Pusher is assigned to that circle
     */
    public boolean isAssignedTo(Circle circle) {
        return assignedCircle == circle;
    }

    /** @return true when this Pusher currently has work */
    public boolean hasAssignment() {
        return assignedCircle != null;
    }

    /**
     * Cancels this Pusher's assignment when it matches the supplied circle.
     *
     * @param circle circle taken by the user
     */
    public void cancelAssignment(Circle circle) {
        if (assignedCircle != circle) {
            return;
        }
        assignedCircle = null;
        state = State.IDLE;
        taskElapsedSeconds = 0;
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

//        // Opposite swing signs make the arms alternate instead of moving together.
//        canvas.drawLine(centerX, shouldersY,
//                centerX + direction * swing, centerY + height * 0.05f, paint);
//        canvas.drawLine(centerX, shouldersY,
//                centerX - direction * swing, centerY + height * 0.05f, paint);
//
//

//////////////////////////////////
        // Pushing uses two forward arms; walking keeps the alternating swing.
        if (state == State.PUSHING) {
            float handsX = centerX + direction * height * 0.32f;
            canvas.drawLine(centerX, shouldersY, handsX, centerY, paint);
            canvas.drawLine(centerX, shouldersY + height * 0.05f,
                    handsX, centerY + height * 0.05f, paint);
        } else {
            canvas.drawLine(centerX, shouldersY,
                    centerX + direction * swing, centerY + height * 0.05f, paint);
            canvas.drawLine(centerX, shouldersY,
                    centerX - direction * swing, centerY + height * 0.05f, paint);
        }



        ///////////////////////////////////
        // The legs use the same phase in opposite directions to create a walking step.
        canvas.drawLine(centerX, hipsY,
                centerX + swing, footY, paint);
        canvas.drawLine(centerX, hipsY,
                centerX - swing, footY, paint);
    }
}
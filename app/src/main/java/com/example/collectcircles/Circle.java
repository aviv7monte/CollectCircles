package com.example.collectcircles;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Circle {
    private float centerX;
    private float centerY;
    private final float radius;
    private final int color;

    /**
     * יוצר עיגול במיקום, בגודל ובצבע הנתונים.
     *
     * @param centerX מיקום מרכז העיגול בציר האופקי
     * @param centerY מיקום מרכז העיגול בציר האנכי
     * @param radius רדיוס העיגול
     * @param color צבע העיגול
     */
    public Circle(float centerX, float centerY, float radius, int color) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.color = color;
    }

    /**
     * @return מיקום מרכז העיגול בציר האופקי
     */
    public float getCenterX() {
        return centerX;
    }

    /**
     * @return מיקום מרכז העיגול בציר האנכי
     */
    public float getCenterY() {
        return centerY;
    }

    /**
     * @return רדיוס העיגול
     */
    public float getRadius() {
        return radius;
    }

    /**
     * מעביר את מרכז העיגול לנקודה חדשה.
     *
     * @param centerX מיקום המרכז החדש בציר האופקי
     * @param centerY מיקום המרכז החדש בציר האנכי
     */
    public void setCenter(float centerX, float centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
    }

    /**
     * מחשב את המרחק בין מרכז העיגול למרכזו של עיגול אחר.
     *
     * @param other העיגול האחר
     * @return המרחק בין מרכזי העיגולים
     */
    public double distanceTo(Circle other) {
        return distanceTo(other.centerX, other.centerY);
    }

    /**
     * מחשב את המרחק בין מרכז העיגול לנקודה נתונה.
     *
     * @param pointX מיקום הנקודה בציר האופקי
     * @param pointY מיקום הנקודה בציר האנכי
     * @return המרחק ממרכז העיגול לנקודה
     */
    public double distanceTo(float pointX, float pointY) {
        float dx = centerX - pointX;
        float dy = centerY - pointY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * בודק אם נקודה נמצאת בתוך העיגול או על היקפו.
     *
     * @param pointX מיקום הנקודה בציר האופקי
     * @param pointY מיקום הנקודה בציר האנכי
     * @return {@code true} אם הנקודה בתוך העיגול או על היקפו
     */
    public boolean contains(float pointX, float pointY) {
        return distanceTo(pointX, pointY) <= radius;
    }

    /**
     * בודק אם לעיגול ולעיגול אחר יש שטח משותף.
     *
     * @param other העיגול האחר
     * @return {@code true} אם העיגולים חופפים
     */
    public boolean overlaps(Circle other) {
        return distanceTo(other) < radius + other.radius;
    }

    /**
     * מצייר את העיגול על משטח הציור הנתון.
     *
     * @param canvas משטח הציור
     * @param paint הגדרות הצבע והסגנון שישמשו לציור
     */
    public void draw(Canvas canvas, Paint paint) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawCircle(centerX, centerY, radius, paint);
    }
}
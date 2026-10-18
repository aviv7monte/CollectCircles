package com.example.collectcircles;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Target extends Circle {
    private final int crossColor;
    private final float crossStrokeWidth;

    /**
     * יוצר מטרה עגולה שבמרכזה מצויר צלב.
     *
     * @param centerX מיקום מרכז המטרה בציר האופקי
     * @param centerY מיקום מרכז המטרה בציר האנכי
     * @param radius רדיוס המטרה
     * @param color צבע המטרה
     * @param crossColor צבע הצלב
     * @param crossStrokeWidth עובי קווי הצלב
     */
    public Target(float centerX, float centerY, float radius, int color,
                  int crossColor, float crossStrokeWidth) {
        super(centerX, centerY, radius, color);
        this.crossColor = crossColor;
        this.crossStrokeWidth = crossStrokeWidth;
    }

    /**
     * בודק אם עיגול נמצא במלואו בתוך המטרה.
     *
     * @param circle העיגול שאותו בודקים
     * @return {@code true} אם כל העיגול נמצא בתוך המטרה
     */
    public boolean fullyContains(Circle circle) {
        return distanceTo(circle) + circle.getRadius() <= getRadius();
    }

    /**
     * מצייר את גוף המטרה ואת הצלב שבמרכזה.
     *
     * @param canvas משטח הציור
     * @param paint הגדרות הצבע והסגנון שישמשו לציור
     */
    @Override
    public void draw(Canvas canvas, Paint paint) {
        super.draw(canvas, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(crossStrokeWidth);
        paint.setColor(crossColor);

        canvas.drawLine(
                getCenterX(), getCenterY() - getRadius(),
                getCenterX(), getCenterY() + getRadius(), paint
        );
        canvas.drawLine(
                getCenterX() - getRadius(), getCenterY(),
                getCenterX() + getRadius(), getCenterY(), paint
        );
    }
}
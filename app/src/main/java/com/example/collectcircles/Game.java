package com.example.collectcircles;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Game {
    private static final int CIRCLE_COUNT = 5;

    private final Random random = new Random();
    private final Target target;
    private final List<Circle> circles = new ArrayList<>();
    private Circle selectedCircle;

    /**
     * יוצר משחק חדש וממקם בו מטרה ועיגולים אקראיים.
     *
     * @param boardWidth רוחב לוח המשחק בפיקסלים
     * @param boardHeight גובה לוח המשחק בפיקסלים
     * @param targetRadius רדיוס המטרה
     * @param targetColor צבע המטרה
     * @param crossColor צבע הצלב שבמרכז המטרה
     * @param crossStrokeWidth עובי קווי הצלב
     * @param circleColor צבע העיגולים שאוספים
     */
    public Game(float boardWidth, float boardHeight, float targetRadius,
                int targetColor, int crossColor, float crossStrokeWidth,
                int circleColor) {
        target = new Target(
                boardWidth / 2f,
                boardHeight / 2f,
                targetRadius,
                targetColor,
                crossColor,
                crossStrokeWidth
        );

        createCircles(boardWidth, boardHeight, circleColor);
    }

    /**
     * מצייר את המטרה ואת כל העיגולים שעדיין נמצאים במשחק.
     *
     * @param canvas משטח הציור
     * @param paint הגדרות הצבע והסגנון שישמשו לציור
     */
    public void draw(Canvas canvas, Paint paint) {
        target.draw(canvas, paint);
        for (Circle circle : circles) {
            circle.draw(canvas, paint);
        }
    }

    /**
     * בוחר את העיגול העליון שמכיל את נקודת המגע.
     *
     * @param x מיקום נקודת המגע בציר האופקי
     * @param y מיקום נקודת המגע בציר האנכי
     * @return {@code true} אם נמצא ונבחר עיגול בנקודה
     */
    public boolean selectCircleAt(float x, float y) {
        for (int i = circles.size() - 1; i >= 0; i--) {
            Circle circle = circles.get(i);
            if (circle.contains(x, y)) {
                selectedCircle = circle;
                return true;
            }
        }

        selectedCircle = null;
        return false;
    }

    /**
     * מעביר את העיגול שנבחר אל נקודה חדשה, אם יש עיגול נבחר.
     *
     * @param x מיקום המרכז החדש בציר האופקי
     * @param y מיקום המרכז החדש בציר האנכי
     */
    public void moveSelectedCircle(float x, float y) {
        if (selectedCircle != null) {
            selectedCircle.setCenter(x, y);
        }
    }

    /**
     * משחרר את העיגול שנבחר ואוסף אותו אם הוא נמצא כולו במטרה.
     *
     * @return {@code true} אם העיגול נאסף; {@code false} אם לא נאסף או לא נבחר עיגול
     */
    public boolean releaseSelectedCircle() {
        if (selectedCircle == null) {
            return false;
        }

        boolean collected = target.fullyContains(selectedCircle);
        if (collected) {
            circles.remove(selectedCircle);
        }
        selectedCircle = null;
        return collected;
    }

    /**
     * מבטל את בחירת העיגול הנוכחי בלי לאסוף אותו.
     */
    public void cancelSelection() {
        selectedCircle = null;
    }

    /**
     * @return {@code true} אם כל העיגולים נאספו
     */
    public boolean isFinished() {
        return circles.isEmpty();
    }

    /**
     * יוצר את עיגולי המשחק במיקומים אקראיים חוקיים.
     *
     * @param boardWidth רוחב לוח המשחק בפיקסלים
     * @param boardHeight גובה לוח המשחק בפיקסלים
     * @param circleColor צבע העיגולים
     */
    private void createCircles(float boardWidth, float boardHeight,
                               int circleColor) {
        float circleRadius = target.getRadius() / 2f;

        while (circles.size() < CIRCLE_COUNT) {
            float x = randomCoordinate(
                    circleRadius,
                    boardWidth - circleRadius
            );
            float y = randomCoordinate(
                    circleRadius,
                    boardHeight - circleRadius
            );
            Circle candidate =
                    new Circle(x, y, circleRadius, circleColor);

            if (!overlapsExistingCircle(candidate)) {
                circles.add(candidate);
            }
        }
    }

    /**
     * בודק אם עיגול מועמד חופף למטרה או לעיגול קיים.
     *
     * @param candidate העיגול שמיקומו נבדק
     * @return {@code true} אם המועמד חופף לעצם שכבר נמצא במשחק
     */
    private boolean overlapsExistingCircle(Circle candidate) {
        if (candidate.overlaps(target)) {
            return true;
        }

        for (Circle circle : circles) {
            if (candidate.overlaps(circle)) {
                return true;
            }
        }
        return false;
    }

    /**
     * מגריל קואורדינטה בטווח הנתון.
     *
     * @param minimum הגבול התחתון של הטווח
     * @param maximum הגבול העליון של הטווח
     * @return קואורדינטה אקראית בין הגבולות
     */
    private float randomCoordinate(float minimum, float maximum) {
        return minimum + random.nextFloat() * (maximum - minimum);
    }
}
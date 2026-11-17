package com.example.collectcircles;

import android.content.Context;
import android.content.SharedPreferences;

public class GameProgress {

    // המפתחות קובעים תחת איזה שם יישמר כל ערך ב-SharedPreferences.
    private static final String CIRCLES_BALANCE_KEY = "circles_balance";
    private static final String LIFETIME_CIRCLES_KEY = "lifetime_circles";
    private static final String PUSHER_COUNT_KEY = "pusher_count";

    // אובייקט השמירה מאפשר לקרוא את ההתקדמות ולשמור אותה במכשיר.
    private final SharedPreferences preferences;

    // עותק של ההתקדמות בזיכרון, לשימוש מהיר כל עוד היישום פועל.
    private long circlesBalance;
    private long lifetimeCircles;
    private int pusherCount;
    // Key used to persist the selected game mode in SharedPreferences.
    private static final String AUTONOMOUS_MODE_KEY = "autonomous_mode";

    // Cached mode used by the current screen.
    private boolean autonomousMode;
    private static final String LAST_PROGRESS_UPDATE_KEY = "last_progress_update";
    private static final String FRACTIONAL_PROGRESS_KEY = "fractional_progress";

    private long lastProgressUpdateMillis;
    private double fractionalProgress;

    static final String PREFERENCES_NAME = "collect_circles_preferences";

    static final long FIRST_PUSHER_PRICE = 64;
    public GameProgress(SharedPreferences preferences) {
        this.preferences = preferences;

        // טוענים את הערכים שנשמרו. אם עדיין אין ערך שמור, מתחילים מאפס.
        circlesBalance = preferences.getLong(CIRCLES_BALANCE_KEY, 0);
        lifetimeCircles = preferences.getLong(LIFETIME_CIRCLES_KEY, 0);
        pusherCount = preferences.getInt(PUSHER_COUNT_KEY, 0);



        autonomousMode = preferences.getBoolean(AUTONOMOUS_MODE_KEY, false);

        lastProgressUpdateMillis = preferences.getLong(
                LAST_PROGRESS_UPDATE_KEY,
                System.currentTimeMillis()
        );
        fractionalProgress = Double.longBitsToDouble(
                preferences.getLong(
                        FRACTIONAL_PROGRESS_KEY,
                        Double.doubleToRawLongBits(0)
                )
        );
    }


    /**
     * Loads the shared game progress used by Activities and Workers.
     *
     * @param context context used to open the SharedPreferences file
     * @return game progress backed by the shared preferences
     */
    public static GameProgress load(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
        );
        return new GameProgress(preferences);
    }

    /**
     * Returns the game mode currently selected by the player.
     */
    public boolean isAutonomousMode() {
        return autonomousMode;
    }

    /**
     * Updates the selected mode and saves it for the next app launch.
     */
    public void setAutonomousMode(boolean autonomousMode) {
        this.autonomousMode = autonomousMode;
        preferences.edit()
                .putBoolean(AUTONOMOUS_MODE_KEY, autonomousMode)
                .apply();
    }

    // מחזיר את מספר העיגולים שאפשר יהיה להוציא על קניות.
    public long getCirclesBalance() {
        return circlesBalance;
    }

    // מחזיר את מספר העיגולים הכולל שנאסף מאז תחילת המשחק.
    public long getLifetimeCircles() {
        return lifetimeCircles;
    }

    // מחזיר את מספר העובדים שנרכשו.
    public int getPusherCount() {
        return pusherCount;
    }

    // מעדכן את שני המונים לאחר שהשחקן אסף עיגול אחד.
    public void recordCollectedCircle() {
        recordCollectedCircles(1);
    }

    /**
     * Adds several collected circles without overflowing either total.
     *
     * @param count number of circles to add
     */
    public void recordCollectedCircles(long count) {
        if (count <= 0) {
            return;
        }

        circlesBalance = addWithSaturation(circlesBalance, count);
        lifetimeCircles = addWithSaturation(lifetimeCircles, count);
        preferences.edit()
                .putLong(CIRCLES_BALANCE_KEY, circlesBalance)
                .putLong(LIFETIME_CIRCLES_KEY, lifetimeCircles)
                .apply();
    }

    /**
     * Adds two non-negative values and stops at the largest long value.
     *
     * @param current current stored value
     * @param addition non-negative amount to add
     * @return the sum, or Long.MAX_VALUE if the sum would overflow
     */
    private long addWithSaturation(long current, long addition) {
        if (Long.MAX_VALUE - current < addition) {
            return Long.MAX_VALUE;
        }
        return current + addition;
    }





    /**
     * Calculates the price for a given number of owned Pushers.
     *
     * @param pusherCount number of Pushers already owned
     * @return the next price, or {@link Long#MAX_VALUE} before overflow
     * @throws IllegalArgumentException if pusherCount is negative
     */
    static long priceForPusherCount(int pusherCount) {
        if (pusherCount < 0) {
            throw new IllegalArgumentException("pusherCount cannot be negative");
        }
        if (pusherCount >= 57) {
            return Long.MAX_VALUE;
        }
        return FIRST_PUSHER_PRICE << pusherCount; // bit shift multiplication
    }

    /**
     * Returns the price that follows the number of Pushers already owned.
     *
     * @return the price of the next Pusher
     */
    public long getNextPusherPrice() {
        return priceForPusherCount(pusherCount);
    }



    /**
     * Checks affordability without changing the saved progress.
     *
     * @return true when the balance can cover the next Pusher's price
     */
    public boolean canBuyPusher() {
        return circlesBalance >= getNextPusherPrice();
    }





    /**
     * Buys one Pusher when the balance can cover its current price.
     *
     * @return true when the purchase succeeds; false when the balance is too low
     */
    public boolean buyPusher() {
        long price = getNextPusherPrice();
        if (circlesBalance < price) {
            return false;
        }

        circlesBalance -= price;
        pusherCount++;

        preferences.edit()
                .putLong(CIRCLES_BALANCE_KEY, circlesBalance)
                .putInt(PUSHER_COUNT_KEY, pusherCount)
                .apply();
        return true;
    }

    /**
     * Records one collected circle in both the spendable and lifetime totals.

    public void recordCollectedCircle() {
        if (circlesBalance < Long.MAX_VALUE) {
            circlesBalance++;
        }
        if (lifetimeCircles < Long.MAX_VALUE) {
            lifetimeCircles++;
        }

        preferences.edit()
                .putLong(CIRCLES_BALANCE_KEY, circlesBalance)
                .putLong(LIFETIME_CIRCLES_KEY, lifetimeCircles)
                .apply();
    }*/


    /**
     * Applies progress earned since the last saved wall-clock timestamp.
     *
     * @param nowMillis current wall-clock time, in milliseconds
     * @return number of whole circles added to the progress totals
     */
    public long settleOfflineProgress(long nowMillis) {
        long elapsedMillis = Math.max(0, nowMillis - lastProgressUpdateMillis);
        lastProgressUpdateMillis = nowMillis;

        long collectedCircles = 0;
        if (autonomousMode && pusherCount > 0) {
            OfflineProgressCalculator.Result result =
                    OfflineProgressCalculator.calculate(
                            elapsedMillis,
                            pusherCount,
                            fractionalProgress
                    );
            collectedCircles = result.getWholeCircles();
            fractionalProgress = result.getFraction();
            circlesBalance = addWithSaturation(circlesBalance, collectedCircles);
            lifetimeCircles = addWithSaturation(lifetimeCircles, collectedCircles);
        }

        preferences.edit()
                .putLong(CIRCLES_BALANCE_KEY, circlesBalance)
                .putLong(LIFETIME_CIRCLES_KEY, lifetimeCircles)
                .putLong(LAST_PROGRESS_UPDATE_KEY, lastProgressUpdateMillis)
                .putLong(
                        FRACTIONAL_PROGRESS_KEY,
                        Double.doubleToRawLongBits(fractionalProgress)
                )
                .apply();
        return collectedCircles;
    }

    /**
     * Saves the moment from which the next offline period will be measured.
     *
     * @param nowMillis current wall-clock time, in milliseconds
     */
    public void markBackgroundStarted(long nowMillis) {
        lastProgressUpdateMillis = nowMillis;
        preferences.edit()
                .putLong(LAST_PROGRESS_UPDATE_KEY, nowMillis)
                .apply();
    }



}
package com.example.collectcircles;

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

    public GameProgress(SharedPreferences preferences) {
        this.preferences = preferences;

        // טוענים את הערכים שנשמרו. אם עדיין אין ערך שמור, מתחילים מאפס.
        circlesBalance = preferences.getLong(CIRCLES_BALANCE_KEY, 0);
        lifetimeCircles = preferences.getLong(LIFETIME_CIRCLES_KEY, 0);
        pusherCount = preferences.getInt(PUSHER_COUNT_KEY, 0);



        autonomousMode = preferences.getBoolean(AUTONOMOUS_MODE_KEY, false);
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
        // עוצרים בערך הגדול ביותר כדי שהמונה לא יגלוש למספר שלילי.
        if (circlesBalance < Long.MAX_VALUE) {
            circlesBalance++;
        }
        if (lifetimeCircles < Long.MAX_VALUE) {
            lifetimeCircles++;
        }

        // שומרים את הערכים החדשים במכשיר; apply מבצע את הכתיבה ברקע.
        preferences.edit()
                .putLong(CIRCLES_BALANCE_KEY, circlesBalance)
                .putLong(LIFETIME_CIRCLES_KEY, lifetimeCircles)
                .apply();
    }
}
package com.example.collectcircles;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

public final class Notifications {

    private static final String CHANNEL_ID = "circle_invitations";
    private static final int NOTIFICATION_ID = 1;

    private Notifications() {
        // זוהי מחלקת עזר בלבד. אין צורך ליצור ממנה אובייקטים.
    }

    public static void createChannel(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);

        // כל התראה חייבת להשתייך לערוץ. יצירת ערוץ שכבר קיים אינה יוצרת כפילות,
        // ולכן בטוח לבצע את הפעולה לפני כל הצגה. חשיבות HIGH מאפשרת למערכת
        // להציג את ההתראה באופן בולט, בהתאם להגדרות שבחר המשתמש במכשיר.
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(
                context.getString(R.string.notification_channel_description)
        );
        manager.createNotificationChannel(channel);
    }

    public static void show(Context context) {
        createChannel(context);
        NotificationManager manager = context.getSystemService(NotificationManager.class);

        // ה-Builder רק מתאר את ההתראה. הקריאה notify היא הפעולה שמוסרת אותה
        // למערכת Android, ומאותו רגע המערכת אחראית להציג אותה למשתמש.
        Notification notification = new Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_text))
                .build();

        manager.notify(NOTIFICATION_ID, notification);
    }
}
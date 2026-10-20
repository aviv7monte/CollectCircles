package com.example.collectcircles;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class CircleMessagingService extends FirebaseMessagingService {

    static final String DATA_TOPIC = "circles";
    static final String NOTIFICATION_TOPIC = "circle_notifications";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        // endpoint ראשון: הודעת data. השירות שלנו בוחר אם להציג אותה.
        if ("circle_invite".equals(message.getData().get("event"))) {
            Notifications.show(this);
        }

        // endpoint שני: notification payload. ברקע FCM מציג אותו בעצמו;
        // בחזית הוא מגיע לכאן ואנו מציגים את אותה התראה קבועה.
        if (message.getNotification() != null) {
            Notifications.show(this);
        }
    }

    @Override
    public void onRegistered(String installationId) {
        super.onRegistered(installationId);

        // מזהה התקנת Firebase עשוי להתחלף. רושמים שוב את שני המנויים;
        // הפעולה בטוחה וחוזרת, ואין צורך לשמור או להדפיס את המזהה עצמו.
        FirebaseMessaging.getInstance().subscribeToTopic(DATA_TOPIC);
        FirebaseMessaging.getInstance().subscribeToTopic(NOTIFICATION_TOPIC);
    }
}
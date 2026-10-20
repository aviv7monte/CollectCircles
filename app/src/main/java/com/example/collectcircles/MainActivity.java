package com.example.collectcircles;

import android.os.Bundle;
import android.widget.Toast;
import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.messaging.FirebaseMessaging;
import com.example.collectcircles.databinding.ActivityMainBinding;
import android.content.SharedPreferences;
import android.os.SystemClock;
import com.google.firebase.functions.FirebaseFunctions;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class    MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private static final String PREFERENCES_NAME =
            "collect_circles_preferences";
    private static final String BEST_TIME_KEY = "best_time_millis";
    private static final long TIMER_REFRESH_MILLIS = 16;
    private static final long NO_BEST_TIME = Long.MAX_VALUE;

    private SharedPreferences preferences;
    private boolean gameRunning;
    private long gameStartTimeMillis;
    private long bestTimeMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        }

        );

        // ממשיכים בתוך onCreate, אבל מחוץ ל-listener של ה-insets.
        // החל מ-Android 13 צריך אישור מפורש כדי להציג התראות.
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                permissionGranted -> {     // callback כאן מוצהר מנגנון
                    // זוהי פונקציה אנונימית - קטע קוד ללא שם - שאנחנו מבקשים שירוץ
                    if (permissionGranted) {
                        Notifications.show(this);
                    } else {
                        Toast.makeText(
                                this,
                                R.string.notification_permission_denied,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        // הרשמה ראשונה לשני ה-topics. הרשמה חוזרת אינה יוצרת מנוי כפול.
        FirebaseMessaging.getInstance().subscribeToTopic(
                CircleMessagingService.DATA_TOPIC
        );
        FirebaseMessaging.getInstance().subscribeToTopic(
                CircleMessagingService.NOTIFICATION_TOPIC
        );

        // ממשיכים בתוך onCreate, אבל מחוץ ל-listener של ה-insets.
        preferences = getSharedPreferences(
                PREFERENCES_NAME,
                MODE_PRIVATE
        );
        bestTimeMillis = preferences.getLong(
                BEST_TIME_KEY,
                NO_BEST_TIME
        );
        showBestTime();

        binding.startButton.setOnClickListener(view -> startGame());
        binding.localNotifButton.setOnClickListener(
                view -> requestPermissionAndShowNotification()
        );
        binding.inviteButton.setOnClickListener(view -> sendInvite());
        binding.gameBoard.setOnGameFinishedListener(this::finishGame);




    }
    private void sendInvite() {
        binding.inviteButton.setEnabled(false);

        FirebaseFunctions.getInstance()
                .getHttpsCallable("sendCircleInvite")
                .call()
                .addOnCompleteListener(task -> {
                    binding.inviteButton.setEnabled(true);
                    int message = task.isSuccessful()
                            ? R.string.invite_sent
                            : R.string.invite_failed;
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                });
    }


    /**
            * יוצר סיבוב חדש, מאפס את זמן ההתחלה ומפעיל שרשרת רענון יחידה.
            */

    private void requestPermissionAndShowNotification() {
        // ב-Android 12 ומטה אין הרשאת התראות בזמן ריצה
        // ב-Android 13 מבקשים אותה אם עדיין לא ניתנה
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            return; // Notifications.show כלומר, אם עכשיו שלחנו בקשה לא נמשיך לשורה של ה
        }

        Notifications.show(this); // מציג התראות רק אם זו לא הלחיצה הראשונה
        // המופיע למעלה במדריך callback בלחיצה הראשונה תתקבל ההתראה בזכות מנגנון
    }

    private void startGame() {
        binding.gameBoard.startNewGame();
        gameStartTimeMillis = SystemClock.elapsedRealtime();
        gameRunning = true;

        binding.startButton.setText(R.string.restart);
        // Removes every queued execution of this exact Runnable from the View's message queue.
        // This stops future timer updates but does not delete or disable timerUpdate,
        // so the same Runnable can be started again later with timerUpdate.run().
        binding.elapsedTimeText.removeCallbacks(timerUpdate);
        timerUpdate.run();
    }


    /**
     * עוצר את הסיבוב הפעיל, מעדכן שיא במידת הצורך ומציג הודעת סיום.
     */
    private void finishGame() {
        if (!gameRunning) {
            return;
        }

        long finalTimeMillis =
                SystemClock.elapsedRealtime() - gameStartTimeMillis;
        gameRunning = false;
        binding.elapsedTimeText.removeCallbacks(timerUpdate);
        showElapsedTime(finalTimeMillis);

        if (finalTimeMillis < bestTimeMillis) {
            bestTimeMillis = finalTimeMillis;
            preferences.edit()
                    .putLong(BEST_TIME_KEY, bestTimeMillis)
                    .apply();
            showBestTime();
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.completion_title)
                .setMessage(getString(
                        R.string.completion_message,
                        millisecondsToSeconds(finalTimeMillis)
                ))
                .setPositiveButton(R.string.ok, null)
                .show();
    }
    /**
     * מציג בתווית הזמן את משך הסיבוב באלפיות שנייה.
     *
     * @param elapsedMillis משך הסיבוב באלפיות שנייה
     */
    private void showElapsedTime(long elapsedMillis) {
        binding.elapsedTimeText.setText(getString(
                R.string.elapsed_time_format,
                millisecondsToSeconds(elapsedMillis)
        ));
    }

    /**
     * מציג את השיא השמור, או את ערך ברירת המחדל אם עדיין אין שיא.
     */
    private void showBestTime() {
        if (bestTimeMillis == NO_BEST_TIME) {
            binding.bestTimeText.setText(R.string.best_time_initial);
            return;
        }

        binding.bestTimeText.setText(getString(
                R.string.best_time_format,
                millisecondsToSeconds(bestTimeMillis)
        ));
    }

    /**
     * ממיר משך מאלפיות שנייה לשניות לצורך תצוגה.
     *
     * @param milliseconds משך באלפיות שנייה
     * @return אותו משך בשניות
     */
    private double millisecondsToSeconds(long milliseconds) {
        return milliseconds / 1000.0;
    }

    private final Runnable timerUpdate = new Runnable() {
        /**
         * מרעננת את הזמן המוצג ומתזמנת רענון נוסף כל עוד המשחק פעיל.
         */
        @Override
        public void run() {
            if (!gameRunning) {
                return;
            }

            showElapsedTime(
                    SystemClock.elapsedRealtime() - gameStartTimeMillis
            );
            binding.elapsedTimeText.postDelayed(
                    this,
                    TIMER_REFRESH_MILLIS
            );
        }
    };


    /**
     * מפעיל מחדש את רענון הזמן כאשר הפעילות חוזרת למסך.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (gameRunning) {
            binding.elapsedTimeText.removeCallbacks(timerUpdate);
            timerUpdate.run();
        }
    }

    /**
     * מסיר עדכוני זמן מתוזמנים כאשר הפעילות אינה גלויה.
     */
    @Override
    protected void onStop() {
        binding.elapsedTimeText.removeCallbacks(timerUpdate);
        super.onStop();
    }
}
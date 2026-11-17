package com.example.collectcircles;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class PusherEligibilityWorker extends Worker {

    /**
     * Creates the Worker with the data supplied by WorkManager.
     *
     * @param appContext application context supplied by WorkManager
     * @param workerParameters input and runtime parameters for this work
     */
    public PusherEligibilityWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParameters
    ) {
        super(appContext, workerParameters);
    }

    /**
     * Checks the saved balance and notifies the player when a Pusher is affordable.
     *
     * @return success after the eligibility check finishes
     */
    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        GameProgress progress = GameProgress.load(context);

        if (progress.canBuyPusher() && Notifications.canShow(context)) {
            Notifications.showPusherAvailable(
                    context,
                    progress.getNextPusherPrice()
            );
        }

        return Result.success();
    }
}
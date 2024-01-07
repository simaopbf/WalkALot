package com.example.actuallayout;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class Notifications {

    private static final String CHANNEL_ID = "steps_notification_channel";
    private static final String CHANNEL_NAME = "Steps Notification Channel";
    private static final int NOTIFICATION_ID = 1;

    public static void showNotification(Context context, String title, String message) {
        // Create a notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel (required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Show the notification
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static void checkAndNotifyStepsGoal(Context context, long userId) {
        // Retrieve steps goal and steps for the current date from your database
        int stepsGoal = // Retrieve steps goal for the user with userId
        int stepsForCurrentDate = // Retrieve steps for the current date and user with userId

        // Check if the steps taken are greater than the goal
        if (stepsForCurrentDate >= stepsGoal) {
            // Show notification if the goal is achieved
            showNotification(context, "Steps Goal Achieved!", "Congratulations! You've reached your steps goal for today.");
        }
    }
}

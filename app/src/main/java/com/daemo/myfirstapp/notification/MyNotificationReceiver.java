package com.daemo.myfirstapp.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.NotificationCompat;

import com.daemo.myfirstapp.Constants;
import com.daemo.myfirstapp.R;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.daemo.myfirstapp.Constants.ACTION_DELETE;
import static com.daemo.myfirstapp.Constants.NOTIFICATION_GROUP;
import static com.daemo.myfirstapp.Constants.NOTIFICATION_GROUP_SUMMARY_ID;

public class MyNotificationReceiver extends BroadcastReceiver {
    public MyNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Constants.ACTION_UPDATE)) updateAction(context, intent);
        else if (intent.getAction().equals(Constants.ACTION_DELETE))
            updateSummaries(context, intent);
        else deleteNotification(context, intent);

    }

    private void deleteNotification(Context context, Intent intent) {
        int int_notif_id = -1;
        if (intent != null) int_notif_id = Integer.parseInt(intent.getDataString());

        ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE)).cancel(int_notif_id);
        updateSummaries(context, intent);
    }

    private void updateSummaries(Context context, Intent intent) {
        updateNotificationSummary(context, (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE));
        if (NotificationActivity.active) {
            Intent respIntent = new Intent();
            respIntent.setClass(context, NotificationActivity.class);
            respIntent.setAction(Constants.ACTION_DELETE);
            context.startActivity(respIntent);
        }
    }

    public void updateAction(Context context, Intent intent) {
        String message = "No message";
        int int_notif_id = -1;
        if (intent != null) {
            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
            if (remoteInput != null)
                message = String.valueOf(remoteInput.getCharSequence(NotificationActivity.key_text_reply));

            int_notif_id = Integer.parseInt(intent.getDataString());
        }

        // Build a new notification, which informs the user that the system handled their interaction with the previous notification.
        NotificationCompat.Builder repliedNotification = new NotificationCompat.Builder(context);
        repliedNotification
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, NotificationActivity.class), 0))
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentText("replied to id " + int_notif_id + " with: " + message)
                .setGroup(NOTIFICATION_GROUP)
                .setAutoCancel(true)
                .setDeleteIntent(getDeletePendingIntent(context))
                .build();

        // Issue the new notification.
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(int_notif_id, repliedNotification.build());
    }

    /**
     * Adds/updates/removes the notification summary as necessary.
     */
    static void updateNotificationSummary(Context context, NotificationManager notificationManager) {
        int numberOfNotifications = getNumberOfNotifications(notificationManager);

        // Remove the notification summary.
        if (numberOfNotifications >= 1) {
            // Add/update the notification summary.
            String notificationContent = context.getString(R.string.sample_notification_summary_content, numberOfNotifications);

            NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(context);
            summaryBuilder
                    //.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, NotificationActivity.class), 0))
                    .setSmallIcon(android.R.drawable.stat_notify_chat)
                    .setStyle(new NotificationCompat.InboxStyle()
                            .setSummaryText(notificationContent))
                    .setGroup(NOTIFICATION_GROUP)
                    .setGroupSummary(true);

            notificationManager.notify(NOTIFICATION_GROUP_SUMMARY_ID, summaryBuilder.build());
        } else notificationManager.cancel(NOTIFICATION_GROUP_SUMMARY_ID);
    }

    static int getNumberOfNotifications(NotificationManager notificationManager) {
        // [BEGIN get_active_notifications]
        // Query the currently displayed notifications.
        StatusBarNotification[] activeNotifications;
        List<StatusBarNotification> validNotifications = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            activeNotifications = notificationManager.getActiveNotifications();
            // [END get_active_notifications]

            // Since the notifications might include a summary notification remove it from the count if it is present.
            for (StatusBarNotification notification : activeNotifications) {
                if (notification.getId() == NOTIFICATION_GROUP_SUMMARY_ID) continue;
                validNotifications.add(notification);
            }
        }
        return validNotifications.size();
    }


    static PendingIntent getDeletePendingIntent(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, MyNotificationReceiver.class);
        intent.setAction(ACTION_DELETE);
        return PendingIntent.getBroadcast(
                context,
                0,
                intent,
                0
        );
    }
}

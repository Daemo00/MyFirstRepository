package com.daemo.myfirstapp.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.NotificationCompat;

import com.daemo.myfirstapp.R;

import java.util.Collections;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.daemo.myfirstapp.common.Constants.ACTION_DELETE;
import static com.daemo.myfirstapp.common.Constants.ACTION_UPDATE;
import static com.daemo.myfirstapp.common.Constants.NOTIFICATION_GROUP;
import static com.daemo.myfirstapp.common.Constants.NOTIFICATION_GROUP_SUMMARY_ID;

public class MyNotificationReceiver extends BroadcastReceiver {

    private NotificationManager notificationManager;

    public MyNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (intent.getAction().equals(ACTION_UPDATE)) updateAction(context, intent);
        else if (intent.getAction().equals(ACTION_DELETE)) updateSummaries(context);
        else deleteNotification(context, intent);
    }

    private void deleteNotification(Context context, Intent intent) {
        int int_notification_id = -1;
        if (intent != null)
            int_notification_id = Integer.parseInt(intent.getDataString());

        notificationManager.cancel(int_notification_id);
        updateSummaries(context);
    }

    private void updateSummaries(Context context) {
        updateNotificationSummary(context, notificationManager);
        if (NotificationActivity.active) {
            Intent respIntent = new Intent();
            respIntent.setClass(context, NotificationActivity.class);
            respIntent.setAction(ACTION_DELETE);
            context.startActivity(respIntent);
        }
    }

    public void updateAction(Context context, Intent intent) {
        String message = "No message";
        int int_notification_id = -1;
        if (intent != null) {
            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
            if (remoteInput != null)
                message = String.valueOf(remoteInput.getCharSequence(NotificationActivity.key_text_reply));

            int_notification_id = Integer.parseInt(intent.getDataString());
        }

        // Build a new notification, which informs the user that the system handled their interaction with the previous notification.
        NotificationCompat.Builder repliedNotification = new NotificationCompat.Builder(context);
        repliedNotification
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, NotificationActivity.class), 0))
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentText("replied to id " + int_notification_id + " with: " + message)
                .setGroup(NOTIFICATION_GROUP)
                .setAutoCancel(true)
                .setDeleteIntent(getDeletePendingIntent(context))
                .build();

        // Issue the new notification.
        notificationManager.notify(int_notification_id, repliedNotification.build());
    }

    /**
     * Adds/updates/removes the notification summary as necessary.
     */
    static void updateNotificationSummary(Context context, NotificationManager notificationManager) {
        int numberOfNotifications = Utils.getNumberOfNotifications(
                notificationManager,
                Collections.singletonList(NOTIFICATION_GROUP_SUMMARY_ID));

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

package com.daemo.myfirstapp.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.daemo.myfirstapp.activities.DialogActivity;
import com.daemo.myfirstapp.activities.MySuperActivity;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Constants;
import com.daemo.myfirstapp.common.Utils;
import com.google.common.base.Objects;

import java.util.Collections;

import static com.daemo.myfirstapp.common.Constants.ACTION_UPDATE;
import static com.daemo.myfirstapp.common.Constants.NOTIFICATION_GROUP;
import static com.daemo.myfirstapp.common.Constants.NOTIFICATION_GROUP_SUMMARY_ID;
import static com.daemo.myfirstapp.notification.MyNotificationReceiver.getDeletePendingIntent;
import static com.daemo.myfirstapp.notification.MyNotificationReceiver.updateNotificationSummary;

public class NotificationActivity extends MySuperActivity {

    static final String key_text_reply = "key_text_reply";

    NotificationCompat.Builder mBuilder;
    private TextView mNumberOfNotifications;
    private NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        // Gets an instance of the NotificationManager service
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            requestShowKeyboardShortcuts();
        mNumberOfNotifications = (TextView) findViewById(R.id.notification_number);

        if (getIntent() != null) {
            Log.i(Utils.getTag(this), "Received an intent with action " + getIntent().getAction());
            if (Objects.equal(getIntent().getAction(), Constants.ACTION_DELETE))
                updateNumberOfNotifications();
        }
    }

    public void issueNotification(View v) {

        int notificationId = com.daemo.myfirstapp.notification.Utils.generateNewNotificationId(mNotificationManager);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle("My content title")
                .setContentText("My content text, id is " + notificationId)
                .setGroup(NOTIFICATION_GROUP)
                .setDeleteIntent(getDeletePendingIntent(this))
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())

                // Set dynamic attributes of notification
                .setContentIntent(((ToggleButton) findViewById(R.id.toggleButton)).isChecked() ?
                        getPendingSpecialIntent() :
                        getPendingIntent());

        mBuilder.addAction(getAction(
                notificationId,
                Constants.ACTION_SNOOZE,
                R.drawable.ic_stat_snooze,
                getString(R.string.snooze)));

        mBuilder.addAction(inlineReply(notificationId));

        progressTask(notificationId);

        // Builds the notification and issues it.
        mNotificationManager.notify(notificationId, mBuilder.build());
        updateNotificationSummary(this, mNotificationManager);
        updateNumberOfNotifications();
    }

    @NonNull
    public NotificationCompat.Action getAction(int notificationId, String action, int icon, String label) {
        Intent intent = new Intent(this, MyNotificationReceiver.class);
        intent.setAction(action);
        intent.setData(Uri.parse(String.valueOf(notificationId)));
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        return new NotificationCompat.Action(icon, label, pIntent);
    }

    private void progressTask(final int notificationId) {
        // Start a lengthy operation in a background thread
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        NotificationCompat.Builder builder = mBuilder;
                        int increment;
                        // Do the "lengthy" operation 20 times
                        for (increment = 0; increment <= 100; increment += 5) {
                            // Sets the progress indicator to a max value, the current completion percentage, and "determinate" state
//                            builder.setProgress(100, increment, false);
                            builder.setProgress(0, 0, true);
                            // Displays the progress bar for the first time.
                            mNotificationManager.notify(notificationId, builder.build());
                            // Sleeps the thread, simulating an operation
                            // that takes time
                            try {
                                // Sleep for 5 seconds
                                Thread.sleep(5 * 1000);
                            } catch (InterruptedException e) {
                                Log.d(Utils.getTag(this), "sleep failure");
                            }
                        }
                        // When the loop is finished, updates the notification
                        builder.setContentText("Download complete")
                                // Removes the progress bar
                                .setProgress(0, 0, false);
                        mNotificationManager.notify(notificationId, builder.build());
                    }
                }
// Starts the thread by calling the run() method in its Runnable
        ).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNumberOfNotifications();
    }

    /**
     * Requests the current number of notifications from the {@link NotificationManager} and
     * display them to the user.
     */
    protected void updateNumberOfNotifications() {
        final int numberOfNotifications = com.daemo.myfirstapp.notification.Utils.getNumberOfNotifications(
                mNotificationManager,
                Collections.singletonList(NOTIFICATION_GROUP_SUMMARY_ID));
        mNumberOfNotifications.setText(getString(R.string.active_notifications,
                numberOfNotifications));
    }

    private PendingIntent getPendingIntent() {
        Intent resultIntent = new Intent(this, this.getClass());

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack
        stackBuilder.addParentStack(this.getClass());
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getPendingSpecialIntent() {
        // Creates an Intent for the Activity
        Intent notifyIntent =
                new Intent(this, DialogActivity.class);
        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        return PendingIntent.getActivity(
                this,
                0,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private PendingIntent getPendingBroadcastIntent(int notificationId) {
        // Creates an Intent for the Activity
        Intent notifyIntent = new Intent();
        notifyIntent.setClass(this, MyNotificationReceiver.class);
        notifyIntent.setAction(ACTION_UPDATE);
        notifyIntent.setData(Uri.parse(String.valueOf(notificationId)));
        // Creates the PendingIntent
        return PendingIntent.getBroadcast(
                this,
                Constants.KEY_ACTION_UPDATE,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private NotificationCompat.Action inlineReply(int notificationId) {
        // Key for the string that's delivered in the action's intent.
        RemoteInput remoteInput = new RemoteInput.Builder(key_text_reply)
                .setLabel("Reply")
                .build();

        PendingIntent intent = getPendingBroadcastIntent(notificationId);

        // Create the reply action and add the remote input.
        return new NotificationCompat.Action.Builder(R.drawable.ic_stat_notification,
                "Reply", intent)
                .addRemoteInput(remoteInput)
                .build();
    }

    static boolean active = false;

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }
}

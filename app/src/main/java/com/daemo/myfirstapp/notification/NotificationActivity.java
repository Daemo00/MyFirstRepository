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

import com.daemo.myfirstapp.Constants;
import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Utils;
import com.google.common.base.Objects;

import static com.daemo.myfirstapp.Constants.ACTION_UPDATE;
import static com.daemo.myfirstapp.Constants.NOTIFICATION_GROUP;
import static com.daemo.myfirstapp.Constants.NOTIFICATION_GROUP_SUMMARY_ID;
import static com.daemo.myfirstapp.notification.MyNotificationReceiver.getDeletePendingIntent;
import static com.daemo.myfirstapp.notification.MyNotificationReceiver.getNumberOfNotifications;
import static com.daemo.myfirstapp.notification.MyNotificationReceiver.updateNotificationSummary;

public class NotificationActivity extends MySuperActivity {

    static final String key_text_reply = "key_text_reply";

    NotificationCompat.Builder mBuilder;
    private TextView mNumberOfNotifications;
    private int sNotificationId = 0;
    private NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gets an instance of the NotificationManager service
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestShowKeyboardShortcuts();
        }
        mNumberOfNotifications = (TextView) findViewById(R.id.notif_number);

        if (getIntent() != null) {
            Log.i(Utils.getTag(this), "Received an intent with action " + getIntent().getAction());
            if (Objects.equal(getIntent().getAction(), Constants.ACTION_DELETE))
                updateNumberOfNotifications();
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_notification;
    }

    public void issueNotification(View v) {
        int notif_id = getNotificationId();
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle("My content title")
                .setContentText("My content text, id is " + notif_id)
                .setGroup(NOTIFICATION_GROUP)
                .setDeleteIntent(getDeletePendingIntent(this))
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())

                // Set dynamic attributes of notification
                .setContentIntent(((ToggleButton) findViewById(R.id.toggleButton)).isChecked() ?
                        getPendingSpecialIntent(notif_id) :
                        getPendingIntent(notif_id));

        mBuilder.addAction(getAction(
                notif_id,
                Constants.ACTION_SNOOZE,
                R.drawable.ic_stat_snooze,
                getString(R.string.snooze)));

        mBuilder.addAction(inlineReply(notif_id));

        progressTask(notif_id);

        // Builds the notification and issues it.
        mNotificationManager.notify(notif_id, mBuilder.build());
        updateNotificationSummary(this, mNotificationManager);
        updateNumberOfNotifications();
    }

    public int getNotificationId() {
        int notificationId = sNotificationId++;

        // Unlikely in the sample, but the int will overflow if used enough so we skip the summary ID.
        // Most apps will prefer a more deterministic way of identifying an ID such as hashing the content of the notification.
        if (notificationId == NOTIFICATION_GROUP_SUMMARY_ID) notificationId = sNotificationId++;

        return notificationId;
    }

    @NonNull
    public NotificationCompat.Action getAction(int notif_id, String action, int icon, String label) {
        Intent intent = new Intent(this, MyNotificationReceiver.class);
        intent.setAction(action);
        intent.setData(Uri.parse(String.valueOf(notif_id)));
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        return new NotificationCompat.Action(icon, label, pIntent);
    }

    private void progressTask(final int notif_id) {
        // Start a lengthy operation in a background thread
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        NotificationCompat.Builder builder = mBuilder;
                        int incr;
                        // Do the "lengthy" operation 20 times
                        for (incr = 0; incr <= 100; incr += 5) {
                            // Sets the progress indicator to a max value, the current completion percentage, and "determinate" state
//                            builder.setProgress(100, incr, false);
                            builder.setProgress(0, 0, true);
                            // Displays the progress bar for the first time.
                            mNotificationManager.notify(notif_id, builder.build());
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
                        mNotificationManager.notify(notif_id, builder.build());
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
        final int numberOfNotifications = getNumberOfNotifications(mNotificationManager);
        mNumberOfNotifications.setText(getString(R.string.active_notifications,
                numberOfNotifications));
    }

    private PendingIntent getPendingIntent(int notif_id) {
        Intent resultIntent = new Intent(this, this.getClass());

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack
        stackBuilder.addParentStack(this.getClass());
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getPendingSpecialIntent(int notif_id) {
        // Creates an Intent for the Activity
        Intent notifyIntent =
                new Intent(this, NotificationSpecialActivity.class);
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

    private PendingIntent getPendingBroadcastIntent(int notif_id) {
        // Creates an Intent for the Activity
        Intent notifyIntent = new Intent();
        notifyIntent.setClass(this, MyNotificationReceiver.class);
        notifyIntent.setAction(ACTION_UPDATE);
        notifyIntent.setData(Uri.parse(String.valueOf(notif_id)));
        // Creates the PendingIntent
        return PendingIntent.getBroadcast(
                this,
                Constants.KEY_ACTION_UPDATE,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }


    private NotificationCompat.Action inlineReply(int notif_id) {
        // Key for the string that's delivered in the action's intent.
        RemoteInput remoteInput = new RemoteInput.Builder(key_text_reply)
                .setLabel("Reply")
                .build();

        PendingIntent intent = getPendingBroadcastIntent(notif_id);

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

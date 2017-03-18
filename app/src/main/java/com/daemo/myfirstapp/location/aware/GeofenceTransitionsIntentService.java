package com.daemo.myfirstapp.location.aware;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.daemo.myfirstapp.MySuperApplication;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Constants;
import com.daemo.myfirstapp.common.Utils;
import com.daemo.myfirstapp.graphics.displayingbitmaps.util.ImageResizer;
import com.daemo.myfirstapp.location.LocationActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {
    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        Log.d(Utils.getTag(this), Utils.debugIntent(intent));
        if (geofencingEvent.hasError()) {
            Log.e(Utils.getTag(this), "Error is " + Constants.Location.getErrorString(this, geofencingEvent.getErrorCode()));
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (Constants.Location.GEOFENCE_TRANSITIONS.contains(geofenceTransition)) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition, triggeringGeofences);

            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails);
            Log.i(Utils.getTag(this), geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e(Utils.getTag(this), getString(R.string.geofence_transition_invalid_type,
                    geofenceTransition));
        }
    }

    private String getGeofenceTransitionDetails(
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences)
            triggeringGeofencesIdsList.add(geofence.getRequestId());

        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }

    private void sendNotification(String notificationDetails) {

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this)

                // Add the main Activity to the task stack as the parent.
                .addParentStack(LocationActivity.class)

                // Create an explicit content Intent that starts the location Activity and push it onto the stack.
                .addNextIntent(new Intent(getApplicationContext(), LocationActivity.class));

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(Constants.Location.NOTIF_GEOFENCE_TRANSITION, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)//    R.drawable.ic_launcher)
                // In a real app, you may want to use a library like Volley to decode the Bitmap.
                .setLargeIcon(
                        ImageResizer.decodeSampledBitmapFromResource(getResources(),
                                android.R.drawable.ic_menu_mylocation,
                                10,
                                10,
                                ((MySuperApplication) getApplication()).getImageCache())
                )
                .setContentTitle(notificationDetails)
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent)
                // Dismiss notification once the user touches it.
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setColor(getBaseContext().getResources().getColor(R.color.colorPrimary, getBaseContext().getTheme()));
        }
        // Get an instance of the Notification manager
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(Constants.Location.NOTIF_GEOFENCE_TRANSITION, builder.build());
    }
}

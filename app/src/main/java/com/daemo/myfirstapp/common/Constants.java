package com.daemo.myfirstapp.common;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.daemo.myfirstapp.BuildConfig;
import com.daemo.myfirstapp.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public final class Constants {

    public static final String ACTION_DELETE = BuildConfig.APPLICATION_ID.concat(".ACTION_DELETE");
    public static final String ACTION_UPDATE = BuildConfig.APPLICATION_ID.concat(".ACTION_UPDATE");
    public static final String ACTION_DISMISS = BuildConfig.APPLICATION_ID.concat(".ACTION_DISMISS");
    public static final String ACTION_SNOOZE = BuildConfig.APPLICATION_ID.concat(".ACTION_SNOOZE");
    public static final String ACTION_PLAY = BuildConfig.APPLICATION_ID.concat(".ACTION_PLAY");
    public static final String ACTION_STOP = BuildConfig.APPLICATION_ID.concat(".ACTION_STOP");
    public static final String ACTION_RETURN_FILE = BuildConfig.APPLICATION_ID.concat(".ACTION_RETURN_FILE");

    public static final String EXTRA_IMAGE = BuildConfig.APPLICATION_ID.concat(".EXTRA_IMAGE");
    public static final String NOTIFICATION_GROUP = BuildConfig.APPLICATION_ID.concat(".NOTIFICATION_GROUP");
    public static final int NOTIFICATION_GROUP_SUMMARY_ID = 100;
    public static final int NOTIFICATION_ID_MUSIC = 1;
    public static final int NOTIFICATION_ID_FIREBASE = 2;

    public static final int REQUEST_CODE_MUSIC = 1;
    public static final int REQUEST_CODE_VIDEO_PERMISSIONS = 2;
    public static final int REQUEST_CODE_LOCATION = 3;

    public static final int REQUEST_CODE_OVERLAY_PERMISSION = 4;
    public static final int KEY_ACTION_DELETE = 101;
    public static final int KEY_ACTION_UPDATE = 102;
    public static final String IMAGE_CACHE_DIR = "image_cache";
    public static final float CACHE_SIZE = 0.25f;
    public static final int WIDGET_INTENT = 1;
    public static final String ARTICLE_SELECTED = BuildConfig.APPLICATION_ID.concat(".article_selected");
    public final static String mediaSessionTag = BuildConfig.APPLICATION_ID.concat(".mediaSessionTag");
    // Create a string for the ImageView label
    public static final String IMAGE_TAG = "icon bitmap";
    public static final String ACTION_REPLACE_FRAGMENT = "replace_fragment";
    public static final String ACTION_FRAGMENT = "action_fragment";
    public static final String ACTION_ADDTOBACKSTACK = "action_addtobackstack";
    public static final String ACTION_FIREBASE_LOGIN_LOGOUT = "Firebase_login";
    public static boolean SERVICE_CHAT_HEAD_RUNNING = false;

    public static final class Cache {
        // Constants to easily toggle various caches
        public static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
        // Default memory cache size in kilobytes
        public static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 5; // 5MB
        // Default disk cache size in bytes
        public static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
        // Compression settings when writing images to disk cache
        public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
        public static final int DEFAULT_COMPRESS_QUALITY = 70;
        public static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
        public static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;
    }

    public static final class Location {

        private static final String PACKAGE_NAME = "com.daemo.myfirstapp.location";

        public static final int REQUEST_CODE_CHECK_SETTINGS = 1;
        public static final int REQUEST_LAST_LOCATION = 2;
        public static final int REQUEST_CONNECTION = 3;
        public static final int REQUEST_LOCATION_UPDATES = 4;
        public static final int REQUEST_GEOFENCES = 5;
        public static final int KEY_REQUEST_GEOFENCE_INTENT = 6;
        public static final int NOTIF_GEOFENCE_TRANSITION = 7;

        static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES_NAME";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
        public static final String KEY_RESULT_DATA = PACKAGE_NAME + ".KEY_RESULT_DATA";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";


        public static List<Integer> GEOFENCE_TRANSITIONS = Arrays.asList(
                Geofence.GEOFENCE_TRANSITION_ENTER,
                Geofence.GEOFENCE_TRANSITION_EXIT
        );
        public static final float GEOFENCE_RADIUS_IN_METERS = 1000; // 1 km
        static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

        private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
        /**
         * 1 hour
         */
        public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;


        public static final HashMap<String, LatLng> LANDMARKS = new HashMap<>();

        static {
            // Fiumara
            LANDMARKS.put("Fiumara", new LatLng(44.413645, 8.880467));

            // Casa
            LANDMARKS.put("Casa", new LatLng(44.400755, 8.982703));

        }

        @NonNull
        public static String getErrorString(Context context, int errorCode) {
            Resources mResources = context.getResources();
            switch (errorCode) {
                case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                    return mResources.getString(R.string.geofence_not_available);
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                    return mResources.getString(R.string.geofence_too_many_geofences);
                case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    return mResources.getString(R.string.geofence_too_many_pending_intents);
                default:
                    return mResources.getString(R.string.unknown_geofence_error);
            }
        }
    }
}
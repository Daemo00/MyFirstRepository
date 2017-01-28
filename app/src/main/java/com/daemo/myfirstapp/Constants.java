package com.daemo.myfirstapp;

public class Constants {

    public static final String ACTION_DELETE = BuildConfig.APPLICATION_ID.concat(".ACTION_DELETE");
    public static final String ACTION_UPDATE = BuildConfig.APPLICATION_ID.concat(".ACTION_UPDATE");
    public static final String ACTION_DISMISS = BuildConfig.APPLICATION_ID.concat(".ACTION_DISMISS");
    public static final String ACTION_SNOOZE = BuildConfig.APPLICATION_ID.concat(".ACTION_SNOOZE");
    public static final String NOTIFICATION_GROUP = BuildConfig.APPLICATION_ID.concat(".NOTIFICATION_GROUP");

    public static final int NOTIFICATION_GROUP_SUMMARY_ID = 100;
    public static final int KEY_ACTION_DELETE = 101;
    public static final int KEY_ACTION_UPDATE = 102;

    public static final String IMAGE_CACHE_DIR = "image_cache";
    public static final String EXTRA_IMAGE = "extra_image";
    public static final float CACHE_SIZE = 0.25f;
    public static final String MY_SUPER_FRAGMENT_TITLE = "MY_SUPER_FRAGMENT_TITLE";
}
package com.daemo.myfirstapp.notification;

import android.app.NotificationManager;
import android.service.notification.StatusBarNotification;

import com.daemo.myfirstapp.common.Constants;
import com.google.common.collect.Lists;

import java.lang.reflect.Field;
import java.text.CollationElementIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Utils {
    public static int getNumberOfNotifications(NotificationManager notificationManager, List<Integer> excluded_ids) {
        // Query the currently displayed notifications.
        StatusBarNotification[] activeNotifications;
        List<StatusBarNotification> validNotifications = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            activeNotifications = notificationManager.getActiveNotifications();

            // Since the notifications might include a summary notification remove it from the count if it is present.
            for (StatusBarNotification notification : activeNotifications) {
                if (excluded_ids.contains(notification.getId())) continue;
                validNotifications.add(notification);
            }
        }
        return validNotifications.size();
    }

    private static int notificationId = 0;
    public static int generateNotificationId() {
        List<Integer> excludedIds = new ArrayList<>();
        for (Field f : Constants.class.getDeclaredFields()) {
            if (f.getName().toUpperCase().contains("NOTIFICATION") && f.getType() == int.class) {
                try {
                    excludedIds.add(f.getInt(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        // Unlikely in the sample, but the int will overflow if used enough so we skip the summary ID.
        // Most apps will prefer a more deterministic way of identifying an ID such as hashing the content of the notification.
        notificationId++;
        while (excludedIds.contains(notificationId))
            notificationId++;

        return notificationId;
    }
}

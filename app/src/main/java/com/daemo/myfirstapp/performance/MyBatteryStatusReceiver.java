package com.daemo.myfirstapp.performance;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.util.Pair;

import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Constants;
import com.daemo.myfirstapp.common.Utils;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.daemo.myfirstapp.common.Constants.NOTIFICATION_BATTERY_GROUP;
import static com.daemo.myfirstapp.common.Constants.NOTIFICATION_GROUP_BATTERY_SUMMARY_ID;
import static com.daemo.myfirstapp.notification.Utils.generateNotificationId;
import static com.daemo.myfirstapp.notification.Utils.getNumberOfNotifications;

public class MyBatteryStatusReceiver extends BroadcastReceiver {

    private NotificationManager notificationManager;

    public MyBatteryStatusReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Date receivedDate = new Date();
        String receivedDateString = SimpleDateFormat.getTimeInstance().format(receivedDate);
        Pair<Integer, String> batteryInfo =
                buildMessage(context);
        int smallIcon = batteryInfo.first;
        String notificationTitle = batteryInfo.second.split(System.lineSeparator(), 2)[0];
        String notificationContent = batteryInfo.second.split(System.lineSeparator(), 2)[1];
        updateNotificationSummary(context);

        int notificationId = generateNotificationId();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(smallIcon)
                .setContentTitle(notificationTitle)
                .setWhen(receivedDate.getTime())
                .setGroup(NOTIFICATION_BATTERY_GROUP)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setSummaryText(String.format(Locale.getDefault(),
                                "Battery update %d of %s", notificationId, receivedDateString))
                        .setBigContentTitle(notificationTitle)
                        .bigText(notificationContent))
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBuilder.setColor(context.getResources().getColor(R.color.colorPrimary, context.getTheme()));
        }

        notificationManager.notify(notificationId, mBuilder.build());
    }

    private void updateNotificationSummary(Context context) {
        int numberOfNotifications = getNumberOfNotifications(
                notificationManager,
                Collections.singletonList(NOTIFICATION_GROUP_BATTERY_SUMMARY_ID));
        Log.d(Utils.getTag(this), String.format("Number of notifications is %d", numberOfNotifications));
        // Remove the notification summary.
        if (numberOfNotifications >= 1) {
            // Add/update the notification summary.
            NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(context);
            summaryBuilder
                    //.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, NotificationActivity.class), 0))
                    .setSmallIcon(android.R.drawable.ic_lock_idle_low_battery)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setGroup(NOTIFICATION_BATTERY_GROUP)
                    .setGroupSummary(true);

            notificationManager.notify(Constants.NOTIFICATION_GROUP_BATTERY_SUMMARY_ID, summaryBuilder.build());
        } else notificationManager.cancel(Constants.NOTIFICATION_GROUP_BATTERY_SUMMARY_ID);
    }

    public static Pair<Integer, String> buildMessage(Context context) {
        Pair<Integer, String> res = new Pair<>(-1, "");
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (intent == null) return res;
        // First line will be the title
        String title = String.format(Locale.getDefault(), "%d, %s",
                intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1),
                parsePluggedStatus(intent));

        String message = "Plugged status: " + parsePluggedStatus(intent) + "\n";
        message += "Battery status: " + parseBatteryStatus(intent) + "\n";
        message += "Health status: " + parseHealthStatus(intent) + "\n";

        message += "Technology: " + intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
        message += "\n";

        message += "Temperature: " + (float) intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10;
        message += "°C\n";

        message += String.format(Locale.getDefault(), "Voltage: %.2f", (float) intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) / 1000);
        message += "V\n";

        message += parseBatteryLevel(intent) + "\n";

        return new Pair<>(
                intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, -1),
                title + System.lineSeparator() + message);
    }

    @NonNull
    private static String parseBatteryLevel(Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float) scale;
        return String.format(Locale.getDefault(), "%d/%d = %.2f", level, scale, batteryPct);
    }

    @NonNull
    private static String parseHealthStatus(Intent intent) {
        String res = "";
        int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        if (health == BatteryManager.BATTERY_HEALTH_COLD) res += "Cold";
        else if (health == BatteryManager.BATTERY_HEALTH_DEAD) res += "Dead";
        else if (health == BatteryManager.BATTERY_HEALTH_GOOD) res += "Good";
        else if (health == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE) res += "Over voltage";
        else if (health == BatteryManager.BATTERY_HEALTH_OVERHEAT) res += "Overheat";
        else if (health == BatteryManager.BATTERY_HEALTH_UNKNOWN) res += "Unknown";
        else if (health == BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE) res += "Unspecified Failure";
        else res += String.valueOf(health);
        return res;
    }

    @NonNull
    private static String parsePluggedStatus(Intent intent) {
        String res = "";
        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB) res += "USB";
        else if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC) res += "AC";
        else if (chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS) res += "Wireless";
        else if (chargePlug == 0) res += "Unplugged";
        else res += String.valueOf(chargePlug);
        return res;
    }

    @NonNull
    private static String parseBatteryStatus(Intent intent) {
        String res = "";
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if (status == BatteryManager.BATTERY_STATUS_CHARGING) res += "Charging";
        else if (status == BatteryManager.BATTERY_STATUS_FULL) res += "Full";
        else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) res += "Discharging";
        else res += String.valueOf(status);
        return res;
    }
}
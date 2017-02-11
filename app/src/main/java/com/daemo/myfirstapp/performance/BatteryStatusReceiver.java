package com.daemo.myfirstapp.performance;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.v7.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class BatteryStatusReceiver extends BroadcastReceiver {

    public BatteryStatusReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Object[] infos = buildMessage(context);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        int notif_id = 111;
        mBuilder.setSmallIcon(((Integer) infos[0]))
                .setContentTitle("Battery update: " + intent.getAction().substring(intent.getAction().lastIndexOf(".") + 1))
                .setContentText(infos[1].toString())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(infos[1].toString()))
                .setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(notif_id, mBuilder.build());
    }

    public static Object[] buildMessage(Context context) {
        Object[] res = new Object[]{-1, ""};
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (intent == null) return res;
        String message = intent.getAction().substring(intent.getAction().lastIndexOf(".") + 1) + "\n";

        message += "Battery status: ";
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if (status == BatteryManager.BATTERY_STATUS_CHARGING) message += "Charging";
        else if (status == BatteryManager.BATTERY_STATUS_FULL) message += "Full";
        else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) message += "Discharging";
        else message += String.valueOf(status);
        message += "\n";

        message += "Plugged status: ";
        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB) message += "USB";
        else if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC) message += "AC";
        else if (chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS) message += "Wireless";
        else if (chargePlug == 0) message += "Unplugged";
        else message += String.valueOf(chargePlug);

        message += "\n";

        message += "Health status: ";
        int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        if (health == BatteryManager.BATTERY_HEALTH_COLD) message += "Cold";
        else if (health == BatteryManager.BATTERY_HEALTH_DEAD) message += "Dead";
        else if (health == BatteryManager.BATTERY_HEALTH_GOOD) message += "Good";
        else if (health == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE) message += "Over voltage";
        else if (health == BatteryManager.BATTERY_HEALTH_OVERHEAT) message += "Overheat";
        else if (health == BatteryManager.BATTERY_HEALTH_UNKNOWN) message += "Unknown";
        else if (health == BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE)
            message += "Unspecified Failure";
        else message += String.valueOf(health);
        message += "\n";

        message += "Technology: " + intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
        message += "\n";

        res[0] = intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, -1);

        message += "Temperature: " + intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        message += "\n";

        message += "Voltage: " + intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
        message += "\n";

        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float) scale;
        message += String.valueOf(level) + "/" + String.valueOf(scale) + " = " + String.valueOf(batteryPct);

        res[1] = message + ".";
        return res;
    }
}
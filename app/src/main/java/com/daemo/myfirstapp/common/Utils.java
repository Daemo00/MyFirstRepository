package com.daemo.myfirstapp.common;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.chatHeads.ChatHeadService;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("unused")
public class Utils {

    public static String getTag(Object inst) {
        return getTag(inst.getClass());
    }

    public static String getTag(Class clazz) {
        String className = clazz.getSimpleName();
        return className.isEmpty() ? "Anonymous Class" : className;
    }
    public static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1).toLowerCase();
    }

    public static String printList(Collection list) {
        StringBuilder res = new StringBuilder("{");
        res.append(System.getProperty("line.separator"));
        int index = 0;
        for (Object o : list)
            res.append(String.format(Locale.getDefault(), "\titem %d:\t%s\t(%s)", index++, o.toString(), o.getClass().getName()))
                    .append(System.getProperty("line.separator"));

        res.append("}");
        return res.toString();
    }

    @Nullable
    public static File getSelectedDirectory(MySuperActivity activity, String dirName, boolean isPublic, boolean isInternal, boolean isCache) {
        File res;

        if (isInternal)
            res = isCache ? activity.getCacheDir() : activity.getFilesDir();
        else
            res = isPublic ? Environment.getExternalStoragePublicDirectory(dirName) : activity.getExternalFilesDir(dirName);

        if (res != null && !res.exists())
            activity.showToast(res + " does not exist, trying to create.. " + res.mkdir());
        return res;
    }

    @NonNull
    public static String[] getFieldsValue(Class clazz, String fieldNameLike) {
        List<String> res = new ArrayList<>();
        try {
            for (Field f : clazz.getFields())
                if (f.getName().toUpperCase().contains(fieldNameLike))
                    res.add((String) f.get(null));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return res.toArray(new String[res.size()]);
    }

    public static String debugIntent(Intent data) {
        if (data == null) return "Intent is null";
        String msg = "Intent has action: " + data.getAction() + "\n";
        msg += "and extras:\n";
        if (data.getExtras() != null) {
            msg += debugBundle(data.getExtras());
        } else msg += "null";
        return msg;
    }

    public static String debugBundle(Bundle bundle) {
        if (bundle == null) return "Bundle is null";
        String msg = "Bundle is:\n";
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if (value instanceof Bundle) msg += debugBundle((Bundle) value);
            else if (value instanceof Collection) msg += printList((Collection) value) + "\n";
            else if (value != null) {
                msg += String.format("\t%s\t%s\t(%s)\n", key, value.toString(), value.getClass().getName());
            }
        }
        return msg;
    }

    /**
     * Returns true only if all elements of @param b are contained in @param a
     */
    public static boolean containsAny(Collection a, Collection b) {
        for (Object o : b) if (!a.contains(o)) return false;
        return true;
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        if (ChatHeadService.class.getName().equals(serviceClass.getName()))
            return Constants.SERVICE_CHAT_HEAD_RUNNING;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("ObsoleteSdkInt")
    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    @SuppressLint("ObsoleteSdkInt")
    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    @SuppressLint("ObsoleteSdkInt")
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    @SuppressLint("ObsoleteSdkInt")
    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    @SuppressLint("ObsoleteSdkInt")
    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean hasNougat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }
}

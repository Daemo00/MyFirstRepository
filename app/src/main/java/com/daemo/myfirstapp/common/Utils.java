package com.daemo.myfirstapp.common;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.daemo.myfirstapp.MySuperActivity;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Utils {

    public static String getTag(Object inst){
        String className = inst.getClass().getSimpleName();
        return className.isEmpty() ? "Anonymous Class" : className;
    }

    public static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1).toLowerCase();
    }

    public static String printList(Collection list) {
        StringBuilder res = new StringBuilder("{");
        int index = 0;
        for (Object o : list)
            res.append("item ").append(index++).append(": ").append(o.toString())
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
}
package com.daemo.myfirstapp;

import android.app.Application;
import android.os.StrictMode;

import java.util.ArrayList;
import java.util.HashMap;


public class MySuperApplication extends Application {

    private static final boolean DEVELOPER_MODE = true;
    // actual store of statistics
    private final ArrayList<HashMap<String, Object>> processList = new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> servicesList = new ArrayList<>();

    @Override
    public void onCreate() {
        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
        super.onCreate();
    }

    public ArrayList<HashMap<String, Object>> getProcessesList() {
        return processList;
    }

    public ArrayList<HashMap<String, Object>> getServicesList() {
        return servicesList;
    }
}

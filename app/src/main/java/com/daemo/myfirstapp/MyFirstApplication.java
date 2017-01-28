package com.daemo.myfirstapp;

import android.app.Application;

import java.util.ArrayList;
import java.util.HashMap;


public class MyFirstApplication extends Application {

    // actual store of statistics
    private final ArrayList<HashMap<String, Object>> processList = new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> servicesList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public ArrayList<HashMap<String, Object>> getProcessesList() {
        return processList;
    }

    public ArrayList<HashMap<String, Object>> getServicesList() {
        return servicesList;
    }
}

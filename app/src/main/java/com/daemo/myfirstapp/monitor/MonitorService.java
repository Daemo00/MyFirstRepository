package com.daemo.myfirstapp.monitor;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.daemo.myfirstapp.MySuperApplication;
import com.daemo.myfirstapp.common.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MonitorService extends Service {
    private boolean initialized = false;
    private final IBinder mBinder = new LocalBinder();
    private ServiceCallback callback = null;
    private Timer timer = null;
    private final Handler mHandler = new Handler();
    private ArrayList<HashMap<String, Object>> servicesList;
    private ArrayList<HashMap<String, Object>> processesList;

    public static int SERVICE_PERIOD = 5000;
    private final ProcessList pl = new ProcessList(this);

    public interface ServiceCallback {
        void sendResults(int resultCode, Bundle b);
    }

    public class LocalBinder extends Binder {
        MonitorService getService() {
            // Return this instance of the service so clients can call public methods
            return MonitorService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Utils.getTag(this), "Service created");
        initialized = true;
        servicesList = ((MySuperApplication) getApplication()).getServicesList();
        processesList = ((MySuperApplication) getApplication()).getProcessesList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(Utils.getTag(this), "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(Utils.getTag(this), "Service bound");
        if (initialized)
            return mBinder;
        return null;
    }

    public void setCallback(ServiceCallback callback) {
        this.callback = callback;
    }

    public void start() {
        Log.d(Utils.getTag(this), "Service started");
//        if (timer == null) {
//            timer = new Timer();
//            timer.schedule(new MonitoringTimerTask(), 500, SERVICE_PERIOD);
//        }
        fillLists();
        if (callback != null) {
            final Bundle b = new Bundle();
            mHandler.post(new Runnable() {
                public void run() {
                    callback.sendResults(1, b);
                }
            });
        }
    }

    public void stop() {
        timer.cancel();
        timer.purge();
        timer = null;
    }

    private class MonitoringTimerTask extends TimerTask {
        @Override
        public void run() {
            fillLists();

            if (callback != null) {
                final Bundle b = new Bundle();
                mHandler.post(new Runnable() {
                    public void run() {
                        callback.sendResults(1, b);
                    }
                });
            }
        }
    }

    void fillServicesList(){
        pl.fillServicesList(servicesList);
        if (callback != null) {
            final Bundle b = new Bundle();
            mHandler.post(new Runnable() {
                public void run() {
                    callback.sendResults(1, b);
                }
            });
        }
    }

    void fillProcessesList(){
        pl.fillProcessesList(processesList);
        if (callback != null) {
            final Bundle b = new Bundle();
            mHandler.post(new Runnable() {
                public void run() {
                    callback.sendResults(1, b);
                }
            });
        }
    }

    void fillLists() {
        fillServicesList();
        fillProcessesList();
    }
}
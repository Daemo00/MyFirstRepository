package com.daemo.myfirstapp.monitor;

import android.app.ActivityManager;
import android.content.Context;
import android.content.ContextWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

class ProcessList {
    static final String COLUMN_SERVICE_NAME = "service";
    static final String COLUMN_SERVICE_PID = "pid";

    static final String COLUMN_PROCESS_NAME = "process";
    static final String COLUMN_PROCESS_PID = "pid";

    private ContextWrapper context;

    ProcessList(ContextWrapper context) {
        this.context = context;
    }

    void fillLists(ArrayList<HashMap<String, Object>> servicesList, ArrayList<HashMap<String, Object>> processesList) {
        processesList.clear();
        servicesList.clear();

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = am.getRunningServices(100);
        List<ActivityManager.RecentTaskInfo> runningAppProcesses = am.getRecentTasks(100, ActivityManager.RECENT_WITH_EXCLUDED);//getRecentTasksForUser(1,2,3);

        HashMap<String, Object> hm;
        for (ActivityManager.RecentTaskInfo runningAppTask : runningAppProcesses) {
            hm = new HashMap<>();
            hm.put(COLUMN_PROCESS_NAME, runningAppTask.numActivities + " activities");
            hm.put(COLUMN_PROCESS_PID, runningAppTask.id);
            processesList.add(hm);
        }

        for (ActivityManager.RunningServiceInfo runningService : runningServices) {
            hm = new HashMap<>();
            hm.put(COLUMN_SERVICE_NAME, runningService.service.flattenToShortString());
            hm.put(COLUMN_SERVICE_PID, runningService.pid);
            servicesList.add(hm);
        }


        Comparator<HashMap<String, Object>> comparator = new Comparator<HashMap<String, Object>>() {
            public int compare(HashMap<String, Object> object1, HashMap<String, Object> object2) {
                return object1.get(COLUMN_SERVICE_NAME).toString().compareToIgnoreCase(object2.get(COLUMN_SERVICE_NAME).toString());
                //return (Integer) object1.get(COLUMN_SERVICE_PID) - (Integer) object2.get(COLUMN_SERVICE_PID);
            }
        };

        //Collections.sort(processesList, comparator);
        Collections.sort(servicesList, comparator);
    }
}
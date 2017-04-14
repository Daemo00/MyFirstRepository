package com.daemo.myfirstapp;

import android.app.Application;
import android.os.StrictMode;

import com.daemo.myfirstapp.common.Constants;
import com.daemo.myfirstapp.graphics.displayingbitmaps.util.AsyncTask;
import com.daemo.myfirstapp.graphics.displayingbitmaps.util.ImageCache;

import java.util.ArrayList;
import java.util.HashMap;


public class MySuperApplication extends Application {

    private static final boolean DEVELOPER_MODE = false;
    // actual store of statistics
    private final ArrayList<HashMap<String, Object>> processList = new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> servicesList = new ArrayList<>();

    private ImageCache imageCache;

    public ImageCache getImageCache() {
        return imageCache;
    }

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
        imageCache =
                new AsyncTask<Object, Object, ImageCache>() {
                    @Override
                    protected ImageCache doInBackground(Object[] params) {
                        ImageCache.ImageCacheParams imageCacheParams = new ImageCache.ImageCacheParams(getApplicationContext(), Constants.IMAGE_CACHE_DIR);
                        imageCacheParams.setMemCacheSizePercent(Constants.CACHE_SIZE);
                        return ImageCache.getInstance(imageCacheParams);
                    }
                }.doInBackground(null);
    }

    public ArrayList<HashMap<String, Object>> getProcessesList() {
        return processList;
    }

    public ArrayList<HashMap<String, Object>> getServicesList() {
        return servicesList;
    }
}

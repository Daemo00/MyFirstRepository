package com.daemo.myfirstapp.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by admin on 18/07/2016.
 */
public class MockLocationProvider {
    String providerName;
    Boolean isEnabled = false;
    LocationManager lm;

    public MockLocationProvider(String name, LocationManager lm) {
        this.providerName = name;
        this.lm = lm;
    }

    public void enable() {
        if (isEnabled) return;
        lm.addTestProvider(providerName, false, false, false, false, false, true, true, 0, 5);
        lm.setTestProviderEnabled(providerName, true);
        isEnabled = true;
    }

    public void pushLocation(Float[] coords) throws Exception {
        if (!isEnabled) throw new Exception(providerName + " is not enabled!");
        lm.setTestProviderLocation(providerName, getMockLocation(coords[0], coords[1]));
        Log.d("MockLocationProvider", "Location (" + coords[0] + ", " + coords[1] + ") set with mock " + providerName + " provider");
    }

    @NonNull
    private Location getMockLocation(double lat, double lon) {
        Location mockLocation = new Location(providerName);
        mockLocation.setLatitude(lat);
        mockLocation.setLongitude(lon);
        mockLocation.setAccuracy(1);
        mockLocation.setAltitude(0);
        mockLocation.setTime(System.currentTimeMillis());
        mockLocation.setElapsedRealtimeNanos(System.currentTimeMillis());
        return mockLocation;
    }

    public void disable() {
        if (!isEnabled) return;
        lm.setTestProviderEnabled(providerName, false);
        lm.removeTestProvider(providerName);
        isEnabled = false;
    }
}
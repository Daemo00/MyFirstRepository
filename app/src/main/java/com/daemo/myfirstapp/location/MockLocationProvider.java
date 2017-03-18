package com.daemo.myfirstapp.location;

import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.util.Log;

class MockLocationProvider {
    private String providerName;
    Boolean isEnabled = false;
    private LocationManager lm;

    MockLocationProvider(String name, LocationManager lm) {
        this.providerName = name;
        this.lm = lm;
    }

    void enable() {
        if (isEnabled) return;
        lm.addTestProvider(providerName, false, false, false, false, false, true, true, 0, 5);
        lm.setTestProviderEnabled(providerName, true);
        isEnabled = true;
    }

    void pushLocation(Float[] coords) throws Exception {
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

    void disable() {
        if (!isEnabled) return;
        lm.setTestProviderEnabled(providerName, false);
        lm.removeTestProvider(providerName);
        isEnabled = false;
    }
}
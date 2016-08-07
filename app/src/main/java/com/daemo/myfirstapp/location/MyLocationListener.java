package com.daemo.myfirstapp.location;

import android.location.*;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.daemo.myfirstapp.Utils;

/**
 * Created by admin on 17/07/2016.
 */
public class MyLocationListener implements LocationListener {

    private final ProviderDetailsFragment fragment;
    private final String providerName;

    public MyLocationListener(ProviderDetailsFragment fragment, String providerName) {
        this.fragment = fragment;
        this.providerName = providerName;
    }

    static String locationDetails(String providerName, Location location) {
        if(location == null) return "Location is null for provider " + providerName;
        return "Provider" + ": " + location.getProvider() + '\n' +
                "Describe contents" + ": " + location.describeContents() + '\n' +
                "Accuracy" + ": " + location.getAccuracy() + '\n' +
                "Altitude" + ": " + location.getAltitude() + '\n' +
                "Bearing" + ": " + location.getBearing() + '\n' +
                "Elapsed real time nanos" + ": " + location.getElapsedRealtimeNanos() + '\n' +
                "Latitude" + ": " + location.getLatitude() + '\n' +
                "Longitude" + ": " + location.getLongitude() + '\n' +
                "Speed" + ": " + location.getSpeed() + '\n' +
                "Time" + ": " + location.getTime() + '\n' +
                "Is from mock provider" + ": " + location.isFromMockProvider() + '\n' +
                "Extras" + ": " + location.getExtras() + '\n';
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
        fragment.addRow(
                "Location has changed: (" + location.getLatitude() + ", " + location.getLongitude() + ")",
                locationDetails(providerName, location));
        }
        else
            fragment.addRow("Location is null");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        String res =  Utils.capitalize(s) + " provider is now ";
        String status = "";
        switch (i){
            case LocationProvider.AVAILABLE: status = "available";break;
            case LocationProvider.OUT_OF_SERVICE: status = "out of service"; break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE: status = "temporarily unavailable"; break;
            default: break;
        }
        res += status + "\n";

        res += "Bundle is: " + bundle.toString();
        fragment.addRow(
                "Status has changed to " + status,
                res);
    }

    @Override
    public void onProviderEnabled(String s) {
        fragment.addRow("Provider " + s + " has been enabled");
    }

    @Override
    public void onProviderDisabled(String s) {
        fragment.addRow("Provider " + s + " has been disabled");
    }
}
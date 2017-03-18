package com.daemo.myfirstapp.location.aware;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Constants;
import com.daemo.myfirstapp.common.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class FetchAddressIntentService extends IntentService {

    private ResultReceiver mReceiver;

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(Constants.Location.LOCATION_DATA_EXTRA);
        mReceiver = intent.getParcelableExtra(Constants.Location.RECEIVER);

        if (location == null) {
            deliverResultToReceiver(Activity.RESULT_CANCELED, "Location not provided");
            return;
        }

        List<Address> addresses = null;
        try {
            addresses = new Geocoder(this, Locale.getDefault())
                    .getFromLocation(
                            location.getLatitude(),
                            location.getLongitude(),
                            // In this sample, get just a single address.
                            1);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_not_available);
            Log.e(Utils.getTag(this), errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(Utils.getTag(this), errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(Utils.getTag(this), errorMessage);
            }
            deliverResultToReceiver(Activity.RESULT_CANCELED, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();

            // Fetch the address lines using getAddressLine, join them, and send them to the thread.
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++)
                addressFragments.add(address.getAddressLine(i));

            Log.i(Utils.getTag(this), getString(R.string.address_found));
            deliverResultToReceiver(Activity.RESULT_OK,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments));
        }
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.Location.KEY_RESULT_DATA, message);
        mReceiver.send(resultCode, bundle);
    }
}
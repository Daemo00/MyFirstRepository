package com.daemo.myfirstapp.location.aware;


import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.daemo.myfirstapp.activities.MySuperActivity;
import com.daemo.myfirstapp.activities.MySuperFragment;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Constants;
import com.daemo.myfirstapp.common.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.daemo.myfirstapp.common.Constants.Location.KEY_REQUEST_GEOFENCE_INTENT;
import static com.daemo.myfirstapp.common.Constants.Location.REQUEST_CODE_CHECK_SETTINGS;
import static com.daemo.myfirstapp.common.Constants.Location.REQUEST_CONNECTION;
import static com.daemo.myfirstapp.common.Constants.Location.REQUEST_GEOFENCES;
import static com.daemo.myfirstapp.common.Constants.Location.REQUEST_LAST_LOCATION;
import static com.daemo.myfirstapp.common.Constants.Location.REQUEST_LOCATION_UPDATES;

public class AwarenessFragment extends MySuperFragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, LocationListener, CompoundButton.OnCheckedChangeListener {

    private static final String KEY_BOOL_IS_UPDATING = "isUpdating";
    private static final String KEY_STRING_LAST_TIMESTAMP = "lastUpdateString";
    private static final String KEY_LOCATION_LAST = "location";
    private static final String KEY_BOOL_IS_GEOFENCE_ADDED = "isGeofenceEnabled";
    protected Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView mLastUpdateText;
    private boolean isUpdating;
    private final ResultCallback<LocationSettingsResult> mLocationSettingsResult = new ResultCallback<LocationSettingsResult>() {
        @Override
        public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
            final Status status = locationSettingsResult.getStatus();
            final LocationSettingsStates locationSettingsStates = locationSettingsResult.getLocationSettingsStates();
            debugLocationSettingsStates(locationSettingsStates);
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    // All location settings are satisfied.
                    // The client can initialize location requests here.
                    getMySuperActivity().showToast("Result is success");
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    getMySuperActivity().showToast("Result is resolution required");
                    // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                        status.startResolutionForResult(getActivity(), REQUEST_CODE_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        // Log the error.
                        e.printStackTrace();
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    getMySuperActivity().showToast("Result is settings change unavailable");
                    // Location settings are not satisfied.
                    // However, we have no way to fix the settings so we won't show the dialog.
                    break;
                default:
                    getMySuperActivity().showToast("Result is " + status.getStatusCode());
            }
        }
    };
    private String mLastUpdateString;
    private String mAddressOutput;
    private boolean mAddressRequested;
    private TextView mAddressText;
    private PendingIntent mGeofencePendingIntent;
    private ToggleButton mToggleGeofencesButton;
    private ToggleButton mToggleConnectGoogleApiClient;
    private ToggleButton mToggleLocationUpdates;

    public boolean isGeofenceEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getResources().getString(R.string.settings_location_enable_geofences), false);
    }

    public void enableGeofenceValue(boolean mGeofenceAdded) {
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                .putBoolean(getResources().getString(R.string.settings_location_enable_geofences), mGeofenceAdded)
                .apply();
    }

    private final ResultCallback<Status> mGeoFenceResultCallback = new ResultCallback<Status>() {
        @Override
        public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
                // Update state and save in shared preferences.
                // Update the UI.
                // Adding geofences enables the Remove Geofences button, and removing geofences enables the Add Geofences button.
                updateUIWidgets(false);

                getMySuperActivity().showToast(getString(isGeofenceEnabled() ? R.string.geofences_added :
                        R.string.geofences_removed));
            } else {
                Log.e(Utils.getTag(this), Constants.Location.getErrorString(getContext(), status.getStatusCode()));
            }
        }
    };
    private Bundle mConnectionHint;
    private AddressResultReceiver mResultReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateValuesFromBundle(savedInstanceState);

        mResultReceiver = new AddressResultReceiver(new Handler());

        buildGoogleApiClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_awareness, viewGroup, true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLatitudeText = (TextView) view.findViewById(R.id.tv_latitude);
        mLongitudeText = (TextView) view.findViewById(R.id.tv_longitude);
        mLastUpdateText = (TextView) view.findViewById(R.id.tv_last_update);
        mAddressText = (TextView) view.findViewById(R.id.tv_address);

        // Get the UI buttons.
        mToggleGeofencesButton = (ToggleButton) view.findViewById(R.id.toggle_geofences_button);
        mToggleConnectGoogleApiClient = (ToggleButton) view.findViewById(R.id.toggle_connect_googleapiclient);
        mToggleLocationUpdates = (ToggleButton) view.findViewById(R.id.toggle_location_updates);
        mToggleGeofencesButton.setOnCheckedChangeListener(this);
        mToggleConnectGoogleApiClient.setOnCheckedChangeListener(this);
        mToggleLocationUpdates.setOnCheckedChangeListener(this);

        view.findViewById(R.id.clear_btn).setOnClickListener(this);
        view.findViewById(R.id.btn_get_curr_location_sett).setOnClickListener(this);
        view.findViewById(R.id.btn_get_last_location).setOnClickListener(this);
        view.findViewById(R.id.address_btn).setOnClickListener(this);
        updateUIWidgets(false);
    }

    public List<Geofence> getGeofenceList() {
        List<Geofence> res = new ArrayList<>();
        int concatenatedTransitions = 0;
        for (Integer geofenceTransition : Constants.Location.GEOFENCE_TRANSITIONS)
            concatenatedTransitions |= geofenceTransition;

        for (Map.Entry<String, LatLng> entry : Constants.Location.LANDMARKS.entrySet()) {

            res.add(new Geofence.Builder()
                    // Set the request ID of the geofence.
                    // This is a string to identify this geofence.
                    .setRequestId(entry.getKey())

                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.Location.GEOFENCE_RADIUS_IN_METERS
                    )

                    // Set the expiration duration of the geofence.
                    // This geofence gets automatically removed after this period of time.
                    .setExpirationDuration(Constants.Location.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                    // Set the transition types of interest.
                    // Alerts are only generated for these transition.
                    // We track entry and exit transitions in this sample.
                    .setTransitionTypes(concatenatedTransitions)

                    // Create the geofence.
                    .build());
        }
        return res;
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(KEY_BOOL_IS_UPDATING))
                isUpdating = savedInstanceState.getBoolean(KEY_BOOL_IS_UPDATING);

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the correct latitude and longitude.
            // Since KEY_LOCATION_LAST was found in the Bundle, we can be sure that mCurrentLocation is not null.
            if (savedInstanceState.keySet().contains(KEY_LOCATION_LAST))
                mLastLocation = savedInstanceState.getParcelable(KEY_LOCATION_LAST);

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(KEY_STRING_LAST_TIMESTAMP))
                mLastUpdateString = savedInstanceState.getString(KEY_STRING_LAST_TIMESTAMP);

            if (savedInstanceState.keySet().contains(KEY_BOOL_IS_GEOFENCE_ADDED))
                enableGeofenceValue(savedInstanceState.getBoolean(KEY_BOOL_IS_GEOFENCE_ADDED));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isUpdating)
            startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isUpdating)
            stopLocationUpdates();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_BOOL_IS_UPDATING, isUpdating);
        savedInstanceState.putParcelable(KEY_LOCATION_LAST, mLastLocation);
        savedInstanceState.putString(KEY_STRING_LAST_TIMESTAMP, mLastUpdateString);
        savedInstanceState.putBoolean(KEY_BOOL_IS_GEOFENCE_ADDED, isGeofenceEnabled());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        super.onStop();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        else getMySuperActivity().showToast(getString(R.string.googleapi_not_connected));
    }

    private void getCurrentLocationSettings() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(createLocationRequest());

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(mLocationSettingsResult);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        getMySuperActivity().showToast("GoogleApiClient Connected");
        Log.d(Utils.getTag(this), "onConnected(" + Utils.debugBundle(connectionHint) + ")");

        if (mLastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                getMySuperActivity().showToast(R.string.no_geocoder_available);
                return;
            }

            if (mAddressRequested) startLocationIntentService();
        }
    }

    private void getLastLocation(int requestCode) {
        // Gets the best and most recent location currently available, which may be null in rare cases when a location is not available.
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getMySuperActivity().checkPermissionsRunTime(requestCode, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
            return;
        }

        if (mGoogleApiClient.isConnected()) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            updateUIWidgets(true);
        } else {
            getMySuperActivity().showOkDialog("Warning", "Connect GoogleApiClient before requesting location", null);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
//        CAUSE_SERVICE_DISCONNECTED = 1;
//        CAUSE_NETWORK_LOST = 2;
        String msg = "Connection suspended: ";
        switch (i) {
            case 1:
                msg += "Service disconnected";
                break;
            case 2:
                msg += "Network lost";
                break;
            default:
                msg += i;
        }
        getMySuperActivity().showToast(msg);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        getMySuperActivity().showToast("Connection failed: " + connectionResult.getErrorMessage());
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getMySuperActivity().checkPermissionsRunTime(REQUEST_LOCATION_UPDATES, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
            return;
        }
        if (mGoogleApiClient.isConnected()) {
            if (!isUpdating) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationRequest(), this);
                isUpdating = true;
            }
        } else {
            getMySuperActivity().showOkDialog("Warning", "Connect GoogleApiClient before starting location updates", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mToggleLocationUpdates.setChecked(false);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Do nothing if something has been denied
        for (int grantResult : grantResults)
            if (grantResult != PackageManager.PERMISSION_GRANTED)
                return;

        switch (requestCode) {
            case REQUEST_CONNECTION:
                onConnected(mConnectionHint);
                mConnectionHint = null;
                break;
            case REQUEST_LAST_LOCATION:
                getLastLocation(REQUEST_LAST_LOCATION);
                break;
            case REQUEST_LOCATION_UPDATES:
                startLocationUpdates();
                break;
            case REQUEST_GEOFENCES:
                enableGeoFences();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_CHECK_SETTINGS:
                LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
                debugLocationSettingsStates(states);
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getMySuperActivity().showToast("Ok");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        getMySuperActivity().showToast("Canceled");
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    public void debugLocationSettingsStates(LocationSettingsStates states) {
        String statesDebug = "Inspecting returned LocationSettingsStates:\n";
        Object[] oo = new Object[]{};
        for (Method method : states.getClass().getDeclaredMethods()) {
            Class<?> returnType = method.getReturnType();
            if (returnType.equals(Boolean.TYPE) && method.getParameterTypes().length == 0) {
                try {
                    statesDebug += method.getName() + "() -> " + method.invoke(states, oo) + "\n";
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        Log.d(Utils.getTag(this), statesDebug);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clear_btn:
                mLatitudeText.setText(null);
                mLongitudeText.setText(null);
                mAddressText.setText(null);
                mLastUpdateText.setText(null);
                break;
            case R.id.btn_get_curr_location_sett:
                getCurrentLocationSettings();
                break;
            case R.id.address_btn:
                fetchAddressButtonHandler();
                break;
            case R.id.btn_get_last_location:
                getLastLocation(REQUEST_LAST_LOCATION);
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) mLastLocation = location;
        updateUIWidgets(true);
    }

    protected void startLocationIntentService() {
        Intent intent = new Intent(getContext(), FetchAddressIntentService.class);
        intent.putExtra(Constants.Location.RECEIVER, mResultReceiver)
                .putExtra(Constants.Location.LOCATION_DATA_EXTRA, mLastLocation);
        getContext().startService(intent);
    }

    public void fetchAddressButtonHandler() {
        // Only start the service to fetch the address if GoogleApiClient is connected.
        if (mGoogleApiClient.isConnected()) {
            startLocationIntentService();
            return;
        }
        // If GoogleApiClient isn't connected, process the user's request by setting mAddressRequested to true.
        // Later, when GoogleApiClient connects, launch the service to fetch the address.
        // As far as the user is concerned, pressing the Fetch Address button immediately kicks off the process of getting the address.
        mAddressRequested = true;
        updateUIWidgets(false);
    }

    void updateUIWidgets(boolean newLocationProvided) {
        if (!newLocationProvided) {
            mToggleConnectGoogleApiClient.setChecked(mGoogleApiClient.isConnected());
            mToggleGeofencesButton.setChecked(isGeofenceEnabled());
            return;
        }

        if (mLastLocation != null) {
            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            mLastUpdateString = SimpleDateFormat.getDateTimeInstance().format(new Date());
            mLastUpdateText.setText(mLastUpdateString);
            mAddressText.setText(mAddressOutput);
        } else {
            Log.d(Utils.getTag(this), "Location is null");
            mAddressText.setText(R.string.location_is_null);
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(getGeofenceList());
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) return mGeofencePendingIntent;
        else {
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addGeofences() and removeGeofences().
            return mGeofencePendingIntent = PendingIntent.getService(getContext(),
                    KEY_REQUEST_GEOFENCE_INTENT,
                    new Intent(getContext(), GeofenceTransitionsIntentService.class),
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    private void enableGeoFences() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ((MySuperActivity) getActivity()).checkPermissionsRunTime(REQUEST_GEOFENCES, Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        if (mGoogleApiClient.isConnected())
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(mGeoFenceResultCallback);
        else {
            getMySuperActivity().showOkDialog("Warning", "Connect GoogleApiClient before enabling geofences", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mToggleGeofencesButton.setChecked(false);
                }
            });
        }
    }

    private void disableGeoFences() {
        if (mGoogleApiClient.isConnected())
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(mGeoFenceResultCallback); // Result processed in onResult().
        else getMySuperActivity().showToast(getString(R.string.googleapi_not_connected));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.toggle_connect_googleapiclient:
                if (isChecked) mGoogleApiClient.connect();
                else mGoogleApiClient.disconnect();
                break;
            case R.id.toggle_geofences_button:
                enableGeofenceValue(isChecked);
                if (isChecked) enableGeoFences();
                else disableGeoFences();
                break;
            case R.id.toggle_location_updates:
                if (isChecked) startLocationUpdates();
                else stopLocationUpdates();
                break;
        }
    }

    private class AddressResultReceiver extends ResultReceiver {

        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultData == null || !resultData.containsKey(Constants.Location.KEY_RESULT_DATA))
                return;
            // Display the address string or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.Location.KEY_RESULT_DATA);
            switch (resultCode) {
                case Activity.RESULT_OK:
                    getMySuperActivity().showToast(getString(R.string.address_found));
                    break;
                case Activity.RESULT_CANCELED:
                    getMySuperActivity().showToast(getString(R.string.no_address_found));
                    break;
            }
            updateUIWidgets(true);
        }
    }
}
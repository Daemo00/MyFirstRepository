package com.daemo.myfirstapp.location.aware;


import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.MySuperFragment;
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
import static com.daemo.myfirstapp.common.Constants.Location.REQUEST_CHECK_SETTINGS;
import static com.daemo.myfirstapp.common.Constants.Location.REQUEST_CONNECTION;
import static com.daemo.myfirstapp.common.Constants.Location.REQUEST_GEOFENCES;
import static com.daemo.myfirstapp.common.Constants.Location.REQUEST_LAST_LOCATION;
import static com.daemo.myfirstapp.common.Constants.Location.REQUEST_LOCATION_UPDATES;

/**
 * A simple {@link Fragment} subclass.
 */
public class AwarenessFragment extends MySuperFragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, LocationListener {


    private static final String KEY_BOOL_IS_UPDATING = "isUpdating";
    private static final String KEY_STRING_LAST_TIMESTAMP = "lastUpdateString";
    private static final String KEY_LOCATION_LAST = "location";
    private GoogleApiClient mGoogleApiClient;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView mLastUpdate;
    private boolean isUpdating;
    private String mLastUpdateString;
    private String mAddressOutput;
    private boolean mAddressRequested;
    private TextView mAddressText;
    private PendingIntent mGeofencePendingIntent;
    private final ResultCallback<Status> mGeoFenceResultCallback = new ResultCallback<Status>() {
        @Override
        public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
                // Update state and save in shared preferences.
                mGeofencesAdded = !mGeofencesAdded;
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean(getResources().getString(R.string.settings_location_enable_geofences), mGeofencesAdded);
                editor.apply();

                // Update the UI.
                // Adding geofences enables the Remove Geofences button, and removing geofences enables the Add Geofences button.
                setButtonsEnabledState();

                getMySuperActivity().showToast(getString(mGeofencesAdded ? R.string.geofences_added :
                        R.string.geofences_removed));
            } else {
                Log.e(Utils.getTag(this), Constants.Location.getErrorString(getContext(), status.getStatusCode()));
            }
        }
    };
    private boolean mGeofencesAdded;
    private Button mAddGeofencesButton;
    private Button mRemoveGeofencesButton;
    private SharedPreferences mSharedPreferences;
    private Bundle mConnectionHint;
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
                    getMySuperActivity().showToast("Location settings result is success");
                    // getLastLocation();
                    startLocationUpdates();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    getMySuperActivity().showToast("Result is resolution required");
                    // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                        status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
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
            }
        }

    };

    private void setButtonsEnabledState() {
        mAddGeofencesButton.setEnabled(!mGeofencesAdded);
        mRemoveGeofencesButton.setEnabled(mGeofencesAdded);
    }

    public AwarenessFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create an instance of GoogleAPIClient.

        updateValuesFromBundle(savedInstanceState);
        mResultReceiver = new AddressResultReceiver(new Handler());

        mGeofencePendingIntent = null;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
//                getContext().getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);

        mGeofencesAdded = mSharedPreferences.getBoolean(getResources().getString(R.string.settings_location_enable_geofences), false);
//                mSharedPreferences.getBoolean(Constants.GEOFENCES_ADDED_KEY, false);

        buildGoogleApiClient();
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        container = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_awareness, container, true);

        mLatitudeText = (TextView) root.findViewById(R.id.tv_latitude);
        mLongitudeText = (TextView) root.findViewById(R.id.tv_longitude);
        mLastUpdate = (TextView) root.findViewById(R.id.tv_last_update);
        mAddressText = (TextView) root.findViewById(R.id.tv_address);

        // Get the UI buttons.
        mAddGeofencesButton = (Button) root.findViewById(R.id.enable_geofences_button);
        mRemoveGeofencesButton = (Button) root.findViewById(R.id.disable_geofences_button);
        mAddGeofencesButton.setOnClickListener(this);
        mRemoveGeofencesButton.setOnClickListener(this);

        root.findViewById(R.id.clear_btn).setOnClickListener(this);
        root.findViewById(R.id.btn_get_curr_location_sett).setOnClickListener(this);
        root.findViewById(R.id.address_btn).setOnClickListener(this);

        setButtonsEnabledState();
        return root;
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isUpdating)
            stopLocationUpdates();
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_BOOL_IS_UPDATING, isUpdating);
        savedInstanceState.putParcelable(KEY_LOCATION_LAST, mLastLocation);
        savedInstanceState.putString(KEY_STRING_LAST_TIMESTAMP, mLastUpdateString);
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isUpdating)
            startLocationUpdates();
    }

    private void getCurrentLocationSettings() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(createLocationRequest());

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(mLocationSettingsResult);
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        getMySuperActivity().showToast("Connected");
        mConnectionHint = connectionHint;
        mLastLocation = getLastLocation(REQUEST_CONNECTION);

        if (mLastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                getMySuperActivity().showToast(R.string.no_geocoder_available);
                return;
            }

            if (mAddressRequested) {
                startLocationIntentService();
            }
        }
    }

    private Location getLastLocation(int requestCode) {
        // Gets the best and most recent location currently available, which may be null in rare cases when a location is not available.
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getMySuperActivity().checkPermissionsRunTime(requestCode, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
            return null;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        }
        return mLastLocation;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
        if (mGoogleApiClient.isConnected() && !isUpdating) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationRequest(), this);
            isUpdating = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(Utils.getTag(this), String.valueOf(requestCode));

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
            case REQUEST_CHECK_SETTINGS:
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
                mLastUpdate.setText(null);
                break;
            case R.id.btn_get_curr_location_sett:
                getCurrentLocationSettings();
                break;
            case R.id.address_btn:
                getLastLocation(REQUEST_LAST_LOCATION);
                fetchAddressButtonHandler();
                break;
            case R.id.enable_geofences_button:
                enableGeoFences();
                break;
            case R.id.disable_geofences_button:
                disableGeoFences();
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            mLastLocation = location;
        }
        updateUIWidgets();
    }

    protected Location mLastLocation;
    private AddressResultReceiver mResultReceiver;

    protected void startLocationIntentService() {
        Intent intent = new Intent(getContext(), FetchAddressIntentService.class);
        intent.putExtra(Constants.Location.RECEIVER, mResultReceiver)
                .putExtra(Constants.Location.LOCATION_DATA_EXTRA, mLastLocation);
        getContext().startService(intent);
    }

    class AddressResultReceiver extends ResultReceiver {

        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

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
            updateUIWidgets();
        }
    }

    public void fetchAddressButtonHandler() {
        // Only start the service to fetch the address if GoogleApiClient is connected.
        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            startLocationIntentService();
            return;
        }
        // If GoogleApiClient isn't connected, process the user's request by setting mAddressRequested to true.
        // Later, when GoogleApiClient connects, launch the service to fetch the address.
        // As far as the user is concerned, pressing the Fetch Address button immediately kicks off the process of getting the address.
        mAddressRequested = true;
        updateUIWidgets();
    }

    void updateUIWidgets() {
        mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
        mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        mLastUpdate.setText(mLastUpdateString == null ? SimpleDateFormat.getDateTimeInstance().format(new Date()) : mLastUpdateString);
        mAddressText.setText(mAddressOutput);
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
        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
        ).setResultCallback(mGeoFenceResultCallback);
    }

    private void disableGeoFences() {
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                // This is the same pending intent that was used in addGeofences().
                getGeofencePendingIntent()
        ).setResultCallback(mGeoFenceResultCallback); // Result processed in onResult().
    }
}
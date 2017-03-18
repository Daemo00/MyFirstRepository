package com.daemo.myfirstapp.location;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.ListViewCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.daemo.myfirstapp.BuildConfig;
import com.daemo.myfirstapp.MySuperFragment;
import com.daemo.myfirstapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ProviderDetailsFragment extends MySuperFragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    static final String PROVIDER = "PROVIDER";

    private String mLocationProvider;
    private MockLocationProvider mock;
    private Timer t;
    private LocationManager locationManager;
    private ArrayAdapter<TitleDescription> adapter;
    private MyLocationListener locationListener;

    public ProviderDetailsFragment() {
    }

    public static ProviderDetailsFragment newInstance(String locationProvider) {
        ProviderDetailsFragment fragment = new ProviderDetailsFragment();
        Bundle args = new Bundle();
        args.putString(PROVIDER, locationProvider);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) mLocationProvider = getArguments().getString(PROVIDER);
        setTitle(mLocationProvider);
        mock = new MockLocationProvider(mLocationProvider, (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE));
        locationListener = new MyLocationListener(this, mLocationProvider);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_provider_details, container, false);

        root.findViewById(R.id.btnMock).setOnClickListener(this);
        root.findViewById(R.id.btnDetails).setOnClickListener(this);
        ((ListView) root.findViewById(R.id.listView)).setOnItemClickListener(this);

        adapter = new ArrayAdapter<TitleDescription>(getActivity(), R.layout.my_two_line_listitem, new ArrayList<TitleDescription>()) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null)
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.my_two_line_listitem, parent, false);

                TitleDescription titleDescription = getItem(position);
                if (titleDescription == null) return convertView;

                TextView tvTitle = (TextView) convertView.findViewById(android.R.id.text1);
                if (tvTitle != null) tvTitle.setText(titleDescription.title);
                TextView tvSubTitle = (TextView) convertView.findViewById(android.R.id.text2);
                if (tvSubTitle != null)
                    tvSubTitle.setText(new SimpleDateFormat("[HH:mm:ss.SSS]", Locale.getDefault()).format(new Date()));

                return convertView;
            }
        };

        adapter.setNotifyOnChange(true);
        ((ListViewCompat) root.findViewById(R.id.listView)).setAdapter(adapter);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        getLocationManager().requestLocationUpdates(mLocationProvider, 1000, 1, locationListener);
//        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            getMySuperActivity().checkPermissionsRunTime(Constants.REQUEST_CODE_LOCATION);
//            return;
//        }
//        Location lastKnown = getLocationManager().getLastKnownLocation(mLocationProvider);
//        if (lastKnown != null)
//            addRow("Last known location is: (" + lastKnown.getLatitude() + ", " + lastKnown.getLongitude() + ")",
//                    MyLocationListener.locationDetails(mLocationProvider, lastKnown));
//        else
//            addRow("Last known location is null");
    }

    public void onMockButtonPressed() {
        Button btnMock = (Button) getView().findViewById(R.id.btnMock);
        if (btnMock == null) return;

        if (mock.isEnabled) {
            if (stopMocking()) btnMock.setText(R.string.start);
        } else {
            if (startMocking()) btnMock.setText(R.string.stop);
        }
    }

    private boolean startMocking() {
        List<String> missing_permissions = missingPermissions();
        if (!missing_permissions.isEmpty()) {
            getMySuperActivity().showToast("missing: " + TextUtils.join(", ", missing_permissions));
            return false;
        }
        mock.enable();

        // Set test location programmatically
        TimerTask timerTask = new TimerTask() {
            @Override
            public boolean cancel() {
                mock.disable();
                return super.cancel();
            }

            @Override
            public void run() {
                try {
                    mock.pushLocation(getCoords());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        (t = new Timer()).scheduleAtFixedRate(timerTask, 0, 1000);
        return true;
    }

    private Float[] getCoords() {
        Float[] res = new Float[]{null, null};

        View v = getView();
        if (v == null) return res;

        EditText etLat = (EditText) v.findViewById(R.id.etLat);
        EditText etLon = (EditText) v.findViewById(R.id.etLon);
        if (etLon == null || etLat == null) return res;

        final float lat = Float.parseFloat(etLat.getText().toString());
        final float lon = Float.parseFloat(etLon.getText().toString());
        return new Float[]{lat, lon};
    }

    private boolean stopMocking() {
        List<String> missing_permissions = missingPermissions();
        if (!missing_permissions.isEmpty()) {
            getMySuperActivity().showToast("missing: " + TextUtils.join(", ", missing_permissions));
            return false;
        }
        mock.disable();
        t.cancel();
        getLocationManager().removeUpdates(locationListener);
        return true;
    }

    private String providerDetails(LocationProvider provider) {
        return "Is enabled? " + getLocationManager().isProviderEnabled(mLocationProvider) + '\n' +
                "Accuracy" + ": " + provider.getAccuracy() + '\n' +
                "Power Requirement" + ": " + provider.getPowerRequirement() + '\n' +
                "Has monetary cost" + ": " + provider.hasMonetaryCost() + '\n' +
                "Requires cell" + ": " + provider.requiresCell() + '\n' +
                "Requires network" + ": " + provider.requiresNetwork() + '\n' +
                "Requires satellite" + ": " + provider.requiresSatellite() + '\n' +
                "Supports altitude" + ": " + provider.supportsAltitude() + '\n' +
                "Supports bearing" + ": " + provider.supportsBearing() + '\n' +
                "Supports speed" + ": " + provider.supportsSpeed() + '\n';
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (missingPermissions().isEmpty() && t != null) t.cancel();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnMock:
                onMockButtonPressed();
                break;
            case R.id.btnDetails:
                onDetailsButtonPressed();
                break;
        }
    }

    private void onDetailsButtonPressed() {
        getMySuperActivity().showOkCancelDialog(
                "Details of " + mLocationProvider + " provider",
                missingPermissions().isEmpty() ? providerDetails(getLocationManager().getProvider(mLocationProvider)) : "Need permissions " + missingPermissions() + " to show details",
                null);
    }

    private void showItemDetails(TitleDescription titleDescription) {
        getMySuperActivity().showOkCancelDialog(
                titleDescription.title,
                titleDescription.description,
                null);
    }

    protected void addRow(String titleDescription) {
        addRow(titleDescription, titleDescription);
    }

    protected void addRow(String title, String description) {
        adapter.add(new TitleDescription(title, description));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        showItemDetails((TitleDescription) parent.getItemAtPosition(position));
    }

    public List<String> missingPermissions() {
        List<String> missing_permissions = new ArrayList<>();
        if (!isMockLocationEnabled()) missing_permissions.add("mock setting");

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            missing_permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            missing_permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        return missing_permissions;
    }

    public boolean isMockLocationEnabled() {
        boolean isMockLocation;
        try {
            //if marshmallow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AppOpsManager opsManager = (AppOpsManager) getContext().getSystemService(Context.APP_OPS_SERVICE);
                isMockLocation = (opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(), BuildConfig.APPLICATION_ID) == AppOpsManager.MODE_ALLOWED);
            } else {
                // in marshmallow this will always return true
                isMockLocation = !Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
            }
        } catch (Exception e) {
            return false;
        }
        return isMockLocation;
    }

    public LocationManager getLocationManager() {
        return locationManager == null ? locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE) : locationManager;
    }

    private class TitleDescription {
        String title;
        String description;

        private TitleDescription(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }
}

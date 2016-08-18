package com.daemo.myfirstapp.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ListViewCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.daemo.myfirstapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ProviderDetailsFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    static final String PROVIDER = "PROVIDER";

    private String mLocationProvider;
    private MockLocationProvider mock;
    private Timer t;
    private LocationManager locationManager;
    private ArrayList<TitleDescription> listItems = new ArrayList<>();
    private ArrayAdapter adapter;
    private View root;
    private TimerTask timerTask;
    private MyLocationListener locationListener;
    private LocationActivity locationActivity;

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
        mock = new MockLocationProvider(mLocationProvider, (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE));
        locationListener = new MyLocationListener(this, mLocationProvider);
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        locationActivity = (LocationActivity) getActivity();
        List<String> missing_permissions = missingPermissions();
        if (!missing_permissions.isEmpty()) {
            locationActivity.showToast("missing: " + TextUtils.join(", ", missing_permissions), Toast.LENGTH_SHORT);
            return;
        }
        getLocationManager().requestLocationUpdates(mLocationProvider, 1000, 1, locationListener);
        Location lastKnown = getLocationManager().getLastKnownLocation(mLocationProvider);
        if (lastKnown != null)
            addRow("Last known location is: (" + lastKnown.getLatitude() + ", " + lastKnown.getLongitude() + ")",
                    MyLocationListener.locationDetails(mLocationProvider, lastKnown));
        else
            addRow("Last known location is null");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_provider_details, container, false);

        root.findViewById(R.id.btnMock).setOnClickListener(this);
        root.findViewById(R.id.btnDetails).setOnClickListener(this);
        ((ListView) root.findViewById(R.id.listView)).setOnItemClickListener(this);

        //noinspection unchecked
        adapter = new ArrayAdapter(getActivity(), R.layout.my_two_line_listitem, listItems) {
            @SuppressLint("SimpleDateFormat")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Log.d(this.getClass().getSimpleName(), "getView(" + position + ", " + convertView + ", " + parent + ")");
                if (convertView == null) {
                    // inflate layout
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.my_two_line_listitem, parent, false);
                }

                TextView tvTitle = (TextView) convertView.findViewById(android.R.id.text1);
                if (tvTitle != null) tvTitle.setText(listItems.get(position).title);
                TextView tvSubTitle = (TextView) convertView.findViewById(android.R.id.text2);
                if (tvSubTitle != null)
                    tvSubTitle.setText(new SimpleDateFormat("[HH:mm:ss.SSS]").format(new Date()));

                return convertView;
            }
        };

        ((ListViewCompat) root.findViewById(R.id.listView))
                .setAdapter(adapter);
        return root;
    }

    public void onMockButtonPressed() {
        Button btnMock = (Button) root.findViewById(R.id.btnMock);
        if (btnMock == null) return;

        if (mock.isEnabled) {
            if (stopMocking()) btnMock.setText("Start");
        } else {
            if (startMocking()) btnMock.setText("Stop");
        }
    }

    private boolean startMocking() {
        List<String> missing_permissions = missingPermissions();
        if (!missing_permissions.isEmpty()) {
            locationActivity.showToast("missing: " + TextUtils.join(", ", missing_permissions), Toast.LENGTH_SHORT);
            return false;
        }
        mock.enable();

        // Set test location programmatically
        timerTask = new TimerTask() {
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

    @SuppressWarnings("MissingPermission")
    private boolean stopMocking() {
        List<String> missing_permissions = missingPermissions();
        if (!missing_permissions.isEmpty()) {
            locationActivity.showToast("missing: " + TextUtils.join(", ", missing_permissions), Toast.LENGTH_SHORT);
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
        new AlertDialog.Builder(getActivity())
                .setMessage(missingPermissions().isEmpty() ? providerDetails(getLocationManager().getProvider(mLocationProvider)) : "Need permissions to show details")
                .setTitle("Details of " + mLocationProvider + " provider")
                .setPositiveButton("Ok", null)
                .create()
                .show();
    }

    private void showItemDetails(int position) {
        new AlertDialog.Builder(getActivity())
                .setMessage(listItems.get(position).description)
                .setTitle(listItems.get(position).title)
                .setPositiveButton("Ok", null)
                .create()
                .show();
    }

    protected void addRow(String titleDescription) {
        addRow(titleDescription, titleDescription);
    }

    protected void addRow(String title, String description) {
        listItems.add(listItems.size(), new TitleDescription(title, description));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        showItemDetails(position);
    }

    public List<String> missingPermissions() {
        List<String> missing_permissions = new ArrayList<>();
        if (!locationActivity.isMockLocationEnabled()) missing_permissions.add("mock setting");

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            missing_permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            missing_permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        return missing_permissions;
    }

    private class TitleDescription {
        String title;
        String description;

        private TitleDescription(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

    public LocationManager getLocationManager() {
        return locationManager == null ? locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE) : locationManager;
    }
}

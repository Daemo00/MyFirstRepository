package com.daemo.myfirstapp.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.text.TextUtilsCompat;
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

import com.daemo.myfirstapp.BuildConfig;
import com.daemo.myfirstapp.R;

import java.sql.Array;
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                    tvSubTitle.setText(new SimpleDateFormat("[HH.mm.ss.SSS]").format(new Date()));

                return convertView;
            }
        };

        ((ListViewCompat) root.findViewById(R.id.listView))
                .setAdapter(
//                        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, listItems)
                        adapter
                );
        return root;
    }

    public void onMockButtonPressed() {
        if (mock.isEnabled) {
            if (stopMocking()) ((Button) getView().findViewById(R.id.btnMock)).setText("Start");
        } else {
            if (startMocking()) ((Button) getView().findViewById(R.id.btnMock)).setText("Stop");
        }
    }

    private boolean startMocking() {
        if (!hasPermissions()) return false;
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
        if (!hasPermissions()) return false;
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
        if (hasPermissions() && t != null) t.cancel();
        super.onDestroy();
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
                .setMessage(hasPermissions() ? providerDetails(getLocationManager().getProvider(mLocationProvider)) : "Need permissions to show details")
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
//        Toast.makeText(getActivity(), title, Toast.LENGTH_SHORT).show();
        listItems.add(listItems.size(), new TitleDescription(title, description));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        showItemDetails(position);
    }

    public boolean hasPermissions() {
        List<String> missing_permissions = new ArrayList<>();
        if (!isMockLocationEnabled(getActivity())) {
            missing_permissions.add("mock setting");
        }

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            missing_permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            missing_permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (missing_permissions.isEmpty()) return true;

        Toast.makeText(getActivity(), "missing: " + TextUtils.join(", ",missing_permissions), Toast.LENGTH_SHORT).show();
        return false;
    }

    public static boolean isMockLocationEnabled(Context context) {
        boolean isMockLocation = false;
        try {
            //if marshmallow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AppOpsManager opsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                isMockLocation = (opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(), BuildConfig.APPLICATION_ID) == AppOpsManager.MODE_ALLOWED);
            } else {
                // in marshmallow this will always return true
                isMockLocation = !android.provider.Settings.Secure.getString(context.getContentResolver(), "mock_location").equals("0");
            }
        } catch (Exception e) {
            return isMockLocation;
        }

        return isMockLocation;
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

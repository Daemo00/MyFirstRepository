package com.daemo.myfirstapp.connectivity.p2p;


import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Looper.getMainLooper;

/**
 * A simple {@link Fragment} subclass.
 */
public class P2pFragment extends Fragment implements View.OnClickListener {


    private static final String SERVER_PORT = "1";
    private IntentFilter intentFilter;
    private MySuperActivity activity;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private boolean isWifiP2pEnabled;
    private WiFiDirectBroadcastReceiver receiver;
    private View root;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private AdapterView.OnItemClickListener itemSelectedListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            final WifiP2pDevice wifiP2pDevice = (WifiP2pDevice) adapterView.getItemAtPosition(i);
            activity.showOkCancelDialog("Connect to " + wifiP2pDevice.deviceName, wifiP2pDevice.toString() + "?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    connect(wifiP2pDevice);
                }
            });
        }
    };
    List<WifiP2pDevice> services;
    private ListView servicesListView;

    public P2pFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Utils.getTag(this), "savedInstanceState is " + savedInstanceState);

        setRetainInstance(true);
        // Initialize only first time
//        if (savedInstanceState == null) {
        activity = (MySuperActivity) getActivity();

        intentFilter = new IntentFilter();

        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(activity, getMainLooper(), null);

        startServiceRegistration();
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_p2p, container, false);
        root.findViewById(R.id.discover_services).setOnClickListener(this);
        servicesListView = (ListView) root.findViewById(R.id.list_services);
        updateList(services);
        servicesListView.setOnItemClickListener(itemSelectedListener);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        activity = (MySuperActivity) getActivity();

        if (receiver == null) receiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        activity.registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        activity.unregisterReceiver(receiver);
    }

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    public boolean isWifiP2pEnabled() {
        return isWifiP2pEnabled;
    }

    public void updateThisDevice(WifiP2pDevice parcelableExtra) {

    }

    public void insertInList(WifiP2pDevice... devices) {
        Collections.addAll(services, devices);
        updateList(services);
    }

    public void updateList(Collection<WifiP2pDevice> list) {
        if (list == null) list = new ArrayList<>();
        services = (List<WifiP2pDevice>) list;

        ArrayAdapter<WifiP2pDevice> arr = new ArrayAdapter<WifiP2pDevice>(activity, R.layout.my_two_line_listitem, services) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                Log.d(Utils.getTag(this), "getView(" + position + ", " + convertView + ", " + parent + ")");
                if (convertView == null) {
                    // inflate layout
                    convertView = LayoutInflater.from(activity).inflate(R.layout.my_two_line_listitem, parent, false);

                    TextView tvTitle = (TextView) convertView.findViewById(android.R.id.text1);
                    if (tvTitle != null) tvTitle.setText(getItem(position).deviceName);
                    TextView tvSubTitle = (TextView) convertView.findViewById(android.R.id.text2);
                    if (tvSubTitle != null) tvSubTitle.setText(getItem(position).deviceAddress);
                }

                return convertView;
            }
        };
        arr.notifyDataSetChanged();

        servicesListView.setAdapter(arr);
    }

    private void startServiceRegistration() {
        //  Create a string map containing information about your service.
        Map<String, String> record = new HashMap<>();
        record.put("listenport", String.valueOf(SERVER_PORT));
        record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
        record.put("available", "visible");

        // Service information.
        // Pass it an instance name, service type _protocol._transportlayer , and the map containing information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp", record);

        // Add the local service, sending the service info, network channel, and listener that will be used to indicate success or failure of the request.
        mManager.addLocalService(mChannel, serviceInfo, getActionListener("addLocalService"));
    }

    final HashMap<String, String> buddies = new HashMap<>();

    private void discoverService() {
        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
        /* Callback includes:
         * fullDomain: full domain name: e.g "printer._ipp._tcp.local."
         * record: TXT record dta as a map of key/value pairs.
         * device: The device running the advertised service.
         */

            public void onDnsSdTxtRecordAvailable(String fullDomain, Map<String, String> record, WifiP2pDevice device) {
                Log.d(Utils.getTag(this), "DnsSdTxtRecord available - " + record.toString());
                buddies.put(device.deviceAddress, record.get("buddyname"));
            }
        };

        WifiP2pManager.DnsSdServiceResponseListener serviceResponseListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice device) {
                // Update the device name with the human-friendly version from the DnsTxtRecord, assuming one arrived.
                device.deviceName = buddies.containsKey(device.deviceAddress) ?
                        buddies.get(device.deviceAddress) : device.deviceName;

                // Add to the custom adapter defined specifically for showing wifi devices.
                insertInList(device);
                Log.d(Utils.getTag(this), "onBonjourServiceAvailable " + instanceName);
            }
        };

        mManager.setDnsSdResponseListeners(mChannel, serviceResponseListener, txtListener);

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest(mChannel, serviceRequest, getActionListener("addServiceRequest"));

        mManager.discoverServices(mChannel, getActionListener("discoverServices"));
    }

    private WifiP2pManager.ActionListener getActionListener(final String method) {
        return new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(Utils.getTag(this), method + " succeeded");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d(Utils.getTag(this), method + " failed with reason " + getReason(reasonCode));
            }
        };
    }

    @NonNull
    public String getReason(int reasonCode) {
        String reason = "";
        switch (reasonCode) {
            case WifiP2pManager.P2P_UNSUPPORTED:
                reason = "P2P_UNSUPPORTED";
                break;
            case WifiP2pManager.ERROR:
                reason = "ERROR";
                break;
            case WifiP2pManager.BUSY:
                reason = "BUSY";
                break;
        }
        return reason;
    }

    public void connect(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, getActionListener("connect"));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.discover_services:
                discoverService();
                break;
        }
    }
}
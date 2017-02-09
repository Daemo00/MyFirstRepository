package com.daemo.myfirstapp.connectivity.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.daemo.myfirstapp.common.Utils;

import java.net.InetAddress;
import java.util.Collections;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private final WifiP2pManager mManager;
    private final WifiP2pManager.Channel mChannel;
    private final P2pFragment p2pFragment;
    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            // Out with the old, in with the new.
            p2pFragment.services.clear();
            Collections.addAll(p2pFragment.services, peerList.getDeviceList().toArray(new WifiP2pDevice[]{}));
            Log.d(Utils.getTag(this), "Listened peers list is " + Utils.printList(peerList.getDeviceList()));

            // If an AdapterView is backed by this data, notify it of the change. For instance, if you have a ListView of available peers, trigger an update.
            p2pFragment.updateList(p2pFragment.services);
            if (p2pFragment.services.size() == 0) {
                Log.d(Utils.getTag(this), "No devices found");
                return;
            }
        }
    };
    private WifiP2pManager.ConnectionInfoListener connectionListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo info) {

            // InetAddress from WifiP2pInfo struct.
            InetAddress groupOwnerAddress = info.groupOwnerAddress;

            // After the group negotiation, we can determine the group owner.
            if (info.groupFormed && info.isGroupOwner) {
                // Do whatever tasks are specific to the group owner.
                // One common case is creating a server thread and accepting incoming connections.
            } else if (info.groupFormed) {
                // The other device acts as the client. In this case, you'll want to create a client thread that connects to the group owner.
            }
        }
    };


    public WiFiDirectBroadcastReceiver() {
        this(null, null, null);
        Log.e(Utils.getTag(this), "Empty constructor should not be called");
    }

    public WiFiDirectBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, P2pFragment p2pFragment) {
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.p2pFragment = p2pFragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                Log.d(Utils.getTag(this), "State changed");
                // Determine if Wifi P2P mode is enabled or not, alert the Activity.
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                p2pFragment.setIsWifiP2pEnabled(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED);
                break;
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                Log.d(Utils.getTag(this), "Peers changed");

                // Request available peers from the wifi p2p manager.
                // This is an asynchronous call and the calling activity is notified with a callback on PeerListListener.onPeersAvailable()
//                WifiP2pDeviceList list = intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
//                Log.d(Utils.getTag(this), "Received peers list is " + Utils.printList(list.getDeviceList()));
                if (mManager != null) mManager.requestPeers(mChannel, peerListListener);
                break;
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                Log.d(Utils.getTag(this), "Connection changed");

                WifiP2pInfo wifiP2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                Log.d(Utils.getTag(this), "WifiP2pInfo is " + wifiP2pInfo.toString());

                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                Log.d(Utils.getTag(this), "NetworkInfo is " + networkInfo.toString());

                WifiP2pGroup groupInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
                Log.d(Utils.getTag(this), "WifiP2pGroup is " + groupInfo.toString());

                // Connection state changed!  We should probably do something about that.
                if (networkInfo.isConnected()) {
                    // We are connected with the other device, request connection info to find group owner IP
                    mManager.requestConnectionInfo(mChannel, connectionListener);
                }
                break;
            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                Log.d(Utils.getTag(this), "This device changed");
//            DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.frag_list);
                p2pFragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
                break;
        }
    }
}
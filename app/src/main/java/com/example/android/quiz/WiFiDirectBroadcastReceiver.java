package com.example.android.quiz;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import com.example.android.quiz.MainActivity;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private HostActivity mActivity1;
    private PlayerActivity mActivity2;
    private int a;

    // constructor
    public WiFiDirectBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, HostActivity mActivity1, PlayerActivity mActivity2, int a) {
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.a = a;
        if (a == 1) {
            this.mActivity1 = mActivity1;
        } else {
            this.mActivity2 = mActivity2;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context, "WiFi P2P State Enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "WiFi P2P State Disabled", Toast.LENGTH_SHORT).show();
            }
        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                if (a == 1) {
                    mManager.requestPeers(mChannel, mActivity1.peerListListener);
                } else {
                    mManager.requestPeers(mChannel, mActivity2.peerListListener);
                }
            }
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    if (a == 1) {
                        mManager.requestConnectionInfo(mChannel, mActivity1.connectionInfoListener);
                    } else {
                        mManager.requestConnectionInfo(mChannel, mActivity2.connectionInfoListener);
                    }
                }
                else {
                    // mActivity.connectionStatus.setText("Device Disconnected");
                    Toast.makeText(context, "Device Disconnected", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // do something
        }
    }
}

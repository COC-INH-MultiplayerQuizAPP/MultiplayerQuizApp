package com.example.android.quiz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {

    // Wifi p2p
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    // list and array for peers online
    List<WifiP2pDevice> peers = new ArrayList<>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    // objects for sending and receiving data
    static final int MESSAGE_READ = 1;

    private ListView listView;
    private TextView btnReady;
    private TextView btnSearchHost;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    int[] arr = new int[5];
                    for (int i = 0; i <  5; i++) {
                        arr[i] = readBuff[i];
                    }
                    Intent intent = new Intent(PlayerActivity.this, StartsInActivity.class);
                    intent.putExtra("questions", arr);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        initializeObjects();
        expListener();
    }

    private void initializeObjects() {
        btnSearchHost = findViewById(R.id.search_for_host);
        listView = findViewById(R.id.hostListView);
        btnReady = findViewById(R.id.ready);

        // btnSend = findViewById(R.id.sendButton);
        // readMsgBox = findViewById(R.id.readMsg); textView
        // writeMsg = findViewById(R.id.writeMsg); editText
        // wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, new HostActivity(), this, 0);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void expListener() {

        //        btnOnOff.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View view) {
        //
        //                // wifiManager.isWifiEnabled() returns true if Wifi is on
        //                if (wifiManager.isWifiEnabled()) {
        //                    wifiManager.setWifiEnabled(false);
        //                    btnOnOff.setText("ON");
        //                }
        //                else {
        //                    wifiManager.setWifiEnabled(true);
        //                    btnOnOff.setText("OFF");
        //                }
        //            }
        //        });

        btnSearchHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        btnSearchHost.setText("SEARCHING");
                        Toast.makeText(getApplicationContext(), "Searching for Host", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(), "Unable to search for Host", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice device = deviceArray[i];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                // config.groupOwnerIntent = 0;

                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(), "Unable to connect to " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // only host will be show
    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if (!peerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];

                int index = 0;

                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                listView.setAdapter(adapter);
            }

            if (peers.size() == 0) {
                Toast.makeText(getApplicationContext(), "No Host Found", Toast.LENGTH_SHORT).show();
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;

            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                serverClass = new ServerClass();
                serverClass.start();

                btnReady.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ripple_host_player));
                btnReady.setTextColor(Color.parseColor("#ffffff"));
            }
            else if (wifiP2pInfo.groupFormed) {
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();

                btnReady.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ripple_host_player));
                btnReady.setTextColor(Color.parseColor("#ffffff"));
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    // server / host side thread
    public class ServerClass extends Thread {
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceive = new SendReceive(this.socket);
                sendReceive.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // client side thread
    public class ClientClass extends Thread {
        Socket socket;
        String hostAdd;

        public ClientClass(InetAddress hostAddress) {
            this.hostAdd = hostAddress.getHostAddress();
            this.socket = new Socket();
        }

        @Override
        public void run() {
            try {
                this.socket.connect(new InetSocketAddress(this.hostAdd, 8888), 500);
                sendReceive = new SendReceive(this.socket);
                sendReceive.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SendReceive extends Thread {
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket socket) {
            this.socket = socket;
            try {
                this.inputStream = socket.getInputStream();
                this.outputStream = socket.getOutputStream();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (socket != null) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // method for sending message
        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            }
            catch (IOException e) {
                e.printStackTrace();
                e.getMessage();
                Log.i("Exception: ", "IOException");
            }
        }
    }
}
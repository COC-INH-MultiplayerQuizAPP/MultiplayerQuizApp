package com.example.android.quiz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class HostActivity extends AppCompatActivity {

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
    private TextView btnStartHost;
    private TextView btnStartGame;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    ArrayList<Socket> socketArrayList;
    ArrayList<SendReceive> sendReceiveArrayList;

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    break;
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        initializeObjects();
        expListener();
    }

    private void initializeObjects() {
        btnStartHost = findViewById(R.id.start_host);
        listView = findViewById(R.id.peerListView);
        btnStartGame = findViewById(R.id.start_game);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this, new PlayerActivity(), 1);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void expListener() {

        btnStartHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        btnStartHost.setText("HOSTING");
                    }
                    @Override
                    public void onFailure(int i) {
                        btnStartHost.setText("RETRY");
                    }
                });

                mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Group Created", Toast.LENGTH_SHORT).show();
                        serverClass = new ServerClass();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(), "Failed to Create Group", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        btnStartGame.setClickable(false);
    }

    // all the peers in the network will be shown here
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
                Toast.makeText(getApplicationContext(), "No device Found", Toast.LENGTH_SHORT).show();
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;

        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            Toast.makeText(getApplicationContext(), "You are Host", Toast.LENGTH_SHORT).show();
            if(deviceArray!=null) {
                serverClass.run();
            }

            btnStartGame.setClickable(true);
            btnStartGame.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ripple_host_player));
            btnStartGame.setTextColor(Color.parseColor("#ffffff"));
        }
        else if (wifiP2pInfo.groupFormed) {
            Toast.makeText(getApplicationContext(), "You are Host", Toast.LENGTH_SHORT).show();
            clientClass = new ClientClass(groupOwnerAddress);
            clientClass.start();

            btnStartGame.setClickable(true);
            btnStartGame.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ripple_host_player));
            btnStartGame.setTextColor(Color.parseColor("#ffffff"));
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
        ServerSocket serverSocket;

        public ServerClass() {
            try{
                serverSocket = new ServerSocket(8888);
                socketArrayList = new ArrayList<Socket>();
                sendReceiveArrayList = new ArrayList<SendReceive>();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                Socket socket = serverSocket.accept();
                socketArrayList.add(socket);
                sendReceiveArrayList.add(new SendReceive(socket));
                // sendReceive.start();
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

    public void startGame(View view) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.questions)));
        String input;
        int len = 0;
        while((input = bufferedReader.readLine()) != null)
            len++;

        Set<Integer> questionsSet = new HashSet<>();
        Random random = new Random();
        while(questionsSet.size() < 5)
            questionsSet.add(random.nextInt(len) + 1);

        Integer[] questionsArray = questionsSet.toArray(new Integer[questionsSet.size()]);
        int[] questions = new int[5];
        for (int i = 0; i < 5; i++) {
            questions[i] = questionsArray[i];
        }

        Intent intent = new Intent(HostActivity.this, StartsInActivity.class);
        intent.putExtra("questions", questions);
        startActivity(intent);

        ByteBuffer byteBuffer = ByteBuffer.allocate(20);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(questions);

        byte[] byteTest = byteBuffer.array();
        byte[] array = new byte[5];

        for (int i = 3, j = 0; i < 20; i += 4, j++) {
            array[j] = byteTest[i];
        }

        for(SendReceive client : sendReceiveArrayList) {
            client.start();
            client.write(array);
        }

        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Group Closed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(), "Failure Closing Group", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

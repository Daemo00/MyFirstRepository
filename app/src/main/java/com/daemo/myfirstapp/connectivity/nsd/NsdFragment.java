package com.daemo.myfirstapp.connectivity.nsd;


import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.daemo.myfirstapp.activities.MySuperActivity;
import com.daemo.myfirstapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class NsdFragment extends Fragment implements View.OnClickListener {


    NsdHelper mNsdHelper;

    private TextView mChatTextView;
    private Handler mUpdateHandler;

    public static final String TAG = "NsdChat";

    ChatConnection mConnection;
    private MySuperActivity activity;
    private View root;

    public NsdFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_nsd, container, false);
        activity = (MySuperActivity) getActivity();

        mChatTextView = (TextView) root.findViewById(R.id.chat);

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String newLine = "[" + new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(new Date()) + "]: ";
                if (msg.getData().containsKey("msg"))
                    newLine += msg.getData().getString("msg");
                else if (msg.getData().containsKey("upd"))
                    newLine += msg.getData().getString("upd");
                addChatLine(newLine);
            }
        };
        mConnection = new ChatConnection(mUpdateHandler);

        mNsdHelper = new NsdHelper(activity, mUpdateHandler);
        mNsdHelper.initializeNsd();

        root.findViewById(R.id.advertise_btn).setOnClickListener(this);
        root.findViewById(R.id.connect_btn).setOnClickListener(this);
        root.findViewById(R.id.discover_btn).setOnClickListener(this);
        root.findViewById(R.id.send_btn).setOnClickListener(this);
        return root;
    }

    public void clickRegister(View v) {
        // Register service
        if (mConnection.getLocalPort() > -1) {
            mNsdHelper.registerService(mConnection.getLocalPort());
        } else {
            Log.d(TAG, "ServerSocket isn't bound.");
        }
    }

    public void clickDiscover(View v) {
        mNsdHelper.discoverServices();
    }

    public void clickConnect(View v) {
        NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
        if (service != null) {
            Log.d(TAG, "Connecting.");
            mConnection.connectToServer(service.getHost(), service.getPort());
        } else {
            Log.d(TAG, "No service to connect to!");
        }
    }

    public void clickSend(View v) {
        EditText messageView = (EditText) root.findViewById(R.id.chatInput);
        if (messageView != null) {
            String messageString = messageView.getText().toString();
            if (!messageString.isEmpty()) {
                mConnection.sendMessage(messageString);
            }
            messageView.setText("");
        }
    }

    public void addChatLine(String line) {
        mChatTextView.append("\n" + line);
    }

    @Override
    public void onPause() {
        if (mNsdHelper != null) {
            mNsdHelper.stopDiscovery();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNsdHelper != null) {
            mNsdHelper.discoverServices();
        }
    }

    @Override
    public void onDestroy() {
        if (mNsdHelper != null) mNsdHelper.tearDown();
        if (mConnection != null) mConnection.tearDown();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.advertise_btn:
                clickRegister(view);
                break;
            case R.id.connect_btn:
                clickConnect(view);
                break;
            case R.id.discover_btn:
                clickDiscover(view);
                break;
            case R.id.send_btn:
                clickSend(view);
                break;
        }
    }
}
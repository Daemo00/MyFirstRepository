package com.daemo.myfirstapp.firebase.storage;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Utils;
import com.daemo.myfirstapp.firebase.MySuperFirebaseFragment;

import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class FirebaseStorageFragment extends MySuperFirebaseFragment implements View.OnClickListener {

    private static final int RC_TAKE_PICTURE = 101;

    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";

    private BroadcastReceiver mBroadcastReceiver;

    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Local broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(Utils.getTag(this), "onReceive:" + Utils.debugIntent(intent));
                getMySuperActivity().hideProgressDialog();

                switch (intent.getAction()) {
                    case MyDownloadService.DOWNLOAD_COMPLETED:
                        // Get number of bytes downloaded
                        long numBytes = intent.getLongExtra(MyDownloadService.EXTRA_BYTES_DOWNLOADED, 0);

                        // Alert success
                        getMySuperActivity().showOkDialog(
                                getString(R.string.success),
                                String.format(Locale.getDefault(), "%d bytes downloaded from %s", numBytes, intent.getStringExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH)),
                                null);
                        break;
                    case MyDownloadService.DOWNLOAD_ERROR:
                        // Alert failure
                        getMySuperActivity().showOkDialog(
                                "Error",
                                String.format(Locale.getDefault(), "Failed to download from %s", intent.getStringExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH)),
                                null);
                        break;
                    case MyUploadService.UPLOAD_COMPLETED:
                    case MyUploadService.UPLOAD_ERROR:
                        onUploadResultIntent(intent);
                        break;
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_firebase_storage, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Click listeners
        view.findViewById(R.id.button_camera).setOnClickListener(this);
        view.findViewById(R.id.button_download).setOnClickListener(this);
    }

//    @Override
//    public void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        // Check if this Activity was launched by clicking on an upload notification
//        if (intent.hasExtra(MyUploadService.EXTRA_DOWNLOAD_URL)) {
//            onUploadResultIntent(intent);
//        }
//    }

    @Override
    public void onStart() {
        super.onStart();
        // Register receiver for uploads and downloads
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
        manager.registerReceiver(mBroadcastReceiver, MyDownloadService.getIntentFilter());
        manager.registerReceiver(mBroadcastReceiver, MyUploadService.getIntentFilter());
    }

    @Override
    public void onStop() {
        super.onStop();

        // Unregister download receiver
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        out.putParcelable(KEY_FILE_URI, mFileUri);
        out.putParcelable(KEY_DOWNLOAD_URL, mDownloadUrl);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // Restore instance state
        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(Utils.getTag(this), "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        if (requestCode == RC_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                mFileUri = data.getData();

                if (mFileUri != null) {
                    uploadFromUri(mFileUri);
                } else {
                    Log.w(Utils.getTag(this), "File URI is null");
                }
            } else getMySuperActivity().showToast("Taking picture failed.");
        }
    }

    private void uploadFromUri(Uri fileUri) {
        Log.d(Utils.getTag(this), "uploadFromUri:src:" + fileUri.toString());

        // Save the File URI
        mFileUri = fileUri;

        // Clear the last download, if any
        mDownloadUrl = null;

        // Start MyUploadService to upload the file, so that the file is uploaded even if this Activity is killed or put in the background
        getActivity().startService(new Intent(getContext(), MyUploadService.class)
                .putExtra(MyUploadService.EXTRA_FILE_URI, fileUri)
                .setAction(MyUploadService.ACTION_UPLOAD));

        // Show loading spinner
        getMySuperActivity().showProgressDialog(getString(R.string.progress_uploading));
    }

    private void beginDownload() {
        // Get path
        String path = "photos/" + mFileUri.getLastPathSegment();

        // Kick off MyDownloadService to download the file
        Intent intent = new Intent(getContext(), MyDownloadService.class)
                .putExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH, path)
                .setAction(MyDownloadService.ACTION_DOWNLOAD);
        getActivity().startService(intent);

        // Show loading spinner
        getMySuperActivity().showProgressDialog(getString(R.string.progress_downloading));
    }

    private void launchCamera() {
        Log.d(Utils.getTag(this), "launchCamera");

        // Pick an image from storage
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, RC_TAKE_PICTURE);
    }

    private void onUploadResultIntent(Intent intent) {
        // Got a new intent from MyUploadService with a success or failure
        mDownloadUrl = intent.getParcelableExtra(MyUploadService.EXTRA_DOWNLOAD_URL);
        mFileUri = intent.getParcelableExtra(MyUploadService.EXTRA_FILE_URI);

        updateUI();
    }

    private void updateUI() {
        View view = getView();
        if (view == null) return;
        // Download URL and Download button
        if (mDownloadUrl != null) {
            ((TextView) view.findViewById(R.id.picture_download_uri)).setText(mDownloadUrl.toString());
            view.findViewById(R.id.layout_download).setVisibility(View.VISIBLE);
        } else {
            ((TextView) view.findViewById(R.id.picture_download_uri)).setText(null);
            view.findViewById(R.id.layout_download).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_camera:
                launchCamera();
                break;
            case R.id.button_download:
                beginDownload();
                break;
        }
    }
}
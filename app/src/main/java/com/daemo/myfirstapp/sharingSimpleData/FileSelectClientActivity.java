package com.daemo.myfirstapp.sharingSimpleData;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Locale;

public class FileSelectClientActivity extends MySuperActivity {

    private Intent mRequestFileIntent;
    private ParcelFileDescriptor mInputPFD;
    private NfcAdapter mNfcAdapter;
    private boolean mAndroidBeamAvailable;
    private FileUriCallback mFileUriCallback;
    private Intent mIntent;
    private File receivedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) // i.e. activity started for first time
            checkPermissionsRunTime(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});

        mRequestFileIntent = new Intent(Intent.ACTION_PICK);
        mRequestFileIntent.setType("*/*");//image/jpg");//
        checkNFC();

        manageIntent();
    }

    private void checkNFC() {
        // NFC isn't available on the device
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
            /*
             * Disable NFC features here.
             * For example, disable menu items or buttons that activate
             * NFC-related features
             */
            // Android Beam file transfer isn't supported
        } else if (Build.VERSION.SDK_INT <
                Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // If Android Beam isn't available, don't continue.
            mAndroidBeamAvailable = false;
            /*
             * Disable Android Beam file transfer features here.
             */
            // Android Beam file transfer is available, continue
        } else {
            Log.d(this.getClass().getSimpleName(), "NfcAdpater instantiated");
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        /*
         * Instantiate a new FileUriCallback to handle requests for
         * URIs
         */
            mFileUriCallback = new FileUriCallback();
            // Set the dynamic callback for URI requests.
            mNfcAdapter.setBeamPushUrisCallback(mFileUriCallback, this);
        }
    }

    // List of URIs to provide to Android Beam
    private Uri[] mFileUris = new Uri[1];

    /**
     * Callback that Android Beam file transfer calls to get
     * files to share
     */
    private class FileUriCallback implements NfcAdapter.CreateBeamUrisCallback {
        public FileUriCallback() {
        }

        /**
         * Create content URIs as needed to share with another device
         */
        @Override
        public Uri[] createBeamUris(NfcEvent event) {
            Log.d(this.getClass().getSimpleName(), "createBeamUris, array has length " + mFileUris.length);
            return mFileUris;
        }
    }

    private void handleViewIntent() {
        // Get the Intent action
        mIntent = getIntent();
        String action = mIntent.getAction();
        /*
         * For ACTION_VIEW, the Activity is being asked to display data.
         * Get the URI.
         */
        if (TextUtils.equals(action, Intent.ACTION_VIEW)) {
            // Get the URI from the Intent
            Uri beamUri = mIntent.getData();
            /*
             * Test for the type of URI, by getting its scheme value
             */
            if (TextUtils.equals(beamUri.getScheme(), "file")) {
                receivedFile = handleFileUri(beamUri);
            } else if (TextUtils.equals(
                    beamUri.getScheme(), "content")) {
                receivedFile = handleContentUri(beamUri);
            }
            showToast("received file for view is " + receivedFile.getAbsolutePath());
        }
    }

    public File handleContentUri(Uri beamUri) {
        // Test the authority of the URI
        if (!TextUtils.equals(beamUri.getAuthority(), MediaStore.AUTHORITY)) {
            /*
             * Handle content URIs for other content providers
             */
            // For a MediaStore content URI
        } else {
            // Get the column that contains the file name
            String[] projection = {MediaStore.MediaColumns.DATA};
            try (Cursor pathCursor = getContentResolver().query(beamUri, projection, null, null, null)) {
                // Check for a valid cursor
                if (pathCursor != null && pathCursor.moveToFirst()) {
                    // Get the full file name including path
                    String fileName = pathCursor.getString(pathCursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                    // Create a File object for the filename
                    return new File(fileName);
                } else {
                    // The query didn't work; return null
                    return null;
                }
            }
        }
        return null;
    }

    public File handleFileUri(Uri beamUri) {
        // Get the path part of the URI
        String fileName = beamUri.getPath();
        // Create a File object for this filename
        return new File(fileName);
    }

    public void requestFile(View v) {
        /**
         * When the user requests a file, send an Intent to the
         * server app.
         * files.
         */
        startActivityForResult(mRequestFileIntent, 0);
    }

    /*
     * When the Activity of the app that hosts files sets a result and calls
     * finish(), this method is invoked. The returned Intent contains the
     * content URI of a selected file. The result code indicates if the
     * selection worked or not.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent returnIntent) {
        // If the selection didn't work
        if (resultCode != RESULT_OK) {
            // Exit without doing anything else
            return;
        } else {
            // Get the file's content URI from the incoming Intent
            Uri returnUri = returnIntent.getData();
            /*
             * Try to open the file for "read" access using the
             * returned URI. If the file isn't found, write to the
             * error log and return.
             */
            try {
                /*
                 * Get the content resolver instance for this context, and use it
                 * to get a ParcelFileDescriptor for the file.
                 */
                mInputPFD = getContentResolver().openFileDescriptor(returnUri, "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(this.getClass().getSimpleName(), "File not found.");
                return;
            }
            // Get a regular file descriptor for the file
            if (mInputPFD != null) {
                FileDescriptor fd = mInputPFD.getFileDescriptor();
            }
            mFileUris[0] = returnUri;

            // Get the file's content URI from the incoming Intent, then get the file's MIME type
            String mimeType = getContentResolver().getType(returnUri);
            TextView typeView = (TextView) findViewById(R.id.tvFileType);
            typeView.setText(mimeType);


            // Get the file's content URI from the incoming Intent, then query the server app to get the file's display name and size.
            try (Cursor returnCursor = getContentResolver().query(returnUri, null, null, null, null)) {

                // Get the column indexes of the data in the Cursor, move to the first row in the Cursor, get the data, and display it.

                if (returnCursor != null) {
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                    returnCursor.moveToFirst();
                    TextView nameView = (TextView) findViewById(R.id.tvFileName);
                    TextView sizeView = (TextView) findViewById(R.id.tvFileSize);
                    nameView.setText(returnCursor.getString(nameIndex));
                    sizeView.setText(String.format(Locale.getDefault(), "%d", returnCursor.getLong(sizeIndex)));
                }
            }
        }
    }

    private void manageIntent() {
        mIntent = getIntent();
        String action = mIntent.getAction();
        String type = mIntent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(mIntent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(mIntent); // Handle single image being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(mIntent); // Handle multiple images being sent
            }
        } else if (Intent.ACTION_VIEW.equals(action) && type != null) {
            handleViewIntent();
        }
    }

    private void handleSendMultipleImages(Intent intent) {
        showToast("handleSendMultipleImages");
    }

    private void handleSendImage(Intent intent) {
        showToast("handleSendImage");
    }

    private void handleSendText(Intent intent) {
        showToast("handleSendText");
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_sharing_simple_data;
    }

    public void sendIntent(View v) {

        String[] projection = {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA
        };
        String[] selectionArgs = {};

        ArrayList<Uri> imageUris = new ArrayList<>();
        ArrayList<String> imageDatas = new ArrayList<>();
        try (Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                "",
                selectionArgs,
                "")) {
            if (cursor != null) {
                cursor.moveToFirst();
                Uri imageUri;
                String imageData;
                for (int i = 0; i < 1; i++, cursor.moveToNext()) {
                    imageUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID))
                    );
                    imageData = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));

                    Log.d(this.getClass().getSimpleName(), "btw, uriToImage is " + imageUri + " and its path is " + imageData);
                    imageUris.add(imageUri);
                    imageDatas.add(imageData);
                }
            }
        }

        Intent sendIntent = new Intent();
//        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);

//        sendIntent.setData(Uri.fromFile(new File(data)));
//        sendIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
        sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
//        sendIntent.putStringArrayListExtra(Intent.EXTRA_TEXT, imageDatas);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "some text");
        sendIntent.setType("image/*");

        if (wantsChooser())
            sendIntent = Intent.createChooser(sendIntent, "I am a chooser");

        startActivity(sendIntent);
    }

    private boolean wantsChooser() {
        RadioGroup rgShowChooser = (RadioGroup) findViewById(R.id.rgShowChooser);
        switch (rgShowChooser.getCheckedRadioButtonId()) {
            case R.id.rbtnShowChooserYes:
                return true;
            case R.id.rbtnShowChooserNo:
                return false;
            default:
                return false;
        }
    }
}

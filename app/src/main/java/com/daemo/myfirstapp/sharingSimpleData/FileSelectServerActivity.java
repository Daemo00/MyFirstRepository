package com.daemo.myfirstapp.sharingSimpleData;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;

import java.io.File;

public class FileSelectServerActivity extends MySuperActivity {
    // The path to the root of this app's internal storage
    private File mPrivateRootDir;
    // The path to the "images" subdirectory
    private File mImagesDir;
    // Array of files in the images subdirectory
    File[] mImageFiles;
    // Array of filenames corresponding to mImageFiles
    String[] mImageFilenames;
    private Intent mResultIntent;
    ListViewCompat mFileListView;


    // Initialize the Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up an Intent to send back to apps that request a file
        mResultIntent = new Intent("com.daemo.myfirstapp.ACTION_RETURN_FILE");
        // Get the files/ subdirectory of internal storage
        mPrivateRootDir = getFilesDir();
        // Get the files/images subdirectory;
        mImagesDir = new File(mPrivateRootDir, "images");//mPrivateRootDir;//
        File f = new File(mImagesDir, "tempFile.txt");
        Log.d(this.getClass().getSimpleName(), "images folder exists? " + mImagesDir.exists());
        // Get the files in the images subdirectory
        mImageFiles = mImagesDir.listFiles() == null ? new File[]{} : mImagesDir.listFiles();

        // Set the Activity's result to null to begin with
        setResult(Activity.RESULT_CANCELED, null);

        // Display the file names in the ListView mFileListView. Back the ListView with the array mImageFilenames, which you can create by iterating through mImageFiles and calling File.getAbsolutePath() for each File
        mImageFilenames = new String[mImageFiles.length];
        for (int i = 0; i < mImageFiles.length; i++)
            mImageFilenames[i] = mImageFiles[i].getAbsolutePath();

        mFileListView = (ListViewCompat) findViewById(R.id.lvFileSelector);
        mFileListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mImageFilenames));
        mFileListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    // When a filename in the ListView is clicked, get its content URI and send it to the requesting app
                    @Override
                    public void onItemClick(AdapterView<?> adapterView,
                                            View view,
                                            int position,
                                            long rowId) {

                        // Get a File for the selected file name. Assume that the file names are in the mImageFilename array.
                        File requestFile = new File(mImageFilenames[position]);
                        // Most file-related method calls need to be in try-catch blocks. Use the FileProvider to get a content URI
                        Uri fileUri = Uri.EMPTY;
                        try {
                            fileUri = FileProvider.getUriForFile(
                                    FileSelectServerActivity.this,
                                    "com.daemo.myfirstapp.fileprovider",
                                    requestFile);
                        } catch (IllegalArgumentException e) {
                            String clickedFilename = requestFile.getAbsolutePath();
                            Log.e(this.getClass().getSimpleName(),
                                    "The selected file can't be shared: " +
                                            clickedFilename);
                            e.printStackTrace();
                        }

                        if (fileUri != Uri.EMPTY) {
                            // Grant temporary read permission to the content URI
                            mResultIntent.addFlags(
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            // Put the Uri and MIME type in the result Intent
                            mResultIntent.setDataAndType(
                                    fileUri,
                                    getContentResolver().getType(fileUri));
                            // Set the result
                            FileSelectServerActivity.this.setResult(Activity.RESULT_OK,
                                    mResultIntent);
                        } else {
                            mResultIntent.setDataAndType(null, "");
                            FileSelectServerActivity.this.setResult(RESULT_CANCELED,
                                    mResultIntent);
                        }
                    }
                });
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_file_select;
    }

    public void onDoneClick(View v) {
        // Associate a method with the Done button
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setIntent(mResultIntent);
    }
}

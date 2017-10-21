package com.daemo.myfirstapp.sharingSimpleData;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.daemo.myfirstapp.activities.MySuperActivity;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Constants;
import com.daemo.myfirstapp.common.Utils;

import java.io.File;

public class FileSelectServerActivity extends MySuperActivity {

    // Array of filenames corresponding to mImageFiles
    String[] mImageFilenames;
    private Intent mResultIntent;

    // Initialize the Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_select);
        // Set up an Intent to send back to apps that request a file
        mResultIntent = new Intent(Constants.ACTION_RETURN_FILE);
        configSpinner();
        configSwitches();
        configList();
        fillList();
    }

    private void configList() {
        ((ListViewCompat) findViewById(R.id.lvFileSelector)).setOnItemClickListener(
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
                        if (requestFile.isDirectory()) {
                            fillList(requestFile.getName());
                        } else {

                            Uri fileUri = Uri.EMPTY;
                            try {
                                fileUri = FileProvider.getUriForFile(
                                        FileSelectServerActivity.this,
                                        "com.daemo.myfirstapp.fileprovider",
                                        requestFile);
                            } catch (IllegalArgumentException e) {
                                Log.e(this.getClass().getSimpleName(), "The selected file can't be shared: " + requestFile.getAbsolutePath());
                                e.printStackTrace();
                            }

                            if (fileUri != Uri.EMPTY) {
                                // Grant temporary read permission to the content URI
                                mResultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                // Put the Uri and MIME type in the result Intent
                                mResultIntent.setDataAndType(fileUri, getContentResolver().getType(fileUri));
                                // Set the result
                                FileSelectServerActivity.this.setResult(Activity.RESULT_OK, mResultIntent);
                            } else {
                                mResultIntent.setDataAndType(null, "");
                                FileSelectServerActivity.this.setResult(RESULT_CANCELED, mResultIntent);
                            }
                        }
                    }
                });
    }

    private void configSwitches() {
        SwitchCompat[] switches = {
                (SwitchCompat) findViewById(R.id.switchCompatCache),
                (SwitchCompat) findViewById(R.id.switchCompatInternal),
                (SwitchCompat) findViewById(R.id.switchCompatPublic)
        };

        for (SwitchCompat aSwitch : switches)
            aSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fillList();
                }
            });
    }

    private void configSpinner() {
        String[] dirs = Utils.getFieldsValue(Environment.class, "DIRECTORY_");
        AppCompatSpinner spinner = (AppCompatSpinner) findViewById(R.id.spinner);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dirs));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fillList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                showToast("Nothing selected");
            }
        });
    }

    private void fillList() {
        fillList("");
    }

    private void fillList(String subDir) {
        File dir = subDir.isEmpty() ? getSelectedDir() : new File(getSelectedDir(), subDir);
        ((TextView) findViewById(R.id.textView4)).setText(dir.getAbsolutePath());

        File[] mImageFiles = dir.listFiles() == null ? new File[]{} : dir.listFiles();

        // Set the Activity's result to null to begin with
        setResult(Activity.RESULT_CANCELED, null);

        // Display the file names in the ListView mFileListView.
        // Back the ListView with the array mImageFilenames, which you can create by iterating through mImageFiles and calling File.getAbsolutePath() for each File
        mImageFilenames = new String[mImageFiles.length];
        for (int i = 0; i < mImageFiles.length; i++)
            mImageFilenames[i] = mImageFiles[i].getAbsolutePath();

        ListViewCompat lv = (ListViewCompat) findViewById(R.id.lvFileSelector);
        ArrayAdapter<String> arr = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mImageFilenames);
        arr.notifyDataSetChanged();
        lv.setAdapter(arr);
    }

    private File getSelectedDir() {
        return Utils.getSelectedDirectory(this,
                ((AppCompatSpinner) findViewById(R.id.spinner)).getSelectedItem().toString(),
                ((SwitchCompat) findViewById(R.id.switchCompatPublic)).isChecked(),
                ((SwitchCompat) findViewById(R.id.switchCompatInternal)).isChecked(),
                ((SwitchCompat) findViewById(R.id.switchCompatCache)).isChecked());
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

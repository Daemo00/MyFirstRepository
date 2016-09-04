package com.daemo.myfirstapp.savingData;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.daemo.myfirstapp.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SavingFiles extends Fragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private View root;
    private final String file_name = "tempFile.txt";
    private SavingActivity savingActivity;
    private final String[] storageDirectories = new String[]{
            Environment.DIRECTORY_ALARMS,
            Environment.DIRECTORY_DCIM,
            Environment.DIRECTORY_DOCUMENTS,
            Environment.DIRECTORY_DOWNLOADS,
            Environment.DIRECTORY_MOVIES,
            Environment.DIRECTORY_MUSIC,
            Environment.DIRECTORY_NOTIFICATIONS,
            Environment.DIRECTORY_PICTURES,
            Environment.DIRECTORY_PODCASTS,
            Environment.DIRECTORY_RINGTONES
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        savingActivity = (SavingActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_saving_files_data, container, false);

        root.findViewById(R.id.btnSaveFile).setOnClickListener(this);
        root.findViewById(R.id.btnLoadFile).setOnClickListener(this);
        ((RadioGroup) root.findViewById(R.id.rgStorageMode)).setOnCheckedChangeListener(this);
        return root;
    }

    @Nullable
    private File getSelectedDirectory() {
        File res = null;
        RadioGroup rg = (RadioGroup) root.findViewById(R.id.rgStorageMode);
        if (rg == null) return null;

        switch (rg.getCheckedRadioButtonId()) {
            case R.id.rbtnInternalStorage:
                CheckBox cbIsCache = (CheckBox) root.findViewById(R.id.cbIsCache);
                res = cbIsCache.isChecked() ? savingActivity.getCacheDir() : savingActivity.getFilesDir();
                break;
            case R.id.rbtnExternalStorage:
                CheckBox cbIsPublic = (CheckBox) root.findViewById(R.id.cbIsPublic);
                Spinner sp = (Spinner) root.findViewById(R.id.spDirectoryStorage);

                String type = sp.getSelectedItem().toString();
                res = cbIsPublic.isChecked() ? Environment.getExternalStoragePublicDirectory(type) : savingActivity.getExternalFilesDir(type);
                break;
        }

        if (res != null && !res.exists() && res.mkdir())
            savingActivity.showToast(res + " created");
        return res;
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.btnSaveFile:
                    save();
                    break;
                case R.id.btnLoadFile:
                    load();
                    break;
            }
        } catch (IOException e) {
            savingActivity.showToast(e.getMessage());
        }
    }

    private void save() throws IOException {
        if (!isExternalStorageWritable()) Log.d(this.getClass().getSimpleName(), "not writable");

        EditText etFileContent = (EditText) root.findViewById(R.id.etFileContent);
        TextView tvFilePath = (TextView) root.findViewById(R.id.tvFilePath);
        if (etFileContent == null || tvFilePath == null) return;

        File file = new File(getSelectedDirectory() + File.separator + file_name);
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write(etFileContent.getText().toString());
        }

        tvFilePath.setText(file.getAbsolutePath());
    }

    private void load() throws IOException {
        if (!isExternalStorageReadable()) Log.d(this.getClass().getSimpleName(), "not readable");

        EditText etFileContent = (EditText) root.findViewById(R.id.etFileContent);
        TextView tvFilePath = (TextView) root.findViewById(R.id.tvFilePath);
        if (etFileContent == null || tvFilePath == null) return;

        StringBuilder res = new StringBuilder();
        File file = new File(getSelectedDirectory() + File.separator + file_name);
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String s;
            while ((s = bufferedReader.readLine()) != null) {
                res.append(s);
            }
        }
        etFileContent.setText(res);

        tvFilePath.setText(file.getAbsolutePath());
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (group.getCheckedRadioButtonId()) {
            case R.id.rbtnInternalStorage:
                buildCbIsCache();
                break;
            case R.id.rbtnExternalStorage:
                buildSpDirectory();
                break;
        }
    }

    private void buildSpDirectory() {
        LinearLayout rl = (LinearLayout) root.findViewById(R.id.rlStorageOptions);
        if (rl == null) return;
        rl.removeAllViews();

        // maybe better to convert to XML layout
        TextView tvDirectoryStorageLabel = new TextView(savingActivity);
        tvDirectoryStorageLabel.setId(R.id.tvDirectoryStorageLabel);
        tvDirectoryStorageLabel.setText("Choose Directory:");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 0;
        tvDirectoryStorageLabel.setLayoutParams(layoutParams);

        Spinner spDirectory = new Spinner(savingActivity, Spinner.MODE_DROPDOWN);
        spDirectory.setId(R.id.spDirectoryStorage);
        layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        spDirectory.setLayoutParams(layoutParams);
        spDirectory.setAdapter(new ArrayAdapter<>(savingActivity, android.R.layout.simple_spinner_item, storageDirectories));

        CheckBox cbIsPublic = new CheckBox(savingActivity);
        cbIsPublic.setId(R.id.cbIsPublic);
        layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        cbIsPublic.setLayoutParams(layoutParams);
        cbIsPublic.setText(R.string.is_public);

        rl.addView(tvDirectoryStorageLabel);
        rl.addView(spDirectory);
        rl.addView(cbIsPublic);
    }

    private void buildCbIsCache() {
        LinearLayout rl = (LinearLayout) root.findViewById(R.id.rlStorageOptions);
        if (rl == null) return;
        rl.removeAllViews();

        CheckBox cbIsCache = new CheckBox(savingActivity);
        cbIsCache.setId(R.id.cbIsCache);
        cbIsCache.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        cbIsCache.setText(R.string.is_cache);

        rl.addView(cbIsCache);
    }
}
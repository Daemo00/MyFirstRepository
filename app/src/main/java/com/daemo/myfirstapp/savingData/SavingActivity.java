package com.daemo.myfirstapp.savingData;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.SectionsPagerAdapter;

public class SavingActivity extends MySuperActivity {

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_data);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        SavingKeyValueData savingKeyValueData = SavingKeyValueData.getInstance();
        savingKeyValueData.setTitle(getString(R.string.saving_key_value_title));

        SavingFiles savingFiles = SavingFiles.getInstance();
        savingFiles.setTitle(getString(R.string.saving_files_title));

        SavingDB savingDB = SavingDB.getInstance();
        savingDB.setTitle(getString(R.string.saving_database_title));

        sectionsPagerAdapter.setFrags(savingKeyValueData, savingFiles, savingDB);
        mViewPager.setAdapter(sectionsPagerAdapter);

        checkPermissionsRunTime(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
}
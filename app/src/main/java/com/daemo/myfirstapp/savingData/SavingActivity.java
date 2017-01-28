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

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        SavingKeyValueData savingKeyValueData = SavingKeyValueData.getInstance(new Bundle());
        savingKeyValueData.setTitle(getString(R.string.saving_key_value_title));

        SavingFiles savingFiles = SavingFiles.getInstance(new Bundle());
        savingFiles.setTitle(getString(R.string.saving_files_title));

        SavingDB savingDB = SavingDB.getInstance(new Bundle());
        savingDB.setTitle(getString(R.string.saving_database_title));

        sectionsPagerAdapter.setFrags(savingKeyValueData, savingFiles, savingDB);
        mViewPager.setAdapter(sectionsPagerAdapter);

//        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
//        tabLayout.setupWithViewPager(mViewPager);

        checkPermissionsRunTime(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_saving_data;
    }

}

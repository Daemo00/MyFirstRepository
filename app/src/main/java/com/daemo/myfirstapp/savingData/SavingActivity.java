package com.daemo.myfirstapp.savingData;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;

public class SavingActivity extends MySuperActivity {

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));

//        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
//        tabLayout.setupWithViewPager(mViewPager);

        checkPermissionsRunTime(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE});
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_saving_data;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return frags[position].fragment;
        }

        @Override
        public int getCount() {
            return frags.length;
        }

        private class TitleFragment {
            String title;
            Fragment fragment;

            public TitleFragment(String title, Fragment fragment) {
                this.title = title;
                this.fragment = fragment;
            }
        }

        TitleFragment[] frags = new TitleFragment[]{
                new TitleFragment(getString(R.string.saving_key_value_title), new SavingKeyValueData()),
                new TitleFragment(getString(R.string.saving_files_title), new SavingFiles()),
                new TitleFragment(getString(R.string.saving_database_title), new SavingDB()),
        };

        @Override
        public CharSequence getPageTitle(int position) {
            return frags[position].title;
        }
    }
}

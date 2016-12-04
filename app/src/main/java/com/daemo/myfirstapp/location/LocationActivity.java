package com.daemo.myfirstapp.location;


import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.daemo.myfirstapp.BuildConfig;
import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.location.aware.AwarenessFragment;

import java.util.List;

public class LocationActivity extends MySuperActivity {
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager()));
        mPager.setOffscreenPageLimit(2);

        // Check permissions here because fragments are continuously created and destroyed
        checkPermissionsRunTime(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        if (!isMockLocationEnabled()) {
            showOkCancelDialog("Pliis", "Activate mock location", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                }
            });
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_location;
    }

    public boolean isMockLocationEnabled() {
        boolean isMockLocation;
        try {
            //if marshmallow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AppOpsManager opsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                isMockLocation = (opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(), BuildConfig.APPLICATION_ID) == AppOpsManager.MODE_ALLOWED);
            } else {
                // in marshmallow this will always return true
                isMockLocation = !android.provider.Settings.Secure.getString(getContentResolver(), "mock_location").equals("0");
            }
        } catch (Exception e) {
            return false;
        }

        return isMockLocation;
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private List<String> locationProviders;
        private Fragment[] frags = new Fragment[]{
                new AwarenessFragment()
        };

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
            locationProviders = ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).getAllProviders();
        }

        @Override
        public Fragment getItem(int position) {
            return position < frags.length ?
                    frags[position] :
                    ProviderDetailsFragment.newInstance(locationProviders.get(position - frags.length));
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return position < frags.length ?
                    frags[position].getClass().getSimpleName() :
                    locationProviders.get(position - frags.length);
        }

        @Override
        public int getCount() {
            return frags.length + locationProviders.size();
        }
    }
}
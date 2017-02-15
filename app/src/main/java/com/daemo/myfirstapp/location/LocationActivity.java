package com.daemo.myfirstapp.location;


import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.daemo.myfirstapp.BuildConfig;
import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Utils;
import com.daemo.myfirstapp.location.aware.AwarenessFragment;

import java.util.Arrays;
import java.util.List;

import static com.daemo.myfirstapp.common.Constants.Location.REQUEST_CHECK_SETTINGS;
import static com.daemo.myfirstapp.common.Constants.Location.REQUEST_CONNECTION;
import static com.daemo.myfirstapp.common.Constants.Location.REQUEST_GEOFENCES;
import static com.daemo.myfirstapp.common.Constants.Location.REQUEST_LAST_LOCATION;
import static com.daemo.myfirstapp.common.Constants.Location.REQUEST_LOCATION_UPDATES;

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
//        checkPermissionsRunTime(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
//        if (!isMockLocationEnabled()) {
//            showOkCancelDialog("Pliis", "Activate mock location", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
//                }
//            });
//        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            ((ScreenSlidePagerAdapter) mPager.getAdapter()).getItem(0).onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(Utils.getTag(this), "Received permission result for requestCode: " + requestCode);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Arrays.asList(REQUEST_CONNECTION, REQUEST_LAST_LOCATION, REQUEST_LOCATION_UPDATES, REQUEST_GEOFENCES).contains(requestCode))
            ((ScreenSlidePagerAdapter) mPager.getAdapter()).getItem(0).onRequestPermissionsResult(requestCode, permissions, grantResults);
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
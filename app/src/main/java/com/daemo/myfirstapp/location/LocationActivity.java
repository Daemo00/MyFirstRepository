package com.daemo.myfirstapp.location;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.daemo.myfirstapp.R;

import java.util.List;

public class LocationActivity extends AppCompatActivity {

    private ViewPager mPager;
    private static String asking_permission;
    private static final int MY_PERMISSIONS_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager()));
        mPager.setOffscreenPageLimit(2);
        // Check permissions here because fragments are continuously created and destroyed
        checkPermissionsRunTime(Manifest.permission.ACCESS_FINE_LOCATION);
        checkPermissionsRunTime(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private boolean checkPermissionsRunTime(String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED)
            return true;

        asking_permission = permission;
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
            showOkCancelPermissionDialog(permission);
        else
            ActivityCompat.requestPermissions(this, new String[]{permission}, MY_PERMISSIONS_REQUEST);
        return false;
    }

    private void showOkCancelPermissionDialog(String permission) {
        new AlertDialog.Builder(this)
                .setMessage("I really need " + permission + " permission because bla bla")
                .setTitle("Pliis")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(LocationActivity.this, new String[]{LocationActivity.asking_permission}, MY_PERMISSIONS_REQUEST);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private List<String> locationProviders;

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
            locationProviders = ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).getAllProviders();
        }

        @Override
        public Fragment getItem(int position) {
            return ProviderDetailsFragment.newInstance(locationProviders.get(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return locationProviders.get(position);
        }

        @Override
        public int getCount() {
            return locationProviders.size();
        }
    }
}
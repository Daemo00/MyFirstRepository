package com.daemo.myfirstapp.location;


import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.MySuperFragment;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.SectionsPagerAdapter;
import com.daemo.myfirstapp.location.aware.AwarenessFragment;

import java.util.ArrayList;
import java.util.List;

public class LocationActivity extends MySuperActivity {

    private SectionsPagerAdapter sectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        // Instantiate a ViewPager and a PagerAdapter.
        ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        List<String> locationProviders = ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).getAllProviders();
        ArrayList<MySuperFragment> locationProviderFragments = new ArrayList<>();
        locationProviderFragments.add(new AwarenessFragment());
        locationProviderFragments.add(new MapsFragment());
        for (String locationProviderName : locationProviders)
            locationProviderFragments.add(ProviderDetailsFragment.newInstance(locationProviderName));

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        sectionsPagerAdapter.setFrags(locationProviderFragments.toArray(new MySuperFragment[]{}));
        mPager.setAdapter(sectionsPagerAdapter);
        mPager.setOffscreenPageLimit(2);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment frag : sectionsPagerAdapter.getFrags())
            frag.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (Fragment frag : sectionsPagerAdapter.getFrags())
            frag.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
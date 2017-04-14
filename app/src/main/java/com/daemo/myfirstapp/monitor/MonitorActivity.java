package com.daemo.myfirstapp.monitor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.SectionsPagerAdapter;

public class MonitorActivity extends MySuperActivity implements MonitorService.ServiceCallback {
    MonitorService backgroundService;
    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ServicesFragment servicesFragment = ServicesFragment.getInstance();
        servicesFragment.setTitle(getString(R.string.services_fragment_title));

        ProcessesFragment processesFragment = ProcessesFragment.getInstance();
        processesFragment.setTitle(getString(R.string.processes_fragment_title));
        sectionsPagerAdapter.setFrags(servicesFragment, processesFragment);
        mViewPager.setAdapter(sectionsPagerAdapter);

        bindService(
                new Intent(this, MonitorService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MonitorService.LocalBinder binder = (MonitorService.LocalBinder) service;
            backgroundService = binder.getService();
            backgroundService.setCallback(MonitorActivity.this);
            backgroundService.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            backgroundService = null;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (backgroundService != null)
            backgroundService.setCallback(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (backgroundService != null)
            backgroundService.setCallback(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    public void sendResults(int resultCode, Bundle b) {
        for (Fragment fragment : ((SectionsPagerAdapter) mViewPager.getAdapter()).getFrags())
            ((MonitorService.ServiceCallback) fragment).sendResults(resultCode, b);
    }
}
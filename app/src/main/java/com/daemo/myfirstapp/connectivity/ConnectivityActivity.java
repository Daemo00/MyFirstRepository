package com.daemo.myfirstapp.connectivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.daemo.myfirstapp.activities.MySuperActivity;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.connectivity.downloadPage.DownloadPageFragment;
import com.daemo.myfirstapp.connectivity.nsd.NsdFragment;
import com.daemo.myfirstapp.connectivity.p2p.P2pFragment;
import com.daemo.myfirstapp.connectivity.syncAdapter.SyncAdapterFragment;
import com.daemo.myfirstapp.connectivity.volley.VolleyFragment;

public class ConnectivityActivity extends MySuperActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connectivity);
        ((ViewPager) findViewById(R.id.pager)).
                setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        Fragment[] frags = {
                new NsdFragment(),
                new P2pFragment(),
                new DownloadPageFragment(),
                new SyncAdapterFragment(),
                new VolleyFragment()
        };

        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return frags[position];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return frags[position].getClass().getSimpleName();
        }

        @Override
        public int getCount() {
            return frags.length;
        }
    }
}

package com.daemo.myfirstapp.multimedia;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;

public class MultimediaActivity extends MySuperActivity {

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager()));
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_multimedia;
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        Fragment[] fragments = new Fragment[]{
                new AudioFragment()
        };

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragments[position].getClass().getSimpleName();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }
    }
}

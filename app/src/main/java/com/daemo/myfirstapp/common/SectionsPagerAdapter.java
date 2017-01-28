package com.daemo.myfirstapp.common;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.daemo.myfirstapp.MySuperFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private MySuperFragment[] frags;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return frags[position];
    }

    @Override
    public int getCount() {
        return frags.length;
    }

    public void setFrags(MySuperFragment... frags) {
        this.frags = frags;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return frags[position].getTitle();
    }

    public MySuperFragment[] getFrags() {
        return frags;
    }
}

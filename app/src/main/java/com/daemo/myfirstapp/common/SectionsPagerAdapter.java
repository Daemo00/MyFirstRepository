package com.daemo.myfirstapp.common;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.daemo.myfirstapp.MySuperFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private Fragment[] frags;

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

    public SectionsPagerAdapter setFrags(MySuperFragment... frags) {
        this.frags = frags;
        return this;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return frags[position] instanceof MySuperFragment ? ((MySuperFragment) frags[position]).getTitle() : Utils.getTag(frags[position]);
    }

    public Fragment[] getFrags() {
        return frags;
    }
}

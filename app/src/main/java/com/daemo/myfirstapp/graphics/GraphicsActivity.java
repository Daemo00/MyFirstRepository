package com.daemo.myfirstapp.graphics;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.graphics.animators.AnimationsFragment;
import com.daemo.myfirstapp.graphics.displayingbitmaps.ui.ImageGridFragment;
import com.daemo.myfirstapp.graphics.interpolators.InterpolatorFragment;
import com.daemo.myfirstapp.graphics.openGL.OpenGLFragment;
import com.daemo.myfirstapp.graphics.transitions.TransitionsFragment;

public class GraphicsActivity extends MySuperActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_graphics;
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        android.support.v4.app.Fragment[] fragments = new android.support.v4.app.Fragment[]{
                new ImageGridFragment(),
                new OpenGLFragment(),
                new TransitionsFragment(),
                new InterpolatorFragment(),
                new AnimationsFragment()
        };

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragments[position].getClass().getSimpleName();
        }
    }
}

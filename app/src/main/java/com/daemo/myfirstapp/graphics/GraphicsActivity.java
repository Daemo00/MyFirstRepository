package com.daemo.myfirstapp.graphics;


import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.MySuperFragment;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.SectionsPagerAdapter;
import com.daemo.myfirstapp.graphics.animators.AnimationsFragment;
import com.daemo.myfirstapp.graphics.displayingbitmaps.ui.ImageGridFragment;
import com.daemo.myfirstapp.graphics.interpolators.InterpolatorFragment;
import com.daemo.myfirstapp.graphics.openGL.OpenGLFragment;
import com.daemo.myfirstapp.graphics.transitions.TransitionsFragment;

public class GraphicsActivity extends MySuperActivity {

    private static MySuperFragment[] frags = {new ImageGridFragment(),
            new OpenGLFragment(),
            new TransitionsFragment(),
            new InterpolatorFragment(),
            new AnimationsFragment()};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphics);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.setFrags(frags);

        // Set up the ViewPager with the sections adapter.
        /*
      The {@link ViewPager} that will host the section contents.
     */
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }
}
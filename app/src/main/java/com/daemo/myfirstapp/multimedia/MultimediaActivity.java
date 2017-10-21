package com.daemo.myfirstapp.multimedia;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.daemo.myfirstapp.activities.MySuperActivity;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.SectionsPagerAdapter;
import com.daemo.myfirstapp.multimedia.audio.AudioFragment;
import com.daemo.myfirstapp.multimedia.video.VideoFragment;

public class MultimediaActivity extends MySuperActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multimedia);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
//        mViewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager()));
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            sectionsPagerAdapter.setFrags(new AudioFragment(), VideoFragment.getInstance());
        mViewPager.setAdapter(sectionsPagerAdapter);
    }
}

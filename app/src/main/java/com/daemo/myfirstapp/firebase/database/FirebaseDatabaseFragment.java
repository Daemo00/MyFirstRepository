package com.daemo.myfirstapp.firebase.database;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daemo.myfirstapp.MySuperFragment;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Constants;
import com.daemo.myfirstapp.common.SectionsPagerAdapter;
import com.daemo.myfirstapp.firebase.MySuperFirebaseFragment;
import com.daemo.myfirstapp.firebase.database.fragment.MyPostsFragment;
import com.daemo.myfirstapp.firebase.database.fragment.MyTopPostsFragment;
import com.daemo.myfirstapp.firebase.database.fragment.NewPostFragment;
import com.daemo.myfirstapp.firebase.database.fragment.RecentPostsFragment;

public class FirebaseDatabaseFragment extends MySuperFirebaseFragment {

    private FragmentPagerAdapter mPagerAdapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Create the adapter that will return a fragment for each section

        mPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager())
                .setFrags(
                        (MySuperFragment) Fragment.instantiate(getContext(), RecentPostsFragment.class.getName()),
                        (MySuperFragment) Fragment.instantiate(getContext(), MyPostsFragment.class.getName()),
                        (MySuperFragment) Fragment.instantiate(getContext(), MyTopPostsFragment.class.getName())
                );
        return inflater.inflate(R.layout.fragment_firebase_database, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Button launches NewPostActivity
        view.findViewById(R.id.fab_new_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(Constants.ACTION_FRAGMENT, Constants.ACTION_REPLACE_FRAGMENT);
                bundle.putBoolean(Constants.ACTION_ADDTOBACKSTACK, true);
                getMySuperActivity().onFragmentInteraction(
                        (MySuperFragment) Fragment.instantiate(getContext(), NewPostFragment.class.getName()),
                        bundle);
            }
        });
    }
}
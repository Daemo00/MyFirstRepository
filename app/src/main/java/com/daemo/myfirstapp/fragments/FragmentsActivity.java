package com.daemo.myfirstapp.fragments;
/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;

public class FragmentsActivity extends MySuperActivity
        implements HeadlinesFragment.OnHeadlineSelectedListener {

    private static boolean isMonoFragment = false;

    static boolean getIsMonoFragment() {
        return isMonoFragment;
    }

    private static boolean isCreatedFirstTime = true;

    /**
     * Called when the activity is first created.
     */
    @SuppressWarnings({"StatementWithEmptyBody", "UnnecessaryReturnStatement"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Creating the activity
        isMonoFragment = findViewById(R.id.fragment_container) != null;
        // reset backstack
        getSupportFragmentManager().popBackStack();

        // If it isMonoFragment, inflate a fragment.
        if (getIsMonoFragment()) {
            // If this is first time ever activity is created, create HeadlinesFragment
            if (savedInstanceState == null) {
                createFragment(true);
                return;
            }

            // If activity has been created for first time in dual mode, we have to create HeadlinesFragment
            if (isCreatedFirstTime) {
                createFragment(true);
                isCreatedFirstTime = false;
            } else {
                getSupportFragmentManager().beginTransaction()
                        .show(getHeadlinesFragment())
                        .commit();
            }
            // All the other times the fragment is created from savedInstanceState
        } else {

        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.news_articles;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("FragmentsActivity", "onDestroy");
        isCreatedFirstTime = true;
        ArticleFragment.mCurrentPosition = 0;
    }

    private Fragment createFragment(boolean isHeadlines) {
        Fragment fragment = isHeadlines ? HeadlinesFragment.getInstance() : ArticleFragment.getInstance();

        // In case this activity was started with special instructions from an Intent, pass the Intent's extras to the fragment as arguments
        fragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        return fragment;
    }

    private ArticleFragment getArticleFragment() {
        ArticleFragment fragment;
        fragment = (ArticleFragment) getSupportFragmentManager().findFragmentById(R.id.article_fragment);
        return fragment;
    }

    private HeadlinesFragment getHeadlinesFragment() {
        HeadlinesFragment fragment;
        fragment = (HeadlinesFragment) getSupportFragmentManager().findFragmentById(R.id.headlines_fragment);
        return fragment;
    }

    public void onArticleSelected(int position) {
        // The user selected the headline of an article from the HeadlinesFragment

        // Capture the article fragment from the activity layout
        if (getIsMonoFragment()) {
            // If the frag is not available, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected article
            Fragment newFragment = ArticleFragment.getInstance();
            Bundle args = new Bundle();
            args.putInt(ArticleFragment.ARG_POSITION, position);
            newFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, newFragment)
                    .addToBackStack("selected article")
                    .commit();
        } else {
            // If article frag is available, we're in two-pane layout

            // Call a method in the ArticleFragment to update its content
            getArticleFragment().updateArticleView(position);
        }
    }
}
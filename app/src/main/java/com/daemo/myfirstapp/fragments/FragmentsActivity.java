package com.daemo.myfirstapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Constants;
import com.daemo.myfirstapp.common.Utils;

public class FragmentsActivity extends MySuperActivity implements HeadlinesFragment.OnHeadlineSelectedListener {

    private int selectedArticle = 0;

    boolean isMonoFragment() {
        return findViewById(R.id.fragment_container) != null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_articles);

        Log.d(Utils.getTag(this), "savedInstanceState is " + Utils.debugBundle(savedInstanceState));
        if (isMonoFragment())
            replaceFragment(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.ARTICLE_SELECTED, selectedArticle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        onArticleSelected(selectedArticle = savedInstanceState.getInt(Constants.ARTICLE_SELECTED));
    }

    private Fragment replaceFragment(Bundle args) {

        // In case this activity was started with special instructions from an Intent, pass the Intent's extras to the headlines fragment as arguments
        if (getIntent().getExtras() != null) args = getIntent().getExtras();
        Fragment fragment = HeadlinesFragment.getInstance(args);
        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, Utils.getTag(fragment))
                .commit();
        return fragment;
    }

    private ArticleFragment getArticleFragment() {
        ArticleFragment fragment;
//        fragment = (ArticleFragment) getSupportFragmentManager().findFragmentByTag(Utils.getTag(ArticleFragment.class));
        fragment = (ArticleFragment) getSupportFragmentManager().findFragmentById(R.id.article_fragment);
        return fragment;
    }

    private HeadlinesFragment getHeadlinesFragment() {
        HeadlinesFragment fragment;
//        fragment = (HeadlinesFragment) getSupportFragmentManager().findFragmentByTag(Utils.getTag(HeadlinesFragment.class));
        fragment = (HeadlinesFragment) getSupportFragmentManager().findFragmentById(R.id.headlines_fragment);
        return fragment;
    }

    @Override
    public void onArticleSelected(int position) {
        selectedArticle = position;
        // The user selected the headline of an article from the HeadlinesFragment
        getSupportFragmentManager().popBackStack();

        // Capture the article fragment from the activity layout
        if (isMonoFragment()) {
            // If the frag is not available, we're in the one-pane layout and must swap frags...
            // Create fragment and give it an argument for the selected article
            Bundle args = new Bundle();
            args.putInt(Constants.ARTICLE_SELECTED, position);
            Fragment newFragment = ArticleFragment.getInstance(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, newFragment, Utils.getTag(newFragment))
                    .addToBackStack("selected article " + position)
                    .commit();
        } else {
            // Call a method in the ArticleFragment to update its content
            getHeadlinesFragment().select(position);
            getArticleFragment().updateArticleView(position);
        }
    }
}
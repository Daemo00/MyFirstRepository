package com.daemo.myfirstapp.fragments;/*
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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daemo.myfirstapp.R;

public class ArticleFragment extends Fragment {
    final static String ARG_POSITION = "position";
    static int mCurrentPosition = 0;


    private static ArticleFragment inst;

    public static ArticleFragment getInstance() {
        return inst == null ? inst = new ArticleFragment() : inst;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(this.getClass().getSimpleName(), "onActivityCreated, getArguments is " + getArguments());
        Log.d(this.getClass().getSimpleName(), "onActivityCreated, savedInstanceState " + savedInstanceState);

        if (getArguments() != null) {
            mCurrentPosition = getArguments().getInt(ARG_POSITION);
        }
        else if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        for (int i = 0; i < getActivity().getSupportFragmentManager().getBackStackEntryCount(); i++)
            Log.d("ArticleFragment", "backstack at " + i + " is " + getActivity().getSupportFragmentManager().getBackStackEntryAt(i));

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.article_view, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Set article based on saved instance state defined during onCreateView
        updateArticleView(mCurrentPosition);
    }

    public void updateArticleView(int position) {
        TextView article;
        if (FragmentsActivity.getIsMonoFragment())
            article = (TextView) getActivity().findViewById(R.id.article);
        else
            article = (TextView) getActivity().getSupportFragmentManager().findFragmentById(R.id.article_fragment).getView();

        article.setText(Ipsum.Articles[position]);
        mCurrentPosition = position;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the current article selection in case we need to recreate the fragment
        outState.putInt(ARG_POSITION, mCurrentPosition);
    }
}
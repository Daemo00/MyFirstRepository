package com.daemo.myfirstapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daemo.myfirstapp.MySuperFragment;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Constants;

public class ArticleFragment extends MySuperFragment {

    private static ArticleFragment inst;

    public static ArticleFragment getInstance(Bundle args) {
        if (inst == null)
            inst = new ArticleFragment();
        inst.setArguments(args);
        return inst;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        container = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.article_view, container, true);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        int article = savedInstanceState == null ? 0 : savedInstanceState.getInt(Constants.ARTICLE_SELECTED);
        if (getArguments() != null) article = getArguments().getInt(Constants.ARTICLE_SELECTED);

        updateArticleView(article);
    }

    public void updateArticleView(int position) {
        if (getView() == null) return;
        TextView article = (TextView) getView().findViewById(R.id.article);
        article.setText(Ipsum.Articles[position]);
    }
}
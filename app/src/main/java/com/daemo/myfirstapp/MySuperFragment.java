package com.daemo.myfirstapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daemo.myfirstapp.common.Utils;

public class MySuperFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private MySuperFragment inst;
    private String title;

    public MySuperFragment() {
        Log.d(Utils.getTag(this), "Called constructor");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) Log.d(Utils.getTag(this), "onCreate, arguments is " + arguments);
        else Log.d(Utils.getTag(this), "onCreate, arguments is null");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SwipeRefreshLayout swipeRefreshLayout = new SwipeRefreshLayout(getContext());
        swipeRefreshLayout.setId(R.id.swipe_layout_superfragment);
        swipeRefreshLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        swipeRefreshLayout.setOnRefreshListener(this);

        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            viewGroup.addView(swipeRefreshLayout);
            return viewGroup;
        }
        return swipeRefreshLayout;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void onRefresh() {
        stopRefreshing();
    }

    public void stopRefreshing() {
        View view = getView();
        if (view != null) {
            View layout = view.findViewById(R.id.swipe_layout_superfragment);
            if (layout instanceof SwipeRefreshLayout) {
                SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) layout;
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }
}
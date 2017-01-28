package com.daemo.myfirstapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.daemo.myfirstapp.common.Utils;

public class MySuperFragment extends Fragment {

    private MySuperFragment inst;
    private String title;

    public MySuperFragment() {
        Log.d(Utils.getTag(this), "Called constructor");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            Log.d(Utils.getTag(this), "onCreate, arguments is " + arguments);
        } else {
            Log.d(Utils.getTag(this), "onCreate, arguments is null");
        }
    }

    public String getTitle() {
        return title;
    }

    public void refresh() {
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

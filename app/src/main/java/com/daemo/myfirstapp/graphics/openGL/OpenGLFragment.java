package com.daemo.myfirstapp.graphics.openGL;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daemo.myfirstapp.MySuperFragment;

public class OpenGLFragment extends MySuperFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return new MyGLSurfaceView(getActivity());
    }
}
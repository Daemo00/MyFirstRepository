package com.daemo.myfirstapp.graphics.openGL;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class OpenGLFragment extends Fragment {

    private MyGLSurfaceView myGLSurfaceView;

    public OpenGLFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_open_gl, container, false);
        myGLSurfaceView = new MyGLSurfaceView(getActivity());
        return myGLSurfaceView;
    }

}
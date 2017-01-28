package com.daemo.myfirstapp.graphics.transitions;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.daemo.myfirstapp.MySuperFragment;
import com.daemo.myfirstapp.R;

import static android.transition.Fade.IN;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransitionsFragment extends MySuperFragment implements View.OnClickListener, Transition.TransitionListener {


    private ViewGroup mSceneRoot;
    private ViewGroup root;
    private Scene mSceneA;
    private Scene mSceneB;
    private Transition mFadeTransition;
    private Transition mMultiTransition;
    private TextView mLabelText;
    private ViewGroup mFragmentRoot;
    private Fade mFade;
    private int mCurrentScene;
    private static final String STATE_CURRENT_SCENE = "current_scene";
    private Scene[] mScenes;
    private Transition mCustomTransition;

    public TransitionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.fragment_transitions, container, false);
        if (null != savedInstanceState) {
            mCurrentScene = savedInstanceState.getInt(STATE_CURRENT_SCENE);
        }
        FrameLayout scenes_container = (FrameLayout) root.findViewById(R.id.scenes_container);


// Create the scene root for the scenes in this app
        mSceneRoot = (ViewGroup) root.findViewById(R.id.scene_root);

// Create the scenes
        mSceneA = Scene.getSceneForLayout(mSceneRoot, R.layout.scene_a, getActivity());
        mSceneB = Scene.getSceneForLayout(mSceneRoot, R.layout.scene_b, getActivity());

        mMultiTransition = TransitionInflater.
                from(getActivity()).
                inflateTransition(R.transition.multiple_transition)
                .addListener(this);

        mFadeTransition = TransitionInflater.
                from(getActivity()).
                inflateTransition(R.transition.fade_transition)
                .addListener(this);

        mCustomTransition = new CustomTransition().addListener(this);

        root.findViewById(R.id.button).setOnClickListener(this);
        root.findViewById(R.id.button_delayed).setOnClickListener(this);
        root.findViewById(R.id.button_custom).setOnClickListener(this);

        // We set up the Scenes here.
        mScenes = new Scene[]{
                Scene.getSceneForLayout(scenes_container, R.layout.scene1, getActivity()),
                Scene.getSceneForLayout(scenes_container, R.layout.scene2, getActivity()),
                Scene.getSceneForLayout(scenes_container, R.layout.scene3, getActivity()),
        };
        // This is the custom Transition.
        // Show the initial Scene.
        TransitionManager.go(mScenes[mCurrentScene % mScenes.length]);

        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                TransitionManager.go(mSceneB, mMultiTransition);
                break;
            case R.id.button_delayed:
                // Create a new TextView and set some View properties
                mLabelText = new TextView(getActivity());
                mLabelText.setText(R.string.label);
                mLabelText.setId(R.id.tv_volley);

                Log.d(this.getClass().getSimpleName(), "root is " + root.toString());
                // Start recording changes to the view hierarchy
                TransitionManager.beginDelayedTransition(
                        root,
                        new Fade(IN).addListener(this));
                // Add the new TextView to the view hierarchy
                root.addView(mLabelText);
                break;
            case R.id.button_custom: {
                mCurrentScene = (mCurrentScene + 1) % mScenes.length;
                // Pass the custom Transition as second argument for TransitionManager.go
                TransitionManager.go(mScenes[mCurrentScene], mCustomTransition);
                break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_SCENE, mCurrentScene);
    }

    @Override
    public void onTransitionStart(Transition transition) {
        Log.d(this.getClass().getSimpleName(), "onTransitionStart(" + transition.toString() + ")");
    }

    @Override
    public void onTransitionEnd(Transition transition) {
        Log.d(this.getClass().getSimpleName(), "onTransitionEnd(" + transition.toString() + ")");

    }

    @Override
    public void onTransitionCancel(Transition transition) {
        Log.d(this.getClass().getSimpleName(), "onTransitionCancel(" + transition.toString() + ")");

    }

    @Override
    public void onTransitionPause(Transition transition) {
        Log.d(this.getClass().getSimpleName(), "onTransitionPause(" + transition.toString() + ")");

    }

    @Override
    public void onTransitionResume(Transition transition) {
        Log.d(this.getClass().getSimpleName(), "onTransitionResume(" + transition.toString() + ")");

    }
}

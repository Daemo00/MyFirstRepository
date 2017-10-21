package com.daemo.myfirstapp.graphics.transitions;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.transition.Scene;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.daemo.myfirstapp.activities.MySuperFragment;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Utils;

public class TransitionsFragment extends MySuperFragment implements View.OnClickListener, Transition.TransitionListener {

    private int mCurrentTextScene, mCurrentScene;
    private static final String STATE_CURRENT_TEXT_SCENE = "current_text_scene";
    private static final String STATE_CURRENT_SCENE = "current_scene";
    private Scene[] mTextScenes, mScenes;
    private Transition mMultiTransition, mCustomTransition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transitions, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup mTextSceneRoot = (ViewGroup) view.findViewById(R.id.scene_root);
        mTextScenes = new Scene[]{
                Scene.getSceneForLayout(mTextSceneRoot, R.layout.text_scene_a, getActivity()),
                Scene.getSceneForLayout(mTextSceneRoot, R.layout.text_scene_b, getActivity())
        };

        FrameLayout scenes_container = (FrameLayout) view.findViewById(R.id.scenes_container);
        mScenes = new Scene[]{
                Scene.getSceneForLayout(scenes_container, R.layout.scene1, getActivity()),
                Scene.getSceneForLayout(scenes_container, R.layout.scene2, getActivity()),
                Scene.getSceneForLayout(scenes_container, R.layout.scene3, getActivity()),
        };

        mMultiTransition = TransitionInflater.
                from(getActivity()).
                inflateTransition(R.transition.multiple_transition)
                .addListener(this);

        mCustomTransition = new CustomTransition().addListener(this);

        view.findViewById(R.id.button_go).setOnClickListener(this);
        view.findViewById(R.id.button_delayed).setOnClickListener(this);
        view.findViewById(R.id.button_custom).setOnClickListener(this);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentScene = savedInstanceState.getInt(STATE_CURRENT_SCENE);
            mCurrentTextScene = savedInstanceState.getInt(STATE_CURRENT_TEXT_SCENE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // This is the custom Transition.
        // Show the initial Scene.
        TransitionManager.go(mScenes[mCurrentScene % mScenes.length]);
        TransitionManager.go(mTextScenes[mCurrentTextScene], mMultiTransition);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_go:
                mCurrentTextScene = (mCurrentTextScene + 1) % mTextScenes.length;
                TransitionManager.go(mTextScenes[mCurrentTextScene], mMultiTransition);
                break;
            case R.id.button_delayed:
                // Create a new TextView and set some View properties
                TextView mLabelText = new TextView(getActivity());
                mLabelText.setText(R.string.label);
                mLabelText.setId(R.id.tv_volley);

                ViewGroup rootView = (ViewGroup) getView();
                // Start recording changes to the view hierarchy
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    TransitionManager.beginDelayedTransition(
                            rootView,
                            new Slide(Gravity.LEFT)
//                            new Fade(Visibility.MODE_IN)
//                            new Explode()
//                            new ChangeBounds()
                    );
                }
                // Add the new TextView to the view hierarchy
                if (rootView != null) rootView.addView(mLabelText);
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
        outState.putInt(STATE_CURRENT_TEXT_SCENE, mCurrentTextScene);
    }

    @Override
    public void onTransitionStart(Transition transition) {
        Log.d(Utils.getTag(this), "onTransitionStart(" + transition.toString() + ")");
    }

    @Override
    public void onTransitionPause(Transition transition) {
        Log.d(Utils.getTag(this), "onTransitionPause(" + transition.toString() + ")");
    }

    @Override
    public void onTransitionResume(Transition transition) {
        Log.d(Utils.getTag(this), "onTransitionResume(" + transition.toString() + ")");
    }

    @Override
    public void onTransitionEnd(Transition transition) {
        Log.d(Utils.getTag(this), "onTransitionEnd(" + transition.toString() + ")");
    }

    @Override
    public void onTransitionCancel(Transition transition) {
        Log.d(Utils.getTag(this), "onTransitionCancel(" + transition.toString() + ")");
    }
}
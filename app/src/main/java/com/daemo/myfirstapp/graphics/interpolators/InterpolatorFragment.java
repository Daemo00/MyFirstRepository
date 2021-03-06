/*
* Copyright 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.daemo.myfirstapp.graphics.interpolators;

import android.animation.ObjectAnimator;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.daemo.myfirstapp.activities.MySuperFragment;
import com.daemo.myfirstapp.R;

/**
 * This sample demonstrates the use of animation interpolators and path animations for
 * Material Design.
 * It shows how an {@link ObjectAnimator} is used to animate two properties of a
 * view (scale X and Y) along a path.
 */
public class InterpolatorFragment extends MySuperFragment {

    /**
     * Spinner for selection of interpolator.
     */
    private Spinner mInterpolatorSpinner;
    /**
     * SeekBar for selection of duration of animation.
     */
    private SeekBar mDurationSeekbar;
    /**
     * TextView that shows animation selected in SeekBar.
     */
    private TextView mDurationLabel;

    /**
     * Interpolators used for animation.
     */
    private Interpolator mInterpolators[];
    /**
     * Path for in (shrinking) animation, from 100% scale to 20%.
     */
    private Path mPathIn;
    /**
     * Path for out (growing) animation, from 20% to 100%.
     */
    private Path mPathOut;

    /**
     * Set to true if View is animated out (is shrunk).
     */
    private boolean mIsOut = false;

    /**
     * Default duration of animation in ms.
     */
    private static final int INITIAL_DURATION_MS = 750;

    /**
     * String used for logging.
     */
    public static final String TAG = "InterpolatorPlaygroundFragment";

    public InterpolatorFragment() {
        // Required empty public constructor
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the fragment_animation layout
        View v = inflater.inflate(R.layout.interpolator_fragment, container, false);

        // Set up the 'animate' button, when it is clicked the view is animated with the options selected: the Interpolator, duration and animation path
        Button button = (Button) v.findViewById(R.id.interpolateButton);
        final View square = v.findViewById(R.id.square);
        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                // Interpolator selected in the spinner
                Interpolator interpolator = mInterpolators[mInterpolatorSpinner.getSelectedItemPosition()];
                // Duration selected in SeekBar
                long duration = mDurationSeekbar.getProgress();
                // Animation path is based on whether animating in or out
                Path path = mIsOut ? mPathIn : mPathOut;

                // Start the animation with the selected options
                startAnimation(square, interpolator, duration, path);

                // Toggle direction of animation (path)
                mIsOut = !mIsOut;
            }
        });

        // Get the label to display the selected duration
        mDurationLabel = (TextView) v.findViewById(R.id.durationLabel);

        // Initialize Interpolators programmatically by loading them from their XML definitions provided by the framework.
        mInterpolators = new Interpolator[]{
                AnimationUtils.loadInterpolator(getActivity(), android.R.interpolator.linear),
                AnimationUtils.loadInterpolator(getActivity(), android.R.interpolator.fast_out_linear_in),
                AnimationUtils.loadInterpolator(getActivity(), android.R.interpolator.fast_out_slow_in),
                AnimationUtils.loadInterpolator(getActivity(), android.R.interpolator.linear_out_slow_in)
        };

        // Load names of interpolators from a resource
        String[] interpolatorNames = getResources().getStringArray(R.array.interpolator_names);

        // Set up the Spinner with the names of interpolators
        mInterpolatorSpinner = (Spinner) v.findViewById(R.id.interpolatorSpinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, interpolatorNames);
        mInterpolatorSpinner.setAdapter(spinnerAdapter);

        // Set up SeekBar that defines the duration of the animation
        mDurationSeekbar = (SeekBar) v.findViewById(R.id.durationSeek);

        // Register listener to update the text label when the SeekBar value is updated
        mDurationSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mDurationLabel.setText(getResources().getString(R.string.animation_duration, i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Set initial progress to trigger SeekBarChangeListener and update UI
        mDurationSeekbar.setProgress(INITIAL_DURATION_MS);

        // The following Path definitions are used by the ObjectAnimator to scale the view.

        // Path for 'in' animation: growing from 20% to 100%
        mPathIn = new Path();
        mPathIn.moveTo(0f, 0f);
        mPathIn.lineTo(360f, 360f);

        // Path for 'out' animation: shrinking from 100% to 20%
        mPathOut = new Path();
        mPathOut.moveTo(360f, 360f);
        mPathOut.lineTo(0f, 0f);
        return v;
    }

    /**
     * Start an animation on the sample view.
     * The view is animated using an {@link ObjectAnimator} on the
     * {@link View#SCALE_X} and {@link View#SCALE_Y} properties, with its animation based on a path.
     * The only two paths defined here ({@link #mPathIn} and {@link #mPathOut}) scale the view
     * uniformly.
     *
     * @param interpolator The interpolator to use for the animation.
     * @param duration     Duration of the animation in ms.
     * @param path         Path of the animation
     * @return The ObjectAnimator used for this animation
     * @see ObjectAnimator#ofFloat(Object, String, String, Path)
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ObjectAnimator startAnimation(View view, Interpolator interpolator, long duration, Path path) {
        // This ObjectAnimator uses the path to change the x and y scale of the mView object.
        ObjectAnimator animator =
//                ObjectAnimator.ofFloat(view, View.ROTATION_X, View.ROTATION_Y, path); // View.SCALE_X, View.SCALE_Y,
                ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f);
        // Set the duration and interpolator for this animation
        animator.setDuration(duration);
        animator.setInterpolator(interpolator);

        animator.start();

        return animator;
    }

    /**
     * Return the array of loaded Interpolators available in this Fragment.
     *
     * @return Interpolators
     */
    public Interpolator[] getInterpolators() {
        return mInterpolators;
    }

    /**
     * Return the animation path for the 'in' (shrinking) animation.
     */
    public Path getPathIn() {
        return mPathIn;
    }

    /**
     * Return the animation path for the 'out' (growing) animation.
     */
    public Path getPathOut() {
        return mPathOut;
    }
}
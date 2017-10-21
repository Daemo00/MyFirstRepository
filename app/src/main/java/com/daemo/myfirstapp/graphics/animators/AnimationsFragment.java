package com.daemo.myfirstapp.graphics.animators;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.daemo.myfirstapp.activities.MySuperFragment;
import com.daemo.myfirstapp.R;

import static com.daemo.myfirstapp.graphics.animators.AnimationsFragment.AnimatedFragment.COLOR;

public class AnimationsFragment extends MySuperFragment implements View.OnClickListener {

    private boolean mBlueLoaded = true;
    private Spinner mSpinner;

    private void loadMain() {
        getActivity().getFragmentManager()
                .beginTransaction()
                .replace(R.id.frags_container, AnimatedFragment.getInstance(mBlueLoaded ? Color.GREEN : Color.BLUE))
                .commit();

//        mBlueLoaded = true;
    }

    private enum Animations {
        CrossFade,
        CardFlip
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_animations, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSpinner = (Spinner) view.findViewById(R.id.spinner);
        mSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, Animations.values()));

        view.findViewById(R.id.animateButton).setOnClickListener(this);
        if (savedInstanceState != null && savedInstanceState.containsKey(COLOR))
            mBlueLoaded = savedInstanceState.getBoolean(COLOR);
        loadMain();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(COLOR, mBlueLoaded);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.animateButton:
                animate();
                break;
        }
    }

    private void animate() {
        mBlueLoaded = !mBlueLoaded;
        switch (Animations.valueOf(mSpinner.getSelectedItem().toString())) {
            case CrossFade:
                crossFade();
                break;
            case CardFlip:
                cardFlip();
                break;
        }
    }

    private void cardFlip() {
        getActivity().getFragmentManager()
                .beginTransaction()

                // Replace the default fragment animations with animator resources representing rotations when switching to the back of the card, as well as animator resources representing rotations when flipping back to the front (e.g. when the system Back button is pressed).
                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)

                // Replace any fragments currently in the container view with a fragment representing the next page (indicated by the just-incremented currentPage variable).
                .replace(R.id.frags_container, AnimatedFragment.getInstance(mBlueLoaded ? Color.GREEN : Color.BLUE))

                // Commit the transaction.
                .commit();
    }

    private void crossFade() {
        getActivity().getFragmentManager()
                .beginTransaction()

                // Replace the default fragment animations with animator resources representing rotations when switching to the back of the card, as well as animator resources representing rotations when flipping back to the front (e.g. when the system Back button is pressed).
                .setCustomAnimations(
                        R.animator.cross_fade_in, R.animator.cross_fade_out,
                        R.animator.cross_fade_in, R.animator.cross_fade_out)

                // Replace any fragments currently in the container view with a fragment representing the next page (indicated by the just-incremented currentPage variable).
                .replace(R.id.frags_container, AnimatedFragment.getInstance(mBlueLoaded ? Color.GREEN : Color.BLUE))

                // Commit the transaction.
                .commit();// Decide which view to hide and which to show.
    }

    public static class AnimatedFragment extends android.app.Fragment {

        static final String COLOR = "color";
        private static AnimatedFragment inst;

        public static AnimatedFragment getInstance(@ColorInt int color) {
            inst = new AnimatedFragment();
            Bundle args = new Bundle();
            args.putInt(COLOR, color);
            inst.setArguments(args);
            return inst;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            Bundle args = getArguments();
            int color = args.getInt(COLOR);
            View view = new View(getActivity());
            view.setId(R.id.square);
            view.setBackgroundColor(color);
            int dim = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dim, dim);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            view.setLayoutParams(layoutParams);
            return view;
        }
    }
}
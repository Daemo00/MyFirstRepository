package com.daemo.myfirstapp.graphics.animators;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.daemo.myfirstapp.MySuperFragment;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.graphics.GraphicsActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class AnimationsFragment extends MySuperFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {


    private boolean mMainLoaded = true;
    private View root;
    private Spinner mSpinner;
    private GraphicsActivity graphicsActivity;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (getActivity().getFragmentManager().getBackStackEntryCount() > 0) {
            getActivity().getFragmentManager().popBackStack();
            mMainLoaded = true;
        }
    }

    private void loadMain() {
        getActivity().getFragmentManager()
                .beginTransaction()
                .replace(R.id.frags_container, new MainFragment())
                .commit();

        mMainLoaded = true;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private enum Animations {
        CrossFade,
        CardFlip
    }

    public AnimationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_animations, container, false);
        graphicsActivity = (GraphicsActivity) getActivity();
        mSpinner = (Spinner) root.findViewById(R.id.spinner);
        mSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, Animations.values()));
        mSpinner.setOnItemSelectedListener(this);

        root.findViewById(R.id.animateButton).setOnClickListener(this);
        if (savedInstanceState == null) {
            loadMain();
        } else {
            mMainLoaded = !(getActivity().getFragmentManager().getBackStackEntryCount() > 0);
        }

        return root;
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
//        try {
//            this.getClass().getDeclaredMethod(mSpinner.getSelectedItem().toString()).invoke(this);
//        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
//            e.printStackTrace();
//        }
        switch (Animations.valueOf(mSpinner.getSelectedItem().toString())) {
            case CrossFade:
                crossFade();
                break;
            case CardFlip:
                cardFlip();
                break;
        }
        mMainLoaded = !mMainLoaded;
    }

    private void cardFlip() {
        if (!mMainLoaded) {
            getActivity().getFragmentManager().popBackStack();
            return;
        }

        // Create and commit a new fragment transaction that adds the fragment for the back of the card, uses custom animations, and is part of the fragment manager's back stack.

        getActivity().getFragmentManager()
                .beginTransaction()

                // Replace the default fragment animations with animator resources representing rotations when switching to the back of the card, as well as animator resources representing rotations when flipping back to the front (e.g. when the system Back button is pressed).
                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)

                // Replace any fragments currently in the container view with a fragment representing the next page (indicated by the just-incremented currentPage variable).
                .replace(R.id.frags_container, new AlterFragment())

                // Add this transaction to the back stack, allowing users to press Back to get to the front of the card.
                .addToBackStack(null)

                // Commit the transaction.
                .commit();
    }

    private void crossFade() {
        if (!mMainLoaded) {
            getActivity().getFragmentManager().popBackStack();
            return;
        }

        getActivity().getFragmentManager()
                .beginTransaction()

                // Replace the default fragment animations with animator resources representing rotations when switching to the back of the card, as well as animator resources representing rotations when flipping back to the front (e.g. when the system Back button is pressed).
                .setCustomAnimations(
                        R.animator.cross_fade_in, R.animator.cross_fade_out,
                        R.animator.cross_fade_in, R.animator.cross_fade_out)

                // Replace any fragments currently in the container view with a fragment representing the next page (indicated by the just-incremented currentPage variable).
                .replace(R.id.frags_container, new AlterFragment())

                // Add this transaction to the back stack, allowing users to press Back to get to the front of the card.
                .addToBackStack(null)

                // Commit the transaction.
                .commit();// Decide which view to hide and which to show.
    }
}
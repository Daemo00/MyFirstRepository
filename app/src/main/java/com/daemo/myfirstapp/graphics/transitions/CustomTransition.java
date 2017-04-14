package com.daemo.myfirstapp.graphics.transitions;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.view.View;
import android.view.ViewGroup;


class CustomTransition extends Transition {
    // Define a key for storing a property value in TransitionValues.values with the syntax package_name:transition_class:property_name to avoid collisions
    private static final String PROPNAME_BACKGROUND =
            "com.daemo.myfirstapp.graphics.transitions:CustomTransition:background";

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        // Call the convenience method captureValues
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        // Call the convenience method captureValues
        captureValues(transitionValues);
    }

    // For the view in transitionValues.view, get the values you want and put them in transitionValues.values
    private void captureValues(TransitionValues transitionValues) {
        // Get a reference to the view
        View view = transitionValues.view;
        // Store its background property in the values map
        transitionValues.values.put(PROPNAME_BACKGROUND, view.getBackground());
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        // This transition can only be applied to views that are on both starting and ending scenes.
        if (startValues == null || endValues == null) return null;
        // Store a convenient reference to the target.
        // Both the starting and ending layout have the same target.
        final View view = endValues.view;
        // Store the object containing the background property for both the starting and ending layouts.
        Drawable startBackground = (Drawable) startValues.values.get(PROPNAME_BACKGROUND);
        Drawable endBackground = (Drawable) endValues.values.get(PROPNAME_BACKGROUND);
        // This transition changes background colors for a target.
        // It doesn't animate any other background changes.
        // If the property isn't a ColorDrawable, ignore the target.
        if (startBackground instanceof ColorDrawable && endBackground instanceof ColorDrawable) {
            ColorDrawable startColor = (ColorDrawable) startBackground;
            ColorDrawable endColor = (ColorDrawable) endBackground;
            // If the background color for the target in the starting and ending layouts is different, create an animation.
            if (startColor.getColor() != endColor.getColor()) {
                // Create a new Animator object to apply to the targets as the transitions framework changes from the starting to the ending layout.
                // Use the class ValueAnimator, which provides a timing pulse to change property values provided to it.
                // The animation runs on the UI thread.
                // The Evaluator controls what type of interpolation is done.
                // In this case, an ArgbEvaluator interpolates between two #argb values, which are specified as the 2nd and 3rd input arguments.
                ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), startColor.getColor(), endColor.getColor());
                // Add an update listener to the Animator object.
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Object value = animation.getAnimatedValue();
                        // Each time the ValueAnimator produces a new frame in the animation, change the background color of the target.
                        // Ensure that the value isn't null.
                        if (value != null) view.setBackgroundColor((Integer) value);
                    }
                });
                // Return the Animator object to the transitions framework.
                // As the framework changes between the starting and ending layouts, it applies the animation you've created.
                return animator;
            }
        }
        // For non-ColorDrawable backgrounds, we just return null, and no animation will take place.
        return null;
    }
}
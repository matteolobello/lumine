/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commit451.morphtransitions;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.transition.ChangeBounds;
import android.transition.TransitionValues;
import android.view.View;
import android.view.ViewGroup;

/**
 * An extension to {@link ChangeBounds} that also morphs the views background (color & corner
 * radius).
 */
@TargetApi(21)
public class MorphTransform extends ChangeBounds {

    public static final long DEFAULT_DURATION = 350L;

    private static final String EXTRA_SHARED_ELEMENT_START_COLOR =
            "EXTRA_SHARED_ELEMENT_START_COLOR";
    private static final String EXTRA_SHARED_ELEMENT_START_CORNER_RADIUS =
            "EXTRA_SHARED_ELEMENT_START_CORNER_RADIUS";

    /**
     * Configure {@code intent} with the extras needed to initialize this transition.
     */
    public static void addExtras(Intent intent, int startColor, int startCornerRadius) {
        intent.putExtra(EXTRA_SHARED_ELEMENT_START_COLOR, startColor);
        intent.putExtra(EXTRA_SHARED_ELEMENT_START_CORNER_RADIUS, startCornerRadius);
    }

    /**
     * Configure {@link MorphTransform}s & set as {@code activity}'s shared element enter and return
     * transitions.
     */
    public static void setup(Activity activity, View target, int endColor, int endCornerRadius) {
        final Intent intent = activity.getIntent();
        if (intent == null
                || !intent.hasExtra(EXTRA_SHARED_ELEMENT_START_COLOR)
                || !intent.hasExtra(EXTRA_SHARED_ELEMENT_START_CORNER_RADIUS)) return;

        final int startColor = activity.getIntent()
                .getIntExtra(EXTRA_SHARED_ELEMENT_START_COLOR, Color.TRANSPARENT);
        final int startCornerRadius =
                intent.getIntExtra(EXTRA_SHARED_ELEMENT_START_CORNER_RADIUS, 0);

        final MorphTransform sharedEnter =
                new MorphTransform(startColor, endColor, startCornerRadius, endCornerRadius);
        // Reverse the start/end params for the return transition
        final MorphTransform sharedReturn =
                new MorphTransform(endColor, startColor, endCornerRadius, startCornerRadius);
        if (target != null) {
            sharedEnter.addTarget(target);
            sharedReturn.addTarget(target);
        }
        activity.getWindow().setSharedElementEnterTransition(sharedEnter);
        activity.getWindow().setSharedElementReturnTransition(sharedReturn);
    }

    private final int startColor;
    private final int endColor;
    private final int startCornerRadius;
    private final int endCornerRadius;

    public MorphTransform(int startColor, int endColor, int startCornerRadius, int endCornerRadius) {
        this.startColor = startColor;
        this.endColor = endColor;
        this.startCornerRadius = startCornerRadius;
        this.endCornerRadius = endCornerRadius;
        setDuration(DEFAULT_DURATION);
        setPathMotion(new GravityArcMotion());
    }

    @Override
    public Animator createAnimator(final ViewGroup sceneRoot,
                                   final TransitionValues startValues,
                                   final TransitionValues endValues) {
        final Animator changeBounds = super.createAnimator(sceneRoot, startValues, endValues);
        if (changeBounds == null) return null;

        TimeInterpolator interpolator = getInterpolator();
        if (interpolator == null) {
            interpolator = AnimUtils.getFastOutSlowInInterpolator(sceneRoot.getContext());
        }

        final MorphDrawable background = new MorphDrawable(startColor, startCornerRadius);
        endValues.view.setBackground(background);

        final Animator color = ObjectAnimator.ofArgb(background, MorphDrawable.COLOR, endColor);
        final Animator corners =
                ObjectAnimator.ofFloat(background, MorphDrawable.CORNER_RADIUS, endCornerRadius);

        // ease in the dialog's child views (fade in & staggered slide up)
        if (endValues.view instanceof ViewGroup) {
            final ViewGroup vg = (ViewGroup) endValues.view;
            final long duration = getDuration() / 2;
            float offset = vg.getHeight() / 3;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View v = vg.getChildAt(i);
                v.setTranslationY(offset);
                v.setAlpha(0f);
                v.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(duration)
                        .setStartDelay(duration)
                        .setInterpolator(interpolator);
                offset *= 1.8f;
            }
        }

        final AnimatorSet transition = new AnimatorSet();
        transition.playTogether(changeBounds, corners, color);
        transition.setDuration(getDuration());
        transition.setInterpolator(interpolator);
        return transition;
    }

}

package com.creativetrends.simple.app.ui;

import android.view.animation.Interpolator;

import androidx.core.view.animation.PathInterpolatorCompat;

public class BezierEaseInterpolator implements Interpolator {

    private static final Interpolator sBezierInterpolator = PathInterpolatorCompat.create(0.25f, 0.1f, 0.25f, 1f);

    @Override
    public float getInterpolation(float input) {
        return sBezierInterpolator.getInterpolation(input);
    }
}
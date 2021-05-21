package com.creativetrends.simple.app.video;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.util.AttributeSet;

class BubbleTrashLayout extends BubbleBaseLayout {
    public static final int VIBRATION_DURATION_IN_MS = 70;
    private boolean magnetismApplied = false;
    private boolean attachedToWindow = false;
    private boolean isVibrateInThisSession = false;

    public BubbleTrashLayout(Context context) {
        super(context);
    }

    public BubbleTrashLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BubbleTrashLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attachedToWindow = false;
    }

    @Override
    public void setVisibility(int visibility) {
        if (attachedToWindow) {

        }
        super.setVisibility(visibility);
    }

    void applyMagnetism() {
        if (!magnetismApplied) {
            magnetismApplied = true;
        }
    }

    void vibrate() {

    }

    void releaseMagnetism() {
        if (magnetismApplied) {
            magnetismApplied = false;
        }
        isVibrateInThisSession = false;
    }

    private void playAnimation(int animationResourceId) {
        if (!isInEditMode()) {
            AnimatorSet animator = (AnimatorSet) AnimatorInflater
                    .loadAnimator(getContext(), animationResourceId);
            animator.setTarget(getChildAt(0));
            animator.start();
        }
    }
}

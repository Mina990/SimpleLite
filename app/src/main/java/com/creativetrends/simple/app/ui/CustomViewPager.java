package com.creativetrends.simple.app.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

/**
 * Created by Creative Trends Apps (Jorell Rutledge) 5/27/2018.
 */
public class CustomViewPager extends ViewPager {
    private Boolean disable = false;
    public CustomViewPager(Context context) {
        super(context);
    }
    public CustomViewPager(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return !disable && super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            return !disable && super.onTouchEvent(event);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void disableScroll(Boolean disable){
        try {
        this.disable = disable;
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }
}
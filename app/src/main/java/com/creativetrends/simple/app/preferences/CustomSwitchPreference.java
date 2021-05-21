package com.creativetrends.simple.app.preferences;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.creativetrends.simple.app.lite.R;

import static android.text.TextUtils.isEmpty;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by Creative Trends Apps.
 */

public class CustomSwitchPreference extends CheckBoxPreference {

    public CustomSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CustomSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomSwitchPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        setWidgetLayoutResource(R.layout.simple_switchpreference);
    }


    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        try {
            CharSequence title = getTitle();
            TextView titleView = view.findViewById(android.R.id.title);
            titleView.setText(title);
            titleView.setVisibility(!isEmpty(title) ? VISIBLE : GONE);
            titleView.setTextSize(15);
            titleView.setTypeface(titleView.getTypeface(), Typeface.BOLD);

            CharSequence summary = getSummary();
            TextView summaryView = view.findViewById(android.R.id.summary);
            summaryView.setText(summary);
            summaryView.setVisibility(!isEmpty(summary) ? VISIBLE : GONE);
            summaryView.setTextSize(14);

        } catch (NullPointerException ignored) {
        } catch (Exception i) {
            i.printStackTrace();
        }
    }


}
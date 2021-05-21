package com.creativetrends.simple.app.preferences;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

import com.creativetrends.simple.app.lite.R;

import static android.graphics.Color.parseColor;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

final class ThemeUtils {

    // material_deep_teal_500
    private static final int FALLBACK_COLOR = parseColor("#1778F2");

    private ThemeUtils() {
        // no instances
    }

    private static boolean isAtLeastL() {
        return SDK_INT >= LOLLIPOP;
    }

    static int resolveAccentColor(Context context) {

        Resources.Theme theme = context.getTheme();

        int attr = isAtLeastL() ? android.R.attr.colorAccent : R.attr.colorAccent;
        TypedArray typedArray = theme.obtainStyledAttributes(new int[]{attr, R.attr.mp_colorAccent});

        int accentColor = typedArray.getColor(0, FALLBACK_COLOR);
        accentColor = typedArray.getColor(1, accentColor);
        return accentColor;
    }

}

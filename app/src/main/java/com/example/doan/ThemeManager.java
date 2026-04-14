package com.example.doan;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.doan.R;

public final class ThemeManager {

    private static final String PREF = "APP_THEME";
    private static final String KEY = "theme_id";

    public static final String THEME_GREEN = "green";
    public static final String THEME_BLUE = "blue";
    public static final String THEME_PURPLE = "purple";
    public static final String THEME_ORANGE = "orange";
    public static final String THEME_TEAL = "teal";

    private ThemeManager() {}

    public static void applyTheme(Activity activity) {
        activity.setTheme(resolveStyle(activity));
    }

    public static void applyThemeContext(Context ctx) {
        // dùng cho ContextThemeWrapper nếu cần; Activity dùng applyTheme
        if (ctx instanceof Activity) {
            applyTheme((Activity) ctx);
        }
    }

    public static int resolveStyle(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String id = p.getString(KEY, THEME_GREEN);
        return styleForId(id);
    }

    public static int styleForId(String id) {
        if (THEME_BLUE.equals(id)) return R.style.Theme_Doan_Blue;
        if (THEME_PURPLE.equals(id)) return R.style.Theme_Doan_Purple;
        if (THEME_ORANGE.equals(id)) return R.style.Theme_Doan_Orange;
        if (THEME_TEAL.equals(id)) return R.style.Theme_Doan_Teal;
        return R.style.Theme_Doan_Green;
    }

    public static String getThemeId(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY, THEME_GREEN);
    }

    public static void saveTheme(Context ctx, String themeId) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY, themeId).apply();
    }

    public static String[] themeIds() {
        return new String[]{THEME_GREEN, THEME_BLUE, THEME_PURPLE, THEME_ORANGE, THEME_TEAL};
    }

    public static String labelVi(String id) {
        if (THEME_BLUE.equals(id)) return "Xanh dương";
        if (THEME_PURPLE.equals(id)) return "Tím";
        if (THEME_ORANGE.equals(id)) return "Cam";
        if (THEME_TEAL.equals(id)) return "Ngọc";
        return "Xanh lá";
    }
}

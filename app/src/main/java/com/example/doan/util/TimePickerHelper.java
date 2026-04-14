package com.example.doan.util;

import androidx.fragment.app.FragmentActivity;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Locale;

public final class TimePickerHelper {

    public interface OnTimePicked {
        void onTime(int hour, int minute);
    }

    private TimePickerHelper() {}

    public static void showPick24h(
            FragmentActivity act,
            String title,
            int hour,
            int minute,
            OnTimePicked callback) {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText(title)
                .build();
        picker.addOnPositiveButtonClickListener(v -> {
            if (callback != null) {
                callback.onTime(picker.getHour(), picker.getMinute());
            }
        });
        picker.show(act.getSupportFragmentManager(), "pick_time_" + title.hashCode());
    }

    public static String formatHHmm(int hour, int minute) {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    }

    /** Mặc định 14:00 nếu không parse được. */
    public static int[] parseHHmm(String s) {
        if (s == null || s.trim().isEmpty()) {
            return new int[]{14, 0};
        }
        try {
            String t = s.trim();
            int colon = t.indexOf(':');
            if (colon <= 0) {
                return new int[]{14, 0};
            }
            int h = Integer.parseInt(t.substring(0, colon).trim());
            int m = Integer.parseInt(t.substring(colon + 1).trim());
            h = Math.max(0, Math.min(23, h));
            m = Math.max(0, Math.min(59, m));
            return new int[]{h, m};
        } catch (Exception e) {
            return new int[]{14, 0};
        }
    }
}

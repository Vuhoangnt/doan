package com.example.doan.util;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Chọn ngày theo lịch Material — không cho chọn ngày trước {@code minUtcMillis}.
 */
public final class DatePickerHelper {

    public interface OnDatePicked {
        void onDatePicked(long selectionUtcMillis);
    }

    private DatePickerHelper() {}

    /** 00:00 hôm nay theo múi giờ máy, dạng UTC millis (tương thích MaterialDatePicker). */
    public static long startOfTodayUtcMillis() {
        return MaterialDatePicker.todayInUtcMilliseconds();
    }

    /** Định dạng yyyy-MM-dd theo giờ địa phương. */
    public static String formatYmdLocal(long selectionUtcMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(selectionUtcMillis));
    }

    public static void showPickDate(
            FragmentActivity activity,
            String title,
            long minUtcMillis,
            Long initialUtcMillis,
            OnDatePicked callback) {
        showPickDate(activity, title, minUtcMillis, initialUtcMillis, null, callback);
    }

    /**
     * @param openAtUtcMillis tháng mở khi hiện lịch (bất kỳ ngày trong tháng). Giúp khách không phải lật từ
     *                        tháng hiện tại khi đã chọn ngày nhận / trả trước đó.
     */
    public static void showPickDate(
            FragmentActivity activity,
            String title,
            long minUtcMillis,
            Long initialUtcMillis,
            @Nullable Long openAtUtcMillis,
            OnDatePicked callback) {
        long sel = initialUtcMillis != null ? initialUtcMillis : minUtcMillis;
        if (sel < minUtcMillis) {
            sel = minUtcMillis;
        }
        long openAt = openAtUtcMillis != null ? Math.max(minUtcMillis, openAtUtcMillis) : sel;
        long endUtc = minUtcMillis + 730L * 24L * 60L * 60L * 1000L;
        if (openAt > endUtc) {
            openAt = endUtc;
        }
        if (sel > endUtc) {
            sel = endUtc;
        }

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setStart(minUtcMillis)
                .setEnd(endUtc)
                .setOpenAt(openAt)
                .setValidator(DateValidatorPointForward.from(minUtcMillis))
                .build();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(title)
                .setCalendarConstraints(constraints)
                .setSelection(sel)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (callback != null) {
                callback.onDatePicked(selection);
            }
        });
        picker.show(activity.getSupportFragmentManager(), "pick_date_" + title);
    }
}

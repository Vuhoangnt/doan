package com.example.doan.util;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

/**
 * Chọn ngày theo lịch Material — không cho chọn ngày trước {@code minUtcMillis}.
 */
public final class DatePickerHelper {

    public interface OnDatePicked {
        void onDatePicked(long selectionUtcMillis);
    }
    public interface OnDateRangePicked {
        void onDateRangePicked(long startUtcMillis, long endUtcMillis);
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
        showPickDate(activity, title, minUtcMillis, initialUtcMillis, openAtUtcMillis, null, callback);
    }

    public static void showPickDate(
            FragmentActivity activity,
            String title,
            long minUtcMillis,
            Long initialUtcMillis,
            @Nullable Long openAtUtcMillis,
            @Nullable Set<Long> disabledUtcDays,
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

        List<CalendarConstraints.DateValidator> validators = new ArrayList<>();
        validators.add(DateValidatorPointForward.from(minUtcMillis));
        if (disabledUtcDays != null && !disabledUtcDays.isEmpty()) {
            validators.add(new DisabledUtcDaysValidator(disabledUtcDays));
        }

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setStart(minUtcMillis)
                .setEnd(endUtc)
                .setOpenAt(openAt)
                .setValidator(CompositeDateValidator.allOf(validators))
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

    public static void showPickDateRange(
            FragmentActivity activity,
            String title,
            long minUtcMillis,
            @Nullable Pair<Long, Long> initialRange,
            @Nullable Long openAtUtcMillis,
            @Nullable Set<Long> disabledUtcDays,
            OnDateRangePicked callback) {
        long start = minUtcMillis;
        long end = minUtcMillis + 24L * 60L * 60L * 1000L;
        if (initialRange != null && initialRange.first != null && initialRange.second != null) {
            start = Math.max(minUtcMillis, initialRange.first);
            end = Math.max(start, initialRange.second);
        }
        long openAt = openAtUtcMillis != null ? Math.max(minUtcMillis, openAtUtcMillis) : start;
        long endUtc = minUtcMillis + 730L * 24L * 60L * 60L * 1000L;
        if (openAt > endUtc) {
            openAt = endUtc;
        }
        if (start > endUtc) {
            start = endUtc;
        }
        if (end > endUtc) {
            end = endUtc;
        }

        List<CalendarConstraints.DateValidator> validators = new ArrayList<>();
        validators.add(DateValidatorPointForward.from(minUtcMillis));
        if (disabledUtcDays != null && !disabledUtcDays.isEmpty()) {
            validators.add(new DisabledUtcDaysValidator(disabledUtcDays));
        }

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setStart(minUtcMillis)
                .setEnd(endUtc)
                .setOpenAt(openAt)
                .setValidator(CompositeDateValidator.allOf(validators))
                .build();

        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(title)
                .setCalendarConstraints(constraints)
                .setSelection(Pair.create(start, end))
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (callback != null && selection != null
                    && selection.first != null && selection.second != null) {
                callback.onDateRangePicked(selection.first, selection.second);
            }
        });
        picker.show(activity.getSupportFragmentManager(), "pick_range_" + title);
    }

    /** Validator loại trừ danh sách ngày UTC (00:00). */
    private static final class DisabledUtcDaysValidator implements CalendarConstraints.DateValidator {
        private final HashSet<Long> disabledDays;

        DisabledUtcDaysValidator(Set<Long> disabledDays) {
            this.disabledDays = new HashSet<>(disabledDays);
        }

        DisabledUtcDaysValidator(Parcel in) {
            long[] arr = in.createLongArray();
            this.disabledDays = new HashSet<>();
            if (arr != null) {
                for (long v : arr) {
                    this.disabledDays.add(v);
                }
            }
        }

        @Override
        public boolean isValid(long date) {
            return !disabledDays.contains(date);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            long[] arr = new long[disabledDays.size()];
            int i = 0;
            for (Long v : disabledDays) {
                arr[i++] = v != null ? v : 0L;
            }
            dest.writeLongArray(arr);
        }

        public static final Parcelable.Creator<DisabledUtcDaysValidator> CREATOR =
                new Parcelable.Creator<DisabledUtcDaysValidator>() {
                    @Override
                    public DisabledUtcDaysValidator createFromParcel(Parcel source) {
                        return new DisabledUtcDaysValidator(source);
                    }

                    @Override
                    public DisabledUtcDaysValidator[] newArray(int size) {
                        return new DisabledUtcDaysValidator[size];
                    }
                };
    }
}

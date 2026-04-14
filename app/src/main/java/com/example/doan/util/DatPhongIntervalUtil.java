package com.example.doan.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Chuẩn hóa khoảng lưu trú [nhận, trả) theo múi giờ máy — dùng để phát hiện trùng lịch phòng.
 */
public final class DatPhongIntervalUtil {

    private DatPhongIntervalUtil() {}

    /**
     * @return thời điểm millis, hoặc {@link Long#MIN_VALUE} nếu không parse được
     */
    public static long parseYmdHmToMillis(String ymd, String hhmm) {
        if (ymd == null || ymd.trim().isEmpty()) {
            return Long.MIN_VALUE;
        }
        int[] hm = TimePickerHelper.parseHHmm(hhmm);
        String t = TimePickerHelper.formatHHmm(hm[0], hm[1]);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        sdf.setLenient(false);
        sdf.setTimeZone(TimeZone.getDefault());
        try {
            Date d = sdf.parse(ymd.trim() + " " + t);
            return d != null ? d.getTime() : Long.MIN_VALUE;
        } catch (ParseException e) {
            return Long.MIN_VALUE;
        }
    }

    /**
     * Khoảng [start, end) nửa mở: phòng trả đúng giờ trả thì khoảng tiếp theo có thể bắt đầu cùng mốc đó.
     *
     * @return {@code [startMs, endMs]} hoặc null nếu dữ liệu sai hoặc end &lt;= start
     */
    public static long[] tryStayIntervalHalfOpen(String ngayNhan, String ngayTra, String gioNhan, String gioTra) {
        if (ngayNhan == null || ngayTra == null) {
            return null;
        }
        long start = parseYmdHmToMillis(ngayNhan, gioNhan);
        long end = parseYmdHmToMillis(ngayTra, gioTra);
        if (start == Long.MIN_VALUE || end == Long.MIN_VALUE) {
            return null;
        }
        if (end <= start) {
            return null;
        }
        return new long[]{start, end};
    }

    /** Hai khoảng [a0,a1) và [b0,b1) giao nhau. */
    public static boolean intervalsOverlapHalfOpen(long a0, long a1, long b0, long b1) {
        return a0 < b1 && b0 < a1;
    }
}

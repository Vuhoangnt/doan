package com.example.doan.util;

import com.example.doan.model.PhongFull;

/**
 * Giá theo đêm: nếu giờ nhận hoặc giờ trả nằm trong khung giờ cao điểm (và phòng có {@code GiaCaoDiem > 0})
 * thì áp dụng {@code GiaCaoDiem}/đêm; ngược lại {@code GiaNgay}/đêm.
 */
public final class PeakPricingUtil {

    private PeakPricingUtil() {}

    public static boolean isInPeakWindow(String hhmm, String tu, String den) {
        if (tu == null || den == null || tu.trim().isEmpty() || den.trim().isEmpty()) {
            return false;
        }
        if (hhmm == null || hhmm.trim().isEmpty()) {
            return false;
        }
        int t = toMinutes(hhmm);
        int a = toMinutes(tu);
        int b = toMinutes(den);
        if (a <= b) {
            return t >= a && t <= b;
        }
        return t >= a || t <= b;
    }

    private static int toMinutes(String s) {
        String[] p = s.trim().split(":");
        int h = Integer.parseInt(p[0].trim());
        int m = p.length > 1 ? Integer.parseInt(p[1].trim()) : 0;
        return h * 60 + m;
    }

    /** Đơn giá một đêm (đã xét giờ cao điểm). */
    public static double demGiaTheoGio(PhongFull p, String gioNhan, String gioTra) {
        double base = p.getGiaNgay();
        if (p.getGiaCaoDiem() <= 0) {
            return base;
        }
        String tu = p.getGioCaoDiemTu();
        String den = p.getGioCaoDiemDen();
        if (tu == null || den == null || tu.trim().isEmpty() || den.trim().isEmpty()) {
            return base;
        }
        boolean peak = isInPeakWindow(gioNhan, tu, den) || isInPeakWindow(gioTra, tu, den);
        return peak ? p.getGiaCaoDiem() : base;
    }
}

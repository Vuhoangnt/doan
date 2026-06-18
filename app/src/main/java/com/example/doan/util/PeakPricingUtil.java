package com.example.doan.util;

import androidx.annotation.Nullable;

import com.example.doan.model.PhongFull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

/**
 * Tính đơn giá một đêm theo cơ chế ưu tiên:
 * <ol>
 *   <li>Ngày lễ (yyyy-MM-dd có trong {@code ngayLeSet}) → {@code GiaNgay * heSoLe}</li>
 *   <li>Giờ cao điểm (giờ nhận HOẶC giờ trả nằm trong khung) → {@code max(GiaCaoDiem, GiaNgay * heSoCaoDiem)}</li>
 *   <li>Còn lại → {@code GiaNgay}</li>
 * </ol>
 * Ngày lễ luôn thắng giờ cao điểm (theo cấu hình).
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

    /**
     * Hệ số nhân giá ngày áp dụng cho đêm đó. Trả về {@code 1.0} nếu không có ngoại lệ.
     */
    public static double heSoChoDem(@Nullable PhongFull p, String ngayYmd,
                                    @Nullable String gioNhan, @Nullable String gioTra,
                                    @Nullable Set<String> ngayLeSet,
                                    double heSoLeMacDinh) {
        if (p == null) {
            return 1.0;
        }
        if (ngayYmd != null && ngayLeSet != null && ngayLeSet.contains(ngayYmd)) {
            return heSoLeMacDinh > 0 ? heSoLeMacDinh : 1.5;
        }
        if (coGioCaoDiem(p, gioNhan, gioTra)) {
            return p.getHeSoCaoDiem() > 0 ? p.getHeSoCaoDiem() : 1.0;
        }
        return 1.0;
    }

    /**
     * Đơn giá cho 1 đêm (chỉ giờ cao điểm, không tính lễ) — giữ tương thích ngược.
     */
    public static double demGiaTheoGio(PhongFull p, String gioNhan, String gioTra) {
        return demGiaMotDem(p, gioNhan, gioTra, null, null, 1.0);
    }

    /**
     * Đơn giá cho 1 đêm, có xét ngày lễ.
     *
     * @param ngayYmd    ngày của đêm đang tính (yyyy-MM-dd); null nếu không có
     * @param ngayLeSet  tập ngày lễ của phòng; null = không có
     * @param heSoLeMacDinh hệ số lễ dùng khi ngày đó thuộc {@code ngayLeSet}
     */
    public static double demGiaMotDem(@Nullable PhongFull p,
                                      @Nullable String gioNhan, @Nullable String gioTra,
                                      @Nullable String ngayYmd,
                                      @Nullable Set<String> ngayLeSet,
                                      double heSoLeMacDinh) {
        return demGiaMotDem(p, gioNhan, gioTra, ngayYmd, ngayLeSet, heSoLeMacDinh, null);
    }

    /**
     * Đơn giá cho 1 đêm, có xét ngày lễ với hệ số riêng mỗi ngày.
     *
     * @param ngayLeHesoMap map ngày lễ → hệ số nhân (từ {@code PhongGiaLeDAO.getNgayLeMapByPhongId});
     *                      null = dùng {@code heSoLeMacDinh} cho mọi ngày lễ
     */
    public static double demGiaMotDem(@Nullable PhongFull p,
                                      @Nullable String gioNhan, @Nullable String gioTra,
                                      @Nullable String ngayYmd,
                                      @Nullable Set<String> ngayLeSet,
                                      double heSoLeMacDinh,
                                      @Nullable java.util.Map<String, Double> ngayLeHesoMap) {
        if (p == null) {
            return 0;
        }
        double base = p.getGiaNgay();
        if (base <= 0) {
            return 0;
        }
        // 1) Ngày lễ thắng
        if (ngayYmd != null && ngayLeSet != null && ngayLeSet.contains(ngayYmd)) {
            double hs = heSoLeMacDinh;
            if (ngayLeHesoMap != null && ngayLeHesoMap.containsKey(ngayYmd)) {
                hs = ngayLeHesoMap.get(ngayYmd);
            } else if (hs <= 0) {
                hs = 1.5;
            }
            return base * hs;
        }
        // 2) Giờ cao điểm
        if (coGioCaoDiem(p, gioNhan, gioTra)) {
            double nhan = base * p.getHeSoCaoDiem();
            if (p.getGiaCaoDiem() > 0) {
                return Math.max(nhan, p.getGiaCaoDiem());
            }
            return nhan;
        }
        // 3) Bình thường
        return base;
    }

    /** Tổng tiền phòng cho cả khoảng [ngayNhan, ngayTra) — tính từng đêm, áp giá lễ riêng nếu có. */
    public static double tongTienPhong(@Nullable PhongFull p,
                                       @Nullable String ngayNhanYmd, @Nullable String ngayTraYmd,
                                       @Nullable String gioNhan, @Nullable String gioTra,
                                       @Nullable Set<String> ngayLeSet,
                                       double heSoLeMacDinh) {
        return tongTienPhong(p, ngayNhanYmd, ngayTraYmd, gioNhan, gioTra,
                ngayLeSet, heSoLeMacDinh, null);
    }

    /**
     * Tổng tiền phòng cho cả khoảng [ngayNhan, ngayTra), áp hệ số riêng từng ngày lễ.
     */
    public static double tongTienPhong(@Nullable PhongFull p,
                                       @Nullable String ngayNhanYmd, @Nullable String ngayTraYmd,
                                       @Nullable String gioNhan, @Nullable String gioTra,
                                       @Nullable Set<String> ngayLeSet,
                                       double heSoLeMacDinh,
                                       @Nullable java.util.Map<String, Double> ngayLeHesoMap) {
        if (p == null || ngayNhanYmd == null || ngayTraYmd == null) {
            return 0;
        }
        int soDem = soDemGiuaNgay(ngayNhanYmd, ngayTraYmd);
        if (soDem <= 0) {
            return 0;
        }
        double tong = 0;
        for (int i = 0; i < soDem; i++) {
            String ngayDem = congNgay(ngayNhanYmd, i);
            tong += demGiaMotDem(p, gioNhan, gioTra, ngayDem, ngayLeSet, heSoLeMacDinh, ngayLeHesoMap);
        }
        return tong;
    }

    /** Tương thích ngược: gọi khi không có thông tin ngày lễ (1 đêm hoặc tất cả các đêm cùng giá). */
    public static double tongTienPhong(@Nullable PhongFull p,
                                       @Nullable String ngayNhanYmd, @Nullable String ngayTraYmd,
                                       @Nullable String gioNhan, @Nullable String gioTra) {
        return tongTienPhong(p, ngayNhanYmd, ngayTraYmd, gioNhan, gioTra,
                Collections.<String>emptySet(), 1.0);
    }

    private static boolean coGioCaoDiem(PhongFull p, String gioNhan, String gioTra) {
        // Đã cấu hình giá cao điểm (GiaCaoDiem > 0) HOẶC hệ số > 1.0 thì mới xét.
        boolean hasPeakConfig = p.getGiaCaoDiem() > 0 || p.getHeSoCaoDiem() > 1.0;
        if (!hasPeakConfig) {
            return false;
        }
        String tu = p.getGioCaoDiemTu();
        String den = p.getGioCaoDiemDen();
        if (tu == null || den == null || tu.trim().isEmpty() || den.trim().isEmpty()) {
            return false;
        }
        return isInPeakWindow(gioNhan, tu, den) || isInPeakWindow(gioTra, tu, den);
    }

    private static int soDemGiuaNgay(String a, String b) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setLenient(false);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d1 = sdf.parse(a);
            Date d2 = sdf.parse(b);
            if (d1 == null || d2 == null) return -1;
            long diff = d2.getTime() - d1.getTime();
            int days = (int) (diff / (24L * 60L * 60L * 1000L));
            return Math.max(1, days);
        } catch (Exception e) {
            return -1;
        }
    }

    private static String congNgay(String ymd, int offsetDays) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setLenient(false);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = sdf.parse(ymd);
            if (d == null) return ymd;
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
            c.setTime(d);
            c.add(Calendar.DAY_OF_MONTH, offsetDays);
            return sdf.format(c.getTime());
        } catch (Exception e) {
            return ymd;
        }
    }

    /** Tập rỗng — dùng như giá trị mặc định an toàn. */
    public static Set<String> emptySet() {
        return new HashSet<>();
    }
}

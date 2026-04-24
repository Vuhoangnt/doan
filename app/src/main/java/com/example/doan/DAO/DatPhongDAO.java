package com.example.doan.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.doan.DatabaseHelper;
import com.example.doan.model.DatPhong;
import com.example.doan.model.PhongFull;
import com.example.doan.model.ThanhToan;
import com.example.doan.util.DatPhongIntervalUtil;
import com.example.doan.util.PeakPricingUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DatPhongDAO {

    /** {@link #insertIfNoOverlap(DatPhong)} — phòng đã có đơn chồng lên khoảng thời gian này. */
    public static final long INSERT_OVERLAP = -2L;
    /** Ngày/giờ nhận–trả không tạo khoảng thời gian hợp lệ (end &lt;= start hoặc parse lỗi). */
    public static final long INSERT_INVALID_STAY = -3L;

    /** Khách vừa gửi yêu cầu — chờ homestay duyệt. */
    public static final String TT_CHO_XAC_NHAN = "Chờ xác nhận";
    /** Homestay đã nhận đơn / xác nhận giữ chỗ (trước khi nhận phòng). */
    public static final String TT_DA_XAC_NHAN = "Đã xác nhận";
    /** Khách đang lưu trú. */
    public static final String TT_DANG_O = "Đang ở";
    /** Đơn ở trạng thái này mới tính là khách đã trả phòng — được phép đánh giá phòng tương ứng. */
    public static final String TT_DA_TRA_PHONG = "Đã trả phòng";
    public static final String TT_DA_HUY = "Đã hủy";

    /** Tên cũ trong DB mẫu — map về {@link #TT_DA_XAC_NHAN}. */
    private static final String LEGACY_DA_DAT = "Đã đặt";

    private final DatabaseHelper dbHelper;
    private final Context appContext;

    public DatPhongDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        appContext = context.getApplicationContext();
    }

    private static int queryPhongId(SQLiteDatabase db, int datPhongId) {
        try (Cursor c = db.rawQuery(
                "SELECT PhongID FROM DatPhong WHERE DatPhongID=?",
                new String[]{String.valueOf(datPhongId)})) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        }
        return -1;
    }

    private static String queryTenPhong(SQLiteDatabase db, int phongId) {
        try (Cursor c = db.rawQuery(
                "SELECT TenPhong FROM Phong WHERE PhongID=?",
                new String[]{String.valueOf(phongId)})) {
            if (c.moveToFirst()) {
                return c.getString(0);
            }
        }
        return "";
    }

    /** Một đơn kèm tên phòng (JOIN) — dùng trước khi cập nhật / thông báo. */
    public DatPhong getByIdWithTenPhong(int datPhongId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                SQL_JOIN + "WHERE D.DatPhongID=? LIMIT 1",
                new String[]{String.valueOf(datPhongId)})) {
            if (c.moveToFirst()) {
                DatPhong d = cursorToDatPhong(c);
                mapJoinColumns(c, d);
                return d;
            }
        }
        return null;
    }

    /** Thứ tự tab / lọc đơn. */
    public static String[] allOrderStatuses() {
        return new String[]{
                TT_CHO_XAC_NHAN,
                TT_DA_XAC_NHAN,
                TT_DANG_O,
                TT_DA_TRA_PHONG,
                TT_DA_HUY
        };
    }

    /** Đơn đã kết thúc / hủy — không giữ phòng, cho phép đặt chồng khung giờ. */
    public static boolean isRoomReleasedStatus(String raw) {
        String n = normalizeStatus(raw);
        return TT_DA_HUY.equals(n) || TT_DA_TRA_PHONG.equals(n);
    }

    /** Chuẩn hóa để so sánh (gộp tên cũ "Đã đặt"). */
    public static String normalizeStatus(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim();
        if (LEGACY_DA_DAT.equals(s)) {
            return TT_DA_XAC_NHAN;
        }
        return s;
    }

    /**
     * Bước chuyển trạng thái thực tế thường dùng (không tính sửa sai).
     * Đã trả phòng / Đã hủy: danh sách rỗng — dùng chọn đầy đủ nếu cần chỉnh.
     */
    public static List<String> allowedNextStatuses(String current) {
        String c = normalizeStatus(current);
        if (TT_CHO_XAC_NHAN.equals(c)) {
            return Arrays.asList(TT_DA_XAC_NHAN, TT_DA_HUY);
        }
        if (TT_DA_XAC_NHAN.equals(c)) {
            return Arrays.asList(TT_DANG_O, TT_DA_HUY);
        }
        if (TT_DANG_O.equals(c)) {
            return Collections.singletonList(TT_DA_TRA_PHONG);
        }
        if (TT_DA_TRA_PHONG.equals(c) || TT_DA_HUY.equals(c)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(allOrderStatuses()));
    }

    private static DatPhong cursorToDatPhong(Cursor c) {
        DatPhong d = new DatPhong();
        d.setDatPhongID(c.getInt(c.getColumnIndexOrThrow("DatPhongID")));
        d.setMaDatPhong(c.getString(c.getColumnIndexOrThrow("MaDatPhong")));
        if (!c.isNull(c.getColumnIndexOrThrow("TaiKhoanID"))) {
            d.setTaiKhoanID(c.getInt(c.getColumnIndexOrThrow("TaiKhoanID")));
        }
        d.setPhongID(c.getInt(c.getColumnIndexOrThrow("PhongID")));
        d.setNgayNhan(c.getString(c.getColumnIndexOrThrow("NgayNhan")));
        d.setNgayTra(c.getString(c.getColumnIndexOrThrow("NgayTra")));
        d.setSoDem(c.getInt(c.getColumnIndexOrThrow("SoDem")));
        d.setTongTien(c.getDouble(c.getColumnIndexOrThrow("TongTien")));
        d.setTrangThai(c.getString(c.getColumnIndexOrThrow("TrangThai")));
        d.setKhachTen(c.getString(c.getColumnIndexOrThrow("KhachTen")));
        d.setKhachEmail(c.getString(c.getColumnIndexOrThrow("KhachEmail")));
        d.setKhachDienThoai(c.getString(c.getColumnIndexOrThrow("KhachDienThoai")));
        d.setKhachCccd(c.getString(c.getColumnIndexOrThrow("KhachCCCD")));
        d.setSoNguoi(c.getInt(c.getColumnIndexOrThrow("SoNguoi")));
        d.setGhiChu(c.getString(c.getColumnIndexOrThrow("GhiChu")));
        d.setNgayTao(c.getString(c.getColumnIndexOrThrow("NgayTao")));
        d.setLoaiDat(c.getString(c.getColumnIndexOrThrow("LoaiDat")));
        int idxNv = c.getColumnIndex("NhanVienXuLyID");
        if (idxNv >= 0 && !c.isNull(idxNv)) {
            d.setNhanVienXuLyID(c.getInt(idxNv));
        }
        int idxGn = c.getColumnIndex("GioNhan");
        if (idxGn >= 0 && !c.isNull(idxGn)) {
            d.setGioNhan(c.getString(idxGn));
        }
        int idxGt = c.getColumnIndex("GioTra");
        if (idxGt >= 0 && !c.isNull(idxGt)) {
            d.setGioTra(c.getString(idxGt));
        }
        return d;
    }

    private static ContentValues toValues(DatPhong d) {
        ContentValues v = new ContentValues();
        v.put("MaDatPhong", d.getMaDatPhong());
        if (d.getTaiKhoanID() != null) {
            v.put("TaiKhoanID", d.getTaiKhoanID());
        } else {
            v.putNull("TaiKhoanID");
        }
        v.put("PhongID", d.getPhongID());
        v.put("NgayNhan", d.getNgayNhan());
        v.put("NgayTra", d.getNgayTra());
        v.put("SoDem", d.getSoDem());
        v.put("TongTien", d.getTongTien());
        v.put("TrangThai", d.getTrangThai());
        v.put("KhachTen", d.getKhachTen());
        v.put("KhachEmail", d.getKhachEmail());
        v.put("KhachDienThoai", d.getKhachDienThoai());
        v.put("KhachCCCD", d.getKhachCccd());
        v.put("SoNguoi", d.getSoNguoi());
        v.put("GhiChu", d.getGhiChu());
        v.put("NgayTao", d.getNgayTao());
        v.put("LoaiDat", d.getLoaiDat());
        if (d.getNhanVienXuLyID() != null) {
            v.put("NhanVienXuLyID", d.getNhanVienXuLyID());
        } else {
            v.putNull("NhanVienXuLyID");
        }
        v.put("GioNhan", d.getGioNhan() != null ? d.getGioNhan() : "14:00");
        v.put("GioTra", d.getGioTra() != null ? d.getGioTra() : "12:00");
        return v;
    }

    public long insert(DatPhong d) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert("DatPhong", null, toValues(d));
    }

    /**
     * Chèn đơn nếu không trùng khoảng [nhận,trả) với đơn cùng phòng còn hiệu lực (trừ đã hủy / đã trả phòng).
     * Gọi trong transaction để tránh hai luồng đặt cùng slot.
     *
     * @return row id nếu thành công, {@code -1} lỗi SQLite, {@link #INSERT_OVERLAP} nếu trùng lịch
     */
    public long insertIfNoOverlap(DatPhong d) {
        if (DatPhongIntervalUtil.tryStayIntervalHalfOpen(
                d.getNgayNhan(), d.getNgayTra(), d.getGioNhan(), d.getGioTra()) == null) {
            return INSERT_INVALID_STAY;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = -1;
        db.beginTransaction();
        try {
            if (hasOverlap(
                    db,
                    d.getPhongID(),
                    d.getNgayNhan(),
                    d.getNgayTra(),
                    d.getGioNhan(),
                    d.getGioTra(),
                    null)) {
                return INSERT_OVERLAP;
            }
            rowId = db.insert("DatPhong", null, toValues(d));
            if (rowId != -1) {
                db.setTransactionSuccessful();
            }
        } finally {
            db.endTransaction();
        }
        if (rowId != -1 && rowId != INSERT_OVERLAP) {
            new PhongDAO(appContext).syncTrangThaiPhongTheoDon(d.getPhongID());
            String tenPhong = queryTenPhong(db, d.getPhongID());
            ThongBaoDAO.notifyNewBooking(appContext, (int) rowId, d.getPhongID(), d.getMaDatPhong(), tenPhong);
        }
        return rowId;
    }

    /**
     * Kiểm tra trùng lịch (đọc DB hiện tại). Dùng cho giao diện hoặc kiểm tra trước khi ghi.
     *
     * @param excludeDatPhongId bỏ qua đơn này (sửa đơn — hiện app chưa dùng)
     */
    public boolean hasOverlap(
            int phongId,
            String ngayNhan,
            String ngayTra,
            String gioNhan,
            String gioTra,
            Integer excludeDatPhongId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return hasOverlap(db, phongId, ngayNhan, ngayTra, gioNhan, gioTra, excludeDatPhongId);
    }

    private static boolean hasOverlap(
            SQLiteDatabase db,
            int phongId,
            String ngayNhan,
            String ngayTra,
            String gioNhan,
            String gioTra,
            Integer excludeDatPhongId) {
        long[] want = DatPhongIntervalUtil.tryStayIntervalHalfOpen(ngayNhan, ngayTra, gioNhan, gioTra);
        if (want == null) {
            return false;
        }
        long w0 = want[0];
        long w1 = want[1];
        try (Cursor c = db.rawQuery(
                "SELECT DatPhongID, NgayNhan, NgayTra, GioNhan, GioTra, TrangThai FROM DatPhong WHERE PhongID=?",
                new String[]{String.valueOf(phongId)})) {
            if (!c.moveToFirst()) {
                return false;
            }
            do {
                int id = c.getInt(c.getColumnIndexOrThrow("DatPhongID"));
                if (excludeDatPhongId != null && id == excludeDatPhongId) {
                    continue;
                }
                String tt = c.getString(c.getColumnIndexOrThrow("TrangThai"));
                if (isRoomReleasedStatus(tt)) {
                    continue;
                }
                String on = c.getString(c.getColumnIndexOrThrow("NgayNhan"));
                String ot = c.getString(c.getColumnIndexOrThrow("NgayTra"));
                int idxGn = c.getColumnIndex("GioNhan");
                int idxGt = c.getColumnIndex("GioTra");
                String gn = (idxGn >= 0 && !c.isNull(idxGn)) ? c.getString(idxGn) : null;
                String gt = (idxGt >= 0 && !c.isNull(idxGt)) ? c.getString(idxGt) : null;
                long[] ex = DatPhongIntervalUtil.tryStayIntervalHalfOpen(on, ot, gn, gt);
                if (ex == null) {
                    continue;
                }
                if (DatPhongIntervalUtil.intervalsOverlapHalfOpen(w0, w1, ex[0], ex[1])) {
                    return true;
                }
            } while (c.moveToNext());
        }
        return false;
    }

    /** Ghi dịch vụ thêm cho đơn đặt (mỗi dòng một loại, số lượng tối thiểu 1). */
    public void insertDatPhongDichVu(int datPhongId, int dichVuId, int soLuong) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("DatPhongID", datPhongId);
        v.put("DichVuID", dichVuId);
        v.put("SoLuong", Math.max(1, soLuong));
        db.insert("DatPhong_DichVu", null, v);
    }

    public List<DatPhong> getAll() {
        List<DatPhong> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM DatPhong ORDER BY NgayTao DESC", null)) {
            if (c.moveToFirst()) {
                do {
                    list.add(cursorToDatPhong(c));
                } while (c.moveToNext());
            }
        }
        return list;
    }

    /**
     * Đơn đang giữ lịch phòng (chờ xác nhận / đã xác nhận / đang ở) — không gồm đã trả / đã hủy.
     * Dùng khi khách xem lịch trước khi đặt.
     */
    public List<DatPhong> getActiveBookingsByPhongId(int phongId) {
        List<DatPhong> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT * FROM DatPhong WHERE PhongID=? ORDER BY NgayNhan ASC, DatPhongID ASC",
                new String[]{String.valueOf(phongId)})) {
            if (c.moveToFirst()) {
                do {
                    DatPhong d = cursorToDatPhong(c);
                    if (!isRoomReleasedStatus(d.getTrangThai())) {
                        out.add(d);
                    }
                } while (c.moveToNext());
            }
        }
        return out;
    }

    public DatPhong getByMaDatPhong(String ma) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT * FROM DatPhong WHERE MaDatPhong=?",
                new String[]{ma})) {
            if (c.moveToFirst()) {
                return cursorToDatPhong(c);
            }
        }
        return null;
    }

    private static void mapJoinColumns(Cursor c, DatPhong d) {
        int idx = c.getColumnIndex("TenPhongPhong");
        if (idx >= 0 && !c.isNull(idx)) {
            d.setTenPhong(c.getString(idx));
        }
        int idxNv = c.getColumnIndex("TenNVPhong");
        if (idxNv >= 0 && !c.isNull(idxNv)) {
            d.setTenNhanVienXuLy(c.getString(idxNv));
        }
    }

    private static final String SQL_JOIN =
            "SELECT D.*, P.TenPhong AS TenPhongPhong, NV.TenNguoiDung AS TenNVPhong "
                    + "FROM DatPhong D "
                    + "LEFT JOIN Phong P ON D.PhongID = P.PhongID "
                    + "LEFT JOIN TaiKhoan NV ON D.NhanVienXuLyID = NV.TaiKhoanID ";

    public List<DatPhong> getByTaiKhoanID(int taiKhoanId) {
        List<DatPhong> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                SQL_JOIN + "WHERE D.TaiKhoanID=? ORDER BY D.NgayTao DESC",
                new String[]{String.valueOf(taiKhoanId)})) {
            if (c.moveToFirst()) {
                do {
                    DatPhong d = cursorToDatPhong(c);
                    mapJoinColumns(c, d);
                    list.add(d);
                } while (c.moveToNext());
            }
        }
        return list;
    }

    public List<DatPhong> getAllWithTenPhong() {
        List<DatPhong> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                SQL_JOIN + "ORDER BY D.NgayTao DESC",
                null)) {
            if (c.moveToFirst()) {
                do {
                    DatPhong d = cursorToDatPhong(c);
                    mapJoinColumns(c, d);
                    list.add(d);
                } while (c.moveToNext());
            }
        }
        return list;
    }

    /** Gắn đơn với tài khoản khách sau khi tự tạo / tìm theo SĐT. */
    public int updateGanTaiKhoanKhach(int datPhongId, int taiKhoanId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("TaiKhoanID", taiKhoanId);
        v.put("LoaiDat", "tai_khoan");
        return db.update("DatPhong", v, "DatPhongID=?",
                new String[]{String.valueOf(datPhongId)});
    }

    public int updateTrangThai(int datPhongId, String trangThai) {
        return updateTrangThaiVaNhanVien(datPhongId, trangThai, null);
    }

    /**
     * Cập nhật trạng thái; nếu {@code nhanVienXuLyId} khác null thì ghi nhận người xác nhận (admin / nhân viên).
     */
    public int updateTrangThaiVaNhanVien(int datPhongId, String trangThai, Integer nhanVienXuLyId) {
        DatPhong before = getByIdWithTenPhong(datPhongId);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int phongId = queryPhongId(db, datPhongId);
        ContentValues v = new ContentValues();
        v.put("TrangThai", trangThai);
        if (nhanVienXuLyId != null) {
            v.put("NhanVienXuLyID", nhanVienXuLyId);
        }
        int n = db.update("DatPhong", v, "DatPhongID=?", new String[]{String.valueOf(datPhongId)});
        if (n > 0 && phongId > 0) {
            new PhongDAO(appContext).syncTrangThaiPhongTheoDon(phongId);
        }
        if (n > 0 && before != null) {
            String oldN = normalizeStatus(before.getTrangThai());
            String newN = normalizeStatus(trangThai);
            if (!oldN.equals(newN)) {
                ThongBaoDAO.notifyOrderStatusChanged(appContext, before, trangThai);
            }
        }
        return n;
    }

    /**
     * Khách (tài khoản) đã có ít nhất một đơn cùng phòng và trạng thái {@link #TT_DA_TRA_PHONG}.
     */
    public boolean hasCompletedStayForRoom(int taiKhoanId, int phongId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT 1 FROM DatPhong WHERE TaiKhoanID=? AND PhongID=? AND TrangThai=? LIMIT 1",
                new String[]{String.valueOf(taiKhoanId), String.valueOf(phongId), TT_DA_TRA_PHONG})) {
            return c.moveToFirst();
        }
    }

    /** Khách đã trả phòng ít nhất một lần (dùng cho đánh giá chung homestay). */
    public boolean hasAnyCompletedStay(int taiKhoanId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT 1 FROM DatPhong WHERE TaiKhoanID=? AND TrangThai=? LIMIT 1",
                new String[]{String.valueOf(taiKhoanId), TT_DA_TRA_PHONG})) {
            return c.moveToFirst();
        }
    }

    /**
     * Đơn đang ở nhưng đã qua ngày trả (theo ngày trên đơn) — dùng nhắc admin đóng đơn.
     */
    public List<DatPhong> listDangOWithNgayTraBeforeToday() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<DatPhong> list = new ArrayList<>();
        try (Cursor c = db.rawQuery(
                SQL_JOIN + "WHERE D.TrangThai=? AND date(D.NgayTra) < date('now','localtime') ORDER BY D.NgayTra",
                new String[]{TT_DANG_O})) {
            while (c.moveToNext()) {
                DatPhong d = cursorToDatPhong(c);
                mapJoinColumns(c, d);
                list.add(d);
            }
        }
        return list;
    }

    /**
     * Khách tự xác nhận trả phòng khi đã thanh toán đủ và đơn đang ở.
     *
     * @return số dòng cập nhật (&gt;0 thành công), {@code -1} chưa thanh toán đủ, {@code -2} không hợp lệ
     */
    public int guestSelfCheckout(int datPhongId, int taiKhoanKhachId) {
        DatPhong d = getByIdWithTenPhong(datPhongId);
        if (d == null || d.getTaiKhoanID() == null || d.getTaiKhoanID() != taiKhoanKhachId) {
            return -2;
        }
        if (!TT_DANG_O.equals(normalizeStatus(d.getTrangThai()))) {
            return -2;
        }

        return -2; // deprecated: dùng guestRequestCheckout()
    }

    /** Tổng tiền dịch vụ thêm của 1 đơn (DatPhong_DichVu x DichVu). */
    private double getTongTienDichVu(int datPhongId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT IFNULL(SUM(DV.Gia * DP.SoLuong),0) "
                        + "FROM DatPhong_DichVu DP "
                        + "JOIN DichVu DV ON DV.DichVuID = DP.DichVuID "
                        + "WHERE DP.DatPhongID=?",
                new String[]{String.valueOf(datPhongId)})) {
            if (c.moveToFirst()) {
                return c.getDouble(0);
            }
        }
        return 0;
    }

    /** Tổng tiền dịch vụ thêm (public — dùng cho màn hình tất toán của khách). */
    public double getTongTienDichVuPublic(int datPhongId) {
        return getTongTienDichVu(datPhongId);
    }

    /**
     * Trả phòng sớm: nếu {@code actualNgayTraYmd} sớm hơn ngày trả trên đơn thì cập nhật:
     * - SoDem = số đêm thực ở
     * - NgayTra = actualNgayTraYmd
     * - TongTien = (tiền phòng theo số đêm ở) + 20% * (tiền phòng của số đêm còn lại) + dịch vụ (giữ nguyên)
     *
     * @return số dòng cập nhật (>0 nếu có cập nhật)
     */
    public int applyCheckoutAdjustmentIfNeeded(int datPhongId, String actualNgayTraYmd) {
        DatPhong d = getByIdWithTenPhong(datPhongId);
        if (d == null) {
            return 0;
        }
        if (!TT_DANG_O.equals(normalizeStatus(d.getTrangThai()))) {
            return 0;
        }
        if (actualNgayTraYmd == null || actualNgayTraYmd.trim().isEmpty()) {
            return 0;
        }
        String planned = d.getNgayTra();
        if (planned == null || planned.trim().isEmpty()) {
            return 0;
        }

        int booked = Math.max(1, d.getSoDem());
        int actual = computeSoDem(d.getNgayNhan(), actualNgayTraYmd);
        if (actual < 1) {
            return 0;
        }

        PhongFull p = new PhongDAO(appContext).getPhongFullById(d.getPhongID());
        if (p == null) {
            return 0;
        }
        String gioNhan = d.getGioNhan() != null ? d.getGioNhan() : "14:00";
        String gioTra = d.getGioTra() != null ? d.getGioTra() : "12:00";
        double giaDem = PeakPricingUtil.demGiaTheoGio(p, gioNhan, gioTra);
        double tongDv = getTongTienDichVu(datPhongId);
        double tongMoi;
        int soDemMoi;

        // So sánh yyyy-MM-dd bằng string là an toàn khi đúng format.
        if (actualNgayTraYmd.compareTo(planned) < 0) {
            // Trả sớm: thu actual đêm + 20% của số đêm còn lại.
            if (actual >= booked) {
                return 0;
            }
            int remaining = booked - actual;
            double tongPhong = giaDem * actual;
            double phiTraSom = giaDem * remaining * 0.2;
            tongMoi = tongPhong + phiTraSom + tongDv;
            soDemMoi = actual;
        } else if (actualNgayTraYmd.compareTo(planned) > 0) {
            // Trả muộn: cộng thêm tiền các đêm vượt.
            int extra = computeSoDem(planned, actualNgayTraYmd);
            if (extra < 1) {
                return 0;
            }
            soDemMoi = booked + extra;
            tongMoi = (giaDem * soDemMoi) + tongDv;
        } else {
            return 0; // đúng ngày, không chỉnh
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("NgayTra", actualNgayTraYmd);
        v.put("SoDem", soDemMoi);
        v.put("TongTien", tongMoi);
        return db.update("DatPhong", v, "DatPhongID=?",
                new String[]{String.valueOf(datPhongId)});
    }

    private static int computeSoDem(String d1, String d2) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setLenient(false);
            java.util.Date a = sdf.parse(d1);
            java.util.Date b = sdf.parse(d2);
            if (a == null || b == null) return -1;
            if (b.before(a)) return -1;
            long diff = b.getTime() - a.getTime();
            int days = (int) java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff);
            return Math.max(1, days);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Khách bấm "Trả phòng" trên app:
     * - Tự tính lại tổng tiền nếu trả sớm/muộn (ngày thực tế = hôm nay).
     * - Nếu còn thiếu tiền, tạo 1 giao dịch "Chưa thanh toán" (phương thức chuyển khoản) để NV xác nhận khi nhận tiền.
     *
     * @return 1 nếu đã ghi nhận yêu cầu
     */
    public int guestRequestCheckout(int datPhongId, int taiKhoanKhachId) {
        DatPhong d = getByIdWithTenPhong(datPhongId);
        if (d == null || d.getTaiKhoanID() == null || d.getTaiKhoanID() != taiKhoanKhachId) {
            return -2;
        }
        if (!TT_DANG_O.equals(normalizeStatus(d.getTrangThai()))) {
            return -2;
        }
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new java.util.Date());
        applyCheckoutAdjustmentIfNeeded(datPhongId, today);
        d = getByIdWithTenPhong(datPhongId);
        if (d == null) {
            return -2;
        }
        ThanhToanDAO ttDao = new ThanhToanDAO(appContext);
        double daThu = ttDao.getTongDaThu(datPhongId);
        double conThieu = Math.max(0, d.getTongTien() - daThu);
        if (conThieu > 1.0) {
            String ngay = today;
            ThanhToan t = new ThanhToan();
            t.setDatPhongID(datPhongId);
            t.setSoTien(conThieu);
            t.setPhuongThuc("Chuyển khoản ngân hàng");
            t.setNgayThanhToan(ngay);
            t.setTrangThai(ThanhToanDAO.TT_CHUA_THANH_TOAN);
            t.setNhanVienGhiNhanID(null);
            ttDao.insert(t);
        }
        return 1;
    }
}

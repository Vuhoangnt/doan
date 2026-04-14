package com.example.doan.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.doan.DatabaseHelper;
import com.example.doan.SessionManager;
import com.example.doan.model.DatPhong;
import com.example.doan.model.ThongBao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ThongBaoDAO {

    /** Nhắc admin / NV: đơn vẫn Đang ở sau ngày trả. */
    public static final String TIEU_DE_NHAC_TRA_PHONG = "Nhắc: Quá hạn trả phòng";

    public static final String DOI_TUONG_ADMIN = "admin";
    public static final String DOI_TUONG_NHAN_VIEN = "nhan_vien";
    public static final String DOI_TUONG_KHACH = "khach";

    public static final String HANH_DONG_MO_QL_DON = "mo_ql_don";
    public static final String HANH_DONG_MO_DON_CUA_TOI = "mo_don_cua_toi";

    private final DatabaseHelper dbHelper;

    public ThongBaoDAO(Context context) {
        dbHelper = new DatabaseHelper(context.getApplicationContext());
    }

    private static String nowIso() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
    }

    private static void insertRow(
            SQLiteDatabase db,
            String doiTuong,
            Integer taiKhoanNhanId,
            String tieuDe,
            String noiDung,
            Integer datPhongId,
            Integer phongId,
            String hanhDong) {
        ContentValues v = new ContentValues();
        v.put("DoiTuongNhan", doiTuong);
        if (taiKhoanNhanId != null) {
            v.put("TaiKhoanNhanID", taiKhoanNhanId);
        } else {
            v.putNull("TaiKhoanNhanID");
        }
        v.put("TieuDe", tieuDe);
        v.put("NoiDung", noiDung);
        v.put("NgayTao", nowIso());
        v.put("DaDoc", 0);
        if (datPhongId != null) {
            v.put("DatPhongID", datPhongId);
        } else {
            v.putNull("DatPhongID");
        }
        if (phongId != null) {
            v.put("PhongID", phongId);
        } else {
            v.putNull("PhongID");
        }
        if (hanhDong != null) {
            v.put("HanhDong", hanhDong);
        } else {
            v.putNull("HanhDong");
        }
        db.insert("ThongBao", null, v);
    }

    /** Đơn mới — admin + toàn bộ nhân viên nhận thông báo, mở màn quản lý đơn. */
    public static void notifyNewBooking(Context ctx, int datPhongId, int phongId, String maDat, String tenPhong) {
        SQLiteDatabase db = new DatabaseHelper(ctx.getApplicationContext()).getWritableDatabase();
        String tp = tenPhong != null && !tenPhong.isEmpty() ? tenPhong : ("#" + phongId);
        String title = "Đơn đặt phòng mới";
        String body = "Mã " + maDat + " — " + tp + ". Chạm để xác nhận / xử lý.";
        insertRow(db, DOI_TUONG_ADMIN, null, title, body, datPhongId, phongId, HANH_DONG_MO_QL_DON);
        insertRow(db, DOI_TUONG_NHAN_VIEN, null, title, body, datPhongId, phongId, HANH_DONG_MO_QL_DON);
    }

    /** Đổi trạng thái đơn — admin xem mọi sự kiện; khách có tài khoản nhận riêng. */
    public static void notifyOrderStatusChanged(Context ctx, DatPhong don, String trangThaiMoi) {
        if (don == null) {
            return;
        }
        SQLiteDatabase db = new DatabaseHelper(ctx.getApplicationContext()).getWritableDatabase();
        String ma = don.getMaDatPhong();
        String tp = don.getTenPhong();
        if (tp == null || tp.isEmpty()) {
            tp = "Phòng #" + don.getPhongID();
        }
        String tt = DatPhongDAO.normalizeStatus(trangThaiMoi);
        String title = "Cập nhật đơn " + ma;
        String bodyAd = tp + " — trạng thái: " + tt;
        insertRow(db, DOI_TUONG_ADMIN, null, title, bodyAd, don.getDatPhongID(), don.getPhongID(), HANH_DONG_MO_QL_DON);

        if (don.getTaiKhoanID() != null && don.getTaiKhoanID() > 0) {
            String bodyKh = "Đơn " + ma + " (" + tp + ") hiện là: " + tt;
            insertRow(db, DOI_TUONG_KHACH, don.getTaiKhoanID(), "Trạng thái đặt phòng", bodyKh,
                    don.getDatPhongID(), don.getPhongID(), HANH_DONG_MO_DON_CUA_TOI);
        }
    }

    private static ThongBao cursorTo(Cursor c) {
        ThongBao t = new ThongBao();
        t.setThongBaoID(c.getInt(c.getColumnIndexOrThrow("ThongBaoID")));
        t.setDoiTuongNhan(c.getString(c.getColumnIndexOrThrow("DoiTuongNhan")));
        int idxTk = c.getColumnIndex("TaiKhoanNhanID");
        if (idxTk >= 0 && !c.isNull(idxTk)) {
            t.setTaiKhoanNhanID(c.getInt(idxTk));
        }
        t.setTieuDe(c.getString(c.getColumnIndexOrThrow("TieuDe")));
        t.setNoiDung(c.getString(c.getColumnIndexOrThrow("NoiDung")));
        t.setNgayTao(c.getString(c.getColumnIndexOrThrow("NgayTao")));
        t.setDaDoc(c.getInt(c.getColumnIndexOrThrow("DaDoc")) != 0);
        int idxDp = c.getColumnIndex("DatPhongID");
        if (idxDp >= 0 && !c.isNull(idxDp)) {
            t.setDatPhongID(c.getInt(idxDp));
        }
        int idxP = c.getColumnIndex("PhongID");
        if (idxP >= 0 && !c.isNull(idxP)) {
            t.setPhongID(c.getInt(idxP));
        }
        int idxH = c.getColumnIndex("HanhDong");
        if (idxH >= 0 && !c.isNull(idxH)) {
            t.setHanhDong(c.getString(idxH));
        }
        return t;
    }

    public List<ThongBao> listForSession(SessionManager session) {
        List<ThongBao> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql;
        String[] args;
        if (!session.isLoggedIn()) {
            return list;
        }
        if (session.isAdmin()) {
            sql = "SELECT * FROM ThongBao ORDER BY ThongBaoID DESC";
            args = null;
        } else if (session.isNhanVien()) {
            sql = "SELECT * FROM ThongBao WHERE DoiTuongNhan=? ORDER BY ThongBaoID DESC";
            args = new String[]{DOI_TUONG_NHAN_VIEN};
        } else if (session.isKhach()) {
            sql = "SELECT * FROM ThongBao WHERE DoiTuongNhan=? AND TaiKhoanNhanID=? ORDER BY ThongBaoID DESC";
            args = new String[]{DOI_TUONG_KHACH, String.valueOf(session.getTaiKhoanId())};
        } else {
            return list;
        }
        try (Cursor c = db.rawQuery(sql, args)) {
            if (c.moveToFirst()) {
                do {
                    list.add(cursorTo(c));
                } while (c.moveToNext());
            }
        }
        return list;
    }

    public void markRead(int thongBaoId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("DaDoc", 1);
        db.update("ThongBao", v, "ThongBaoID=?", new String[]{String.valueOf(thongBaoId)});
    }

    /** Đánh dấu đã đọc toàn bộ thông báo hiển thị với vai trò hiện tại (khi mở danh sách). */
    public void markAllReadForSession(SessionManager session) {
        if (!session.isLoggedIn()) {
            return;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("DaDoc", 1);
        if (session.isAdmin()) {
            db.update("ThongBao", v, "DaDoc=0", null);
        } else if (session.isNhanVien()) {
            db.update("ThongBao", v, "DaDoc=0 AND DoiTuongNhan=?", new String[]{DOI_TUONG_NHAN_VIEN});
        } else if (session.isKhach()) {
            db.update("ThongBao", v, "DaDoc=0 AND DoiTuongNhan=? AND TaiKhoanNhanID=?",
                    new String[]{DOI_TUONG_KHACH, String.valueOf(session.getTaiKhoanId())});
        }
    }

    public int countUnread(SessionManager session) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql;
        String[] args;
        if (!session.isLoggedIn()) {
            return 0;
        }
        if (session.isAdmin()) {
            sql = "SELECT COUNT(*) FROM ThongBao WHERE DaDoc=0";
            args = null;
        } else if (session.isNhanVien()) {
            sql = "SELECT COUNT(*) FROM ThongBao WHERE DaDoc=0 AND DoiTuongNhan=?";
            args = new String[]{DOI_TUONG_NHAN_VIEN};
        } else if (session.isKhach()) {
            sql = "SELECT COUNT(*) FROM ThongBao WHERE DaDoc=0 AND DoiTuongNhan=? AND TaiKhoanNhanID=?";
            args = new String[]{DOI_TUONG_KHACH, String.valueOf(session.getTaiKhoanId())};
        } else {
            return 0;
        }
        try (Cursor c = db.rawQuery(sql, args)) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        }
        return 0;
    }

    /**
     * Tạo thông báo cho admin + nhân viên nếu có đơn Đang ở quá ngày trả;
     * tránh trùng trong 24 giờ cho cùng một đơn.
     */
    public static void notifyOverdueStaysIfNeeded(Context ctx) {
        DatPhongDAO dpDao = new DatPhongDAO(ctx.getApplicationContext());
        List<DatPhong> overdue = dpDao.listDangOWithNgayTraBeforeToday();
        if (overdue.isEmpty()) {
            return;
        }
        SQLiteDatabase db = new DatabaseHelper(ctx.getApplicationContext()).getWritableDatabase();
        for (DatPhong d : overdue) {
            try (Cursor c = db.rawQuery(
                    "SELECT 1 FROM ThongBao WHERE DatPhongID=? AND TieuDe=? AND NgayTao >= datetime('now','-1 day') LIMIT 1",
                    new String[]{String.valueOf(d.getDatPhongID()), TIEU_DE_NHAC_TRA_PHONG})) {
                if (c.moveToFirst()) {
                    continue;
                }
            }
            String tp = d.getTenPhong();
            if (tp == null || tp.isEmpty()) {
                tp = "Phòng #" + d.getPhongID();
            }
            String body = "Mã " + d.getMaDatPhong() + " — " + tp + ". Ngày trả " + d.getNgayTra()
                    + " nhưng vẫn Đang ở — cập nhật trạng thái hoặc xác nhận khách đã trả.";
            insertRow(db, DOI_TUONG_ADMIN, null, TIEU_DE_NHAC_TRA_PHONG, body, d.getDatPhongID(), d.getPhongID(), HANH_DONG_MO_QL_DON);
            insertRow(db, DOI_TUONG_NHAN_VIEN, null, TIEU_DE_NHAC_TRA_PHONG, body, d.getDatPhongID(), d.getPhongID(), HANH_DONG_MO_QL_DON);
        }
    }
}

package com.example.doan.DAO;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.doan.DatabaseHelper;
import com.example.doan.model.ChartPoint;
import com.example.doan.model.ThongKeTongQuan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ThongKeDAO {

    private final DatabaseHelper dbHelper;

    public ThongKeDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    private static int scalarInt(SQLiteDatabase db, String sql, String[] args) {
        try (Cursor c = db.rawQuery(sql, args)) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        }
        return 0;
    }

    private static double scalarDouble(SQLiteDatabase db, String sql, String[] args) {
        try (Cursor c = db.rawQuery(sql, args)) {
            if (c.moveToFirst()) {
                return c.getDouble(0);
            }
        }
        return 0;
    }

    public ThongKeTongQuan layTongQuan() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ThongKeTongQuan t = new ThongKeTongQuan();

        t.tongPhong = scalarInt(db, "SELECT COUNT(*) FROM Phong", null);
        // Phòng đang có khách: theo đơn (khớp QL đặt phòng); phòng trống theo bảng Phong sau khi đồng bộ.
        t.phongDangO = scalarInt(db,
                "SELECT COUNT(DISTINCT PhongID) FROM DatPhong WHERE TrangThai=?",
                new String[]{DatPhongDAO.TT_DANG_O});
        t.phongTrong = scalarInt(db, "SELECT COUNT(*) FROM Phong WHERE TrangThai=?", new String[]{"Trống"});

        t.tongDonDat = scalarInt(db, "SELECT COUNT(*) FROM DatPhong", null);
        t.donChoXacNhan = scalarInt(db, "SELECT COUNT(*) FROM DatPhong WHERE TrangThai=?", new String[]{"Chờ xác nhận"});
        t.donDaDat = scalarInt(db, "SELECT COUNT(*) FROM DatPhong WHERE TrangThai=? OR TrangThai=?",
                new String[]{DatPhongDAO.TT_DA_XAC_NHAN, "Đã đặt"});
        t.donDangO = scalarInt(db, "SELECT COUNT(*) FROM DatPhong WHERE TrangThai=?", new String[]{"Đang ở"});

        t.tongDoanhThuUocTinh = scalarDouble(db, "SELECT IFNULL(SUM(TongTien),0) FROM DatPhong", null);

        t.soTaiKhoanKhach = scalarInt(db, "SELECT COUNT(*) FROM TaiKhoan WHERE Role=?", new String[]{"khach"});
        t.soTaiKhoanNhanVien = scalarInt(db, "SELECT COUNT(*) FROM TaiKhoan WHERE Role=?", new String[]{"nhanvien"});

        t.soTinTuc = scalarInt(db, "SELECT COUNT(*) FROM TinTuc", null);
        t.soDichVu = scalarInt(db, "SELECT COUNT(*) FROM DichVu", null);

        return t;
    }

    /** 12 cột: T1…T12 — tổng TongTien theo NgayTao trong năm. */
    public List<ChartPoint> tongTienTheoThangTrongNam(int year) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<ChartPoint> list = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            String prefix = String.format(Locale.US, "%04d-%02d", year, m);
            double sum = scalarDouble(db,
                    "SELECT IFNULL(SUM(TongTien),0) FROM DatPhong WHERE NgayTao IS NOT NULL AND NgayTao LIKE ?",
                    new String[]{prefix + "%"});
            list.add(new ChartPoint("T" + m, (float) sum));
        }
        return list;
    }

    /** Mỗi ngày trong tháng — nhãn 1,2,… */
    public List<ChartPoint> tongTienTheoNgayTrongThang(int year, int month) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        int max = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        List<ChartPoint> list = new ArrayList<>();
        for (int d = 1; d <= max; d++) {
            String day = String.format(Locale.US, "%04d-%02d-%02d", year, month, d);
            double sum = scalarDouble(db,
                    "SELECT IFNULL(SUM(TongTien),0) FROM DatPhong WHERE NgayTao=?",
                    new String[]{day});
            list.add(new ChartPoint(String.valueOf(d), (float) sum));
        }
        return list;
    }

    /** Mỗi năm trong khoảng [fromYear, toYear]. */
    public List<ChartPoint> tongTienTheoNam(int fromYear, int toYear) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<ChartPoint> list = new ArrayList<>();
        for (int y = fromYear; y <= toYear; y++) {
            String like = String.format(Locale.US, "%04d", y) + "-%";
            double sum = scalarDouble(db,
                    "SELECT IFNULL(SUM(TongTien),0) FROM DatPhong WHERE NgayTao IS NOT NULL AND NgayTao LIKE ?",
                    new String[]{like});
            list.add(new ChartPoint(String.valueOf(y), (float) sum));
        }
        return list;
    }
}

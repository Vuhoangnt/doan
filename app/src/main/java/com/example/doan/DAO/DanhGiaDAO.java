package com.example.doan.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.doan.DatabaseHelper;
import com.example.doan.model.DanhGia;

import java.util.ArrayList;
import java.util.List;

public class DanhGiaDAO {

    public static final String DUYET_DA = "da_duyet";
    public static final String DUYET_CHO = "cho_duyet";
    public static final String DUYET_TU_CHOI = "tu_choi";

    private final DatabaseHelper dbHelper;

    public DanhGiaDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public boolean insert(DanhGia d) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        if (d.getTaiKhoanID() != null) {
            v.put("TaiKhoanID", d.getTaiKhoanID());
        } else {
            v.putNull("TaiKhoanID");
        }
        v.put("TenHienThi", d.getTenHienThi());
        v.put("SoSao", d.getSoSao());
        v.put("NoiDung", d.getNoiDung());
        v.put("NgayTao", d.getNgayTao());
        if (d.getPhongID() != null) {
            v.put("PhongID", d.getPhongID());
        } else {
            v.putNull("PhongID");
        }
        String tt = d.getTrangThaiDuyet();
        if (tt == null || tt.isEmpty()) {
            tt = DUYET_DA;
        }
        v.put("TrangThaiDuyet", tt);
        return db.insert("DanhGia", null, v) != -1;
    }

    /** Đánh giá đã duyệt, gắn phòng (mới nhất trước). */
    public List<DanhGia> getByPhongId(int phongId) {
        List<DanhGia> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT DanhGiaID, TaiKhoanID, TenHienThi, SoSao, NoiDung, NgayTao, PhongID, TrangThaiDuyet " +
                        "FROM DanhGia WHERE PhongID=? AND TrangThaiDuyet=? ORDER BY DanhGiaID DESC LIMIT 40",
                new String[]{String.valueOf(phongId), DUYET_DA})) {
            if (c.moveToFirst()) {
                do {
                    list.add(row(c));
                } while (c.moveToNext());
            }
        }
        return list;
    }

    /** Danh sách công khai: chỉ đã duyệt. */
    public List<DanhGia> getApprovedNewestFirst() {
        List<DanhGia> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT DanhGiaID, TaiKhoanID, TenHienThi, SoSao, NoiDung, NgayTao, PhongID, TrangThaiDuyet " +
                        "FROM DanhGia WHERE TrangThaiDuyet=? ORDER BY DanhGiaID DESC",
                new String[]{DUYET_DA})) {
            if (c.moveToFirst()) {
                do {
                    list.add(row(c));
                } while (c.moveToNext());
            }
        }
        return list;
    }

    /** Admin: bình luận chờ duyệt (mới trước). */
    public List<DanhGia> getPendingNewestFirst() {
        List<DanhGia> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT DanhGiaID, TaiKhoanID, TenHienThi, SoSao, NoiDung, NgayTao, PhongID, TrangThaiDuyet " +
                        "FROM DanhGia WHERE TrangThaiDuyet=? ORDER BY DanhGiaID DESC",
                new String[]{DUYET_CHO})) {
            if (c.moveToFirst()) {
                do {
                    list.add(row(c));
                } while (c.moveToNext());
            }
        }
        return list;
    }

    public boolean updateTrangThaiDuyet(int danhGiaId, String trangThai) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("TrangThaiDuyet", trangThai);
        return db.update("DanhGia", v, "DanhGiaID=?", new String[]{String.valueOf(danhGiaId)}) > 0;
    }

    /** Trang chủ: đánh giá đã duyệt, giới hạn số dòng. */
    public List<DanhGia> getApprovedNewestFirstLimit(int limit) {
        int n = Math.max(1, Math.min(50, limit));
        List<DanhGia> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT DanhGiaID, TaiKhoanID, TenHienThi, SoSao, NoiDung, NgayTao, PhongID, TrangThaiDuyet " +
                        "FROM DanhGia WHERE TrangThaiDuyet=? ORDER BY DanhGiaID DESC LIMIT " + n,
                new String[]{DUYET_DA})) {
            if (c.moveToFirst()) {
                do {
                    list.add(row(c));
                } while (c.moveToNext());
            }
        }
        return list;
    }

    private static DanhGia row(Cursor c) {
        DanhGia d = new DanhGia();
        d.setDanhGiaID(c.getInt(0));
        if (!c.isNull(1)) {
            d.setTaiKhoanID(c.getInt(1));
        }
        d.setTenHienThi(c.getString(2));
        d.setSoSao(c.getInt(3));
        d.setNoiDung(c.getString(4));
        d.setNgayTao(c.getString(5));
        if (!c.isNull(6)) {
            d.setPhongID(c.getInt(6));
        }
        if (c.getColumnCount() > 7 && !c.isNull(7)) {
            d.setTrangThaiDuyet(c.getString(7));
        } else {
            d.setTrangThaiDuyet(DUYET_DA);
        }
        return d;
    }
}

package com.example.doan.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.doan.DatabaseHelper;
import com.example.doan.model.PhongGiaLe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PhongGiaLeDAO {

    private final DatabaseHelper dbHelper;

    public PhongGiaLeDAO(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * Lấy danh sách ngày lễ của một phòng, sắp theo ngày tăng dần.
     */
    public List<PhongGiaLe> getByPhongId(int phongId) {
        List<PhongGiaLe> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT ID, PhongID, NgayLe, HeSoNhan, IFNULL(GhiChu,'') " +
                        "FROM Phong_GiaLe WHERE PhongID=? ORDER BY NgayLe ASC",
                new String[]{String.valueOf(phongId)})) {
            if (c.moveToFirst()) {
                do {
                    PhongGiaLe g = new PhongGiaLe();
                    g.setId(c.getLong(0));
                    g.setPhongID(c.getInt(1));
                    g.setNgayLe(c.getString(2));
                    g.setHeSoNhan(c.getDouble(3));
                    g.setGhiChu(c.getString(4));
                    out.add(g);
                } while (c.moveToNext());
            }
        }
        return out;
    }

    /**
     * Tập ngày lễ (yyyy-MM-dd) của một phòng — dùng nhanh khi tính giá từng đêm.
     */
    public Set<String> getNgayLeSetByPhongId(int phongId) {
        Set<String> out = new HashSet<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT NgayLe FROM Phong_GiaLe WHERE PhongID=?",
                new String[]{String.valueOf(phongId)})) {
            if (c.moveToFirst()) {
                do {
                    out.add(c.getString(0));
                } while (c.moveToNext());
            }
        }
        return out;
    }

    /**
     * Map ngày lễ → hệ số nhân (yyyy-MM-dd → heSo). Dùng thay thế getNgayLeSetByPhongId
     * để tính giá chính xác theo hệ số riêng mỗi ngày.
     */
    public java.util.Map<String, Double> getNgayLeMapByPhongId(int phongId) {
        java.util.Map<String, Double> out = new java.util.HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT NgayLe, HeSoNhan FROM Phong_GiaLe WHERE PhongID=?",
                new String[]{String.valueOf(phongId)})) {
            if (c.moveToFirst()) {
                do {
                    String ngay = c.getString(0);
                    double hs = c.getDouble(1);
                    out.put(ngay, hs > 0 ? hs : 1.0);
                } while (c.moveToNext());
            }
        }
        return out;
    }

    /**
     * Thêm / cập nhật một ngày lễ cho phòng. UNIQUE(PhongID, NgayLe) nên INSERT OR REPLACE
     * sẽ tự ghi đè nếu trùng ngày.
     *
     * @return rowId (-1 nếu lỗi)
     */
    public long upsert(PhongGiaLe g) {
        if (g == null || g.getPhongID() <= 0 || g.getNgayLe() == null || g.getNgayLe().trim().isEmpty()) {
            return -1;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("PhongID", g.getPhongID());
        v.put("NgayLe", g.getNgayLe().trim());
        v.put("HeSoNhan", g.getHeSoNhan() > 0 ? g.getHeSoNhan() : 1.5);
        v.put("GhiChu", g.getGhiChu() != null ? g.getGhiChu() : "");
        return db.insertWithOnConflict(
                "Phong_GiaLe", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public int deleteById(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("Phong_GiaLe", "ID=?", new String[]{String.valueOf(id)});
    }

    public int deleteByPhongAndNgay(int phongId, String ngayLe) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("Phong_GiaLe", "PhongID=? AND NgayLe=?",
                new String[]{String.valueOf(phongId), ngayLe});
    }

    /**
     * Lấy hệ số nhân cho 1 phòng + 1 ngày; 1.0 nếu ngày đó không phải ngày lễ.
     */
    public double getHeSoNhanFor(int phongId, String ngayYmd) {
        if (ngayYmd == null || ngayYmd.trim().isEmpty()) {
            return 1.0;
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT HeSoNhan FROM Phong_GiaLe WHERE PhongID=? AND NgayLe=? LIMIT 1",
                new String[]{String.valueOf(phongId), ngayYmd.trim()})) {
            if (c.moveToFirst()) {
                double v = c.getDouble(0);
                return v > 0 ? v : 1.0;
            }
        }
        return 1.0;
    }
}

package com.example.doan.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.doan.DatabaseHelper;
import com.example.doan.model.TinTuc;

import java.util.ArrayList;
import java.util.List;

public class TinTucDAO {

    private final DatabaseHelper dbHelper;

    public TinTucDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public boolean insert(TinTuc t) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("TieuDe", t.getTieuDe());
        v.put("NoiDung", t.getNoiDung());
        v.put("NgayDang", t.getNgayDang());
        return db.insert("TinTuc", null, v) != -1;
    }

    public boolean update(TinTuc t) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("TieuDe", t.getTieuDe());
        v.put("NoiDung", t.getNoiDung());
        v.put("NgayDang", t.getNgayDang());
        int r = db.update("TinTuc", v, "TinTucID=?", new String[]{String.valueOf(t.getTinTucID())});
        return r > 0;
    }

    public boolean delete(int tinTucId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("TinTuc", "TinTucID=?", new String[]{String.valueOf(tinTucId)}) > 0;
    }

    public List<TinTuc> getAll() {
        List<TinTuc> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM TinTuc ORDER BY NgayDang DESC", null)) {
            if (c.moveToFirst()) {
                do {
                    TinTuc t = new TinTuc();
                    t.setTinTucID(c.getInt(c.getColumnIndexOrThrow("TinTucID")));
                    t.setTieuDe(c.getString(c.getColumnIndexOrThrow("TieuDe")));
                    t.setNoiDung(c.getString(c.getColumnIndexOrThrow("NoiDung")));
                    t.setNgayDang(c.getString(c.getColumnIndexOrThrow("NgayDang")));
                    list.add(t);
                } while (c.moveToNext());
            }
        }
        return list;
    }

    /** Tin mới nhất cho trang chủ (giới hạn số bài). */
    public List<TinTuc> getLatest(int limit) {
        int n = Math.max(1, Math.min(50, limit));
        List<TinTuc> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT TinTucID, TieuDe, NoiDung, NgayDang FROM TinTuc ORDER BY NgayDang DESC LIMIT " + n,
                null)) {
            if (c.moveToFirst()) {
                do {
                    TinTuc t = new TinTuc();
                    t.setTinTucID(c.getInt(c.getColumnIndexOrThrow("TinTucID")));
                    t.setTieuDe(c.getString(c.getColumnIndexOrThrow("TieuDe")));
                    t.setNoiDung(c.getString(c.getColumnIndexOrThrow("NoiDung")));
                    t.setNgayDang(c.getString(c.getColumnIndexOrThrow("NgayDang")));
                    list.add(t);
                } while (c.moveToNext());
            }
        }
        return list;
    }
}

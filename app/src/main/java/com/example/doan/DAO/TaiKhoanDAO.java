package com.example.doan.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.example.doan.DatabaseHelper;
import com.example.doan.model.TaiKhoan;

import java.util.ArrayList;
import java.util.List;

public class TaiKhoanDAO {

    DatabaseHelper dbHelper;

    public TaiKhoanDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    private static void mapRow(Cursor c, TaiKhoan tk) {
        tk.setTaiKhoanID(c.getInt(c.getColumnIndexOrThrow("TaiKhoanID")));
        tk.setTenDangNhap(c.getString(c.getColumnIndexOrThrow("TenDangNhap")));
        tk.setMatKhau(c.getString(c.getColumnIndexOrThrow("MatKhau")));
        tk.setRole(c.getString(c.getColumnIndexOrThrow("Role")));
        tk.setTenNguoiDung(c.getString(c.getColumnIndexOrThrow("TenNguoiDung")));
        tk.setDienThoai(c.getString(c.getColumnIndexOrThrow("DienThoai")));
        tk.setEmail(c.getString(c.getColumnIndexOrThrow("Email")));
        tk.setCccd(c.getString(c.getColumnIndexOrThrow("CCCD")));
        int idx = c.getColumnIndex("AnhDaiDien");
        if (idx >= 0 && !c.isNull(idx)) {
            tk.setAnhDaiDien(c.getString(idx));
        } else {
            tk.setAnhDaiDien("");
        }
    }

    @Nullable
    public TaiKhoan getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM TaiKhoan WHERE TaiKhoanID=?",
                new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) {
                TaiKhoan tk = new TaiKhoan();
                mapRow(c, tk);
                return tk;
            }
        }
        return null;
    }

    public boolean update(TaiKhoan tk) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TenDangNhap", tk.getTenDangNhap());
        values.put("MatKhau", tk.getMatKhau());
        values.put("Role", tk.getRole());
        values.put("TenNguoiDung", tk.getTenNguoiDung());
        values.put("DienThoai", tk.getDienThoai());
        values.put("Email", tk.getEmail());
        values.put("CCCD", tk.getCccd());
        values.put("AnhDaiDien", tk.getAnhDaiDien());
        int r = db.update("TaiKhoan", values, "TaiKhoanID=?",
                new String[]{String.valueOf(tk.getTaiKhoanID())});
        return r > 0;
    }

    public List<TaiKhoan> getByRole(String role) {
        List<TaiKhoan> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM TaiKhoan WHERE Role=? ORDER BY TenDangNhap",
                new String[]{role})) {
            if (c.moveToFirst()) {
                do {
                    TaiKhoan tk = new TaiKhoan();
                    mapRow(c, tk);
                    list.add(tk);
                } while (c.moveToNext());
            }
        }
        return list;
    }

    public List<TaiKhoan> getAll() {
        List<TaiKhoan> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM TaiKhoan", null)) {
            if (c.moveToFirst()) {
                do {
                    TaiKhoan tk = new TaiKhoan();
                    mapRow(c, tk);
                    list.add(tk);
                } while (c.moveToNext());
            }
        }
        return list;
    }

    public static String normalizePhoneDigits(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (Character.isDigit(ch)) {
                b.append(ch);
            }
        }
        return b.toString();
    }

    @Nullable
    public TaiKhoan findKhachByPhoneDigits(String digits) {
        if (digits == null || digits.isEmpty()) {
            return null;
        }
        for (TaiKhoan t : getByRole("khach")) {
            if (digits.equals(normalizePhoneDigits(t.getDienThoai()))) {
                return t;
            }
            if (digits.equals(normalizePhoneDigits(t.getTenDangNhap()))) {
                return t;
            }
        }
        return null;
    }

    public boolean existsTenDangNhap(String tenDangNhap) {
        if (tenDangNhap == null || tenDangNhap.isEmpty()) {
            return true;
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT 1 FROM TaiKhoan WHERE TenDangNhap=? LIMIT 1",
                new String[]{tenDangNhap})) {
            return c.moveToFirst();
        }
    }

    public long insert(TaiKhoan tk) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TenDangNhap", tk.getTenDangNhap());
        values.put("MatKhau", tk.getMatKhau());
        values.put("Role", tk.getRole());
        values.put("TenNguoiDung", tk.getTenNguoiDung());
        values.put("DienThoai", tk.getDienThoai());
        values.put("Email", tk.getEmail());
        values.put("CCCD", tk.getCccd());
        values.put("AnhDaiDien", tk.getAnhDaiDien());
        return db.insert("TaiKhoan", null, values);
    }

    public boolean delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete("TaiKhoan", "TaiKhoanID=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public TaiKhoan checkLogin(String user, String pass) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT * FROM TaiKhoan WHERE TenDangNhap=? AND MatKhau=?",
                new String[]{user, pass})) {
            if (c.moveToFirst()) {
                TaiKhoan tk = new TaiKhoan();
                mapRow(c, tk);
                return tk;
            }
        }
        return null;
    }

    public TaiKhoan login(String user, String pass) {
        return checkLogin(user, pass);
    }
}

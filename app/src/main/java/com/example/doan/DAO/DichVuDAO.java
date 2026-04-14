package com.example.doan.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.doan.DatabaseHelper;
import com.example.doan.model.DichVu;

import java.util.ArrayList;
import java.util.List;

public class DichVuDAO {

    private final DatabaseHelper dbHelper;

    public DichVuDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public List<DichVu> getAll() {
        List<DichVu> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT DichVuID, TenDichVu, Gia, MoTa FROM DichVu ORDER BY TenDichVu COLLATE NOCASE",
                null)) {
            if (c.moveToFirst()) {
                do {
                    list.add(cursorToDichVu(c));
                } while (c.moveToNext());
            }
        }
        return list;
    }

    private static DichVu cursorToDichVu(Cursor c) {
        DichVu d = new DichVu();
        d.setDichVuID(c.getInt(0));
        d.setTenDichVu(c.getString(1));
        d.setGia(c.getDouble(2));
        d.setMoTa(c.isNull(3) ? "" : c.getString(3));
        return d;
    }

    public long insert(DichVu d) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("TenDichVu", d.getTenDichVu());
        v.put("Gia", d.getGia());
        v.put("MoTa", d.getMoTa() != null ? d.getMoTa() : "");
        return db.insert("DichVu", null, v);
    }

    public int update(DichVu d) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("TenDichVu", d.getTenDichVu());
        v.put("Gia", d.getGia());
        v.put("MoTa", d.getMoTa() != null ? d.getMoTa() : "");
        return db.update("DichVu", v, "DichVuID=?",
                new String[]{String.valueOf(d.getDichVuID())});
    }

    /** Xóa dịch vụ và các liên kết phòng / đơn đặt. */
    public int delete(int dichVuId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("Phong_DichVu", "DichVuID=?", new String[]{String.valueOf(dichVuId)});
        db.delete("DatPhong_DichVu", "DichVuID=?", new String[]{String.valueOf(dichVuId)});
        return db.delete("DichVu", "DichVuID=?", new String[]{String.valueOf(dichVuId)});
    }
}

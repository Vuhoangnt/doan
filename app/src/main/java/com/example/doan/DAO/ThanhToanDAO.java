package com.example.doan.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.example.doan.DatabaseHelper;
import com.example.doan.model.ThanhToan;

import java.util.ArrayList;
import java.util.List;

public class ThanhToanDAO {

    public static final String TT_DA_THANH_TOAN = "Đã thanh toán";
    public static final String TT_CHUA_THANH_TOAN = "Chưa thanh toán";

    private final DatabaseHelper dbHelper;

    public ThanhToanDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /** Đơn đã thu đủ (so với tổng tiền đơn, dung sai 1 đồng). */
    public boolean isOrderFullyPaid(int datPhongId, double tongTienDon) {
        return getTongDaThu(datPhongId) + 1.0 >= tongTienDon;
    }

    /** Tổng đã thu (chỉ các dòng đã thanh toán). */
    public double getTongDaThu(int datPhongId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT IFNULL(SUM(SoTien),0) FROM ThanhToan WHERE DatPhongID=? AND TrangThai=?",
                new String[]{String.valueOf(datPhongId), TT_DA_THANH_TOAN})) {
            if (c.moveToFirst()) {
                return c.getDouble(0);
            }
        }
        return 0;
    }

    public List<ThanhToan> getByDatPhongId(int datPhongId) {
        List<ThanhToan> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT * FROM ThanhToan WHERE DatPhongID=? ORDER BY ThanhToanID DESC",
                new String[]{String.valueOf(datPhongId)})) {
            if (c.moveToFirst()) {
                do {
                    list.add(cursorToThanhToan(c));
                } while (c.moveToNext());
            }
        }
        return list;
    }

    /** Lấy giao dịch "Chưa thanh toán" mới nhất của đơn (để xác nhận cọc nhanh). */
    @Nullable
    public Integer getLatestPendingId(int datPhongId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT ThanhToanID FROM ThanhToan WHERE DatPhongID=? AND TrangThai=? ORDER BY ThanhToanID DESC LIMIT 1",
                new String[]{String.valueOf(datPhongId), TT_CHUA_THANH_TOAN})) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        }
        return null;
    }

    private static ThanhToan cursorToThanhToan(Cursor c) {
        ThanhToan t = new ThanhToan();
        t.setThanhToanID(c.getInt(c.getColumnIndexOrThrow("ThanhToanID")));
        t.setDatPhongID(c.getInt(c.getColumnIndexOrThrow("DatPhongID")));
        t.setSoTien(c.getDouble(c.getColumnIndexOrThrow("SoTien")));
        t.setPhuongThuc(c.getString(c.getColumnIndexOrThrow("PhuongThuc")));
        t.setNgayThanhToan(c.getString(c.getColumnIndexOrThrow("NgayThanhToan")));
        t.setTrangThai(c.getString(c.getColumnIndexOrThrow("TrangThai")));
        int idxNv = c.getColumnIndex("NhanVienGhiNhanID");
        if (idxNv >= 0 && !c.isNull(idxNv)) {
            t.setNhanVienGhiNhanID(c.getInt(idxNv));
        }
        return t;
    }

    public long insert(ThanhToan t) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("DatPhongID", t.getDatPhongID());
        v.put("SoTien", t.getSoTien());
        v.put("PhuongThuc", t.getPhuongThuc());
        v.put("NgayThanhToan", t.getNgayThanhToan());
        v.put("TrangThai", t.getTrangThai());
        if (t.getNhanVienGhiNhanID() != null) {
            v.put("NhanVienGhiNhanID", t.getNhanVienGhiNhanID());
        } else {
            v.putNull("NhanVienGhiNhanID");
        }
        return db.insert("ThanhToan", null, v);
    }

    public int updateTrangThai(int thanhToanId, String trangThai) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("TrangThai", trangThai);
        return db.update("ThanhToan", v, "ThanhToanID=?",
                new String[]{String.valueOf(thanhToanId)});
    }

    /** Nhân viên xác nhận đã thu (đổi trạng thái + ghi nhận NV). */
    public int confirmDaThanhToan(int thanhToanId, int nhanVienId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("TrangThai", TT_DA_THANH_TOAN);
        if (nhanVienId > 0) {
            v.put("NhanVienGhiNhanID", nhanVienId);
        } else {
            v.putNull("NhanVienGhiNhanID");
        }
        return db.update("ThanhToan", v, "ThanhToanID=?",
                new String[]{String.valueOf(thanhToanId)});
    }

    /** Xác nhận đã thu và cập nhật phương thức (vd: tiền mặt / chuyển khoản). */
    public int confirmDaThanhToan(int thanhToanId, int nhanVienId, @Nullable String phuongThuc) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("TrangThai", TT_DA_THANH_TOAN);
        if (nhanVienId > 0) {
            v.put("NhanVienGhiNhanID", nhanVienId);
        } else {
            v.putNull("NhanVienGhiNhanID");
        }
        if (phuongThuc != null) {
            v.put("PhuongThuc", phuongThuc);
        }
        return db.update("ThanhToan", v, "ThanhToanID=?",
                new String[]{String.valueOf(thanhToanId)});
    }

    @Nullable
    public String getTenNhanVienGhiNhan(int taiKhoanId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT TenNguoiDung FROM TaiKhoan WHERE TaiKhoanID=?",
                new String[]{String.valueOf(taiKhoanId)})) {
            if (c.moveToFirst()) {
                return c.getString(0);
            }
        }
        return null;
    }
}

package com.example.doan.DAO;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.doan.DatabaseHelper;
import com.example.doan.model.PhongFull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class PhongDAO {

    private static final String ANH_SEP = "|||";

    /** Giá trị cột Phong.TrangThai — đồng bộ với đơn {@code Đang ở}. */
    public static final String PHONG_TRANG_TRONG = "Trống";
    public static final String PHONG_TRANG_DANG_O = "Đang ở";

    DatabaseHelper dbHelper;

    public PhongDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Cập nhật Phong.TrangThai: {@link #PHONG_TRANG_DANG_O} nếu có ít nhất một đơn trạng thái "Đang ở",
     * không thì {@link #PHONG_TRANG_TRONG}.
     */
    public void syncTrangThaiPhongTheoDon(int phongId) {
        if (phongId <= 0) {
            return;
        }
        SQLiteDatabase dbR = dbHelper.getReadableDatabase();
        SQLiteDatabase dbW = dbHelper.getWritableDatabase();
        boolean coDangO;
        try (Cursor c = dbR.rawQuery(
                "SELECT 1 FROM DatPhong WHERE PhongID=? AND TrangThai=? LIMIT 1",
                new String[]{String.valueOf(phongId), PHONG_TRANG_DANG_O})) {
            coDangO = c.moveToFirst();
        }
        if (coDangO) {
            dbW.execSQL("UPDATE Phong SET TrangThai=? WHERE PhongID=?",
                    new Object[]{PHONG_TRANG_DANG_O, phongId});
        } else {
            // Chỉ bỏ "Đang ở" khi hết đơn tương ứng — không đổi Bảo trì / trạng thái khác.
            dbW.execSQL("UPDATE Phong SET TrangThai=? WHERE PhongID=? AND TrangThai=?",
                    new Object[]{PHONG_TRANG_TRONG, phongId, PHONG_TRANG_DANG_O});
        }
    }

    /** Đồng bộ toàn bộ phòng (sửa dữ liệu cũ / sau nhập tay). */
    public void syncAllPhongTrangThaiTheoDon() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT PhongID FROM Phong", null)) {
            while (c.moveToNext()) {
                syncTrangThaiPhongTheoDon(c.getInt(0));
            }
        }
    }

    // ================= LẤY DANH SÁCH PHÒNG (mỗi phòng một dòng) =================
    public List<PhongFull> getAllPhongFull() {
        List<PhongFull> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql =
                "SELECT P.PhongID, P.TenPhong, P.GiaNgay, P.MoTa, P.TrangThai, IFNULL(P.SoNguoiToiDa, 2), "
                        + "IFNULL(P.GiaCaoDiem,0), IFNULL(P.GioCaoDiemTu,''), IFNULL(P.GioCaoDiemDen,''), "
                        + "(SELECT A.UrlAnh FROM AnhPhong A WHERE A.PhongID = P.PhongID ORDER BY A.AnhID LIMIT 1), "
                        + "(SELECT GROUP_CONCAT(A.UrlAnh, '|||') FROM AnhPhong A WHERE A.PhongID = P.PhongID) "
                        + "FROM Phong P";

        try (Cursor c = db.rawQuery(sql, null)) {
            if (c.moveToFirst()) {
                do {
                    PhongFull p = new PhongFull();
                    p.setPhongID(c.getInt(0));
                    p.setTenPhong(c.getString(1));
                    p.setGiaNgay(c.getDouble(2));
                    p.setMoTa(c.getString(3));
                    p.setTrangThai(c.getString(4));
                    p.setSoNguoiToiDa(c.getInt(5));
                    p.setGiaCaoDiem(c.getDouble(6));
                    p.setGioCaoDiemTu(c.getString(7));
                    p.setGioCaoDiemDen(c.getString(8));
                    p.setUrlAnh(c.getString(9));
                    String concat = c.getString(10);
                    if (concat != null && !concat.isEmpty()) {
                        p.setAnhUrlsList(new ArrayList<>(Arrays.asList(concat.split(Pattern.quote(ANH_SEP), -1))));
                    } else {
                        p.setAnhUrlsList(new ArrayList<>());
                    }
                    p.setDichVuList(new ArrayList<>());
                    list.add(p);
                } while (c.moveToNext());
            }
        }
        return list;
    }

    public List<String> getAnhUrlsByPhongId(int phongId) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT UrlAnh FROM AnhPhong WHERE PhongID=? ORDER BY AnhID",
                new String[]{String.valueOf(phongId)})) {
            if (c.moveToFirst()) {
                do {
                    list.add(c.getString(0));
                } while (c.moveToNext());
            }
        }
        return list;
    }

    private void replaceAnhPhong(int phongId, List<String> urls) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM AnhPhong WHERE PhongID=?", new Object[]{phongId});
        if (urls == null) {
            return;
        }
        for (String u : urls) {
            if (u != null && !u.trim().isEmpty()) {
                db.execSQL("INSERT INTO AnhPhong VALUES (null,?,?)",
                        new Object[]{phongId, u.trim()});
            }
        }
    }

    // ================= THÊM PHÒNG =================
    public void insertPhong(String ten, double gia, String mota, String trangthai,
                            int soNguoiToiDa,
                            List<String> anhUrls,
                            double giaCaoDiem, String gioCaoDiemTu, String gioCaoDiemDen) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.execSQL(
                "INSERT INTO Phong (TenPhong, GiaNgay, MoTa, TrangThai, SoNguoiToiDa, GiaCaoDiem, GioCaoDiemTu, GioCaoDiemDen) VALUES (?,?,?,?,?,?,?,?)",
                new Object[]{ten, gia, mota, trangthai, soNguoiToiDa, giaCaoDiem, gioCaoDiemTu, gioCaoDiemDen});

        int phongID = 0;
        try (Cursor c = db.rawQuery("SELECT last_insert_rowid()", null)) {
            if (c.moveToFirst()) {
                phongID = c.getInt(0);
            }
        }
        replaceAnhPhong(phongID, anhUrls);
    }

    // ================= UPDATE =================
    public void updatePhong(int id, String ten, double gia, String mota,
                            String trangthai, int soNguoiToiDa,
                            List<String> anhUrls,
                            double giaCaoDiem, String gioCaoDiemTu, String gioCaoDiemDen) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.execSQL("UPDATE Phong SET TenPhong=?, GiaNgay=?, MoTa=?, TrangThai=?, SoNguoiToiDa=?, GiaCaoDiem=?, GioCaoDiemTu=?, GioCaoDiemDen=? WHERE PhongID=?",
                new Object[]{ten, gia, mota, trangthai, soNguoiToiDa, giaCaoDiem, gioCaoDiemTu, gioCaoDiemDen, id});

        replaceAnhPhong(id, anhUrls);
    }

    // ================= DELETE =================
    public void deletePhong(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.execSQL("DELETE FROM Phong_DichVu WHERE PhongID=?", new Object[]{id});
        db.execSQL("DELETE FROM AnhPhong WHERE PhongID=?", new Object[]{id});
        db.execSQL(
                "DELETE FROM DatPhong_DichVu WHERE DatPhongID IN (SELECT DatPhongID FROM DatPhong WHERE PhongID=?)",
                new Object[]{id});
        db.execSQL(
                "DELETE FROM ThanhToan WHERE DatPhongID IN (SELECT DatPhongID FROM DatPhong WHERE PhongID=?)",
                new Object[]{id});
        db.execSQL("DELETE FROM DatPhong WHERE PhongID=?", new Object[]{id});
        db.execSQL("DELETE FROM Phong WHERE PhongID=?", new Object[]{id});
    }

    /** Tên phòng để hiển thị đánh giá; null nếu không có. */
    public String getTenPhongById(int phongID) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT TenPhong FROM Phong WHERE PhongID=?",
                new String[]{String.valueOf(phongID)})) {
            if (c.moveToFirst()) {
                return c.getString(0);
            }
        }
        return null;
    }
}

package com.example.doan.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.doan.DatabaseHelper;
import com.example.doan.model.HomestayThongTin;

public class HomestayThongTinDAO {

    private final DatabaseHelper dbHelper;

    public HomestayThongTinDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /** Luôn trả về đối tượng (fallback mặc định nếu DB trống). */
    public HomestayThongTin getHomestay() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT * FROM HomestayThongTin WHERE ID=1", null)) {
            if (c.moveToFirst()) {
                return mapRow(c);
            }
        }
        return defaultInfo();
    }

    /** Admin: lưu cấu hình hiển thị trang chủ (bản ghi ID = 1). */
    public boolean updateTrangChuCaiDat(HomestayThongTin h) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("TrangChu_AnhNen", nullToEmpty(h.getTrangChuAnhNen()));
        v.put("TrangChu_TieuDe", nullToEmpty(h.getTrangChuTieuDe()));
        v.put("TrangChu_CamKet", nullToEmpty(h.getTrangChuCamKet()));
        v.put("TrangChu_HienNhanh", h.getTrangChuHienNhanh() != 0 ? 1 : 0);
        v.put("TrangChu_HienTin", h.getTrangChuHienTin() != 0 ? 1 : 0);
        v.put("TrangChu_HienDanhGia", h.getTrangChuHienDanhGia() != 0 ? 1 : 0);
        v.put("TrangChu_HienDichVu", h.getTrangChuHienDichVu() != 0 ? 1 : 0);
        v.put("TrangChu_HienViTri", h.getTrangChuHienViTri() != 0 ? 1 : 0);
        v.put("TrangChu_SoTin", Math.max(1, Math.min(50, h.getTrangChuSoTin())));
        v.put("TrangChu_SoDanhGia", Math.max(1, Math.min(50, h.getTrangChuSoDanhGia())));
        v.put("DiaChi", nullToEmpty(h.getDiaChi()));
        if (h.getBanDoViDo() != null) {
            v.put("BanDo_ViDo", h.getBanDoViDo());
        } else {
            v.putNull("BanDo_ViDo");
        }
        if (h.getBanDoKinhDo() != null) {
            v.put("BanDo_KinhDo", h.getBanDoKinhDo());
        } else {
            v.putNull("BanDo_KinhDo");
        }
        v.put("BanDo_GhiChu", nullToEmpty(h.getBanDoGhiChu()));
        v.put("App_NenKhach", nullToEmpty(h.getAppNenKhach()));
        v.put("App_NenNhanVien", nullToEmpty(h.getAppNenNhanVien()));
        v.put("App_NenAdmin", nullToEmpty(h.getAppNenAdmin()));
        v.put("TrangChu_AnhNen_NV", nullToEmpty(h.getTrangChuAnhNenNv()));
        v.put("TrangChu_TieuDe_NV", nullToEmpty(h.getTrangChuTieuDeNv()));
        v.put("TrangChu_CamKet_NV", nullToEmpty(h.getTrangChuCamKetNv()));
        int affected = db.update("HomestayThongTin", v, "ID=?", new String[]{"1"});
        if (affected > 0) {
            return true;
        }
        // Trường hợp dữ liệu thiếu bản ghi ID=1: upsert để cấu hình (bao gồm banner ảnh từ máy) vẫn được lưu.
        v.put("ID", 1);
        long rowId = db.insertWithOnConflict("HomestayThongTin", null, v, SQLiteDatabase.CONFLICT_REPLACE);
        return rowId != -1;
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private static HomestayThongTin mapRow(Cursor c) {
        HomestayThongTin h = new HomestayThongTin();
        h.setId(c.getInt(c.getColumnIndexOrThrow("ID")));
        h.setTen(c.getString(c.getColumnIndexOrThrow("Ten")));
        h.setGioiThieu(c.getString(c.getColumnIndexOrThrow("GioiThieu")));
        h.setDiaChi(c.getString(c.getColumnIndexOrThrow("DiaChi")));
        h.setDienThoai(c.getString(c.getColumnIndexOrThrow("DienThoai")));
        h.setEmail(c.getString(c.getColumnIndexOrThrow("Email")));
        h.setGioMoCua(c.getString(c.getColumnIndexOrThrow("GioMoCua")));
        int idxAnh = c.getColumnIndex("TrangChu_AnhNen");
        if (idxAnh >= 0 && !c.isNull(idxAnh)) {
            h.setTrangChuAnhNen(c.getString(idxAnh));
        }
        int idxTd = c.getColumnIndex("TrangChu_TieuDe");
        if (idxTd >= 0 && !c.isNull(idxTd)) {
            h.setTrangChuTieuDe(c.getString(idxTd));
        }
        int idxCk = c.getColumnIndex("TrangChu_CamKet");
        if (idxCk >= 0 && !c.isNull(idxCk)) {
            h.setTrangChuCamKet(c.getString(idxCk));
        }
        int i = c.getColumnIndex("TrangChu_HienNhanh");
        if (i >= 0) {
            h.setTrangChuHienNhanh(c.getInt(i));
        }
        i = c.getColumnIndex("TrangChu_HienTin");
        if (i >= 0) {
            h.setTrangChuHienTin(c.getInt(i));
        }
        i = c.getColumnIndex("TrangChu_HienDanhGia");
        if (i >= 0) {
            h.setTrangChuHienDanhGia(c.getInt(i));
        }
        i = c.getColumnIndex("TrangChu_HienDichVu");
        if (i >= 0) {
            h.setTrangChuHienDichVu(c.getInt(i));
        }
        i = c.getColumnIndex("TrangChu_HienViTri");
        if (i >= 0) {
            h.setTrangChuHienViTri(c.getInt(i));
        }
        i = c.getColumnIndex("TrangChu_SoTin");
        if (i >= 0) {
            h.setTrangChuSoTin(c.getInt(i));
        }
        i = c.getColumnIndex("TrangChu_SoDanhGia");
        if (i >= 0) {
            h.setTrangChuSoDanhGia(c.getInt(i));
        }
        i = c.getColumnIndex("BanDo_ViDo");
        if (i >= 0 && !c.isNull(i)) {
            h.setBanDoViDo(c.getDouble(i));
        }
        i = c.getColumnIndex("BanDo_KinhDo");
        if (i >= 0 && !c.isNull(i)) {
            h.setBanDoKinhDo(c.getDouble(i));
        }
        i = c.getColumnIndex("BanDo_GhiChu");
        if (i >= 0 && !c.isNull(i)) {
            h.setBanDoGhiChu(c.getString(i));
        }
        i = c.getColumnIndex("App_NenKhach");
        if (i >= 0 && !c.isNull(i)) {
            h.setAppNenKhach(c.getString(i));
        }
        i = c.getColumnIndex("App_NenNhanVien");
        if (i >= 0 && !c.isNull(i)) {
            h.setAppNenNhanVien(c.getString(i));
        }
        i = c.getColumnIndex("App_NenAdmin");
        if (i >= 0 && !c.isNull(i)) {
            h.setAppNenAdmin(c.getString(i));
        }
        i = c.getColumnIndex("TrangChu_AnhNen_NV");
        if (i >= 0 && !c.isNull(i)) {
            h.setTrangChuAnhNenNv(c.getString(i));
        }
        i = c.getColumnIndex("TrangChu_TieuDe_NV");
        if (i >= 0 && !c.isNull(i)) {
            h.setTrangChuTieuDeNv(c.getString(i));
        }
        i = c.getColumnIndex("TrangChu_CamKet_NV");
        if (i >= 0 && !c.isNull(i)) {
            h.setTrangChuCamKetNv(c.getString(i));
        }
        return h;
    }

    private static HomestayThongTin defaultInfo() {
        HomestayThongTin h = new HomestayThongTin();
        h.setId(1);
        h.setTen("Homestay Du Lịch");
        h.setGioiThieu("Nghỉ dưỡng ấm cúng, tiện nghi hiện đại.");
        h.setDiaChi("123 Đường Biển, TP. Du Lịch");
        h.setDienThoai("0900 000 000");
        h.setEmail("info@homestay.vn");
        h.setGioMoCua("Nhận phòng 14:00 — Trả phòng 12:00");
        h.setTrangChuAnhNen("phong2");
        h.setTrangChuTieuDe("");
        h.setTrangChuCamKet("");
        h.setTrangChuHienNhanh(1);
        h.setTrangChuHienTin(1);
        h.setTrangChuHienDanhGia(1);
        h.setTrangChuHienDichVu(1);
        h.setTrangChuHienViTri(1);
        h.setTrangChuSoTin(4);
        h.setTrangChuSoDanhGia(5);
        h.setBanDoViDo(null);
        h.setBanDoKinhDo(null);
        h.setBanDoGhiChu("");
        h.setAppNenKhach("");
        h.setAppNenNhanVien("");
        h.setAppNenAdmin("");
        h.setTrangChuAnhNenNv("");
        h.setTrangChuTieuDeNv("");
        h.setTrangChuCamKetNv("");
        return h;
    }
}

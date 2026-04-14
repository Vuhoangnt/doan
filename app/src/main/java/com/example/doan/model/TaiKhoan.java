package com.example.doan.model;

import androidx.annotation.Nullable;

public class TaiKhoan {
    private int taiKhoanID;
    private String tenDangNhap;
    private String matKhau;
    private String role;
    private String tenNguoiDung;
    private String dienThoai;
    private String email;
    private String cccd;
    /** Đường dẫn file ảnh đại diện trên máy (internal storage). */
    private String anhDaiDien = "";

    public TaiKhoan() {}

    public TaiKhoan(int id, String user, String pass, String role,
                    String ten, String phone, String email, String cccd) {
        this(id, user, pass, role, ten, phone, email, cccd, "");
    }

    public TaiKhoan(int id, String user, String pass, String role,
                    String ten, String phone, String email, String cccd, String anhDaiDien) {
        this.taiKhoanID = id;
        this.tenDangNhap = user;
        this.matKhau = pass;
        this.role = role;
        this.tenNguoiDung = ten;
        this.dienThoai = phone;
        this.email = email;
        this.cccd = cccd;
        this.anhDaiDien = anhDaiDien != null ? anhDaiDien : "";
    }

    // Getter & Setter
    public int getTaiKhoanID() { return taiKhoanID; }
    public void setTaiKhoanID(int taiKhoanID) { this.taiKhoanID = taiKhoanID; }

    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }

    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTenNguoiDung() { return tenNguoiDung; }
    public void setTenNguoiDung(String tenNguoiDung) { this.tenNguoiDung = tenNguoiDung; }

    public String getDienThoai() { return dienThoai; }
    public void setDienThoai(String dienThoai) { this.dienThoai = dienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public String getAnhDaiDien() {
        return anhDaiDien != null ? anhDaiDien : "";
    }

    public void setAnhDaiDien(@Nullable String anhDaiDien) {
        this.anhDaiDien = anhDaiDien != null ? anhDaiDien : "";
    }
}
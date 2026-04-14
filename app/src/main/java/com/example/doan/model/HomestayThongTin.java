package com.example.doan.model;

import androidx.annotation.Nullable;

public class HomestayThongTin {
    private int id;
    private String ten;
    private String gioiThieu;
    private String diaChi;
    private String dienThoai;
    private String email;
    private String gioMoCua;

    /** Tên drawable / file ảnh nền hero trang chủ (vd: phong2, bg_guest_hero). */
    private String trangChuAnhNen;
    private String trangChuTieuDe;
    private String trangChuCamKet;
    private int trangChuHienNhanh = 1;
    private int trangChuHienTin = 1;
    private int trangChuHienDanhGia = 1;
    private int trangChuHienDichVu = 1;
    private int trangChuHienViTri = 1;
    private int trangChuSoTin = 4;
    private int trangChuSoDanhGia = 5;
    @Nullable
    private Double banDoViDo;
    @Nullable
    private Double banDoKinhDo;
    /** Ghi chú hiển thị kèm bản đồ (vd: cổng phụ, landmark). */
    private String banDoGhiChu;

    /** Ảnh nền vùng nội dung app — khách / vãng lai (tên drawable hoặc đường dẫn file). */
    private String appNenKhach;
    /** Ảnh nền — nhân viên. */
    private String appNenNhanVien;
    /** Ảnh nền — quản trị. */
    private String appNenAdmin;

    /** Hero trang chủ khi đăng nhập vai trò nhân viên (ảnh + chữ riêng; để trống thì dùng cấu hình khách). */
    private String trangChuAnhNenNv;
    private String trangChuTieuDeNv;
    private String trangChuCamKetNv;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getGioiThieu() {
        return gioiThieu;
    }

    public void setGioiThieu(String gioiThieu) {
        this.gioiThieu = gioiThieu;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getDienThoai() {
        return dienThoai;
    }

    public void setDienThoai(String dienThoai) {
        this.dienThoai = dienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGioMoCua() {
        return gioMoCua;
    }

    public void setGioMoCua(String gioMoCua) {
        this.gioMoCua = gioMoCua;
    }

    public String getTrangChuAnhNen() {
        return trangChuAnhNen;
    }

    public void setTrangChuAnhNen(String trangChuAnhNen) {
        this.trangChuAnhNen = trangChuAnhNen;
    }

    public String getTrangChuTieuDe() {
        return trangChuTieuDe;
    }

    public void setTrangChuTieuDe(String trangChuTieuDe) {
        this.trangChuTieuDe = trangChuTieuDe;
    }

    public String getTrangChuCamKet() {
        return trangChuCamKet;
    }

    public void setTrangChuCamKet(String trangChuCamKet) {
        this.trangChuCamKet = trangChuCamKet;
    }

    public int getTrangChuHienNhanh() {
        return trangChuHienNhanh;
    }

    public void setTrangChuHienNhanh(int trangChuHienNhanh) {
        this.trangChuHienNhanh = trangChuHienNhanh;
    }

    public int getTrangChuHienTin() {
        return trangChuHienTin;
    }

    public void setTrangChuHienTin(int trangChuHienTin) {
        this.trangChuHienTin = trangChuHienTin;
    }

    public int getTrangChuHienDanhGia() {
        return trangChuHienDanhGia;
    }

    public void setTrangChuHienDanhGia(int trangChuHienDanhGia) {
        this.trangChuHienDanhGia = trangChuHienDanhGia;
    }

    public int getTrangChuHienDichVu() {
        return trangChuHienDichVu;
    }

    public void setTrangChuHienDichVu(int trangChuHienDichVu) {
        this.trangChuHienDichVu = trangChuHienDichVu;
    }

    public int getTrangChuHienViTri() {
        return trangChuHienViTri;
    }

    public void setTrangChuHienViTri(int trangChuHienViTri) {
        this.trangChuHienViTri = trangChuHienViTri;
    }

    public int getTrangChuSoTin() {
        return trangChuSoTin;
    }

    public void setTrangChuSoTin(int trangChuSoTin) {
        this.trangChuSoTin = trangChuSoTin;
    }

    public int getTrangChuSoDanhGia() {
        return trangChuSoDanhGia;
    }

    public void setTrangChuSoDanhGia(int trangChuSoDanhGia) {
        this.trangChuSoDanhGia = trangChuSoDanhGia;
    }

    @Nullable
    public Double getBanDoViDo() {
        return banDoViDo;
    }

    public void setBanDoViDo(@Nullable Double banDoViDo) {
        this.banDoViDo = banDoViDo;
    }

    @Nullable
    public Double getBanDoKinhDo() {
        return banDoKinhDo;
    }

    public void setBanDoKinhDo(@Nullable Double banDoKinhDo) {
        this.banDoKinhDo = banDoKinhDo;
    }

    public String getBanDoGhiChu() {
        return banDoGhiChu;
    }

    public void setBanDoGhiChu(String banDoGhiChu) {
        this.banDoGhiChu = banDoGhiChu;
    }

    public String getAppNenKhach() {
        return appNenKhach;
    }

    public void setAppNenKhach(String appNenKhach) {
        this.appNenKhach = appNenKhach;
    }

    public String getAppNenNhanVien() {
        return appNenNhanVien;
    }

    public void setAppNenNhanVien(String appNenNhanVien) {
        this.appNenNhanVien = appNenNhanVien;
    }

    public String getAppNenAdmin() {
        return appNenAdmin;
    }

    public void setAppNenAdmin(String appNenAdmin) {
        this.appNenAdmin = appNenAdmin;
    }

    public String getTrangChuAnhNenNv() {
        return trangChuAnhNenNv;
    }

    public void setTrangChuAnhNenNv(String trangChuAnhNenNv) {
        this.trangChuAnhNenNv = trangChuAnhNenNv;
    }

    public String getTrangChuTieuDeNv() {
        return trangChuTieuDeNv;
    }

    public void setTrangChuTieuDeNv(String trangChuTieuDeNv) {
        this.trangChuTieuDeNv = trangChuTieuDeNv;
    }

    public String getTrangChuCamKetNv() {
        return trangChuCamKetNv;
    }

    public void setTrangChuCamKetNv(String trangChuCamKetNv) {
        this.trangChuCamKetNv = trangChuCamKetNv;
    }
}

package com.example.doan.model;

public class ThanhToan {
    private int thanhToanID;
    private int datPhongID;
    private double soTien;
    private String phuongThuc;
    private String ngayThanhToan;
    private String trangThai;
    private Integer nhanVienGhiNhanID;

    public int getThanhToanID() {
        return thanhToanID;
    }

    public void setThanhToanID(int thanhToanID) {
        this.thanhToanID = thanhToanID;
    }

    public int getDatPhongID() {
        return datPhongID;
    }

    public void setDatPhongID(int datPhongID) {
        this.datPhongID = datPhongID;
    }

    public double getSoTien() {
        return soTien;
    }

    public void setSoTien(double soTien) {
        this.soTien = soTien;
    }

    public String getPhuongThuc() {
        return phuongThuc;
    }

    public void setPhuongThuc(String phuongThuc) {
        this.phuongThuc = phuongThuc;
    }

    public String getNgayThanhToan() {
        return ngayThanhToan;
    }

    public void setNgayThanhToan(String ngayThanhToan) {
        this.ngayThanhToan = ngayThanhToan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Integer getNhanVienGhiNhanID() {
        return nhanVienGhiNhanID;
    }

    public void setNhanVienGhiNhanID(Integer nhanVienGhiNhanID) {
        this.nhanVienGhiNhanID = nhanVienGhiNhanID;
    }
}

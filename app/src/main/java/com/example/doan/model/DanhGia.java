package com.example.doan.model;

import androidx.annotation.Nullable;

public class DanhGia {
    private int danhGiaID;
    @Nullable
    private Integer taiKhoanID;
    private String tenHienThi;
    private int soSao;
    private String noiDung;
    private String ngayTao;
    @Nullable
    private Integer phongID;
    /** {@code da_duyet} | {@code cho_duyet} | {@code tu_choi} */
    private String trangThaiDuyet = "da_duyet";

    public int getDanhGiaID() {
        return danhGiaID;
    }

    public void setDanhGiaID(int danhGiaID) {
        this.danhGiaID = danhGiaID;
    }

    @Nullable
    public Integer getTaiKhoanID() {
        return taiKhoanID;
    }

    public void setTaiKhoanID(@Nullable Integer taiKhoanID) {
        this.taiKhoanID = taiKhoanID;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public void setTenHienThi(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public int getSoSao() {
        return soSao;
    }

    public void setSoSao(int soSao) {
        this.soSao = soSao;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(String ngayTao) {
        this.ngayTao = ngayTao;
    }

    @Nullable
    public Integer getPhongID() {
        return phongID;
    }

    public void setPhongID(@Nullable Integer phongID) {
        this.phongID = phongID;
    }

    public String getTrangThaiDuyet() {
        return trangThaiDuyet;
    }

    public void setTrangThaiDuyet(String trangThaiDuyet) {
        this.trangThaiDuyet = trangThaiDuyet;
    }
}

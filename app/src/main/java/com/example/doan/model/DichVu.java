package com.example.doan.model;

public class DichVu {
    private int dichVuID;
    private String tenDichVu;
    private double gia;
    private String moTa;

    public DichVu() {}

    public int getDichVuID() {
        return dichVuID;
    }

    public void setDichVuID(int dichVuID) {
        this.dichVuID = dichVuID;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public double getGia() {
        return gia;
    }

    public void setGia(double gia) {
        this.gia = gia;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}

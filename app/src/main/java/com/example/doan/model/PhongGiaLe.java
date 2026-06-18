package com.example.doan.model;

/**
 * Một ngày lễ (hoặc ngày đặc biệt) áp giá riêng cho một phòng.
 * Giá đêm = {@code Phong.GiaNgay * heSoNhan}.
 */
public class PhongGiaLe {
    private long id;
    private int phongID;
    /** yyyy-MM-dd */
    private String ngayLe;
    /** Hệ số nhân với {@code GiaNgay}. VD: 1.5 = +50%, 2.0 = x2. */
    private double heSoNhan = 1.5;
    private String ghiChu;

    public PhongGiaLe() {}

    public PhongGiaLe(int phongID, String ngayLe, double heSoNhan, String ghiChu) {
        this.phongID = phongID;
        this.ngayLe = ngayLe;
        this.heSoNhan = heSoNhan;
        this.ghiChu = ghiChu;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getPhongID() { return phongID; }
    public void setPhongID(int phongID) { this.phongID = phongID; }

    public String getNgayLe() { return ngayLe; }
    public void setNgayLe(String ngayLe) { this.ngayLe = ngayLe; }

    public double getHeSoNhan() { return heSoNhan; }
    public void setHeSoNhan(double heSoNhan) { this.heSoNhan = heSoNhan; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}

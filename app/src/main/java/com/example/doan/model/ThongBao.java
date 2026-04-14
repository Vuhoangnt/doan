package com.example.doan.model;

public class ThongBao {

    private int thongBaoID;
    private String doiTuongNhan;
    private Integer taiKhoanNhanID;
    private String tieuDe;
    private String noiDung;
    private String ngayTao;
    private boolean daDoc;
    private Integer datPhongID;
    private Integer phongID;
    private String hanhDong;

    public int getThongBaoID() {
        return thongBaoID;
    }

    public void setThongBaoID(int thongBaoID) {
        this.thongBaoID = thongBaoID;
    }

    public String getDoiTuongNhan() {
        return doiTuongNhan;
    }

    public void setDoiTuongNhan(String doiTuongNhan) {
        this.doiTuongNhan = doiTuongNhan;
    }

    public Integer getTaiKhoanNhanID() {
        return taiKhoanNhanID;
    }

    public void setTaiKhoanNhanID(Integer taiKhoanNhanID) {
        this.taiKhoanNhanID = taiKhoanNhanID;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
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

    public boolean isDaDoc() {
        return daDoc;
    }

    public void setDaDoc(boolean daDoc) {
        this.daDoc = daDoc;
    }

    public Integer getDatPhongID() {
        return datPhongID;
    }

    public void setDatPhongID(Integer datPhongID) {
        this.datPhongID = datPhongID;
    }

    public Integer getPhongID() {
        return phongID;
    }

    public void setPhongID(Integer phongID) {
        this.phongID = phongID;
    }

    public String getHanhDong() {
        return hanhDong;
    }

    public void setHanhDong(String hanhDong) {
        this.hanhDong = hanhDong;
    }
}

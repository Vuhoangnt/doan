package com.example.doan.model;

public class TinTuc {
    private int tinTucID;
    private String tieuDe;
    private String noiDung;
    private String ngayDang;

    public TinTuc() {}

    public int getTinTucID() { return tinTucID; }
    public void setTinTucID(int tinTucID) { this.tinTucID = tinTucID; }

    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getNgayDang() { return ngayDang; }
    public void setNgayDang(String ngayDang) { this.ngayDang = ngayDang; }
}

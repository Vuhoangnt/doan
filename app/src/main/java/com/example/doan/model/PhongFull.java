package com.example.doan.model;

import java.util.ArrayList;
import java.util.List;

public class PhongFull {
    private int phongID;
    private String tenPhong;
    private double giaNgay;
    private String moTa;
    private String trangThai;
    /** Số khách tối đa (cột Phong.SoNguoiToiDa). */
    private int soNguoiToiDa = 2;
    private String urlAnh;
    /** Tất cả ảnh (drawable / file); ảnh đầu dùng cho thumbnail. */
    private List<String> anhUrlsList;
    /** 0 = không bật giá cao điểm (chỉ dùng giá ngày thường). */
    private double giaCaoDiem;
    private String gioCaoDiemTu;
    private String gioCaoDiemDen;

    // ✅ nhiều dịch vụ
    private List<String> dichVuList;

    public PhongFull() {}

    // ===== Constructor =====
    public PhongFull(int phongID, String tenPhong, double giaNgay,
                     String moTa, String trangThai, String urlAnh,
                     List<String> dichVuList) {
        this.phongID = phongID;
        this.tenPhong = tenPhong;
        this.giaNgay = giaNgay;
        this.moTa = moTa;
        this.trangThai = trangThai;
        this.urlAnh = urlAnh;
        this.dichVuList = dichVuList;
    }

    // ===== Getter Setter =====
    public int getPhongID() { return phongID; }
    public void setPhongID(int phongID) { this.phongID = phongID; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    public double getGiaNgay() { return giaNgay; }
    public void setGiaNgay(double giaNgay) { this.giaNgay = giaNgay; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public int getSoNguoiToiDa() { return soNguoiToiDa; }
    public void setSoNguoiToiDa(int soNguoiToiDa) { this.soNguoiToiDa = soNguoiToiDa; }

    public String getUrlAnh() { return urlAnh; }
    public void setUrlAnh(String urlAnh) { this.urlAnh = urlAnh; }

    public List<String> getAnhUrlsList() {
        return anhUrlsList;
    }

    public void setAnhUrlsList(List<String> anhUrlsList) {
        this.anhUrlsList = anhUrlsList;
    }

    /** Danh sách ảnh hiển thị (ưu tiên đủ từ CSDL, không thì ảnh đại diện). */
    public List<String> resolveAllImageRefs() {
        if (anhUrlsList != null && !anhUrlsList.isEmpty()) {
            return anhUrlsList;
        }
        List<String> one = new ArrayList<>();
        if (urlAnh != null && !urlAnh.trim().isEmpty()) {
            one.add(urlAnh.trim());
        }
        return one;
    }

    public List<String> getDichVuList() { return dichVuList; }
    public void setDichVuList(List<String> dichVuList) { this.dichVuList = dichVuList; }

    public double getGiaCaoDiem() {
        return giaCaoDiem;
    }

    public void setGiaCaoDiem(double giaCaoDiem) {
        this.giaCaoDiem = giaCaoDiem;
    }

    public String getGioCaoDiemTu() {
        return gioCaoDiemTu;
    }

    public void setGioCaoDiemTu(String gioCaoDiemTu) {
        this.gioCaoDiemTu = gioCaoDiemTu;
    }

    public String getGioCaoDiemDen() {
        return gioCaoDiemDen;
    }

    public void setGioCaoDiemDen(String gioCaoDiemDen) {
        this.gioCaoDiemDen = gioCaoDiemDen;
    }
}
package com.example.doan.model;

/**
 * Đặt phòng: khách có tài khoản (TaiKhoanID) hoặc khách vãng lai (TaiKhoanID = null, bắt buộc KhachTen + KhachDienThoai).
 */
public class DatPhong {

    private int datPhongID;
    private String maDatPhong;
    private Integer taiKhoanID; // null nếu khách không đăng nhập
    private int phongID;
    private String ngayNhan;
    private String ngayTra;
    private int soDem;
    private double tongTien;
    private String trangThai;
    private String khachTen;
    private String khachEmail;
    private String khachDienThoai;
    private String khachCccd;
    private int soNguoi;
    private String ghiChu;
    private String ngayTao;
    /** khach_vang_lai | tai_khoan */
    private String loaiDat;
    private Integer nhanVienXuLyID;
    /** Chỉ hiển thị — từ JOIN Phong */
    private String tenPhong;
    /** Chỉ hiển thị — từ JOIN TaiKhoan nhân viên xử lý */
    private String tenNhanVienXuLy;
    /** Giờ nhận / trả phòng (HH:mm). */
    private String gioNhan;
    private String gioTra;

    public DatPhong() {}

    public int getDatPhongID() { return datPhongID; }
    public void setDatPhongID(int datPhongID) { this.datPhongID = datPhongID; }

    public String getMaDatPhong() { return maDatPhong; }
    public void setMaDatPhong(String maDatPhong) { this.maDatPhong = maDatPhong; }

    public Integer getTaiKhoanID() { return taiKhoanID; }
    public void setTaiKhoanID(Integer taiKhoanID) { this.taiKhoanID = taiKhoanID; }

    public int getPhongID() { return phongID; }
    public void setPhongID(int phongID) { this.phongID = phongID; }

    public String getNgayNhan() { return ngayNhan; }
    public void setNgayNhan(String ngayNhan) { this.ngayNhan = ngayNhan; }

    public String getNgayTra() { return ngayTra; }
    public void setNgayTra(String ngayTra) { this.ngayTra = ngayTra; }

    public int getSoDem() { return soDem; }
    public void setSoDem(int soDem) { this.soDem = soDem; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getKhachTen() { return khachTen; }
    public void setKhachTen(String khachTen) { this.khachTen = khachTen; }

    public String getKhachEmail() { return khachEmail; }
    public void setKhachEmail(String khachEmail) { this.khachEmail = khachEmail; }

    public String getKhachDienThoai() { return khachDienThoai; }
    public void setKhachDienThoai(String khachDienThoai) { this.khachDienThoai = khachDienThoai; }

    public String getKhachCccd() { return khachCccd; }
    public void setKhachCccd(String khachCccd) { this.khachCccd = khachCccd; }

    public int getSoNguoi() { return soNguoi; }
    public void setSoNguoi(int soNguoi) { this.soNguoi = soNguoi; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getNgayTao() { return ngayTao; }
    public void setNgayTao(String ngayTao) { this.ngayTao = ngayTao; }

    public String getLoaiDat() { return loaiDat; }
    public void setLoaiDat(String loaiDat) { this.loaiDat = loaiDat; }

    public Integer getNhanVienXuLyID() { return nhanVienXuLyID; }
    public void setNhanVienXuLyID(Integer nhanVienXuLyID) { this.nhanVienXuLyID = nhanVienXuLyID; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    public String getTenNhanVienXuLy() { return tenNhanVienXuLy; }
    public void setTenNhanVienXuLy(String tenNhanVienXuLy) { this.tenNhanVienXuLy = tenNhanVienXuLy; }

    public String getGioNhan() {
        return gioNhan;
    }

    public void setGioNhan(String gioNhan) {
        this.gioNhan = gioNhan;
    }

    public String getGioTra() {
        return gioTra;
    }

    public void setGioTra(String gioTra) {
        this.gioTra = gioTra;
    }

}

package com.example.doan;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * CSDL homestay du lịch.
 * <p>
 * Role trong {@code TaiKhoan}: {@code admin} | {@code nhanvien} | {@code khach}
 * <p>
 * Đặt phòng: {@code DatPhong.TaiKhoanID} có thể NULL — khách không đăng nhập vẫn đặt được
 * (bắt buộc {@code KhachTen}, {@code KhachDienThoai}; {@code LoaiDat} = {@code khach_vang_lai}).
 * Khách có tài khoản: điền {@code TaiKhoanID} và thường dùng {@code LoaiDat} = {@code tai_khoan}.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "HomestayDB.db";
    private static final int DB_VERSION = 18;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // ===== TÀI KHOẢN: admin / nhanvien / khach =====
        db.execSQL(
                "CREATE TABLE TaiKhoan (" +
                        "TaiKhoanID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "TenDangNhap TEXT UNIQUE," +
                        "MatKhau TEXT," +
                        "Role TEXT," +
                        "TenNguoiDung TEXT," +
                        "DienThoai TEXT," +
                        "Email TEXT," +
                        "CCCD TEXT," +
                        "AnhDaiDien TEXT DEFAULT ''" +
                        ")"
        );

        // ===== PHÒNG =====
        db.execSQL(
                "CREATE TABLE Phong (" +
                        "PhongID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "TenPhong TEXT," +
                        "GiaNgay REAL," +
                        "MoTa TEXT," +
                        "TrangThai TEXT," +
                        "SoNguoiToiDa INTEGER DEFAULT 2," +
                        "LoaiPhong TEXT," +
                        "GiaCaoDiem REAL DEFAULT 0," +
                        "GioCaoDiemTu TEXT," +
                        "GioCaoDiemDen TEXT)"
        );

        // ===== ẢNH PHÒNG =====
        db.execSQL(
                "CREATE TABLE AnhPhong (" +
                        "AnhID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "PhongID INTEGER," +
                        "UrlAnh TEXT," +
                        "FOREIGN KEY (PhongID) REFERENCES Phong(PhongID))"
        );

        // ===== DỊCH VỤ =====
        db.execSQL(
                "CREATE TABLE DichVu (" +
                        "DichVuID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "TenDichVu TEXT," +
                        "Gia REAL," +
                        "MoTa TEXT)"
        );

        // ===== ĐẶT PHÒNG (khách vãng lai: TaiKhoanID NULL) =====
        db.execSQL(
                "CREATE TABLE DatPhong (" +
                        "DatPhongID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "MaDatPhong TEXT NOT NULL UNIQUE," +
                        "TaiKhoanID INTEGER," +
                        "PhongID INTEGER NOT NULL," +
                        "NgayNhan TEXT NOT NULL," +
                        "NgayTra TEXT NOT NULL," +
                        "SoDem INTEGER NOT NULL DEFAULT 1," +
                        "TongTien REAL," +
                        "TrangThai TEXT," +
                        "KhachTen TEXT NOT NULL," +
                        "KhachEmail TEXT," +
                        "KhachDienThoai TEXT NOT NULL," +
                        "KhachCCCD TEXT," +
                        "SoNguoi INTEGER NOT NULL DEFAULT 1," +
                        "GhiChu TEXT," +
                        "NgayTao TEXT," +
                        "LoaiDat TEXT NOT NULL DEFAULT 'khach_vang_lai'," +
                        "NhanVienXuLyID INTEGER," +
                        "GioNhan TEXT NOT NULL DEFAULT '14:00'," +
                        "GioTra TEXT NOT NULL DEFAULT '12:00'," +
                        "FOREIGN KEY (TaiKhoanID) REFERENCES TaiKhoan(TaiKhoanID)," +
                        "FOREIGN KEY (PhongID) REFERENCES Phong(PhongID)," +
                        "FOREIGN KEY (NhanVienXuLyID) REFERENCES TaiKhoan(TaiKhoanID))"
        );

        // ===== CHI TIẾT DỊCH VỤ =====
        db.execSQL(
                "CREATE TABLE DatPhong_DichVu (" +
                        "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "DatPhongID INTEGER," +
                        "DichVuID INTEGER," +
                        "SoLuong INTEGER," +
                        "FOREIGN KEY (DatPhongID) REFERENCES DatPhong(DatPhongID)," +
                        "FOREIGN KEY (DichVuID) REFERENCES DichVu(DichVuID))"
        );

        // ===== THANH TOÁN =====
        db.execSQL(
                "CREATE TABLE ThanhToan (" +
                        "ThanhToanID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "DatPhongID INTEGER," +
                        "SoTien REAL," +
                        "PhuongThuc TEXT," +
                        "NgayThanhToan TEXT," +
                        "TrangThai TEXT," +
                        "NhanVienGhiNhanID INTEGER," +
                        "FOREIGN KEY (DatPhongID) REFERENCES DatPhong(DatPhongID)," +
                        "FOREIGN KEY (NhanVienGhiNhanID) REFERENCES TaiKhoan(TaiKhoanID))"
        );

        db.execSQL(
                "CREATE TABLE Phong_DichVu (" +
                        "PhongID INTEGER," +
                        "DichVuID INTEGER," +
                        "PRIMARY KEY (PhongID, DichVuID)," +
                        "FOREIGN KEY (PhongID) REFERENCES Phong(PhongID)," +
                        "FOREIGN KEY (DichVuID) REFERENCES DichVu(DichVuID)" +
                        ")"
        );

        // ===== TIN TỨC =====
        db.execSQL(
                "CREATE TABLE TinTuc (" +
                        "TinTucID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "TieuDe TEXT," +
                        "NoiDung TEXT," +
                        "NgayDang TEXT)"
        );

        // ===== THÔNG TIN HOMESTAY (một bản ghi ID = 1) =====
        db.execSQL(
                "CREATE TABLE HomestayThongTin (" +
                        "ID INTEGER PRIMARY KEY," +
                        "Ten TEXT," +
                        "GioiThieu TEXT," +
                        "DiaChi TEXT," +
                        "DienThoai TEXT," +
                        "Email TEXT," +
                        "GioMoCua TEXT," +
                        "TrangChu_AnhNen TEXT," +
                        "TrangChu_TieuDe TEXT," +
                        "TrangChu_CamKet TEXT," +
                        "TrangChu_HienNhanh INTEGER NOT NULL DEFAULT 1," +
                        "TrangChu_HienTin INTEGER NOT NULL DEFAULT 1," +
                        "TrangChu_HienDanhGia INTEGER NOT NULL DEFAULT 1," +
                        "TrangChu_HienDichVu INTEGER NOT NULL DEFAULT 1," +
                        "TrangChu_HienViTri INTEGER NOT NULL DEFAULT 1," +
                        "TrangChu_SoTin INTEGER NOT NULL DEFAULT 4," +
                        "TrangChu_SoDanhGia INTEGER NOT NULL DEFAULT 5," +
                        "BanDo_ViDo REAL," +
                        "BanDo_KinhDo REAL," +
                        "BanDo_GhiChu TEXT," +
                        "App_NenKhach TEXT DEFAULT ''," +
                        "App_NenNhanVien TEXT DEFAULT ''," +
                        "App_NenAdmin TEXT DEFAULT ''," +
                        "TrangChu_AnhNen_NV TEXT DEFAULT ''," +
                        "TrangChu_TieuDe_NV TEXT DEFAULT ''," +
                        "TrangChu_CamKet_NV TEXT DEFAULT ''" +
                        ")"
        );

        // ===== ĐÁNH GIÁ (khách — có/không tài khoản) =====
        db.execSQL(
                "CREATE TABLE DanhGia (" +
                        "DanhGiaID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "TaiKhoanID INTEGER," +
                        "TenHienThi TEXT NOT NULL," +
                        "SoSao INTEGER NOT NULL," +
                        "NoiDung TEXT," +
                        "NgayTao TEXT," +
                        "PhongID INTEGER," +
                        "TrangThaiDuyet TEXT NOT NULL DEFAULT 'da_duyet'," +
                        "FOREIGN KEY (TaiKhoanID) REFERENCES TaiKhoan(TaiKhoanID)," +
                        "FOREIGN KEY (PhongID) REFERENCES Phong(PhongID))"
        );

        // ===== THÔNG BÁO (theo vai trò / tài khoản khách) =====
        db.execSQL(
                "CREATE TABLE ThongBao (" +
                        "ThongBaoID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "DoiTuongNhan TEXT NOT NULL," +
                        "TaiKhoanNhanID INTEGER," +
                        "TieuDe TEXT," +
                        "NoiDung TEXT," +
                        "NgayTao TEXT," +
                        "DaDoc INTEGER NOT NULL DEFAULT 0," +
                        "DatPhongID INTEGER," +
                        "PhongID INTEGER," +
                        "HanhDong TEXT)"
        );

        insertSampleData(db);
    }

    private void insertSampleData(SQLiteDatabase db) {

        // ===== TÀI KHOẢN — mật khẩu mẫu đều: 123456 =====
        // admin | nhanvien1 | khach1 | khach2
        db.execSQL("INSERT INTO TaiKhoan VALUES (null,'admin','123456','admin','Quản trị','0900000000','admin@gmail.com','','')");
        db.execSQL("INSERT INTO TaiKhoan VALUES (null,'nhanvien1','123456','nhanvien','Lê Thu Hà','0900000003','nhanvien@gmail.com','','')");
        db.execSQL("INSERT INTO TaiKhoan VALUES (null,'khach1','123456','khach','Nguyễn Văn A','0900000001','a@gmail.com','0123456789','')");
        db.execSQL("INSERT INTO TaiKhoan VALUES (null,'khach2','123456','khach','Trần Thị B','0900000002','b@gmail.com','9876543210','')");
        db.execSQL("INSERT INTO TaiKhoan VALUES (null,'khach3','123456','khach','Phạm Minh K','0900000004','k@gmail.com','','')");
        db.execSQL("INSERT INTO TaiKhoan VALUES (null,'khach4','123456','khach','Hoàng Thị D','0900000005','d@gmail.com','','')");
        db.execSQL("INSERT INTO TaiKhoan VALUES (null,'nhanvien2','123456','nhanvien','Trần Văn Em','0900000006','nv2@gmail.com','','')");
        db.execSQL("INSERT INTO TaiKhoan VALUES (null,'khach5','123456','khach','Đỗ Thị F','0900000007','f@gmail.com','','')");
        db.execSQL("INSERT INTO TaiKhoan VALUES (null,'khach6','123456','khach','Vũ Quang G','0900000008','g@gmail.com','','')");
        db.execSQL("INSERT INTO TaiKhoan VALUES (null,'khach7','123456','khach','Bùi Thanh H','0900000009','h@gmail.com','','')");
        db.execSQL("INSERT INTO TaiKhoan VALUES (null,'khach8','123456','khach','Ngô Thu I','0900000010','i@gmail.com','','')");

        // ===== PHÒNG (LoaiPhong: Don / Doi / GiaDinh — gợi ý báo cáo & filter) =====
        // Cột thêm: GiaCaoDiem, GioCaoDiemTu, GioCaoDiemDen (0 / rỗng = không bật giờ cao điểm)
        db.execSQL("INSERT INTO Phong VALUES (null,'Phòng Đơn',300000,'1 giường, view đẹp','Trống',2,'Don',350000,'18:00','22:00')");
        db.execSQL("INSERT INTO Phong VALUES (null,'Phòng Đôi',500000,'2 giường, rộng rãi','Trống',4,'Doi',550000,'17:00','21:00')");
        db.execSQL("INSERT INTO Phong VALUES (null,'Phòng Gia Đình',700000,'Phòng lớn cho gia đình','Trống',6,'GiaDinh',0,'','')");
        db.execSQL("INSERT INTO Phong VALUES (null,'Phòng Studio',400000,'Ban công riêng, bếp nhỏ, máy lạnh','Trống',2,'Studio',0,'','')");
        db.execSQL("INSERT INTO Phong VALUES (null,'Phòng Hướng Biển',850000,'View biển toàn cảnh, bồn tắm','Trống',2,'Doi',900000,'19:00','23:00')");
        db.execSQL("INSERT INTO Phong VALUES (null,'Phòng Dorm',250000,'Giường tầng, phù hợp nhóm bạn','Trống',8,'Dorm',0,'','')");
        db.execSQL("INSERT INTO Phong VALUES (null,'Phòng Deluxe',650000,'Nội thất cao cấp, minibar','Trống',2,'Doi',720000,'18:00','22:00')");
        db.execSQL("INSERT INTO Phong VALUES (null,'Phòng Penthouse',1200000,'Tầng thượng, sân thượng riêng','Trống',4,'GiaDinh',0,'','')");
        db.execSQL("INSERT INTO Phong VALUES (null,'Phòng Economy',220000,'Gọn gàng, tối ưu chi phí','Trống',1,'Don',0,'','')");
        db.execSQL("INSERT INTO Phong VALUES (null,'Phòng Bungalow',950000,'Nhà gỗ riêng trong vườn','Trống',3,'GiaDinh',0,'','')");

        // ===== ẢNH =====
        db.execSQL("INSERT INTO AnhPhong VALUES (null,1,'phong1')");
        db.execSQL("INSERT INTO AnhPhong VALUES (null,2,'phong2')");
        db.execSQL("INSERT INTO AnhPhong VALUES (null,3,'phong3')");
        db.execSQL("INSERT INTO AnhPhong VALUES (null,4,'phong1')");
        db.execSQL("INSERT INTO AnhPhong VALUES (null,5,'phong2')");
        db.execSQL("INSERT INTO AnhPhong VALUES (null,6,'phong3')");
        db.execSQL("INSERT INTO AnhPhong VALUES (null,7,'phong1')");
        db.execSQL("INSERT INTO AnhPhong VALUES (null,7,'phong2')");
        db.execSQL("INSERT INTO AnhPhong VALUES (null,8,'phong3')");
        db.execSQL("INSERT INTO AnhPhong VALUES (null,9,'phong1')");
        db.execSQL("INSERT INTO AnhPhong VALUES (null,10,'phong2')");

        // ===== DỊCH VỤ =====
        db.execSQL("INSERT INTO DichVu VALUES (null,'Thuê xe máy',150000,'Thuê theo ngày')");
        db.execSQL("INSERT INTO DichVu VALUES (null,'Bữa sáng',50000,'Ăn sáng tại homestay')");
        db.execSQL("INSERT INTO DichVu VALUES (null,'Giặt ủi',30000,'Theo kg')");
        db.execSQL("INSERT INTO DichVu VALUES (null,'Đưa đón sân bay',200000,'Xe riêng')");
        db.execSQL("INSERT INTO DichVu VALUES (null,'Spa gội đầu',120000,'Đặt trước')");
        db.execSQL("INSERT INTO DichVu VALUES (null,'Tour tham quan',300000,'Theo nhóm')");
        db.execSQL("INSERT INTO DichVu VALUES (null,'BBQ vườn',250000,'Bếp nướng + than, tối đa 8 người')");
        db.execSQL("INSERT INTO DichVu VALUES (null,'Thuê xe đạp',80000,'Theo ngày')");
        db.execSQL("INSERT INTO DichVu VALUES (null,'Cà phê sáng tại phòng',35000,'2 ly / ngày')");
        db.execSQL("INSERT INTO DichVu VALUES (null,'Dọn phòng sâu',100000,'Theo lần')");

        // ===== PHÒNG - DỊCH VỤ =====
        db.execSQL("INSERT INTO Phong_DichVu VALUES (1,1)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (1,2)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (2,2)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (2,3)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (3,1)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (3,2)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (3,4)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (4,2)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (4,3)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (5,1)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (5,4)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (5,5)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (6,2)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (6,6)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (7,2)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (7,7)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (7,8)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (8,1)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (8,2)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (8,4)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (9,2)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (9,10)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (10,1)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (10,6)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (10,7)");
        db.execSQL("INSERT INTO Phong_DichVu VALUES (10,9)");

        // ===== ĐẶT PHÒNG: tai_khoan + khach_vang_lai (không đăng nhập) =====
        // Cột: ... LoaiDat, NhanVienXuLyID, GioNhan, GioTra
        // TaiKhoanID: admin=1, nhanvien1=2, khach1–4=3–6, nhanvien2=7, khach5–8=8–11
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260300001',3,1,'2026-03-15','2026-03-16',1,300000,'Đã xác nhận','Nguyễn Văn A','a@gmail.com','0900000001','0123456789',2,'','2026-03-14','tai_khoan',null,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260300002',4,2,'2026-04-08','2026-04-12',4,2000000,'Đang ở','Trần Thị B','b@gmail.com','0900000002','9876543210',3,'Đã qua ngày trả — dữ liệu mẫu nhắc admin','2026-03-14','tai_khoan',null,'15:00','11:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260310001',null,1,'2026-04-01','2026-04-03',2,600000,'Chờ xác nhận','Lê Văn C','c@gmail.com','0912345678','',2,'Đặt qua form công khai, không tài khoản','2026-03-15','khach_vang_lai',null,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260320001',5,4,'2026-05-10','2026-05-12',2,800000,'Đã xác nhận','Khách E','e@gmail.com','0933444555','',2,'','2026-03-10','tai_khoan',null,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260320002',null,5,'2026-06-01','2026-06-05',4,3400000,'Chờ xác nhận','Võ Lan','lan@gmail.com','0977888999','',2,'','2026-03-12','khach_vang_lai',null,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260320003',3,6,'2026-04-20','2026-04-21',1,250000,'Đã xác nhận','Nguyễn Văn A','a@gmail.com','0900000001','',1,'','2026-03-08','tai_khoan',null,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260402001',3,3,'2026-04-08','2026-04-12',4,2800000,'Đang ở','Nguyễn Văn A','a@gmail.com','0900000001','0123456789',4,'Gia đình nghỉ lễ','2026-04-06','tai_khoan',2,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260402002',4,1,'2026-01-08','2026-01-10',2,600000,'Đã trả phòng','Trần Thị B','b@gmail.com','0900000002','9876543210',2,'','2026-01-05','tai_khoan',2,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260402003',9,1,'2026-07-05','2026-07-09',4,1200000,'Chờ xác nhận','Vũ Quang G','g@gmail.com','0900000008','',2,'Đặt tour hè','2026-04-01','tai_khoan',null,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260402004',6,1,'2026-02-14','2026-02-16',2,600000,'Chờ xác nhận','Hoàng Thị D','d@gmail.com','0900000005','',2,'Valentine','2026-02-01','tai_khoan',null,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260402005',8,2,'2026-08-10','2026-08-15',5,2500000,'Đã xác nhận','Đỗ Thị F','f@gmail.com','0900000007','',2,'','2026-04-02','tai_khoan',7,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260402006',null,2,'2026-04-25','2026-04-26',1,500000,'Đã hủy','Khách đổi lịch','x@gmail.com','0911000222','',1,'Khách hủy sát ngày','2026-04-10','khach_vang_lai',null,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260402007',3,4,'2026-03-01','2026-03-03',2,800000,'Đã trả phòng','Nguyễn Văn A','a@gmail.com','0900000001','0123456789',2,'','2026-02-28','tai_khoan',2,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260402008',null,5,'2026-05-20','2026-05-23',3,2550000,'Chờ xác nhận','Phạm Hải','hai@gmail.com','0922111333','',2,'Cần view biển','2026-04-03','khach_vang_lai',null,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260402009',5,6,'2026-03-05','2026-03-07',2,500000,'Đã trả phòng','Phạm Minh K','k@gmail.com','0900000004','',3,'Nhóm bạn','2026-03-01','tai_khoan',2,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260402010',10,7,'2026-07-01','2026-07-05',4,2600000,'Chờ xác nhận','Bùi Thanh H','h@gmail.com','0900000009','',2,'','2026-04-04','tai_khoan',null,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260402011',11,8,'2026-09-01','2026-09-05',4,4800000,'Đã xác nhận','Ngô Thu I','i@gmail.com','0900000010','',4,'Sinh nhật','2026-04-05','tai_khoan',2,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260402012',null,9,'2026-10-01','2026-10-06',5,1100000,'Đã xác nhận','Lý Mai','mai@gmail.com','0933000444','',1,'Công tác ngắn','2026-04-06','khach_vang_lai',2,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260402013',8,10,'2026-11-10','2026-11-17',7,6650000,'Chờ xác nhận','Đỗ Thị F','f@gmail.com','0900000007','',3,'Tuần trăng mật','2026-04-07','tai_khoan',null,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260402014',null,3,'2026-05-01','2026-05-03',2,1400000,'Đã trả phòng','Khách vãng lai','v@gmail.com','0944000555','',2,'','2026-04-20','khach_vang_lai',7,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260402015',4,1,'2026-12-20','2026-12-25',5,1500000,'Chờ xác nhận','Trần Thị B','b@gmail.com','0900000002','9876543210',2,'Giáng sinh','2026-04-08','tai_khoan',null,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260402016',null,4,'2026-08-01','2026-08-04',3,1200000,'Chờ xác nhận','Doanh nghiệp X','sales@x.vn','0955000666','',1,'Team building','2026-04-09','khach_vang_lai',null,'14:00','12:00')");
        db.execSQL("INSERT INTO DatPhong VALUES (null,'DP20260402017',9,6,'2026-09-10','2026-09-12',2,500000,'Đã xác nhận','Vũ Quang G','g@gmail.com','0900000008','',2,'','2026-04-10','tai_khoan',null,'14:00','12:00')");

        // ===== DỊCH VỤ ĐẶT (DatPhongID theo thứ tự chèn: 1…23) =====
        db.execSQL("INSERT INTO DatPhong_DichVu VALUES (null,1,1,1)");
        db.execSQL("INSERT INTO DatPhong_DichVu VALUES (null,1,2,2)");
        db.execSQL("INSERT INTO DatPhong_DichVu VALUES (null,7,1,1)");
        db.execSQL("INSERT INTO DatPhong_DichVu VALUES (null,7,2,2)");
        db.execSQL("INSERT INTO DatPhong_DichVu VALUES (null,8,3,2)");
        db.execSQL("INSERT INTO DatPhong_DichVu VALUES (null,10,2,1)");
        db.execSQL("INSERT INTO DatPhong_DichVu VALUES (null,11,4,1)");
        db.execSQL("INSERT INTO DatPhong_DichVu VALUES (null,16,1,2)");
        db.execSQL("INSERT INTO DatPhong_DichVu VALUES (null,17,5,1)");
        db.execSQL("INSERT INTO DatPhong_DichVu VALUES (null,17,6,1)");
        db.execSQL("INSERT INTO DatPhong_DichVu VALUES (null,18,2,3)");
        db.execSQL("INSERT INTO DatPhong_DichVu VALUES (null,18,8,2)");
        db.execSQL("INSERT INTO DatPhong_DichVu VALUES (null,19,7,1)");

        // ===== THANH TOÁN =====
        db.execSQL("INSERT INTO ThanhToan VALUES (null,1,400000,'Tiền mặt','2026-03-16','Đã thanh toán',2)");
        db.execSQL("INSERT INTO ThanhToan VALUES (null,2,2000000,'Chuyển khoản','2026-04-11','Đã thanh toán',2)");
        db.execSQL("INSERT INTO ThanhToan VALUES (null,7,2800000,'Chuyển khoản','2026-04-07','Đã thanh toán',2)");
        db.execSQL("INSERT INTO ThanhToan VALUES (null,8,600000,'Chuyển khoản','2026-01-10','Đã thanh toán',2)");
        db.execSQL("INSERT INTO ThanhToan VALUES (null,13,800000,'Tiền mặt','2026-03-03','Đã thanh toán',7)");
        db.execSQL("INSERT INTO ThanhToan VALUES (null,15,500000,'Chuyển khoản','2026-03-07','Đã thanh toán',2)");
        db.execSQL("INSERT INTO ThanhToan VALUES (null,18,600000,'Quẹt thẻ','2026-10-02','Đã thanh toán',2)");
        db.execSQL("INSERT INTO ThanhToan VALUES (null,18,500000,'Chuyển khoản','2026-10-05','Chưa thanh toán',null)");
        db.execSQL("INSERT INTO ThanhToan VALUES (null,20,1400000,'Tiền mặt','2026-05-03','Đã thanh toán',7)");

        // ===== TIN TỨC =====
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Khai trương','Giảm giá 20% tuần đầu — áp dụng mọi loại phòng. Liên hệ hotline để nhận mã ưu đãi.','2026-02-04')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Ưu đãi hè','Giảm 15% cho khách đặt sớm trước 30 ngày. Không áp dụng ngày lễ.','2026-03-01')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Combo gia đình','Đặt Phòng Gia Đình từ 2 đêm tặng bữa sáng miễn phí cho 4 người.','2026-03-05')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Sự kiện cuối tuần','Âm nhạc acoustic tối thứ 7 tại sân vườn — miễn phí cho khách lưu trú.','2026-03-08')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Mở rộng dịch vụ Spa','Gội đầu thư giãn và massage chân — đặt tại lễ tân.','2026-03-10')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Lưu ý mùa mưa','Homestay có mái che sân — vẫn tổ chức BBQ khi trời nhẹ.','2026-03-12')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Chính sách hoàn tiền','Hủy trước 7 ngày hoàn 80%; trước 3 ngày hoàn 50%.','2026-03-14')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Cảm ơn khách hàng','Top 50 đánh giá 5 sao tháng 2 — cảm ơn bạn đã tin chọn.','2026-03-16')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Ra mắt Penthouse','Phòng tầng thượng mới — ưu đãi 10% cho 20 booking đầu tiên.','2026-04-01')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Workation tháng 4','Gói 7 ngày phòng Economy + wifi tốc độ cao cho dân remote.','2026-04-02')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Bungalow vườn','Khu nhà gỗ riêng — phù hợp cặp đôi, có bếp nướng ngoài trời.','2026-04-03')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Thuê xe đạp miễn phí','Khách lưu trú từ 3 đêm được tặng 1 ngày xe đạp (theo tình trạng).','2026-04-04')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Lễ 30/4 — 1/5','Đặt sớm giữ giá; một số phòng đã gần hết chỗ.','2026-04-05')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Hội thảo nhỏ','Thuê sảnh vườn + phòng Studio cho nhóm dưới 15 người.','2026-04-06')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Gói BBQ tối','Đặt trước 24h — phục vụ tại Bungalow hoặc sân chung.','2026-04-07')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Cập nhật app','Theo dõi đơn và tin tức khuyến mãi ngay trên ứng dụng.','2026-04-08')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Cảm ơn tháng 3','Doanh thu và đánh giá tích cực — homestay nâng cấp máy nước nóng.','2026-04-09')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Check-in nhanh','Khách có tài khoản có thể gửi CCCD trước để làm thủ tục nhanh hơn.','2026-04-10')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Trả phòng online','Khách đã thanh toán đủ có thể xác nhận trả phòng trong mục Đơn của tôi.','2026-04-11')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Ưu đãi phòng Economy','Đặt từ 3 đêm giảm thêm 5% — phù hợp khách bụi và công tác.','2026-04-11')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Homestay pet-friendly','Một số phòng cho phép thú cưng nhỏ — liên hệ trước khi đặt.','2026-04-12')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Giờ trả phòng linh hoạt','Trao đổi lễ tân nếu cần trả muộn (phụ phí theo giờ).','2026-04-12')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Đánh giá sau lưu trú','Khách đăng nhập có thể gửi đánh giá sau khi đơn ở trạng thái Đã trả phòng.','2026-04-13')");
        db.execSQL("INSERT INTO TinTuc VALUES (null,'Nhắc nhở nhân viên','Hệ thống gửi thông báo nếu đơn vẫn Đang ở sau ngày trả dự kiến.','2026-04-13')");

        // ===== THÔNG TIN HOMESTAY =====
        db.execSQL(
                "INSERT INTO HomestayThongTin VALUES (1,'Homestay Du Lịch'," +
                        "'Không gian xanh, view đẹp, phù hợp nghỉ dưỡng và khám phá. " +
                        "Đội ngũ hỗ trợ 24/7, đặt phòng linh hoạt có hoặc không tài khoản.'," +
                        "'123 Đường Biển, Phường Trung Tâm, TP. Du Lịch'," +
                        "'0337 124 322 ','khanhhuyen@homestay.vn','Nhận phòng từ 14:00 — Trả phòng trước 12:00'," +
                        "'phong2','','',1,1,1,1,1,5,6,12.2388,109.1967,'Cổng sau gần bãi xe — gọi khi tới','','','','','','')"
        );

        // ===== ĐÁNH GIÁ MẪU (TrangThaiDuyet: da_duyet | cho_duyet) =====
        db.execSQL("INSERT INTO DanhGia VALUES (null,3,'Nguyễn Văn A',5,'Phòng sạch, view đẹp, chủ nhà nhiệt tình!','2026-03-01',1,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,null,'Khách lẻ',4,'Tiện nghi tốt, giá hợp lý.','2026-01-20',null,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,4,'Trần Thị B',5,'Phòng Đơn yên tĩnh, giường êm. Sẽ quay lại!','2026-02-10',1,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,null,'Minh Anh',4,'Ban công nhìn vườn rất chill.','2026-02-14',1,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,5,'Phạm Minh K',5,'Phòng Đôi rộng, máy lạnh mát.','2026-02-20',2,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,null,'Du khách HN',5,'Vợ chồng mình rất thích phòng này.','2026-02-22',2,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,3,'Lê Thu Hà',4,'Ổn trong tầm giá, wifi ổn định.','2026-02-25',2,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,null,'Gia đình 4 người',5,'Phòng Gia Đình đủ chỗ, bếp tiện.','2026-01-28',3,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,4,'Hoàng Thị D',4,'Trẻ con thích phòng rộng. Hơi ồn buổi sáng do xe.','2026-03-02',3,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,null,'Solo traveler',5,'Studio có bếp nhỏ — tự nấu được mì.','2026-03-04',4,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,5,'Khách Đà Nẵng',5,'Studio decor xinh, ảnh đẹp.','2026-03-06',4,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,null,'Review ẩn danh',3,'Hơi nhỏ so với ảnh nhưng sạch.','2026-03-07',4,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,3,'Quốc tế',5,'Sea view tuyệt vời! Best homestay.','2026-02-18',5,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,null,'Cặp đôi',5,'Bồn tắm view biển — romantic.','2026-02-19',5,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,4,'Nhóm bạn',4,'Dorm rẻ, đủ ổ cắm. Nhà tắm chung sạch.','2026-03-09',6,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,null,'Backpacker',5,'Đúng kiểu dorm giao lưu, staff thân thiện.','2026-03-11',6,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,null,'Khách quen',5,'Homestay như nhà — lần thứ 3 rồi!','2026-01-15',null,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,5,'Lan Chi',4,'Sạch sẽ, chỉ hơi xa trung tâm một chút.','2026-02-01',1,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,null,'Anh Tuấn',4,'Đặt phòng đơn cho công tác — ổn.','2026-02-05',1,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,3,'Chị Hương',5,'Con mình thích vườn sau nhà.','2026-03-13',3,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,null,'Khách booking OTA',3,'Ổn nhưng check-in hơi chậm 15p.','2026-03-14',2,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,8,'Đỗ Thị F',5,'Deluxe xứng đáng từng đồng — minibar tiện.','2026-04-01',7,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,null,'Khách Sài Gòn',5,'Penthouse view quá đã, bồn tắm sạch.','2026-04-02',8,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,9,'Vũ Quang G',4,'Economy nhỏ nhưng gọn, phù hợp đi một mình.','2026-04-03',9,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,null,'Cặp vợ chồng',5,'Bungalow yên tĩnh, sáng nghe chim hót.','2026-04-04',10,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,10,'Bùi Thanh H',5,'Deluxe decor hiện đại, nhân viên hỗ trợ tốt.','2026-04-05',7,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,null,'Review Google',4,'Penthouse rộng — cầu thang hơi dốc với trẻ nhỏ.','2026-04-06',8,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,null,'Du lịch bụi',5,'Economy rẻ mà sạch, gần chợ.','2026-04-07',9,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,11,'Ngô Thu I',5,'Bungalow riêng tư — BBQ buổi tối tuyệt.','2026-04-08',10,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,null,'Khách doanh nghiệp',4,'Không gian họp nhỏ ổn, wifi ổn định.','2026-04-09',null,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,8,'Khách quay lại',5,'Lần 2 vẫn chọn Deluxe — ổn định chất lượng.','2026-04-10',7,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,3,'Khách HCM',5,'Studio gọn, máy lạnh êm — phù hợp đi một mình.','2026-04-11',4,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,null,'Ẩn danh',4,'Phòng Đơn đủ dùng, chỉ hơi nhỏ vali lớn.','2026-04-11',1,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,4,'Gia đình Nha Trang',5,'Phòng Gia Đình rộng, trẻ có chỗ chơi.','2026-04-12',3,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,null,'Review Facebook',5,'Hướng biển đúng như ảnh — bình minh đẹp.','2026-04-12',5,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,5,'Nhóm trekking',4,'Dorm tiện giao lưu, ổ khóa tủ hơi cũ.','2026-04-13',6,'da_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,3,'Khách demo',5,'Bình luận chờ admin duyệt (mẫu).','2026-04-14',2,'cho_duyet')");
        db.execSQL("INSERT INTO DanhGia VALUES (null,null,'Ẩn danh',4,'Cần duyệt — homestay yên tĩnh.','2026-04-14',null,'cho_duyet')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Thứ tự: bảng phụ thuộc trước
        db.execSQL("DROP TABLE IF EXISTS DatPhong_DichVu");
        db.execSQL("DROP TABLE IF EXISTS ThanhToan");
        db.execSQL("DROP TABLE IF EXISTS DatPhong");
        db.execSQL("DROP TABLE IF EXISTS Phong_DichVu");
        db.execSQL("DROP TABLE IF EXISTS AnhPhong");
        db.execSQL("DROP TABLE IF EXISTS DanhGia");
        db.execSQL("DROP TABLE IF EXISTS ThongBao");
        db.execSQL("DROP TABLE IF EXISTS Phong");
        db.execSQL("DROP TABLE IF EXISTS DichVu");
        db.execSQL("DROP TABLE IF EXISTS TinTuc");
        db.execSQL("DROP TABLE IF EXISTS HomestayThongTin");
        db.execSQL("DROP TABLE IF EXISTS TaiKhoan");
        onCreate(db);
    }
}

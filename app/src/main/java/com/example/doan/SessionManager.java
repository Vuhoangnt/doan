package com.example.doan;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.doan.model.TaiKhoan;

public class SessionManager {

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_NHANVIEN = "nhanvien";
    public static final String ROLE_KHACH = "khach";

    private static final String PREF_NAME = "USER_SESSION";
    private static final String KEY_MUST_CHANGE_PASSWORD = "must_change_password";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // ===== LƯU SESSION =====
    public void saveUser(TaiKhoan tk) {
        editor.putInt("id", tk.getTaiKhoanID());
        editor.putString("user", tk.getTenDangNhap());
        editor.putString("role", tk.getRole());
        editor.putString("name", tk.getTenNguoiDung());
        editor.apply();
    }

    // ===== KIỂM TRA ĐÃ LOGIN CHƯA =====
    public boolean isLoggedIn() {
        return pref.contains("user");
    }

    // ===== LẤY USER =====
    public String getUsername() {
        return pref.getString("user", "");
    }

    public String getRole() {
        return pref.getString("role", "khach");
    }

    public int getTaiKhoanId() {
        return pref.getInt("id", -1);
    }

    public String getDisplayName() {
        return pref.getString("name", "");
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(getRole());
    }

    public boolean isNhanVien() {
        return ROLE_NHANVIEN.equals(getRole());
    }

    public boolean isKhach() {
        return ROLE_KHACH.equals(getRole());
    }

    /** Nhãn hiển thị tiếng Việt cho {@link #getRole()}. */
    public static String roleLabelVi(String role) {
        if (ROLE_ADMIN.equals(role)) return "Quản trị";
        if (ROLE_NHANVIEN.equals(role)) return "Nhân viên";
        if (ROLE_KHACH.equals(role)) return "Khách hàng";
        return role != null ? role : "";
    }

    // ===== LOGOUT =====
    public void logout() {
        editor.clear();
        editor.apply();
    }

    /** Sau khi tự tạo tài khoản đặt phòng (mật khẩu mặc định 123). */
    public void setMustChangePassword(boolean v) {
        editor.putBoolean(KEY_MUST_CHANGE_PASSWORD, v);
        editor.apply();
    }

    public boolean mustChangePassword() {
        return pref.getBoolean(KEY_MUST_CHANGE_PASSWORD, false);
    }

    public void clearMustChangePassword() {
        editor.putBoolean(KEY_MUST_CHANGE_PASSWORD, false);
        editor.apply();
    }
}
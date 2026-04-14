package com.example.doan;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.Nullable;
import com.example.doan.DAO.HomestayThongTinDAO;
import com.example.doan.model.HomestayThongTin;
import com.example.doan.util.RoomImageUtils;

/**
 * Áp ảnh nền vùng nội dung chính theo cấu hình admin (HomestayThongTin).
 */
public final class AppBackgroundHelper {

    private AppBackgroundHelper() {}

    public static void apply(Activity activity, View root, @Nullable String ref) {
        if (root == null) {
            return;
        }
        if (ref == null || ref.trim().isEmpty()) {
            root.setBackgroundResource(R.color.admin_background);
            root.invalidate();
            return;
        }
        Drawable d = RoomImageUtils.loadDrawableForBackground(activity, ref.trim());
        if (d != null) {
            root.setBackground(d);
        } else {
            root.setBackgroundResource(R.color.admin_background);
        }
        root.invalidate();
    }

    /** Chọn nền theo vai trò (khách / nhân viên / admin). */
    public static void applyForSession(Activity activity, View root) {
        if (activity == null || root == null) {
            return;
        }
        HomestayThongTin h = new HomestayThongTinDAO(activity).getHomestay();
        SessionManager sm = new SessionManager(activity);
        String ref;
        if (!sm.isLoggedIn()) {
            ref = h.getAppNenKhach();
        } else if (sm.isAdmin()) {
            ref = h.getAppNenAdmin();
        } else if (sm.isNhanVien()) {
            ref = h.getAppNenNhanVien();
        } else {
            ref = h.getAppNenKhach();
        }
        apply(activity, root, ref);
    }
}

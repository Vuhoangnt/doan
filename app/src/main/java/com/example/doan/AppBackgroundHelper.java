package com.example.doan;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;
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
        int surface = MaterialColors.getColor(activity, com.google.android.material.R.attr.colorSurface,
                ContextCompat.getColor(activity, R.color.app_background));
        if (ref == null || ref.trim().isEmpty()) {
            root.setBackgroundColor(surface);
            root.invalidate();
            return;
        }
        Drawable d = RoomImageUtils.loadDrawableForBackground(activity, ref.trim());
        if (d != null) {
            root.setBackground(d);
        } else {
            root.setBackgroundColor(surface);
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

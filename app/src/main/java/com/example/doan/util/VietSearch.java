package com.example.doan.util;

import com.example.doan.model.PhongFull;

import java.text.Normalizer;
import java.util.Locale;

/** Chuẩn hóa chuỗi để tìm kiếm tiếng Việt (bỏ dấu, không phân biệt hoa thường). */
public final class VietSearch {

    private VietSearch() {
    }

    public static String normalize(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        String n = Normalizer.normalize(s.trim(), Normalizer.Form.NFD);
        StringBuilder sb = new StringBuilder(n.length());
        for (int i = 0; i < n.length(); i++) {
            char c = n.charAt(i);
            if (Character.getType(c) != Character.NON_SPACING_MARK) {
                sb.append(c);
            }
        }
        return sb.toString().toLowerCase(Locale.ROOT);
    }

    /** Tìm theo tên, mô tả, hoặc mã phòng (số). */
    public static boolean matchesPhong(String queryNormalized, PhongFull p) {
        if (queryNormalized.isEmpty()) {
            return true;
        }
        String blob = normalize(p.getTenPhong()) + " "
                + normalize(p.getMoTa() != null ? p.getMoTa() : "") + " "
                + p.getPhongID();
        return blob.contains(queryNormalized);
    }
}

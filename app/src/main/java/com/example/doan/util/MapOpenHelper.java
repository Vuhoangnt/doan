package com.example.doan.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.example.doan.model.HomestayThongTin;

/**
 * Mở Google Maps / ứng dụng bản đồ theo tọa độ đã lưu hoặc theo địa chỉ.
 */
public final class MapOpenHelper {

    private MapOpenHelper() {}

    public static void openHomestayLocation(Context context, HomestayThongTin h) {
        if (h == null) {
            return;
        }
        Double lat = h.getBanDoViDo();
        Double lng = h.getBanDoKinhDo();
        String label = h.getTen() != null && !h.getTen().isEmpty() ? h.getTen() : "Homestay";
        String note = h.getBanDoGhiChu();
        if (note != null && !note.trim().isEmpty()) {
            label = label + " — " + note.trim();
        }
        Intent intent;
        if (lat != null && lng != null && !Double.isNaN(lat) && !Double.isNaN(lng)) {
            String q = lat + "," + lng + "(" + label + ")";
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + lat + "," + lng + "?q=" + Uri.encode(q)));
        } else {
            String addr = h.getDiaChi() != null && !h.getDiaChi().isEmpty() ? h.getDiaChi() : label;
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(addr)));
        }
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            String webQ = lat != null && lng != null
                    ? lat + "," + lng
                    : (h.getDiaChi() != null ? h.getDiaChi() : label);
            Intent browser = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(webQ)));
            context.startActivity(browser);
        }
    }
}

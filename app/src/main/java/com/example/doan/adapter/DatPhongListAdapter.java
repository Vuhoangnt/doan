package com.example.doan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.doan.DAO.DatPhongDAO;
import com.example.doan.R;
import com.example.doan.model.DatPhong;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatPhongListAdapter extends BaseAdapter {

    public interface GuestCheckoutListener {
        void onGuestTraPhong(DatPhong d);
    }

    private final Context context;
    private final List<DatPhong> list;
    /** Đã thu theo DatPhongID (admin / lễ tân); null = không hiển thị. */
    private Map<Integer, Double> daThuTheoDon;
    private boolean guestOrdersMode;
    @Nullable
    private GuestCheckoutListener guestCheckoutListener;

    public DatPhongListAdapter(Context context, List<DatPhong> list) {
        this(context, list, null);
    }

    public DatPhongListAdapter(Context context, List<DatPhong> list, Map<Integer, Double> daThuTheoDon) {
        this.context = context;
        this.list = list;
        this.daThuTheoDon = daThuTheoDon != null ? daThuTheoDon : new HashMap<>();
    }

    public void setDaThuMap(Map<Integer, Double> map) {
        this.daThuTheoDon = map != null ? map : new HashMap<>();
    }

    public void setGuestOrdersMode(boolean on, @Nullable GuestCheckoutListener listener) {
        guestOrdersMode = on;
        guestCheckoutListener = listener;
    }

    public void updateData(List<DatPhong> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_datphong, parent, false);
        }
        DatPhong d = list.get(position);
        TextView txtMa = convertView.findViewById(R.id.txtMaDat);
        TextView txtTenPhong = convertView.findViewById(R.id.txtTenPhong);
        TextView txtKhach = convertView.findViewById(R.id.txtKhach);
        TextView txtNgay = convertView.findViewById(R.id.txtNgay);
        TextView txtTrangThai = convertView.findViewById(R.id.txtTrangThai);
        TextView txtThuHo = convertView.findViewById(R.id.txtThuHo);
        TextView txtTongTien = convertView.findViewById(R.id.txtTongTien);
        TextView txtNv = convertView.findViewById(R.id.txtNhanVienXuLy);
        MaterialButton btnTra = convertView.findViewById(R.id.btnGuestTraPhong);

        txtMa.setText(d.getMaDatPhong());
        String tenP = d.getTenPhong();
        txtTenPhong.setText(tenP != null && !tenP.isEmpty() ? ("Phòng: " + tenP) : ("Phòng ID: " + d.getPhongID()));
        txtKhach.setText(d.getKhachTen() + " • " + d.getKhachDienThoai());
        String gn = d.getGioNhan();
        String gt = d.getGioTra();
        String gio = "";
        if (gn != null && !gn.isEmpty() && gt != null && !gt.isEmpty()) {
            gio = " · " + gn + " → " + gt;
        }
        txtNgay.setText(d.getNgayNhan() + " → " + d.getNgayTra() + " (" + d.getSoDem() + " đêm)" + gio);
        txtTrangThai.setText("Trạng thái: " + DatPhongDAO.normalizeStatus(d.getTrangThai()));
        String nv = d.getTenNhanVienXuLy();
        if (nv != null && !nv.isEmpty()) {
            txtNv.setVisibility(View.VISIBLE);
            txtNv.setText("Xác nhận bởi: " + nv);
        } else {
            txtNv.setVisibility(View.GONE);
        }
        txtTongTien.setText(String.format(Locale.getDefault(), "Tổng đơn: %,.0f đ", d.getTongTien()));
        Double daThu = daThuTheoDon.get(d.getDatPhongID());
        if (daThu != null) {
            txtThuHo.setVisibility(View.VISIBLE);
            double thucThu = daThu;
            txtThuHo.setText(String.format(Locale.getDefault(),
                    "Đã thu: %,.0f đ — Còn: %,.0f đ",
                    thucThu, Math.max(0, d.getTongTien() - thucThu)));
        } else {
            txtThuHo.setVisibility(View.GONE);
        }
        if (btnTra != null) {
            boolean dangO = DatPhongDAO.TT_DANG_O.equals(DatPhongDAO.normalizeStatus(d.getTrangThai()));
            double thu = daThuTheoDon.getOrDefault(d.getDatPhongID(), 0d);
            boolean duTien = thu + 1.0 >= d.getTongTien();
            if (guestOrdersMode && guestCheckoutListener != null && dangO && duTien) {
                btnTra.setVisibility(View.VISIBLE);
                btnTra.setOnClickListener(v -> guestCheckoutListener.onGuestTraPhong(d));
            } else {
                btnTra.setVisibility(View.GONE);
            }
        }
        return convertView;
    }
}

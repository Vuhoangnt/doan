package com.example.doan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.doan.DAO.PhongDAO;
import com.example.doan.R;
import com.example.doan.model.DanhGia;

import java.util.List;

public class DanhGiaAdapter extends BaseAdapter {

    private final Context context;
    private final List<DanhGia> list;
    private final PhongDAO phongDAO;

    public DanhGiaAdapter(Context context, List<DanhGia> list, PhongDAO phongDAO) {
        this.context = context;
        this.list = list;
        this.phongDAO = phongDAO;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_danh_gia, parent, false);
        }
        DanhGia d = list.get(position);
        TextView txtTen = convertView.findViewById(R.id.txtTenNguoi);
        TextView txtSao = convertView.findViewById(R.id.txtSao);
        TextView txtNgayPhong = convertView.findViewById(R.id.txtNgayPhong);
        TextView txtNd = convertView.findViewById(R.id.txtNoiDung);

        txtTen.setText(d.getTenHienThi());
        txtSao.setText(starString(d.getSoSao()));
        txtNd.setText(d.getNoiDung() != null ? d.getNoiDung() : "");

        String ngay = d.getNgayTao() != null ? d.getNgayTao() : "";
        String extra = "";
        if (d.getPhongID() != null) {
            String tenP = phongDAO.getTenPhongById(d.getPhongID());
            if (tenP != null) {
                extra = " · " + tenP;
            }
        }
        txtNgayPhong.setText(ngay + extra);

        return convertView;
    }

    private static String starString(int n) {
        n = Math.max(0, Math.min(5, n));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append('★');
        }
        for (int i = n; i < 5; i++) {
            sb.append('☆');
        }
        return sb.toString();
    }
}

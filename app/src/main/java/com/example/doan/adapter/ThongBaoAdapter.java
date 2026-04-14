package com.example.doan.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.doan.R;
import com.example.doan.model.ThongBao;

import java.util.ArrayList;
import java.util.List;

public class ThongBaoAdapter extends BaseAdapter {

    private final Context context;
    private final List<ThongBao> list;

    public ThongBaoAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<>();
    }

    public void setData(List<ThongBao> data) {
        list.clear();
        if (data != null) {
            list.addAll(data);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public ThongBao getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).getThongBaoID();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.item_thong_bao, parent, false);
        }
        ThongBao t = list.get(position);
        TextView tvT = v.findViewById(R.id.txtThongBaoTieuDe);
        TextView tvN = v.findViewById(R.id.txtThongBaoNoiDung);
        TextView tvG = v.findViewById(R.id.txtThongBaoNgay);
        tvT.setText(t.getTieuDe() != null ? t.getTieuDe() : "");
        tvN.setText(t.getNoiDung() != null ? t.getNoiDung() : "");
        tvG.setText(t.getNgayTao() != null ? t.getNgayTao() : "");
        int style = t.isDaDoc() ? Typeface.NORMAL : Typeface.BOLD;
        tvT.setTypeface(null, style);
        tvN.setTypeface(null, style);
        return v;
    }
}

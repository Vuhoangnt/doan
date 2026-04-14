package com.example.doan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.doan.R;
import com.example.doan.model.TinTuc;

import java.util.List;

public class TinTucAdapter extends BaseAdapter {

    private final Context context;
    private final List<TinTuc> list;

    public TinTucAdapter(Context context, List<TinTuc> list) {
        this.context = context;
        this.list = list;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_tintuc, parent, false);
        }
        TinTuc t = list.get(position);
        TextView txtTieuDe = convertView.findViewById(R.id.txtTieuDe);
        TextView txtNgay = convertView.findViewById(R.id.txtNgay);
        TextView txtNoiDung = convertView.findViewById(R.id.txtNoiDung);
        txtTieuDe.setText(t.getTieuDe());
        txtNgay.setText(t.getNgayDang() != null ? t.getNgayDang() : "");
        txtNoiDung.setText(t.getNoiDung() != null ? t.getNoiDung() : "");
        return convertView;
    }
}

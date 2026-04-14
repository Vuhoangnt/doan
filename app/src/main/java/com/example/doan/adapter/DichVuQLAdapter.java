package com.example.doan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.doan.R;
import com.example.doan.model.DichVu;

import java.util.List;
import java.util.Locale;

public class DichVuQLAdapter extends BaseAdapter {

    private final Context context;
    private final List<DichVu> list;

    public DichVuQLAdapter(Context context, List<DichVu> list) {
        this.context = context;
        this.list = list;
    }

    public void updateData(List<DichVu> newList) {
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
        return list.get(position).getDichVuID();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_dich_vu_ql, parent, false);
        }
        DichVu d = list.get(position);
        TextView t1 = convertView.findViewById(R.id.txtTenDichVu);
        TextView t2 = convertView.findViewById(R.id.txtGiaDichVu);
        TextView t3 = convertView.findViewById(R.id.txtMoTaDichVu);
        t1.setText(d.getTenDichVu());
        t2.setText(String.format(Locale.getDefault(), "%,.0f đ", d.getGia()));
        String mt = d.getMoTa();
        t3.setText(mt != null && !mt.isEmpty() ? mt : "—");
        return convertView;
    }
}

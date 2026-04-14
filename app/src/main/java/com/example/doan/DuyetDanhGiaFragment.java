package com.example.doan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.doan.DAO.DanhGiaDAO;
import com.example.doan.DAO.PhongDAO;
import com.example.doan.model.DanhGia;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class DuyetDanhGiaFragment extends Fragment implements DataRefreshable {

    private ListView listView;
    private TextView txtEmpty;
    private DanhGiaDAO dao;
    private PhongDAO phongDao;
    private final List<DanhGia> list = new ArrayList<>();
    private DuyetAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_duyet_danh_gia, container, false);
        listView = v.findViewById(R.id.listDuyetDanhGia);
        txtEmpty = v.findViewById(R.id.txtDuyetDanhGiaEmpty);
        dao = new DanhGiaDAO(requireContext());
        phongDao = new PhongDAO(requireContext());
        listView.setEmptyView(txtEmpty);
        adapter = new DuyetAdapter();
        listView.setAdapter(adapter);
        refreshData();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void refreshData() {
        list.clear();
        list.addAll(dao.getPendingNewestFirst());
        adapter.notifyDataSetChanged();
    }

    private class DuyetAdapter extends BaseAdapter {

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
            return list.get(position).getDanhGiaID();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                row = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_duyet_danh_gia_row, parent, false);
            }
            DanhGia d = list.get(position);
            TextView t1 = row.findViewById(R.id.txtDuyetTenSao);
            TextView t2 = row.findViewById(R.id.txtDuyetNgayPhong);
            TextView t3 = row.findViewById(R.id.txtDuyetNoiDung);
            MaterialButton btnOk = row.findViewById(R.id.btnDuyetChapNhan);
            MaterialButton btnNo = row.findViewById(R.id.btnDuyetTuChoi);

            String stars = starsStr(d.getSoSao());
            t1.setText(d.getTenHienThi() + " · " + stars);
            String phongTen = "";
            if (d.getPhongID() != null) {
                String t = phongDao.getTenPhongById(d.getPhongID());
                phongTen = t != null ? t : "";
            }
            String line = d.getNgayTao() != null ? d.getNgayTao() : "";
            if (!phongTen.isEmpty()) {
                line = line.isEmpty() ? phongTen : line + " · " + phongTen;
            }
            t2.setText(line);
            t3.setText(d.getNoiDung() != null ? d.getNoiDung() : "");

            int id = d.getDanhGiaID();
            btnOk.setOnClickListener(v -> {
                if (dao.updateTrangThaiDuyet(id, DanhGiaDAO.DUYET_DA)) {
                    Toast.makeText(requireContext(), R.string.duyet_danh_gia_ok, Toast.LENGTH_SHORT).show();
                    refreshData();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).refreshVisibleFragmentFromDb();
                    }
                }
            });
            btnNo.setOnClickListener(v -> {
                if (dao.updateTrangThaiDuyet(id, DanhGiaDAO.DUYET_TU_CHOI)) {
                    Toast.makeText(requireContext(), R.string.duyet_danh_gia_reject_ok, Toast.LENGTH_SHORT).show();
                    refreshData();
                }
            });
            return row;
        }
    }

    private static String starsStr(int n) {
        if (n < 1) {
            n = 1;
        }
        if (n > 5) {
            n = 5;
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < n; i++) {
            b.append('★');
        }
        return b.toString();
    }
}

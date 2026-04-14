package com.example.doan;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.doan.DAO.TaiKhoanDAO;
import com.example.doan.DataRefreshable;
import com.example.doan.adapter.TaiKhoanAdapter;
import com.example.doan.model.TaiKhoan;

import java.util.List;

public class QLTaiKhoan extends Fragment implements DataRefreshable {

    ListView listView;
    TaiKhoanDAO dao;
    List<TaiKhoan> list;
    TaiKhoanAdapter adapter;

    public QLTaiKhoan() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_q_l_tai_khoan, container, false);

        listView = view.findViewById(R.id.listTaiKhoan);
        dao = new TaiKhoanDAO(requireContext());

        refreshData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void refreshData() {
        loadData();
    }

    private void loadData() {
        list = dao.getAll();
        adapter = new TaiKhoanAdapter(requireContext(), list, dao);
        listView.setAdapter(adapter);
    }
}
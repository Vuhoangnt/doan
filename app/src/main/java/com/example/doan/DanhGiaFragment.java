package com.example.doan;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.doan.DAO.DanhGiaDAO;
import com.example.doan.DAO.DatPhongDAO;
import com.example.doan.DAO.PhongDAO;
import com.example.doan.adapter.DanhGiaAdapter;
import com.example.doan.model.DanhGia;
import com.example.doan.model.PhongFull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DanhGiaFragment extends Fragment implements DataRefreshable {

    private ListView listView;
    private TextView txtEmpty;
    private FloatingActionButton fab;
    private DanhGiaDAO dao;
    private PhongDAO phongDAO;
    private List<DanhGia> list;
    private DanhGiaAdapter adapter;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_danh_gia, container, false);
        listView = view.findViewById(R.id.listDanhGia);
        txtEmpty = view.findViewById(R.id.txtDanhGiaEmpty);
        fab = view.findViewById(R.id.fabThemDanhGia);
        listView.setEmptyView(txtEmpty);
        dao = new DanhGiaDAO(requireContext());
        phongDAO = new PhongDAO(requireContext());
        session = new SessionManager(requireContext());

        fab.setOnClickListener(v -> showThemDanhGiaDialog());
        updateFabVisibility();
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
        updateFabVisibility();
    }

    /** Khách: chỉ hiện FAB khi đã có đơn đã trả phòng. Chưa đăng nhập: ẩn. Admin/NV: luôn hiện. */
    private void updateFabVisibility() {
        if (!session.isLoggedIn()) {
            fab.setVisibility(View.GONE);
            return;
        }
        if (session.isAdmin() || session.isNhanVien()) {
            fab.setVisibility(View.VISIBLE);
            return;
        }
        if (session.isKhach()) {
            DatPhongDAO dpDao = new DatPhongDAO(requireContext());
            fab.setVisibility(dpDao.hasAnyCompletedStay(session.getTaiKhoanId())
                    ? View.VISIBLE : View.GONE);
            return;
        }
        fab.setVisibility(View.GONE);
    }

    private void loadData() {
        list = dao.getApprovedNewestFirst();
        adapter = new DanhGiaAdapter(requireContext(), list, phongDAO);
        listView.setAdapter(adapter);
    }

    private void showThemDanhGiaDialog() {
        if (!session.isLoggedIn()) {
            Toast.makeText(requireContext(), R.string.danh_gia_need_login, Toast.LENGTH_LONG).show();
            return;
        }

        DatPhongDAO dpDao = new DatPhongDAO(requireContext());
        boolean khach = session.isKhach();
        int uid = session.getTaiKhoanId();

        if (khach && !dpDao.hasAnyCompletedStay(uid)) {
            Toast.makeText(requireContext(), R.string.danh_gia_need_stay, Toast.LENGTH_LONG).show();
            return;
        }

        View form = getLayoutInflater().inflate(R.layout.dialog_danh_gia, null);
        EditText edtTen = form.findViewById(R.id.edtTenDanhGia);
        RatingBar rating = form.findViewById(R.id.ratingDanhGia);
        EditText edtNd = form.findViewById(R.id.edtNoiDungDanhGia);
        Spinner spPhong = form.findViewById(R.id.spinnerPhongDanhGia);

        if (session.isLoggedIn()) {
            String name = session.getDisplayName();
            if (name != null && !name.isEmpty()) {
                edtTen.setText(name);
            } else {
                edtTen.setText(session.getUsername());
            }
        }

        List<PhongFull> phongs = phongDAO.getAllPhongFull();
        List<String> labels = new ArrayList<>();
        final List<Integer> ids = new ArrayList<>();
        labels.add(getString(R.string.danh_gia_spinner_chung));
        ids.add(-1);
        for (PhongFull p : phongs) {
            if (!khach || dpDao.hasCompletedStayForRoom(uid, p.getPhongID())) {
                labels.add(p.getTenPhong());
                ids.add(p.getPhongID());
            }
        }

        ArrayAdapter<String> spAd = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, labels);
        spAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPhong.setAdapter(spAd);

        AlertDialog dlg = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.danh_gia_dialog_title)
                .setView(form)
                .setPositiveButton(R.string.danh_gia_gui, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dlg.setOnShowListener(d -> dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String ten = edtTen.getText().toString().trim();
            String nd = edtNd.getText().toString().trim();
            int sao = (int) rating.getRating();
            if (ten.isEmpty()) {
                Toast.makeText(requireContext(), R.string.danh_gia_err_ten, Toast.LENGTH_SHORT).show();
                return;
            }
            if (sao < 1 || sao > 5) {
                Toast.makeText(requireContext(), R.string.danh_gia_err_sao, Toast.LENGTH_SHORT).show();
                return;
            }
            if (nd.isEmpty()) {
                Toast.makeText(requireContext(), R.string.danh_gia_err_nd, Toast.LENGTH_SHORT).show();
                return;
            }
            int pos = spPhong.getSelectedItemPosition();
            Integer phongId = ids.get(pos);
            if (phongId != null && phongId == -1) {
                phongId = null;
            }

            if (khach) {
                if (phongId == null) {
                    if (!dpDao.hasAnyCompletedStay(uid)) {
                        Toast.makeText(requireContext(), R.string.danh_gia_err_chua_tra_phong,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                } else {
                    if (!dpDao.hasCompletedStayForRoom(uid, phongId)) {
                        Toast.makeText(requireContext(), R.string.danh_gia_err_chua_tra_phong,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }

            DanhGia dg = new DanhGia();
            dg.setTaiKhoanID(session.getTaiKhoanId());
            dg.setTenHienThi(ten);
            dg.setSoSao(sao);
            dg.setNoiDung(nd);
            dg.setNgayTao(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            dg.setPhongID(phongId);
            if (khach) {
                dg.setTrangThaiDuyet(DanhGiaDAO.DUYET_CHO);
            } else {
                dg.setTrangThaiDuyet(DanhGiaDAO.DUYET_DA);
            }

            if (dao.insert(dg)) {
                Toast.makeText(requireContext(),
                        khach ? R.string.danh_gia_ok_pending : R.string.danh_gia_ok,
                        Toast.LENGTH_LONG).show();
                loadData();
                dlg.dismiss();
            } else {
                Toast.makeText(requireContext(), R.string.danh_gia_fail, Toast.LENGTH_SHORT).show();
            }
        }));

        dlg.show();
    }

    /**
     * Mở hộp thoại đánh giá với phòng gợi ý (sau khi khách tự trả phòng).
     */
    public static void showReviewDialogForPhong(@NonNull Fragment host, int phongId) {
        SessionManager session = new SessionManager(host.requireContext());
        if (!session.isLoggedIn() || !session.isKhach()) {
            return;
        }
        DatPhongDAO dpDao = new DatPhongDAO(host.requireContext());
        int uid = session.getTaiKhoanId();
        if (!dpDao.hasCompletedStayForRoom(uid, phongId)) {
            Toast.makeText(host.requireContext(), R.string.danh_gia_err_chua_tra_phong, Toast.LENGTH_LONG).show();
            return;
        }

        DanhGiaDAO dao = new DanhGiaDAO(host.requireContext());
        PhongDAO phongDAO = new PhongDAO(host.requireContext());

        View form = host.getLayoutInflater().inflate(R.layout.dialog_danh_gia, null);
        EditText edtTen = form.findViewById(R.id.edtTenDanhGia);
        RatingBar rating = form.findViewById(R.id.ratingDanhGia);
        EditText edtNd = form.findViewById(R.id.edtNoiDungDanhGia);
        Spinner spPhong = form.findViewById(R.id.spinnerPhongDanhGia);

        String name = session.getDisplayName();
        if (name != null && !name.isEmpty()) {
            edtTen.setText(name);
        } else {
            edtTen.setText(session.getUsername());
        }

        List<PhongFull> phongs = phongDAO.getAllPhongFull();
        List<String> labels = new ArrayList<>();
        final List<Integer> ids = new ArrayList<>();
        labels.add(host.getString(R.string.danh_gia_spinner_chung));
        ids.add(-1);
        int selectPos = 0;
        for (PhongFull p : phongs) {
            if (dpDao.hasCompletedStayForRoom(uid, p.getPhongID())) {
                labels.add(p.getTenPhong());
                ids.add(p.getPhongID());
                if (p.getPhongID() == phongId) {
                    selectPos = labels.size() - 1;
                }
            }
        }

        ArrayAdapter<String> spAd = new ArrayAdapter<>(host.requireContext(),
                android.R.layout.simple_spinner_item, labels);
        spAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPhong.setAdapter(spAd);
        spPhong.setSelection(selectPos);

        AlertDialog dlg = new AlertDialog.Builder(host.requireContext())
                .setTitle(R.string.danh_gia_dialog_title)
                .setView(form)
                .setPositiveButton(R.string.danh_gia_gui, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dlg.setOnShowListener(d -> dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String ten = edtTen.getText().toString().trim();
            String nd = edtNd.getText().toString().trim();
            int sao = (int) rating.getRating();
            if (ten.isEmpty()) {
                Toast.makeText(host.requireContext(), R.string.danh_gia_err_ten, Toast.LENGTH_SHORT).show();
                return;
            }
            if (sao < 1 || sao > 5) {
                Toast.makeText(host.requireContext(), R.string.danh_gia_err_sao, Toast.LENGTH_SHORT).show();
                return;
            }
            if (nd.isEmpty()) {
                Toast.makeText(host.requireContext(), R.string.danh_gia_err_nd, Toast.LENGTH_SHORT).show();
                return;
            }
            int pos = spPhong.getSelectedItemPosition();
            Integer pId = ids.get(pos);
            if (pId != null && pId == -1) {
                pId = null;
            }
            if (pId != null && !dpDao.hasCompletedStayForRoom(uid, pId)) {
                Toast.makeText(host.requireContext(), R.string.danh_gia_err_chua_tra_phong, Toast.LENGTH_LONG).show();
                return;
            }

            DanhGia dg = new DanhGia();
            dg.setTaiKhoanID(session.getTaiKhoanId());
            dg.setTenHienThi(ten);
            dg.setSoSao(sao);
            dg.setNoiDung(nd);
            dg.setNgayTao(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            dg.setPhongID(pId);
            dg.setTrangThaiDuyet(DanhGiaDAO.DUYET_CHO);

            if (dao.insert(dg)) {
                Toast.makeText(host.requireContext(), R.string.danh_gia_ok_pending, Toast.LENGTH_LONG).show();
                if (host instanceof DataRefreshable) {
                    ((DataRefreshable) host).refreshData();
                }
                dlg.dismiss();
            } else {
                Toast.makeText(host.requireContext(), R.string.danh_gia_fail, Toast.LENGTH_SHORT).show();
            }
        }));

        dlg.show();
    }
}

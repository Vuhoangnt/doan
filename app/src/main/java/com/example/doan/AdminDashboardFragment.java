package com.example.doan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.doan.DAO.PhongDAO;
import com.example.doan.DAO.ThongKeDAO;
import com.example.doan.model.ThongKeTongQuan;

import java.util.Locale;

public class AdminDashboardFragment extends Fragment implements DataRefreshable {

    private ThongKeDAO thongKeDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        thongKeDAO = new ThongKeDAO(requireContext());

        view.findViewById(R.id.btnGotoPhong).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).adminNavigateTo(R.id.nav_phong);
            }
        });
        view.findViewById(R.id.btnGotoDatPhong).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).adminNavigateTo(R.id.nav_ql_dat_phong);
            }
        });
        view.findViewById(R.id.btnGotoTaiKhoan).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).adminNavigateTo(R.id.nav_ql_tai_khoan);
            }
        });
        view.findViewById(R.id.btnGotoTinTuc).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openFragmentWithBackStack(new TinTucFragment(), getString(R.string.nav_tin_tuc));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void refreshData() {
        View v = getView();
        if (v == null || thongKeDAO == null) {
            return;
        }
        new PhongDAO(requireContext()).syncAllPhongTrangThaiTheoDon();
        ThongKeTongQuan t = thongKeDAO.layTongQuan();

        TextView txtDoanhThu = v.findViewById(R.id.txtDoanhThu);
        txtDoanhThu.setText(String.format(Locale.getDefault(), "%,.0f đ", t.tongDoanhThuUocTinh));

        ((TextView) v.findViewById(R.id.txtTongPhong)).setText(String.valueOf(t.tongPhong));
        ((TextView) v.findViewById(R.id.txtPhongTrong)).setText(String.valueOf(t.phongTrong));
        ((TextView) v.findViewById(R.id.txtPhongDangO)).setText(String.valueOf(t.phongDangO));
        ((TextView) v.findViewById(R.id.txtTongDon)).setText(String.valueOf(t.tongDonDat));

        ((TextView) v.findViewById(R.id.txtDonCho)).setText(String.valueOf(t.donChoXacNhan));
        ((TextView) v.findViewById(R.id.txtDonDaDat)).setText(String.valueOf(t.donDaDat));
        ((TextView) v.findViewById(R.id.txtDonDangO)).setText(String.valueOf(t.donDangO));

        ((TextView) v.findViewById(R.id.txtSoKhach)).setText(String.valueOf(t.soTaiKhoanKhach));
        ((TextView) v.findViewById(R.id.txtSoNhanVien)).setText(String.valueOf(t.soTaiKhoanNhanVien));
        ((TextView) v.findViewById(R.id.txtSoTinTuc)).setText(String.valueOf(t.soTinTuc));
        ((TextView) v.findViewById(R.id.txtSoDichVu)).setText(String.valueOf(t.soDichVu));
    }
}

package com.example.doan;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.doan.DAO.TaiKhoanDAO;
import com.example.doan.adapter.TaiKhoanAdapter;
import com.example.doan.model.TaiKhoan;

import java.util.List;

public class QLNhanVienFragment extends Fragment implements DataRefreshable {

    private ListView listView;
    private TaiKhoanDAO dao;
    private List<TaiKhoan> list;
    private TaiKhoanAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ql_nhan_vien, container, false);
        listView = view.findViewById(R.id.listNhanVien);
        view.findViewById(R.id.btnThemNhanVien).setOnClickListener(v -> showAddDialog());
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
        list = dao.getByRole(SessionManager.ROLE_NHANVIEN);
        adapter = new TaiKhoanAdapter(requireContext(), list, dao);
        listView.setAdapter(adapter);
    }

    private void showAddDialog() {
        View form = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_nhanvien, null);
        EditText edtUser = form.findViewById(R.id.edtNvUser);
        EditText edtPass = form.findViewById(R.id.edtNvPass);
        EditText edtTen = form.findViewById(R.id.edtNvTen);
        EditText edtPhone = form.findViewById(R.id.edtNvPhone);
        EditText edtEmail = form.findViewById(R.id.edtNvEmail);

        new AlertDialog.Builder(requireContext())
                .setTitle("Thêm nhân viên")
                .setView(form)
                .setPositiveButton("Lưu", (d, w) -> {
                    String u = edtUser.getText().toString().trim();
                    String p = edtPass.getText().toString().trim();
                    String ten = edtTen.getText().toString().trim();
                    if (u.isEmpty() || p.isEmpty() || ten.isEmpty()) {
                        Toast.makeText(requireContext(), "Điền đủ tài khoản, mật khẩu, họ tên", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    TaiKhoan tk = new TaiKhoan();
                    tk.setTenDangNhap(u);
                    tk.setMatKhau(p);
                    tk.setRole(SessionManager.ROLE_NHANVIEN);
                    tk.setTenNguoiDung(ten);
                    tk.setDienThoai(edtPhone.getText().toString().trim());
                    tk.setEmail(edtEmail.getText().toString().trim());
                    tk.setCccd("");
                    tk.setAnhDaiDien("");
                    if (dao.insert(tk) != -1) {
                        Toast.makeText(requireContext(), "Đã thêm nhân viên", Toast.LENGTH_SHORT).show();
                        loadData();
                    } else {
                        Toast.makeText(requireContext(), "Không thể thêm (trùng tài khoản?)", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}

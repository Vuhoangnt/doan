package com.example.doan.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doan.DAO.TaiKhoanDAO;
import com.example.doan.R;
import com.example.doan.SessionManager;
import com.example.doan.model.TaiKhoan;

import java.util.List;

public class TaiKhoanAdapter extends BaseAdapter {

    private final Context context;
    private final List<TaiKhoan> list;
    private final TaiKhoanDAO dao;

    public TaiKhoanAdapter(Context context, List<TaiKhoan> list, TaiKhoanDAO dao) {
        this.context = context;
        this.list = list;
        this.dao = dao;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_taikhoan, parent, false);
        }

        TextView txtUser = view.findViewById(R.id.txtUser);
        TextView txtRole = view.findViewById(R.id.txtRole);

        TaiKhoan tk = list.get(i);

        txtUser.setText(tk.getTenDangNhap());
        txtRole.setText("Vai trò: " + SessionManager.roleLabelVi(tk.getRole()));

        view.setOnClickListener(v -> showManageDialog(tk));
        return view;
    }

    private void showManageDialog(TaiKhoan tk) {
        new AlertDialog.Builder(context)
                .setTitle(tk.getTenDangNhap())
                .setItems(new String[]{"Sửa", "Xóa"}, (d, which) -> {
                    if (which == 0) {
                        openEditDialog(tk);
                    } else {
                        confirmDelete(tk);
                    }
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void openEditDialog(TaiKhoan tk) {
        View form = LayoutInflater.from(context).inflate(R.layout.dialog_edit_taikhoan, null);
        TextView txtRole = form.findViewById(R.id.txtRoleReadonly);
        EditText edtUser = form.findViewById(R.id.edtEditUser);
        EditText edtPass = form.findViewById(R.id.edtEditPass);
        EditText edtTen = form.findViewById(R.id.edtEditTen);
        EditText edtPhone = form.findViewById(R.id.edtEditPhone);
        EditText edtEmail = form.findViewById(R.id.edtEditEmail);

        txtRole.setText("Vai trò: " + SessionManager.roleLabelVi(tk.getRole()) + " (không đổi)");
        edtUser.setText(tk.getTenDangNhap());
        edtPass.setText(tk.getMatKhau());
        edtTen.setText(tk.getTenNguoiDung());
        edtPhone.setText(tk.getDienThoai() != null ? tk.getDienThoai() : "");
        edtEmail.setText(tk.getEmail() != null ? tk.getEmail() : "");

        AlertDialog dlg = new AlertDialog.Builder(context)
                .setTitle("Sửa tài khoản")
                .setView(form)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dlg.setOnShowListener(di -> dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String u = edtUser.getText().toString().trim();
            String p = edtPass.getText().toString().trim();
            String ten = edtTen.getText().toString().trim();
            if (u.isEmpty() || p.isEmpty() || ten.isEmpty()) {
                Toast.makeText(context, "Điền đủ tên đăng nhập, mật khẩu, họ tên", Toast.LENGTH_SHORT).show();
                return;
            }
            tk.setTenDangNhap(u);
            tk.setMatKhau(p);
            tk.setTenNguoiDung(ten);
            tk.setDienThoai(edtPhone.getText().toString().trim());
            tk.setEmail(edtEmail.getText().toString().trim());
            if (dao.update(tk)) {
                Toast.makeText(context, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
                dlg.dismiss();
            } else {
                Toast.makeText(context, "Không lưu được (trùng tài khoản?)", Toast.LENGTH_SHORT).show();
            }
        }));

        dlg.show();
    }

    private void confirmDelete(TaiKhoan tk) {
        SessionManager sm = new SessionManager(context);
        if (tk.getTaiKhoanID() == sm.getTaiKhoanId()) {
            Toast.makeText(context, "Không thể xóa chính tài khoản đang đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        if (SessionManager.ROLE_ADMIN.equals(tk.getRole())) {
            Toast.makeText(context, "Không xóa tài khoản quản trị", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(context)
                .setTitle("Xóa tài khoản")
                .setMessage("Xóa \"" + tk.getTenDangNhap() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (dao.delete(tk.getTaiKhoanID())) {
                        int idx = -1;
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).getTaiKhoanID() == tk.getTaiKhoanID()) {
                                idx = i;
                                break;
                            }
                        }
                        if (idx >= 0) {
                            list.remove(idx);
                        }
                        notifyDataSetChanged();
                        Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Không xóa được", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}

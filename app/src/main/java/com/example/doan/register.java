package com.example.doan;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doan.DAO.TaiKhoanDAO;
import com.example.doan.model.TaiKhoan;

public class register extends AppCompatActivity {

    EditText edtUser, edtPass, edtName, edtPhone, edtEmail, edtCCCD;
    Button btnRegister;
    TaiKhoanDAO dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtUser = findViewById(R.id.edtUser);
        edtPass = findViewById(R.id.edtPass);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        edtCCCD = findViewById(R.id.edtCCCD);
        btnRegister = findViewById(R.id.btnRegister);

        dao = new TaiKhoanDAO(this);

        btnRegister.setOnClickListener(v -> {

            TaiKhoan tk = new TaiKhoan();

            tk.setTenDangNhap(edtUser.getText().toString());
            tk.setMatKhau(edtPass.getText().toString());
            tk.setRole("khach");
            tk.setTenNguoiDung(edtName.getText().toString());
            tk.setDienThoai(edtPhone.getText().toString());
            tk.setEmail(edtEmail.getText().toString());
            tk.setCccd(edtCCCD.getText().toString());

            long kq = dao.insert(tk);

            if (kq != -1) {
                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Tài khoản đã tồn tại", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
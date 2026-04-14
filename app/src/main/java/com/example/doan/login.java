package com.example.doan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doan.DAO.TaiKhoanDAO;
import com.example.doan.model.TaiKhoan;

public class login extends AppCompatActivity {

    EditText edtUser, edtPass;
    Button btnLogin;
    TextView txtRegister;
    TaiKhoanDAO dao;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        session = new SessionManager(this);

        edtUser = findViewById(R.id.edtUser);
        edtPass = findViewById(R.id.edtPass);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegister = findViewById(R.id.txtRegister);

        dao = new TaiKhoanDAO(this);

        // LOGIN
        btnLogin.setOnClickListener(v -> {

            TaiKhoan tk = dao.login(
                    edtUser.getText().toString(),
                    edtPass.getText().toString()
            );

            if (tk != null) {

                // 🔥 LƯU SESSION
                session.saveUser(tk);
                session.clearMustChangePassword();

                Toast.makeText(this,
                        "Đăng nhập thành công — " + SessionManager.roleLabelVi(tk.getRole()),
                        Toast.LENGTH_SHORT).show();

                startActivity(new Intent(this, MainActivity.class));
                finish();

            } else {
                Toast.makeText(this, "Sai tài khoản", Toast.LENGTH_SHORT).show();
            }
        });
        // sang đăng ký
        txtRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, register.class));
        });
        if (session.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
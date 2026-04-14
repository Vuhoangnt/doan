package com.example.doan;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.doan.DAO.TaiKhoanDAO;
import com.example.doan.model.TaiKhoan;
import com.example.doan.util.RoomImageUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class SettingsFragment extends Fragment implements DataRefreshable {

    private ActivityResultLauncher<String> pickAvatar;
    private ImageView imgAvatar;
    private TextView txtNeedLogin;
    private TaiKhoanDAO taiKhoanDAO;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickAvatar = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::onAvatarPicked);
    }

    private void onAvatarPicked(@Nullable Uri uri) {
        SessionManager sm = new SessionManager(requireContext());
        if (uri == null || !sm.isLoggedIn()) {
            return;
        }
        try {
            String path = RoomImageUtils.copyUriToProfileImagesDir(requireContext(), uri);
            TaiKhoan tk = taiKhoanDAO.getById(sm.getTaiKhoanId());
            if (tk == null) {
                return;
            }
            tk.setAnhDaiDien(path);
            if (taiKhoanDAO.update(tk)) {
                RoomImageUtils.loadInto(imgAvatar, path);
                Toast.makeText(requireContext(), R.string.settings_avatar_saved, Toast.LENGTH_SHORT).show();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).refreshDrawerHeader();
                }
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        taiKhoanDAO = new TaiKhoanDAO(requireContext());

        LinearLayout containerThemes = v.findViewById(R.id.containerThemeChoices);
        String[] ids = ThemeManager.themeIds();
        String current = ThemeManager.getThemeId(requireContext());
        LayoutInflater themedInflater = LayoutInflater.from(requireContext());
        for (String id : ids) {
            View item = themedInflater.inflate(R.layout.item_theme_choice, containerThemes, false);
            MaterialCardView card = (MaterialCardView) item;
            TextView tv = item.findViewById(R.id.txtThemeLabel);
            tv.setText(ThemeManager.labelVi(id));
            if (id.equals(current)) {
                tv.setText(tv.getText() + " ✓");
            }
            card.setOnClickListener(x -> {
                ThemeManager.saveTheme(requireContext(), id);
                requireActivity().recreate();
            });
            containerThemes.addView(item);
        }

        imgAvatar = v.findViewById(R.id.imgSettingsAvatar);
        txtNeedLogin = v.findViewById(R.id.txtSettingsAvatarNeedLogin);
        MaterialButton btnPick = v.findViewById(R.id.btnChonAvatarTuMay);
        btnPick.setOnClickListener(x -> {
            SessionManager sm = new SessionManager(requireContext());
            if (!sm.isLoggedIn()) {
                Toast.makeText(requireContext(), R.string.settings_avatar_need_login, Toast.LENGTH_LONG).show();
                return;
            }
            pickAvatar.launch("image/*");
        });

        bindAvatarSection();
        return v;
    }

    private void bindAvatarSection() {
        SessionManager sm = new SessionManager(requireContext());
        boolean in = sm.isLoggedIn();
        if (txtNeedLogin != null) {
            txtNeedLogin.setVisibility(in ? View.GONE : View.VISIBLE);
        }
        if (!in) {
            imgAvatar.setImageResource(R.mipmap.ic_launcher_round);
            return;
        }
        TaiKhoan tk = taiKhoanDAO.getById(sm.getTaiKhoanId());
        if (tk != null && tk.getAnhDaiDien() != null && !tk.getAnhDaiDien().isEmpty()) {
            RoomImageUtils.loadInto(imgAvatar, tk.getAnhDaiDien());
        } else {
            imgAvatar.setImageResource(R.mipmap.ic_launcher_round);
        }
    }

    @Override
    public void refreshData() {
        bindAvatarSection();
    }
}

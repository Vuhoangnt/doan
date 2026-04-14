package com.example.doan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.doan.DAO.ThongBaoDAO;
import com.example.doan.adapter.ThongBaoAdapter;
import com.example.doan.model.ThongBao;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ThongBaoFragment extends Fragment implements DataRefreshable {

    private ThongBaoDAO dao;
    private ThongBaoAdapter adapter;
    private ListView listView;
    private TextView txtEmpty;
    private TextView txtHint;
    private MaterialCardView cardPermission;
    private MaterialButton btnPermRequest;
    private MaterialButton btnPermSettings;
    private MaterialButton btnMarkAllRead;

    private final ActivityResultLauncher<String> requestImagePerm =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> updatePermissionCardVisibility());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thong_bao, container, false);
        listView = view.findViewById(R.id.listThongBao);
        txtEmpty = view.findViewById(R.id.txtThongBaoEmpty);
        txtHint = view.findViewById(R.id.txtThongBaoHint);
        cardPermission = view.findViewById(R.id.cardThongBaoPermission);
        btnPermRequest = view.findViewById(R.id.btnThongBaoPermRequest);
        btnPermSettings = view.findViewById(R.id.btnThongBaoPermSettings);
        btnMarkAllRead = view.findViewById(R.id.btnThongBaoMarkAllRead);
        dao = new ThongBaoDAO(requireContext());
        adapter = new ThongBaoAdapter(requireContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, v, position, id) -> onItemClicked(adapter.getItem(position)));

        btnPermRequest.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 33) {
                requestImagePerm.launch(Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                requestImagePerm.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });
        btnPermSettings.setOnClickListener(v -> {
            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
            i.setData(uri);
            startActivity(i);
        });
        btnMarkAllRead.setOnClickListener(v -> {
            SessionManager s = new SessionManager(requireContext());
            dao.markAllReadForSession(s);
            loadListUi(s);
            if (getActivity() != null) {
                getActivity().invalidateOptionsMenu();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        SessionManager s = new SessionManager(requireContext());
        updatePermissionCardVisibility();
        loadListUi(s);
    }

    @Override
    public void refreshData() {
        SessionManager s = new SessionManager(requireContext());
        loadListUi(s);
    }

    private void updatePermissionCardVisibility() {
        if (cardPermission == null) {
            return;
        }
        SessionManager s = new SessionManager(requireContext());
        if (!s.isLoggedIn() || s.isKhach()) {
            cardPermission.setVisibility(View.GONE);
            return;
        }
        cardPermission.setVisibility(hasImageReadPermission() ? View.GONE : View.VISIBLE);
    }

    private boolean hasImageReadPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void loadListUi(SessionManager s) {
        if (txtHint == null) {
            return;
        }
        if (!s.isLoggedIn()) {
            txtHint.setText(R.string.thong_bao_hint_guest);
            txtHint.setVisibility(View.VISIBLE);
            adapter.setData(null);
            txtEmpty.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            if (btnMarkAllRead != null) {
                btnMarkAllRead.setVisibility(View.GONE);
            }
            return;
        }
        txtHint.setVisibility(View.GONE);
        if (s.isAdmin()) {
            txtHint.setText(R.string.thong_bao_hint_admin);
            txtHint.setVisibility(View.VISIBLE);
        } else if (s.isNhanVien()) {
            txtHint.setText(R.string.thong_bao_hint_staff);
            txtHint.setVisibility(View.VISIBLE);
        } else if (s.isKhach()) {
            txtHint.setText(R.string.thong_bao_hint_khach);
            txtHint.setVisibility(View.VISIBLE);
        }
        List<ThongBao> rows = dao.listForSession(s);
        adapter.setData(rows);
        boolean empty = rows.isEmpty();
        txtEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (btnMarkAllRead != null) {
            btnMarkAllRead.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
    }

    private void onItemClicked(ThongBao t) {
        if (t == null || getActivity() == null) {
            return;
        }
        if (!(getActivity() instanceof MainActivity)) {
            return;
        }
        dao.markRead(t.getThongBaoID());

        SessionManager s = new SessionManager(requireContext());
        MainActivity act = (MainActivity) getActivity();
        String hd = t.getHanhDong();
        Integer dp = t.getDatPhongID();
        if (ThongBaoDAO.HANH_DONG_MO_QL_DON.equals(hd) && dp != null && dp > 0
                && (s.isAdmin() || s.isNhanVien())) {
            act.openQlDatPhongAndDetail(dp);
        } else if (ThongBaoDAO.HANH_DONG_MO_DON_CUA_TOI.equals(hd)) {
            if (s.isKhach()) {
                act.openDonCuaToiAndHighlight(dp);
            } else if (s.isAdmin() && dp != null && dp > 0) {
                act.openQlDatPhongAndDetail(dp);
            }
        }

        if (listView != null) {
            listView.post(() -> {
                if (isAdded()) {
                    loadListUi(new SessionManager(requireContext()));
                }
                act.invalidateOptionsMenu();
            });
        } else {
            act.invalidateOptionsMenu();
        }
    }
}

package com.example.doan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.doan.DAO.HomestayThongTinDAO;
import com.example.doan.model.HomestayThongTin;
import com.example.doan.util.MapOpenHelper;
import com.google.android.material.card.MaterialCardView;

public class HomestayInfoFragment extends Fragment implements DataRefreshable {

    private HomestayThongTinDAO dao;
    private TextView txtTen;
    private TextView txtGioiThieu;
    private TextView txtDiaChi;
    private TextView txtMapNote;
    private TextView txtPhone;
    private TextView txtEmail;
    private TextView txtGio;
    private MaterialCardView cardMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_homestay_info, container, false);
        dao = new HomestayThongTinDAO(requireContext());
        txtTen = view.findViewById(R.id.txtHomestayTen);
        txtGioiThieu = view.findViewById(R.id.txtHomestayGioiThieu);
        txtDiaChi = view.findViewById(R.id.txtHomestayDiaChi);
        txtMapNote = view.findViewById(R.id.txtHomestayMapNote);
        txtPhone = view.findViewById(R.id.txtHomestayPhone);
        txtEmail = view.findViewById(R.id.txtHomestayEmail);
        txtGio = view.findViewById(R.id.txtHomestayGio);
        cardMap = view.findViewById(R.id.cardHomestayMap);
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
        if (dao == null || txtTen == null) {
            return;
        }
        HomestayThongTin h = dao.getHomestay();
        txtTen.setText(h.getTen());
        txtGioiThieu.setText(h.getGioiThieu());
        txtDiaChi.setText(h.getDiaChi());
        String mapNote = h.getBanDoGhiChu();
        if (mapNote != null && !mapNote.trim().isEmpty()) {
            txtMapNote.setVisibility(View.VISIBLE);
            txtMapNote.setText(getString(R.string.homestay_map_note_label) + "\n" + mapNote.trim());
        } else {
            txtMapNote.setVisibility(View.GONE);
        }
        txtPhone.setText(h.getDienThoai());
        txtEmail.setText(h.getEmail());
        txtGio.setText(h.getGioMoCua());

        String phone = h.getDienThoai() != null ? h.getDienThoai().replaceAll("\\s+", "") : "";
        txtPhone.setOnClickListener(v -> {
            if (!phone.isEmpty()) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
            }
        });

        String em = h.getEmail();
        txtEmail.setOnClickListener(v -> {
            if (em != null && !em.isEmpty()) {
                Intent send = new Intent(Intent.ACTION_SENDTO);
                send.setData(Uri.parse("mailto:" + em));
                try {
                    startActivity(Intent.createChooser(send, getString(R.string.homestay_send_email)));
                } catch (Exception ignored) {
                }
            }
        });

        cardMap.setOnClickListener(v -> MapOpenHelper.openHomestayLocation(requireContext(), h));
    }
}

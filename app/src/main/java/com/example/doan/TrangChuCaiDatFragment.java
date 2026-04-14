package com.example.doan;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.doan.DAO.HomestayThongTinDAO;
import com.example.doan.model.HomestayThongTin;
import com.example.doan.util.MapOpenHelper;
import com.example.doan.util.RoomImageUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class TrangChuCaiDatFragment extends Fragment implements DataRefreshable {

    private HomestayThongTinDAO dao;
    private TextInputEditText edtAnh;
    private TextInputEditText edtTieuDe;
    private TextInputEditText edtCamKet;
    private TextInputEditText edtAnhNv;
    private TextInputEditText edtTieuDeNv;
    private TextInputEditText edtCamKetNv;
    private TextInputEditText edtDiaChi;
    private TextInputEditText edtViDo;
    private TextInputEditText edtKinhDo;
    private TextInputEditText edtBanDoGhiChu;
    private TextInputEditText edtSoTin;
    private TextInputEditText edtSoDg;
    private MaterialSwitch swNhanh;
    private MaterialSwitch swTin;
    private MaterialSwitch swDg;
    private MaterialSwitch swDv;
    private MaterialSwitch swViTri;
    private ImageView imgPreview;
    private ImageView imgPreviewNv;
    /** Ảnh hero: null = ô khách; khác = ô đích khi chọn từ máy. */
    private TextInputEditText pendingHeroImageTarget;
    private ActivityResultLauncher<PickVisualMediaRequest> pickHeroImage;
    private ActivityResultLauncher<PickVisualMediaRequest> pickAppBgImage;
    private ActivityResultLauncher<Intent> pickMapLocation;
    private TextInputEditText pendingAppBgTarget;

    private TextInputEditText edtAppNenKhach;
    private TextInputEditText edtAppNenNhanVien;
    private TextInputEditText edtAppNenAdmin;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickHeroImage = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                this::onHeroImagePicked);
        pickAppBgImage = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                this::onAppBgPicked);
        pickMapLocation = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
                        return;
                    }
                    Intent data = result.getData();
                    double lat = data.getDoubleExtra(MapPickerActivity.EXTRA_LAT, 0);
                    double lng = data.getDoubleExtra(MapPickerActivity.EXTRA_LNG, 0);
                    if (edtViDo != null) {
                        edtViDo.setText(String.format(Locale.US, "%.7f", lat));
                    }
                    if (edtKinhDo != null) {
                        edtKinhDo.setText(String.format(Locale.US, "%.7f", lng));
                    }
                });
    }

    private void onAppBgPicked(@Nullable Uri uri) {
        if (uri == null || pendingAppBgTarget == null || getContext() == null) {
            pendingAppBgTarget = null;
            return;
        }
        TextInputEditText edt = pendingAppBgTarget;
        String oldPath = textOf(edt);
        try {
            String path = RoomImageUtils.copyUriToAppBgDir(requireContext(), uri);
            RoomImageUtils.deleteAppStoredImageIfReplaced(requireContext(), oldPath, path);
            edt.setText(path);
            Toast.makeText(requireContext(), R.string.trang_chu_cai_dat_anh_da_chon, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        pendingAppBgTarget = null;
    }

    private void onHeroImagePicked(@Nullable Uri uri) {
        if (uri == null || getContext() == null) {
            pendingHeroImageTarget = null;
            return;
        }
        TextInputEditText target = pendingHeroImageTarget != null ? pendingHeroImageTarget : edtAnh;
        pendingHeroImageTarget = null;
        if (target == null) {
            return;
        }
        String oldPath = textOf(target);
        if (target == edtAnh && imgPreview != null) {
            imgPreview.setVisibility(View.VISIBLE);
            imgPreview.setImageDrawable(null);
            imgPreview.setImageURI(uri);
        } else if (target == edtAnhNv && imgPreviewNv != null) {
            imgPreviewNv.setVisibility(View.VISIBLE);
            imgPreviewNv.setImageDrawable(null);
            imgPreviewNv.setImageURI(uri);
        }
        try {
            String path = RoomImageUtils.copyUriToHomeHeroDir(requireContext(), uri);
            RoomImageUtils.deleteAppStoredImageIfReplaced(requireContext(), oldPath, path);
            target.setText(path);
            if (target == edtAnh) {
                refreshHeroPreview(path);
            } else if (target == edtAnhNv) {
                refreshHeroPreviewNv(path);
            }
            Toast.makeText(requireContext(), R.string.trang_chu_cai_dat_anh_da_chon, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            if (target == edtAnh) {
                refreshHeroPreview(oldPath.isEmpty() ? null : oldPath);
            } else if (target == edtAnhNv) {
                refreshHeroPreviewNv(oldPath.isEmpty() ? null : oldPath);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_trang_chu_cai_dat, container, false);
        dao = new HomestayThongTinDAO(requireContext());
        edtAnh = v.findViewById(R.id.edtTrangChuAnhNen);
        edtTieuDe = v.findViewById(R.id.edtTrangChuTieuDe);
        edtCamKet = v.findViewById(R.id.edtTrangChuCamKet);
        edtAnhNv = v.findViewById(R.id.edtTrangChuAnhNenNv);
        edtTieuDeNv = v.findViewById(R.id.edtTrangChuTieuDeNv);
        edtCamKetNv = v.findViewById(R.id.edtTrangChuCamKetNv);
        edtDiaChi = v.findViewById(R.id.edtHomestayDiaChi);
        edtViDo = v.findViewById(R.id.edtBanDoViDo);
        edtKinhDo = v.findViewById(R.id.edtBanDoKinhDo);
        edtBanDoGhiChu = v.findViewById(R.id.edtBanDoGhiChu);
        edtSoTin = v.findViewById(R.id.edtSoTin);
        edtSoDg = v.findViewById(R.id.edtSoDanhGia);
        swNhanh = v.findViewById(R.id.swHienNhanh);
        swTin = v.findViewById(R.id.swHienTin);
        swDg = v.findViewById(R.id.swHienDanhGia);
        swDv = v.findViewById(R.id.swHienDichVu);
        swViTri = v.findViewById(R.id.swHienViTri);
        imgPreview = v.findViewById(R.id.imgPreviewTrangChuHero);
        imgPreviewNv = v.findViewById(R.id.imgPreviewTrangChuHeroNv);
        edtAppNenKhach = v.findViewById(R.id.edtAppNenKhach);
        edtAppNenNhanVien = v.findViewById(R.id.edtAppNenNhanVien);
        edtAppNenAdmin = v.findViewById(R.id.edtAppNenAdmin);
        MaterialButton btn = v.findViewById(R.id.btnLuuTrangChu);
        btn.setOnClickListener(x -> save());
        v.findViewById(R.id.btnChonAnhNenTuMay).setOnClickListener(x -> {
            pendingHeroImageTarget = edtAnh;
            launchPickImage(pickHeroImage);
        });
        v.findViewById(R.id.btnChonAnhNenTuMayNv).setOnClickListener(x -> {
            pendingHeroImageTarget = edtAnhNv;
            launchPickImage(pickHeroImage);
        });
        v.findViewById(R.id.btnChonAppNenKhach).setOnClickListener(x -> {
            pendingAppBgTarget = edtAppNenKhach;
            launchPickImage(pickAppBgImage);
        });
        v.findViewById(R.id.btnChonAppNenNhanVien).setOnClickListener(x -> {
            pendingAppBgTarget = edtAppNenNhanVien;
            launchPickImage(pickAppBgImage);
        });
        v.findViewById(R.id.btnChonAppNenAdmin).setOnClickListener(x -> {
            pendingAppBgTarget = edtAppNenAdmin;
            launchPickImage(pickAppBgImage);
        });
        v.findViewById(R.id.btnMoBanDoXemThu).setOnClickListener(x -> previewMap());
        v.findViewById(R.id.btnChonViTriBanDo).setOnClickListener(x -> openMapPicker());
        refreshData();
        return v;
    }

    /** Photo Picker (AndroidX) — không cần READ_MEDIA_* khi hệ thống dùng bộ chọn ảnh mới. */
    private void launchPickImage(ActivityResultLauncher<PickVisualMediaRequest> launcher) {
        PickVisualMediaRequest req = new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build();
        launcher.launch(req);
    }

    private void openMapPicker() {
        Intent i = new Intent(requireContext(), MapPickerActivity.class);
        Double lat = parseDoubleField(edtViDo);
        Double lng = parseDoubleField(edtKinhDo);
        if (lat != null && lng != null) {
            i.putExtra(MapPickerActivity.EXTRA_LAT, lat);
            i.putExtra(MapPickerActivity.EXTRA_LNG, lng);
        }
        pickMapLocation.launch(i);
    }

    private void previewMap() {
        HomestayThongTin h = buildHomestayFromForm();
        MapOpenHelper.openHomestayLocation(requireContext(), h);
    }

    private HomestayThongTin buildHomestayFromForm() {
        HomestayThongTin h = dao.getHomestay();
        h.setDiaChi(textOf(edtDiaChi));
        h.setBanDoViDo(parseDoubleField(edtViDo));
        h.setBanDoKinhDo(parseDoubleField(edtKinhDo));
        h.setBanDoGhiChu(textOf(edtBanDoGhiChu));
        return h;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void refreshData() {
        if (dao == null || edtAnh == null) {
            return;
        }
        HomestayThongTin h = dao.getHomestay();
        edtAnh.setText(h.getTrangChuAnhNen() != null ? h.getTrangChuAnhNen() : "");
        edtTieuDe.setText(h.getTrangChuTieuDe() != null ? h.getTrangChuTieuDe() : "");
        edtCamKet.setText(h.getTrangChuCamKet() != null ? h.getTrangChuCamKet() : "");
        if (edtAnhNv != null) {
            edtAnhNv.setText(h.getTrangChuAnhNenNv() != null ? h.getTrangChuAnhNenNv() : "");
        }
        if (edtTieuDeNv != null) {
            edtTieuDeNv.setText(h.getTrangChuTieuDeNv() != null ? h.getTrangChuTieuDeNv() : "");
        }
        if (edtCamKetNv != null) {
            edtCamKetNv.setText(h.getTrangChuCamKetNv() != null ? h.getTrangChuCamKetNv() : "");
        }
        edtDiaChi.setText(h.getDiaChi() != null ? h.getDiaChi() : "");
        edtViDo.setText(h.getBanDoViDo() != null ? String.valueOf(h.getBanDoViDo()) : "");
        edtKinhDo.setText(h.getBanDoKinhDo() != null ? String.valueOf(h.getBanDoKinhDo()) : "");
        edtBanDoGhiChu.setText(h.getBanDoGhiChu() != null ? h.getBanDoGhiChu() : "");
        edtSoTin.setText(String.valueOf(h.getTrangChuSoTin()));
        edtSoDg.setText(String.valueOf(h.getTrangChuSoDanhGia()));
        swNhanh.setChecked(h.getTrangChuHienNhanh() != 0);
        swTin.setChecked(h.getTrangChuHienTin() != 0);
        swDg.setChecked(h.getTrangChuHienDanhGia() != 0);
        swDv.setChecked(h.getTrangChuHienDichVu() != 0);
        swViTri.setChecked(h.getTrangChuHienViTri() != 0);
        refreshHeroPreview(h.getTrangChuAnhNen());
        refreshHeroPreviewNv(h.getTrangChuAnhNenNv());
        if (edtAppNenKhach != null) {
            edtAppNenKhach.setText(h.getAppNenKhach() != null ? h.getAppNenKhach() : "");
        }
        if (edtAppNenNhanVien != null) {
            edtAppNenNhanVien.setText(h.getAppNenNhanVien() != null ? h.getAppNenNhanVien() : "");
        }
        if (edtAppNenAdmin != null) {
            edtAppNenAdmin.setText(h.getAppNenAdmin() != null ? h.getAppNenAdmin() : "");
        }
    }

    private void refreshHeroPreview(String ref) {
        if (imgPreview == null) {
            return;
        }
        if (ref == null || ref.trim().isEmpty()) {
            imgPreview.setImageDrawable(null);
            imgPreview.setVisibility(View.GONE);
            return;
        }
        imgPreview.setVisibility(View.VISIBLE);
        imgPreview.setImageDrawable(null);
        RoomImageUtils.loadInto(imgPreview, ref);
    }

    private void refreshHeroPreviewNv(String ref) {
        if (imgPreviewNv == null) {
            return;
        }
        if (ref == null || ref.trim().isEmpty()) {
            imgPreviewNv.setImageDrawable(null);
            imgPreviewNv.setVisibility(View.GONE);
            return;
        }
        imgPreviewNv.setVisibility(View.VISIBLE);
        imgPreviewNv.setImageDrawable(null);
        RoomImageUtils.loadInto(imgPreviewNv, ref);
    }

    private void save() {
        HomestayThongTin h = dao.getHomestay();
        h.setTrangChuAnhNen(textOf(edtAnh));
        h.setTrangChuTieuDe(textOf(edtTieuDe));
        h.setTrangChuCamKet(textOf(edtCamKet));
        h.setTrangChuAnhNenNv(edtAnhNv != null ? textOf(edtAnhNv) : "");
        h.setTrangChuTieuDeNv(edtTieuDeNv != null ? textOf(edtTieuDeNv) : "");
        h.setTrangChuCamKetNv(edtCamKetNv != null ? textOf(edtCamKetNv) : "");
        h.setDiaChi(textOf(edtDiaChi));
        h.setBanDoViDo(parseDoubleField(edtViDo));
        h.setBanDoKinhDo(parseDoubleField(edtKinhDo));
        h.setBanDoGhiChu(textOf(edtBanDoGhiChu));
        h.setTrangChuHienNhanh(swNhanh.isChecked() ? 1 : 0);
        h.setTrangChuHienTin(swTin.isChecked() ? 1 : 0);
        h.setTrangChuHienDanhGia(swDg.isChecked() ? 1 : 0);
        h.setTrangChuHienDichVu(swDv.isChecked() ? 1 : 0);
        h.setTrangChuHienViTri(swViTri.isChecked() ? 1 : 0);
        h.setTrangChuSoTin(parseInt(edtSoTin, h.getTrangChuSoTin()));
        h.setTrangChuSoDanhGia(parseInt(edtSoDg, h.getTrangChuSoDanhGia()));
        h.setAppNenKhach(edtAppNenKhach != null ? textOf(edtAppNenKhach) : "");
        h.setAppNenNhanVien(edtAppNenNhanVien != null ? textOf(edtAppNenNhanVien) : "");
        h.setAppNenAdmin(edtAppNenAdmin != null ? textOf(edtAppNenAdmin) : "");
        String expectedGuestHero = h.getTrangChuAnhNen() != null ? h.getTrangChuAnhNen().trim() : "";
        if (dao.updateTrangChuCaiDat(h)) {
            HomestayThongTin latest = dao.getHomestay();
            String actualGuestHero = latest.getTrangChuAnhNen() != null ? latest.getTrangChuAnhNen().trim() : "";
            if (!expectedGuestHero.equals(actualGuestHero)) {
                Toast.makeText(requireContext(), "Đã lưu nhưng ảnh banner chưa đồng bộ. Vui lòng lưu lại.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireContext(), R.string.trang_chu_cai_dat_saved, Toast.LENGTH_SHORT).show();
            }
            refreshData();
            View v = getView();
            if (v != null) {
                v.post(() -> {
                    if (dao != null && edtAnh != null) {
                        refreshData();
                    }
                });
            }
            if (getActivity() instanceof MainActivity) {
                MainActivity ma = (MainActivity) getActivity();
                ma.notifyHomestayConfigChanged();
            }
        } else {
            Toast.makeText(requireContext(), R.string.trang_chu_cai_dat_save_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    private static Double parseDoubleField(TextInputEditText e) {
        if (e.getText() == null) {
            return null;
        }
        String s = e.getText().toString().trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(s.replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String textOf(TextInputEditText e) {
        if (e.getText() == null) {
            return "";
        }
        return e.getText().toString().trim();
    }

    private static int parseInt(TextInputEditText e, int fallback) {
        if (e.getText() == null || TextUtils.isEmpty(e.getText().toString().trim())) {
            return fallback;
        }
        try {
            return Integer.parseInt(e.getText().toString().trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}

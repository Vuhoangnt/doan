package com.example.doan;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.doan.DAO.DatPhongDAO;
import com.example.doan.DAO.ThanhToanDAO;
import com.example.doan.DAO.PhongDAO;
import com.example.doan.adapter.DatPhongListAdapter;
import com.example.doan.model.DatPhong;
import com.example.doan.model.ThanhToan;
import com.example.doan.model.PhongFull;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.example.doan.util.PeakPricingUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DonCuaToiFragment extends Fragment implements DataRefreshable {

    public static final String ARG_OPEN_DAT_PHONG_ID = "argOpenDatPhongId";

    private ListView listView;
    private TextView txtEmpty;
    private MaterialCardView cardEmpty;
    private DatPhongDAO dao;
    private List<DatPhong> list;
    private DatPhongListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_don_cua_toi, container, false);
        listView = view.findViewById(R.id.listDonCuaToi);
        txtEmpty = view.findViewById(R.id.txtEmptyDon);
        cardEmpty = view.findViewById(R.id.cardEmptyDonWrap);
        dao = new DatPhongDAO(requireContext());
        list = new ArrayList<>();
        adapter = new DatPhongListAdapter(requireContext(), list);
        listView.setAdapter(adapter);
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
        SessionManager session = new SessionManager(requireContext());
        if (!session.isLoggedIn() || !session.isKhach()) {
            list.clear();
            adapter.notifyDataSetChanged();
            listView.setVisibility(View.GONE);
            showEmptyCard(R.string.don_cua_toi_need_login);
            return;
        }
        List<DatPhong> data = dao.getByTaiKhoanID(session.getTaiKhoanId());
        Map<Integer, Double> daThu = new HashMap<>();
        ThanhToanDAO ttDao = new ThanhToanDAO(requireContext());
        for (DatPhong d : data) {
            daThu.put(d.getDatPhongID(), ttDao.getTongDaThu(d.getDatPhongID()));
        }
        adapter.setDaThuMap(daThu);
        adapter.setGuestOrdersMode(true, this::onGuestRequestedCheckout);
        adapter.setGuestDepositListener(this::onGuestRequestedDeposit);
        adapter.updateData(data);
        if (data.isEmpty()) {
            listView.setVisibility(View.GONE);
            showEmptyCard(R.string.don_cua_toi_empty);
        } else {
            cardEmpty.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
        Bundle args = getArguments();
        int openId = 0;
        if (args != null) {
            openId = args.getInt(ARG_OPEN_DAT_PHONG_ID, 0);
            if (openId > 0) {
                args.remove(ARG_OPEN_DAT_PHONG_ID);
            }
        }
        if (openId > 0 && !data.isEmpty()) {
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).getDatPhongID() == openId) {
                    final int pos = i;
                    listView.post(() -> listView.setSelectionFromTop(pos, 0));
                    break;
                }
            }
        }
    }

    private void onGuestRequestedCheckout(DatPhong d) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.don_cua_toi_btn_tra_phong)
                .setMessage(R.string.don_cua_toi_tra_phong_confirm)
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.don_cua_toi_btn_tat_toan, (di, w) -> showTatToanDialog(d))
                .setPositiveButton(R.string.don_cua_toi_btn_tra_phong, (di, w) -> doGuestCheckout(d))
                .show();
    }

    private void onGuestRequestedDeposit(DatPhong d) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View v = inflater.inflate(R.layout.dialog_khach_gui_coc, null, false);
        TextView txtReq = v.findViewById(R.id.txtGuiCocRequired);
        Spinner spinner = v.findViewById(R.id.spinnerGuiCocPhuongThuc);
        TextInputEditText edtTien = v.findViewById(R.id.edtGuiCocSoTien);

        ThanhToanDAO ttDao = new ThanhToanDAO(requireContext());
        double daThu = ttDao.getTongDaThu(d.getDatPhongID());
        double cocMin = d.getTongTien() * 0.2;
        double canThuThem = Math.max(0, cocMin - daThu);
        if (txtReq != null) {
            txtReq.setText(String.format(Locale.getDefault(),
                    "Cọc tối thiểu (20%%): %,.0f đ — Đã thu: %,.0f đ — Cần cọc thêm: %,.0f đ",
                    cocMin, daThu, canThuThem));
        }

        String[] methods = getResources().getStringArray(R.array.payment_methods);
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, methods);
        spinner.setAdapter(spinAdapter);
        // Gửi cọc từ app: mặc định chuyển khoản và không cho đổi.
        int transferIdx = 1; // payment_methods[1] = "Chuyển khoản ngân hàng"
        if (transferIdx >= 0 && transferIdx < methods.length) {
            spinner.setSelection(transferIdx);
        }
        spinner.setEnabled(false);
        if (edtTien != null) {
            edtTien.setText(canThuThem > 0 ? String.format(Locale.getDefault(), "%.0f", canThuThem) : "");
        }

        AlertDialog dlg = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.khach_gui_coc_title)
                .setView(v)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.khach_gui_coc_title, null)
                .create();

        dlg.setOnShowListener(di -> dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
            String sTien = edtTien.getText() != null ? edtTien.getText().toString().trim() : "";
            double soTien;
            try {
                soTien = Double.parseDouble(sTien.replace(",", "."));
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), R.string.khach_gui_coc_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
            if (soTien <= 0) {
                Toast.makeText(requireContext(), R.string.khach_gui_coc_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
            if (soTien + 1.0 < canThuThem) {
                Toast.makeText(requireContext(),
                        getString(R.string.khach_gui_coc_too_low, canThuThem),
                        Toast.LENGTH_LONG).show();
                return;
            }
            String pt = (transferIdx >= 0 && transferIdx < methods.length) ? methods[transferIdx] : "Chuyển khoản";
            String ngay = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            ThanhToan t = new ThanhToan();
            t.setDatPhongID(d.getDatPhongID());
            t.setSoTien(soTien);
            t.setPhuongThuc(pt);
            t.setNgayThanhToan(ngay);
            t.setTrangThai(ThanhToanDAO.TT_CHUA_THANH_TOAN);
            t.setNhanVienGhiNhanID(null);

            if (ttDao.insert(t) > 0) {
                Toast.makeText(requireContext(), R.string.khach_gui_coc_sent, Toast.LENGTH_SHORT).show();
                dlg.dismiss();
                loadData();
            } else {
                Toast.makeText(requireContext(), R.string.don_cua_toi_tra_phong_fail, Toast.LENGTH_SHORT).show();
            }
        }));
        dlg.show();
    }

    private void doGuestCheckout(DatPhong d) {
        SessionManager s = new SessionManager(requireContext());
        int r = dao.guestRequestCheckout(d.getDatPhongID(), s.getTaiKhoanId());
        if (r <= 0) {
            Toast.makeText(requireContext(), R.string.don_cua_toi_tra_phong_fail, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(requireContext(), R.string.don_cua_toi_checkout_sent, Toast.LENGTH_SHORT).show();
        loadData();
    }

    private void showTatToanDialog(DatPhong d) {
        // Cập nhật tổng tiền nếu trả sớm/muộn (ngày thực tế = hôm nay) để số liệu đúng.
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        dao.applyCheckoutAdjustmentIfNeeded(d.getDatPhongID(), today);
        DatPhong updated = dao.getByIdWithTenPhong(d.getDatPhongID());
        if (updated == null) {
            Toast.makeText(requireContext(), R.string.don_cua_toi_tra_phong_fail, Toast.LENGTH_SHORT).show();
            return;
        }

        ThanhToanDAO ttDao = new ThanhToanDAO(requireContext());
        double daThu = ttDao.getTongDaThu(updated.getDatPhongID());
        double tongDv = dao.getTongTienDichVuPublic(updated.getDatPhongID());

        PhongFull p = new PhongDAO(requireContext()).getPhongFullById(updated.getPhongID());
        double giaDem = (p != null)
                ? PeakPricingUtil.demGiaTheoGio(p,
                updated.getGioNhan() != null ? updated.getGioNhan() : "14:00",
                updated.getGioTra() != null ? updated.getGioTra() : "12:00")
                : 0;
        double tongPhong = giaDem > 0 ? giaDem * Math.max(1, updated.getSoDem()) : 0;
        double phiPhat = Math.max(0, updated.getTongTien() - tongPhong - tongDv);

        double cocMin = updated.getTongTien() * 0.2;
        double conLai = Math.max(0, updated.getTongTien() - daThu);

        StringBuilder sb = new StringBuilder();
        sb.append("Mã: ").append(updated.getMaDatPhong()).append('\n');
        sb.append(String.format(Locale.getDefault(), "Tiền phòng: %,.0f đ\n", tongPhong));
        sb.append(String.format(Locale.getDefault(), "Dịch vụ: %,.0f đ\n", tongDv));
        if (phiPhat > 1.0) {
            sb.append(String.format(Locale.getDefault(), "Điều chỉnh trả sớm/muộn: %,.0f đ\n", phiPhat));
        }
        sb.append(String.format(Locale.getDefault(), "Tổng cần thanh toán: %,.0f đ\n", updated.getTongTien()));
        sb.append(String.format(Locale.getDefault(), "Cọc tối thiểu (20%%): %,.0f đ\n", cocMin));
        sb.append(String.format(Locale.getDefault(), "Đã thu (gồm cọc): %,.0f đ\n", daThu));
        sb.append(String.format(Locale.getDefault(), "Còn phải trả: %,.0f đ", conLai));

        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_khach_tat_toan, null, false);
        TextView body = v.findViewById(R.id.txtTatToanBody);
        if (body != null) {
            body.setText(sb.toString());
        }

        AlertDialog dlg = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.khach_tat_toan_title)
                .setView(v)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.khach_tat_toan_send, null)
                .create();

        dlg.setOnShowListener(di -> dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
            if (conLai <= 1.0) {
                Toast.makeText(requireContext(), R.string.khach_tat_toan_nothing_due, Toast.LENGTH_SHORT).show();
                dlg.dismiss();
                return;
            }
            // Tạo giao dịch chờ xác nhận cho số còn thiếu (chuyển khoản).
            ThanhToan t = new ThanhToan();
            t.setDatPhongID(updated.getDatPhongID());
            t.setSoTien(conLai);
            t.setPhuongThuc("Chuyển khoản ngân hàng");
            t.setNgayThanhToan(today);
            t.setTrangThai(ThanhToanDAO.TT_CHUA_THANH_TOAN);
            t.setNhanVienGhiNhanID(null);
            if (ttDao.insert(t) > 0) {
                Toast.makeText(requireContext(), R.string.khach_tat_toan_sent, Toast.LENGTH_SHORT).show();
                dlg.dismiss();
                loadData();
            } else {
                Toast.makeText(requireContext(), R.string.don_cua_toi_tra_phong_fail, Toast.LENGTH_SHORT).show();
            }
        }));
        dlg.show();
    }

    private void showEmptyCard(@StringRes int messageRes) {
        txtEmpty.setText(messageRes);
        cardEmpty.setVisibility(View.VISIBLE);
    }
}

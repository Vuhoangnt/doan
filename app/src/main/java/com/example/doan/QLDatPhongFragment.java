package com.example.doan;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.doan.DAO.DatPhongDAO;
import com.example.doan.DAO.ThanhToanDAO;
import com.example.doan.adapter.DatPhongListAdapter;
import com.example.doan.model.DatPhong;
import com.example.doan.model.ThanhToan;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class QLDatPhongFragment extends Fragment implements DataRefreshable {

    /** Mở sẵn hộp thoại chi tiết đơn (sau khi điều hướng từ thông báo). */
    public static final String ARG_OPEN_DAT_PHONG_ID = "argOpenDatPhongId";

    private ListView listView;
    private TabLayout tabLayout;
    private DatPhongDAO dao;
    private ThanhToanDAO ttDao;
    private final List<DatPhong> allOrders = new ArrayList<>();
    private List<DatPhong> list;
    private DatPhongListAdapter adapter;
    private int currentTabPosition;
    private final Map<Integer, Double> daThuMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ql_dat_phong, container, false);
        listView = view.findViewById(R.id.listQlDatPhong);
        tabLayout = view.findViewById(R.id.tabQlDatPhong);
        TextView txtTitle = view.findViewById(R.id.txtQlDonTitle);
        TextView txtSubtitle = view.findViewById(R.id.txtQlDonSubtitle);
        SessionManager sessionHeader = new SessionManager(requireContext());
        if (sessionHeader.isNhanVien()) {
            txtTitle.setText(R.string.le_tan_don_title);
            txtSubtitle.setText(R.string.le_tan_don_hint);
        } else {
            txtTitle.setText(R.string.admin_ql_don_title);
            txtSubtitle.setText(R.string.admin_ql_don_hint);
        }

        dao = new DatPhongDAO(requireContext());
        ttDao = new ThanhToanDAO(requireContext());
        list = new ArrayList<>();
        adapter = new DatPhongListAdapter(requireContext(), list, daThuMap);
        listView.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText(R.string.ql_dat_tab_all));
        for (String s : DatPhongDAO.allOrderStatuses()) {
            tabLayout.addTab(tabLayout.newTab().setText(s));
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabPosition = tab.getPosition();
                applyFilter();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        listView.setOnItemClickListener((parent, v, position, id) -> {
            SessionManager s = new SessionManager(requireContext());
            if (!s.isAdmin() && !s.isNhanVien()) {
                return;
            }
            DatPhong d = list.get(position);
            showChiTietDon(d, s);
        });
        refreshData();
        view.post(this::maybeOpenPendingDatPhong);
        return view;
    }

    private void maybeOpenPendingDatPhong() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }
        int id = args.getInt(ARG_OPEN_DAT_PHONG_ID, 0);
        if (id <= 0) {
            return;
        }
        args.remove(ARG_OPEN_DAT_PHONG_ID);
        SessionManager s = new SessionManager(requireContext());
        if (!s.isAdmin() && !s.isNhanVien()) {
            return;
        }
        for (DatPhong d : allOrders) {
            if (d.getDatPhongID() == id) {
                showChiTietDon(d, s);
                return;
            }
        }
        DatPhong d = dao.getByIdWithTenPhong(id);
        if (d != null) {
            showChiTietDon(d, s);
        }
    }

    private void showChiTietDon(DatPhong d, SessionManager session) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dlgView = inflater.inflate(R.layout.dialog_le_tan_chi_tiet_don, null, false);
        TextView txtChiTiet = dlgView.findViewById(R.id.txtChiTietDon);
        TextView txtCocHint = dlgView.findViewById(R.id.txtChiTietCocHint);
        MaterialButton btnStatus = dlgView.findViewById(R.id.btnDonDoiTrangThai);
        MaterialButton btnPay = dlgView.findViewById(R.id.btnDonThanhToan);
        MaterialButton btnHist = dlgView.findViewById(R.id.btnDonLichSuTT);

        txtChiTiet.setText(buildChiTietText(d));
        if (txtCocHint != null) {
            String normalized = DatPhongDAO.normalizeStatus(d.getTrangThai());
            if (DatPhongDAO.TT_CHO_XAC_NHAN.equals(normalized)) {
                txtCocHint.setVisibility(View.VISIBLE);
            } else {
                txtCocHint.setVisibility(View.GONE);
            }
        }

        AlertDialog dlg = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(d.getMaDatPhong())
                .setView(dlgView)
                .setNegativeButton("Đóng", null)
                .create();

        btnStatus.setOnClickListener(v -> {
            dlg.dismiss();
            showDoiTrangThaiDialog(d, session);
        });
        btnPay.setOnClickListener(v -> {
            dlg.dismiss();
            showThanhToanDialog(inflater, d, session);
        });
        btnHist.setOnClickListener(v -> showLichSuThanhToan(d, session));
        dlg.show();
    }

    private void showDoiTrangThaiDialog(DatPhong d, SessionManager session) {
        List<String> next = DatPhongDAO.allowedNextStatuses(d.getTrangThai());
        List<String> items = new ArrayList<>(next);
        items.add(getString(R.string.order_status_other_full));
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(d.getMaDatPhong())
                .setItems(items.toArray(new String[0]), (dialog, which) -> {
                    if (which == items.size() - 1) {
                        showFullTrangThaiDialog(d, session);
                        return;
                    }
                    applyTrangThai(d, session, next.get(which));
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showFullTrangThaiDialog(DatPhong d, SessionManager session) {
        String[] all = DatPhongDAO.allOrderStatuses();
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(d.getMaDatPhong())
                .setItems(all, (dialog, which) -> applyTrangThai(d, session, all[which]))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void applyTrangThai(DatPhong d, SessionManager session, String tt) {
        try {
            Integer nvId = null;
            if (session != null && (session.isAdmin() || session.isNhanVien())) {
                nvId = session.getTaiKhoanId();
            }

            String cur = DatPhongDAO.normalizeStatus(d.getTrangThai());
            String next = DatPhongDAO.normalizeStatus(tt);

            // Trả phòng sớm/muộn: tự tính lại tổng tiền trước khi đóng đơn.
            if (DatPhongDAO.TT_DANG_O.equals(cur) && DatPhongDAO.TT_DA_TRA_PHONG.equals(next)) {
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                dao.applyCheckoutAdjustmentIfNeeded(d.getDatPhongID(), today);
            }

            if (DatPhongDAO.TT_CHO_XAC_NHAN.equals(cur) && DatPhongDAO.TT_DA_XAC_NHAN.equals(next)) {
                double tongDon = d.getTongTien();
                double cocMin = tongDon * 0.2;
                double daThu = ttDao.getTongDaThu(d.getDatPhongID());
                if (daThu + 1.0 < cocMin) {
                    Toast.makeText(requireContext(),
                            getString(R.string.le_tan_deposit_block_confirm_fmt, cocMin, daThu),
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }

            if (dao.updateTrangThaiVaNhanVien(d.getDatPhongID(), tt, nvId) > 0) {
                Toast.makeText(requireContext(), "Đã cập nhật: " + tt, Toast.LENGTH_SHORT).show();
                refreshOrdersFromDb();
                currentTabPosition = 0;
                if (tabLayout != null && tabLayout.getTabCount() > 0) {
                    TabLayout.Tab first = tabLayout.getTabAt(0);
                    if (first != null) {
                        if (tabLayout.getSelectedTabPosition() != 0) {
                            tabLayout.selectTab(first);
                        } else {
                            applyFilter();
                        }
                    } else {
                        applyFilter();
                    }
                } else {
                    applyFilter();
                }
            }
        } catch (Throwable ex) {
            StringBuilder sb = new StringBuilder();
            sb.append("Lỗi đổi trạng thái\n\n");
            sb.append(ex.getClass().getName());
            if (ex.getMessage() != null && !ex.getMessage().trim().isEmpty()) {
                sb.append(": ").append(ex.getMessage());
            }
            sb.append("\n\nStacktrace (rút gọn):\n");
            StackTraceElement[] st = ex.getStackTrace();
            int n = Math.min(8, st != null ? st.length : 0);
            for (int i = 0; i < n; i++) {
                sb.append("• ").append(st[i].toString()).append('\n');
            }
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Lỗi")
                    .setMessage(sb.toString().trim())
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    private void showThanhToanDialog(LayoutInflater inflater, DatPhong d, SessionManager session) {
        try {
            View payView = inflater.inflate(R.layout.dialog_le_tan_thanh_toan, null, false);
            TextView txtCoc = payView.findViewById(R.id.txtThanhToanCoc);
            TextView txtTong = payView.findViewById(R.id.txtThanhToanTongDon);
            TextView txtDaThu = payView.findViewById(R.id.txtThanhToanDaThu);
            TextView txtConLai = payView.findViewById(R.id.txtThanhToanConLai);
            Spinner spinner = payView.findViewById(R.id.spinnerPhuongThuc);
            TextInputEditText edtTien = payView.findViewById(R.id.edtThanhToanSoTien);

        double tongDon = d.getTongTien();
        double daThu = ttDao.getTongDaThu(d.getDatPhongID());
        double conLai = Math.max(0, tongDon - daThu);

        String normalized = DatPhongDAO.normalizeStatus(d.getTrangThai());
        Integer pendingId = ttDao.getLatestPendingId(d.getDatPhongID());
        if (txtCoc != null) {
            if (DatPhongDAO.TT_CHO_XAC_NHAN.equals(normalized)) {
                double cocMin = tongDon * 0.2;
                double canThuThem = Math.max(0, cocMin - daThu);
                txtCoc.setText(getString(R.string.le_tan_deposit_required_fmt, cocMin, daThu, canThuThem));
                txtCoc.setVisibility(View.VISIBLE);
                if (edtTien != null) {
                    edtTien.setText(canThuThem > 0 ? String.format(Locale.getDefault(), "%.0f", canThuThem) : "");
                }
            } else {
                txtCoc.setVisibility(View.GONE);
            }
        }

        txtTong.setText(String.format(Locale.getDefault(), "Tổng đơn: %,.0f đ", tongDon));
        txtDaThu.setText(String.format(Locale.getDefault(), "Đã thu: %,.0f đ", daThu));
        txtConLai.setText(String.format(Locale.getDefault(), "Còn lại: %,.0f đ", conLai));

        String[] methods = getResources().getStringArray(R.array.payment_methods);
            if (spinner != null) {
            ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, methods);
            spinner.setAdapter(spinAdapter);
                // Với đơn chờ xác nhận: mặc định chọn chuyển khoản nhưng cho phép đổi sang tiền mặt.
                if (DatPhongDAO.TT_CHO_XAC_NHAN.equals(normalized)) {
                    int transferIdx = 1; // payment_methods[1] = "Chuyển khoản ngân hàng"
                    if (transferIdx >= 0 && methods != null && transferIdx < methods.length) {
                        spinner.setSelection(transferIdx);
                    }
                    spinner.setEnabled(true);
                }
        }

        AlertDialog payDlg = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.le_tan_action_payment)
                .setView(payView)
                .setPositiveButton(R.string.le_tan_payment_btn_ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        payDlg.setOnShowListener(dialog -> payDlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (edtTien == null || spinner == null) {
                Toast.makeText(requireContext(), "Lỗi giao diện thanh toán — thử mở lại.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Nếu đơn đang Chờ xác nhận và có giao dịch cọc Chưa thanh toán => nút này dùng để XÁC NHẬN CỌC.
            if (DatPhongDAO.TT_CHO_XAC_NHAN.equals(normalized) && pendingId != null) {
                int nvId = session.getTaiKhoanId();
                try {
                    int pos = spinner.getSelectedItemPosition();
                    String pt = (methods != null && methods.length > 0)
                            ? (pos >= 0 && pos < methods.length ? methods[pos] : methods[0])
                            : "Chuyển khoản ngân hàng";
                    if (ttDao.confirmDaThanhToan(pendingId, nvId, pt) > 0) {
                        // sau khi xác nhận cọc, tự xác nhận đơn nếu đủ 20%
                        DatPhong updated = dao.getByIdWithTenPhong(d.getDatPhongID());
                        if (updated != null) {
                            double cocMin = updated.getTongTien() * 0.2;
                            double thuNow = ttDao.getTongDaThu(updated.getDatPhongID());
                            if (thuNow + 1.0 >= cocMin) {
                                dao.updateTrangThaiVaNhanVien(updated.getDatPhongID(), DatPhongDAO.TT_DA_XAC_NHAN, nvId);
                            }
                        }
                        Toast.makeText(requireContext(), "Đã xác nhận nhận cọc", Toast.LENGTH_SHORT).show();
                        payDlg.dismiss();
                        reload();
                    } else {
                        Toast.makeText(requireContext(), "Không xác nhận được cọc", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    Toast.makeText(requireContext(),
                            "Lỗi khi xác nhận cọc: " + ex.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
                return;
            }

            String sTien = edtTien.getText() != null ? edtTien.getText().toString().trim() : "";
            double soTien;
            try {
                soTien = Double.parseDouble(sTien.replace(",", "."));
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), R.string.le_tan_payment_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
            if (soTien <= 0) {
                Toast.makeText(requireContext(), R.string.le_tan_payment_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
            int pos = spinner.getSelectedItemPosition();
            String pt = (methods != null && methods.length > 0)
                    ? (pos >= 0 && pos < methods.length ? methods[pos] : methods[0])
                    : "Khác";

            String ngay = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            ThanhToan t = new ThanhToan();
            t.setDatPhongID(d.getDatPhongID());
            t.setSoTien(soTien);
            t.setPhuongThuc(pt);
            t.setNgayThanhToan(ngay);
            t.setTrangThai(ThanhToanDAO.TT_DA_THANH_TOAN);
            int nvId = session.getTaiKhoanId();
            if (nvId > 0) {
                t.setNhanVienGhiNhanID(nvId);
            }

            try {
                if (ttDao.insert(t) > 0) {
                    // Nếu đơn đang chờ xác nhận: thu (cọc hoặc bất kỳ khoản nào) đủ 20% thì tự xác nhận đơn.
                    if (DatPhongDAO.TT_CHO_XAC_NHAN.equals(normalized)) {
                        DatPhong updated = dao.getByIdWithTenPhong(d.getDatPhongID());
                        if (updated != null) {
                            double cocMin = updated.getTongTien() * 0.2;
                            double thuNow = ttDao.getTongDaThu(updated.getDatPhongID());
                            if (thuNow + 1.0 >= cocMin) {
                                int nvId2 = session.getTaiKhoanId();
                                dao.updateTrangThaiVaNhanVien(updated.getDatPhongID(), DatPhongDAO.TT_DA_XAC_NHAN, nvId2);
                            }
                        }
                    }
                    Toast.makeText(requireContext(), R.string.le_tan_payment_saved, Toast.LENGTH_SHORT).show();
                    payDlg.dismiss();
                    reload();
                } else {
                    Toast.makeText(requireContext(), "Không lưu được", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ex) {
                Toast.makeText(requireContext(),
                        "Lỗi khi lưu thanh toán: " + ex.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }));
            payDlg.show();
        } catch (Throwable ex) {
            StringBuilder sb = new StringBuilder();
            sb.append("Lỗi mở thanh toán\n\n");
            sb.append(ex.getClass().getName());
            if (ex.getMessage() != null && !ex.getMessage().trim().isEmpty()) {
                sb.append(": ").append(ex.getMessage());
            }
            sb.append("\n\nStacktrace (rút gọn):\n");
            StackTraceElement[] st = ex.getStackTrace();
            int n = Math.min(8, st != null ? st.length : 0);
            for (int i = 0; i < n; i++) {
                sb.append("• ").append(st[i].toString()).append('\n');
            }
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Lỗi")
                    .setMessage(sb.toString().trim())
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    private void showLichSuThanhToan(DatPhong d, SessionManager session) {
        List<ThanhToan> rows = ttDao.getByDatPhongId(d.getDatPhongID());
        if (rows.isEmpty()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.le_tan_action_payment_history)
                    .setMessage("Chưa có giao dịch.")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }
        List<String> items = new ArrayList<>();
        for (ThanhToan t : rows) {
            String nv = "";
            if (t.getNhanVienGhiNhanID() != null) {
                String ten = ttDao.getTenNhanVienGhiNhan(t.getNhanVienGhiNhanID());
                if (ten != null) {
                    nv = " — NV: " + ten;
                }
            }
            items.add(String.format(Locale.getDefault(),
                    "%,.0f đ — %s — %s — %s%s",
                    t.getSoTien(), t.getPhuongThuc(), t.getNgayThanhToan(), t.getTrangThai(), nv));
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.le_tan_action_payment_history)
                .setItems(items.toArray(new String[0]), (dialog, which) -> {
                    ThanhToan pick = rows.get(which);
                    if (!session.isAdmin() && !session.isNhanVien()) {
                        return;
                    }
                    String st = pick.getTrangThai() != null ? pick.getTrangThai().trim() : "";
                    if (ThanhToanDAO.TT_DA_THANH_TOAN.equals(st)) {
                        return;
                    }
                    if (!ThanhToanDAO.TT_CHUA_THANH_TOAN.equals(st)) {
                        Toast.makeText(requireContext(), "Giao dịch không ở trạng thái chờ xác nhận.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int nvId = session.getTaiKhoanId();
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Xác nhận đã thu?")
                            .setMessage(items.get(which))
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton("Xác nhận", (d2, w2) -> {
                                // Khi xác nhận từ lịch sử, mặc định giữ nguyên phương thức đã lưu (khách gửi: chuyển khoản).
                                if (ttDao.confirmDaThanhToan(pick.getThanhToanID(), nvId) > 0) {
                                    Toast.makeText(requireContext(), "Đã xác nhận đã thu", Toast.LENGTH_SHORT).show();
                                    // Nếu đơn đang "Chờ xác nhận" và sau khi xác nhận đã thu đủ cọc (>=20%), tự xác nhận đơn.
                                    DatPhong updated = dao.getByIdWithTenPhong(d.getDatPhongID());
                                    if (updated != null) {
                                        String stDon = DatPhongDAO.normalizeStatus(updated.getTrangThai());
                                        if (DatPhongDAO.TT_CHO_XAC_NHAN.equals(stDon)) {
                                            double cocMin = updated.getTongTien() * 0.2;
                                            double daThu = ttDao.getTongDaThu(updated.getDatPhongID());
                                            if (daThu + 1.0 >= cocMin) {
                                                dao.updateTrangThaiVaNhanVien(updated.getDatPhongID(), DatPhongDAO.TT_DA_XAC_NHAN, nvId);
                                            }
                                        } else if (DatPhongDAO.TT_DANG_O.equals(stDon)
                                                && ttDao.isOrderFullyPaid(updated.getDatPhongID(), updated.getTongTien())) {
                                            // Nếu đơn đang ở và sau khi xác nhận đã thu thì đã đủ tiền, tự đóng đơn.
                                            dao.updateTrangThaiVaNhanVien(updated.getDatPhongID(), DatPhongDAO.TT_DA_TRA_PHONG, nvId);
                                        }
                                    }
                                    reload();
                                } else {
                                    Toast.makeText(requireContext(), "Không cập nhật được", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .show();
                })
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private String buildChiTietText(DatPhong d) {
        StringBuilder sb = new StringBuilder();
        sb.append("Mã: ").append(d.getMaDatPhong()).append('\n');
        String tp = d.getTenPhong();
        sb.append("Phòng: ").append(tp != null && !tp.isEmpty() ? tp : ("ID " + d.getPhongID())).append('\n');
        sb.append("Khách: ").append(d.getKhachTen()).append(" — ").append(d.getKhachDienThoai()).append('\n');
        if (!TextUtils.isEmpty(d.getKhachEmail())) {
            sb.append("Email: ").append(d.getKhachEmail()).append('\n');
        }
        sb.append("Nhận → Trả: ").append(d.getNgayNhan()).append(" → ").append(d.getNgayTra())
                .append(" (").append(d.getSoDem()).append(" đêm)\n");
        String gn = d.getGioNhan();
        String gt = d.getGioTra();
        if (!TextUtils.isEmpty(gn) && !TextUtils.isEmpty(gt)) {
            sb.append("Giờ: ").append(gn).append(" → ").append(gt).append('\n');
        }
        sb.append("Số người: ").append(d.getSoNguoi()).append('\n');
        sb.append(String.format(Locale.getDefault(), "Tổng: %,.0f đ\n", d.getTongTien()));
        sb.append("Trạng thái: ").append(DatPhongDAO.normalizeStatus(d.getTrangThai())).append('\n');
        String nv = d.getTenNhanVienXuLy();
        if (nv != null && !nv.isEmpty()) {
            sb.append("NV xử lý đơn: ").append(nv).append('\n');
        }
        double daThu = ttDao.getTongDaThu(d.getDatPhongID());
        double cocMin = d.getTongTien() * 0.2;
        sb.append(String.format(Locale.getDefault(), "Cọc tối thiểu (20%%): %,.0f đ\n", cocMin));
        sb.append(String.format(Locale.getDefault(),
                "Đã thu: %,.0f đ — Còn: %,.0f đ",
                daThu, Math.max(0, d.getTongTien() - daThu)));
        return sb.toString();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void refreshData() {
        reload();
    }

    private void reload() {
        refreshOrdersFromDb();
        applyFilter();
    }

    /** Đọc lại đơn + số tiền đã thu từ CSDL (giữ nguyên tab đang chọn). */
    private void refreshOrdersFromDb() {
        allOrders.clear();
        allOrders.addAll(dao.getAllWithTenPhong());
        daThuMap.clear();
        for (DatPhong d : allOrders) {
            daThuMap.put(d.getDatPhongID(), ttDao.getTongDaThu(d.getDatPhongID()));
        }
        adapter.setDaThuMap(daThuMap);
    }

    private void applyFilter() {
        list.clear();
        if (currentTabPosition <= 0) {
            list.addAll(allOrders);
        } else {
            int idx = currentTabPosition - 1;
            String[] opts = DatPhongDAO.allOrderStatuses();
            if (idx >= 0 && idx < opts.length) {
                String wantN = DatPhongDAO.normalizeStatus(opts[idx]);
                for (DatPhong d : allOrders) {
                    if (DatPhongDAO.normalizeStatus(d.getTrangThai()).equals(wantN)) {
                        list.add(d);
                    }
                }
            } else {
                list.addAll(allOrders);
            }
        }
        adapter.notifyDataSetChanged();
    }

}

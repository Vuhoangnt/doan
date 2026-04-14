package com.example.doan;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.doan.DAO.TinTucDAO;
import com.example.doan.adapter.TinTucAdapter;
import com.example.doan.model.TinTuc;
import com.example.doan.util.DatePickerHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TinTucFragment extends Fragment implements DataRefreshable {

    private ListView listView;
    private FloatingActionButton fab;
    private TinTucDAO dao;
    private List<TinTuc> list;
    private TinTucAdapter adapter;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tin_tuc, container, false);
        listView = view.findViewById(R.id.listTinTuc);
        fab = view.findViewById(R.id.fabAddTinTuc);
        dao = new TinTucDAO(requireContext());
        session = new SessionManager(requireContext());
        if (session.isAdmin()) {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(v -> showTinTucDialog(null));
            listView.setOnItemClickListener((p, v, pos, id) -> {
                if (list != null && pos < list.size()) {
                    showTinTucDialog(list.get(pos));
                }
            });
        } else {
            fab.setVisibility(View.GONE);
            listView.setOnItemClickListener((p, v, pos, id) -> {
                if (list != null && pos < list.size()) {
                    showTinTucReadOnly(list.get(pos));
                }
            });
        }
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
        list = dao.getAll();
        adapter = new TinTucAdapter(requireContext(), list);
        listView.setAdapter(adapter);
    }

    private void showTinTucReadOnly(TinTuc t) {
        View v = getLayoutInflater().inflate(R.layout.dialog_tin_tuc_detail, null);
        TextView txtTieu = v.findViewById(R.id.txtDetailTieuDe);
        TextView txtNgay = v.findViewById(R.id.txtDetailNgay);
        TextView txtNd = v.findViewById(R.id.txtDetailNoiDung);
        txtTieu.setText(t.getTieuDe());
        txtNgay.setText(t.getNgayDang() != null ? t.getNgayDang() : "");
        txtNd.setText(t.getNoiDung() != null ? t.getNoiDung() : "");
        new AlertDialog.Builder(requireContext())
                .setView(v)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private String todayYmd() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private boolean isNotPastDate(String ymd) {
        if (ymd == null || ymd.isEmpty()) return false;
        return ymd.compareTo(todayYmd()) >= 0;
    }

    @Nullable
    private Long parseYmdToUtcMillis(String ymd) {
        if (ymd == null || ymd.isEmpty()) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setLenient(false);
            sdf.setTimeZone(TimeZone.getDefault());
            Date d = sdf.parse(ymd.trim());
            return d != null ? d.getTime() : null;
        } catch (ParseException e) {
            return null;
        }
    }

    private void showTinTucDialog(@Nullable TinTuc existing) {
        FragmentActivity act = requireActivity();
        View form = getLayoutInflater().inflate(R.layout.dialog_tin_tuc, null);
        EditText edtTieu = form.findViewById(R.id.edtTieuDe);
        EditText edtNd = form.findViewById(R.id.edtNoiDung);
        TextView txtNgay = form.findViewById(R.id.txtNgayDang);

        final Long[] ngayMs = {null};

        if (existing != null) {
            edtTieu.setText(existing.getTieuDe());
            edtNd.setText(existing.getNoiDung());
            String nd = existing.getNgayDang();
            txtNgay.setText(nd != null ? nd : todayYmd());
            Long parsed = parseYmdToUtcMillis(nd);
            long min = DatePickerHelper.startOfTodayUtcMillis();
            if (parsed != null && parsed >= min) {
                ngayMs[0] = parsed;
            }
        } else {
            long t = DatePickerHelper.startOfTodayUtcMillis();
            ngayMs[0] = t;
            txtNgay.setText(DatePickerHelper.formatYmdLocal(t));
        }

        txtNgay.setOnClickListener(v -> {
            long min = DatePickerHelper.startOfTodayUtcMillis();
            long initSel = ngayMs[0] != null ? ngayMs[0] : min;
            if (initSel < min) {
                initSel = min;
            }
            DatePickerHelper.showPickDate(act, "Ngày đăng", min, initSel, sel -> {
                ngayMs[0] = sel;
                txtNgay.setText(DatePickerHelper.formatYmdLocal(sel));
            });
        });

        AlertDialog.Builder b = new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Thêm tin" : "Sửa tin")
                .setView(form);

        if (existing != null) {
            b.setNeutralButton("Xóa", null);
        }
        b.setPositiveButton("Lưu", null);
        b.setNegativeButton("Hủy", null);

        AlertDialog dlg = b.create();
        dlg.setOnShowListener(d -> {
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String tieu = edtTieu.getText().toString().trim();
                String nd = edtNd.getText().toString().trim();
                String ngayStr = txtNgay.getText().toString().trim();
                if (tieu.isEmpty() || nd.isEmpty() || ngayStr.isEmpty()) {
                    Toast.makeText(requireContext(), "Điền đủ các trường", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isNotPastDate(ngayStr)) {
                    Toast.makeText(requireContext(), "Ngày đăng không được trước hôm nay", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (existing == null) {
                    TinTuc t = new TinTuc();
                    t.setTieuDe(tieu);
                    t.setNoiDung(nd);
                    t.setNgayDang(ngayStr);
                    if (dao.insert(t)) {
                        Toast.makeText(requireContext(), "Đã thêm tin", Toast.LENGTH_SHORT).show();
                        loadData();
                        dlg.dismiss();
                    } else {
                        Toast.makeText(requireContext(), "Không lưu được", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    existing.setTieuDe(tieu);
                    existing.setNoiDung(nd);
                    existing.setNgayDang(ngayStr);
                    if (dao.update(existing)) {
                        Toast.makeText(requireContext(), "Đã cập nhật", Toast.LENGTH_SHORT).show();
                        loadData();
                        dlg.dismiss();
                    } else {
                        Toast.makeText(requireContext(), "Không lưu được", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            if (existing != null) {
                android.widget.Button btnNeu = dlg.getButton(AlertDialog.BUTTON_NEUTRAL);
                if (btnNeu != null) {
                    btnNeu.setOnClickListener(v ->
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Xóa tin")
                                    .setMessage("Xóa tin này?")
                                    .setPositiveButton("Xóa", (d2, w) -> {
                                        if (dao.delete(existing.getTinTucID())) {
                                            Toast.makeText(requireContext(), "Đã xóa", Toast.LENGTH_SHORT).show();
                                            loadData();
                                            dlg.dismiss();
                                        } else {
                                            Toast.makeText(requireContext(), "Không xóa được", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .setNegativeButton("Hủy", null)
                                    .show());
                }
            }
        });
        dlg.show();
    }
}

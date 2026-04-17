package com.example.doan;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.doan.DAO.DichVuDAO;
import com.example.doan.adapter.DichVuQLAdapter;
import com.example.doan.model.DichVu;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class QLDichVuFragment extends Fragment implements DataRefreshable {

    private ListView listView;
    private DichVuDAO dao;
    private List<DichVu> list;
    private DichVuQLAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ql_dich_vu, container, false);
        listView = view.findViewById(R.id.listDichVu);
        MaterialButton btn = view.findViewById(R.id.btnThemDichVu);
        btn.setOnClickListener(v -> showDialog(null));
        dao = new DichVuDAO(requireContext());
        refreshData();
        listView.setOnItemClickListener((parent, v, position, id) -> showDialog(list.get(position)));
        listView.setOnItemLongClickListener((parent, v, position, id) -> {
            DichVu d = list.get(position);
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.ql_dich_vu_delete_title)
                    .setMessage(getString(R.string.ql_dich_vu_delete_msg, d.getTenDichVu()))
                    .setPositiveButton(R.string.ql_dich_vu_delete_ok, (di, w) -> {
                        if (dao.delete(d.getDichVuID()) > 0) {
                            Toast.makeText(requireContext(), R.string.ql_dich_vu_deleted, Toast.LENGTH_SHORT).show();
                            loadData();
                        } else {
                            Toast.makeText(requireContext(), R.string.ql_dich_vu_delete_fail, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return true;
        });
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
        if (adapter == null) {
            adapter = new DichVuQLAdapter(requireContext(), list);
            listView.setAdapter(adapter);
        } else {
            adapter.updateData(list);
        }
    }

    private void showDialog(@Nullable DichVu existing) {
        View form = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_dich_vu, null);
        TextInputEditText edtTen = form.findViewById(R.id.edtDvTen);
        TextInputEditText edtGia = form.findViewById(R.id.edtDvGia);
        TextInputEditText edtMoTa = form.findViewById(R.id.edtDvMoTa);
        if (existing != null) {
            edtTen.setText(existing.getTenDichVu());
            edtGia.setText(String.valueOf((long) existing.getGia()));
            edtMoTa.setText(existing.getMoTa());
        }
        AlertDialog dlg = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(existing == null ? R.string.ql_dich_vu_add : R.string.ql_dich_vu_edit)
                .setView(form)
                .setPositiveButton(R.string.ql_dich_vu_save, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dlg.setOnShowListener(d -> {
            Button pos = dlg.getButton(AlertDialog.BUTTON_POSITIVE);
            pos.setOnClickListener(v -> {
                String ten = edtTen.getText().toString().trim();
                if (ten.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.ql_dich_vu_err_name, Toast.LENGTH_SHORT).show();
                    return;
                }
                double gia;
                try {
                    gia = Double.parseDouble(edtGia.getText().toString().trim().replace(",", "."));
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), R.string.ql_dich_vu_err_price, Toast.LENGTH_SHORT).show();
                    return;
                }
                DichVu dv = existing != null ? existing : new DichVu();
                dv.setTenDichVu(ten);
                dv.setGia(gia);
                dv.setMoTa(edtMoTa.getText().toString().trim());
                if (existing == null) {
                    if (dao.insert(dv) != -1) {
                        Toast.makeText(requireContext(), R.string.ql_dich_vu_saved, Toast.LENGTH_SHORT).show();
                        loadData();
                        dlg.dismiss();
                    }
                } else {
                    if (dao.update(dv) > 0) {
                        Toast.makeText(requireContext(), R.string.ql_dich_vu_saved, Toast.LENGTH_SHORT).show();
                        loadData();
                        dlg.dismiss();
                    }
                }
            });
        });
        dlg.show();
    }
}

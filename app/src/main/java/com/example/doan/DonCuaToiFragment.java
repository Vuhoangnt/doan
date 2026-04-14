package com.example.doan;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.doan.DAO.DatPhongDAO;
import com.example.doan.DAO.ThanhToanDAO;
import com.example.doan.adapter.DatPhongListAdapter;
import com.example.doan.model.DatPhong;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.don_cua_toi_btn_tra_phong)
                .setMessage(R.string.don_cua_toi_tra_phong_confirm)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.don_cua_toi_btn_tra_phong, (di, w) -> doGuestCheckout(d))
                .show();
    }

    private void doGuestCheckout(DatPhong d) {
        SessionManager s = new SessionManager(requireContext());
        int r = dao.guestSelfCheckout(d.getDatPhongID(), s.getTaiKhoanId());
        if (r == -1) {
            Toast.makeText(requireContext(), R.string.don_cua_toi_tra_phong_need_pay, Toast.LENGTH_LONG).show();
            return;
        }
        if (r <= 0) {
            Toast.makeText(requireContext(), R.string.don_cua_toi_tra_phong_fail, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(requireContext(), R.string.don_cua_toi_tra_phong_ok, Toast.LENGTH_SHORT).show();
        loadData();
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
        final int phongId = d.getPhongID();
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.don_cua_toi_rate_title)
                .setMessage(R.string.don_cua_toi_rate_prompt)
                .setNegativeButton(R.string.don_cua_toi_rate_later, null)
                .setPositiveButton(R.string.don_cua_toi_rate_now, (d2, w) ->
                        DanhGiaFragment.showReviewDialogForPhong(DonCuaToiFragment.this, phongId))
                .show();
    }

    private void showEmptyCard(@StringRes int messageRes) {
        txtEmpty.setText(messageRes);
        cardEmpty.setVisibility(View.VISIBLE);
    }
}

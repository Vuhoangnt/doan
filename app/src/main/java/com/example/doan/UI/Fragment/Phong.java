package com.example.doan.UI.Fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.HorizontalScrollView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.doan.MainActivity;
import com.example.doan.DAO.DatPhongDAO;
import com.example.doan.DAO.PhongDAO;
import com.example.doan.DataRefreshable;
import com.example.doan.R;
import com.example.doan.SessionManager;
import com.example.doan.adapter.PhongAdapter;
import com.example.doan.model.DatPhong;
import com.example.doan.model.PhongFull;
import com.example.doan.util.VietSearch;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.color.MaterialColors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.android.material.tabs.TabLayout;

public class Phong extends Fragment implements DataRefreshable {

    private static final double GUEST_PRICE_LOW = 400_000d;
    private static final double GUEST_PRICE_HIGH = 700_000d;

    ListView listView;
    PhongDAO dao;
    List<PhongFull> list;
    PhongAdapter adapter;
    private ActivityResultLauncher<PickVisualMediaRequest> pickRoomImages;

    private MaterialCardView cardPhongHeader;
    private TextView txtPhongHeaderTitle;
    private TextView txtPhongHeaderSubtitle;
    private TextView txtPhongStatTong;
    private TextView txtPhongStatTrong;
    private TextView txtPhongStatDang;
    private MaterialButton btnThemPhong;

    private MaterialCardView cardPhongLichBieu;
    private TableLayout tablePhongLich;
    private TabLayout tabPhongLich;
    private HorizontalScrollView scrollPhongLichTable;
    private LinearLayout layoutPhongLichCards;
    private LinearLayout layoutPhongLichTimeline;
    /** Tab lịch đặt: 0 bảng, 1 thẻ phòng, 2 đơn theo ngày. */
    private int lastLichTabIndex;

    private MaterialCardView cardGuestSearch;
    private ChipGroup chipGuestPrice;
    private ChipGroup chipGuestSort;
    private TextView txtGuestPhongResultCount;
    private LinearLayout layoutGuestFilters;
    private ImageButton btnToggleGuestFilters;
    private LinearLayout layoutPhongQlListHeader;
    private boolean guestFiltersExpanded = true;

    private List<PhongFull> guestDisplayList;
    private boolean guestSearchBound;
    /** Mode đã gắn với adapter hiện tại (tránh tạo lại adapter không cần thiết). */
    private String boundAdapterMode;
    /** true = adapter đang trỏ tới guestDisplayList; false = trỏ tới list đầy đủ. */
    private boolean boundUsesGuestList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickRoomImages = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(10),
                uris -> {
                    if (adapter != null) {
                        adapter.onRoomImagesPicked(uris);
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_phong, container, false);

        listView = view.findViewById(R.id.listPhong);
        cardPhongHeader = view.findViewById(R.id.cardPhongHeader);
        txtPhongHeaderTitle = view.findViewById(R.id.txtPhongHeaderTitle);
        txtPhongHeaderSubtitle = view.findViewById(R.id.txtPhongHeaderSubtitle);
        txtPhongStatTong = view.findViewById(R.id.txtPhongStatTong);
        txtPhongStatTrong = view.findViewById(R.id.txtPhongStatTrong);
        txtPhongStatDang = view.findViewById(R.id.txtPhongStatDang);
        btnThemPhong = view.findViewById(R.id.btnThemPhong);
        cardPhongLichBieu = view.findViewById(R.id.cardPhongLichBieu);
        tablePhongLich = view.findViewById(R.id.tablePhongLich);
        cardGuestSearch = view.findViewById(R.id.cardGuestSearch);
        chipGuestPrice = view.findViewById(R.id.chipGuestPrice);
        chipGuestSort = view.findViewById(R.id.chipGuestSort);
        txtGuestPhongResultCount = view.findViewById(R.id.txtGuestPhongResultCount);
        layoutGuestFilters = view.findViewById(R.id.layoutGuestFilters);
        btnToggleGuestFilters = view.findViewById(R.id.btnToggleGuestFilters);
        layoutPhongQlListHeader = view.findViewById(R.id.layoutPhongQlListHeader);
        tabPhongLich = view.findViewById(R.id.tabPhongLich);
        scrollPhongLichTable = view.findViewById(R.id.scrollPhongLichTable);
        layoutPhongLichCards = view.findViewById(R.id.layoutPhongLichCards);
        layoutPhongLichTimeline = view.findViewById(R.id.layoutPhongLichTimeline);
        setupPhongLichTabs();
        if (btnToggleGuestFilters != null) {
            btnToggleGuestFilters.setOnClickListener(v -> toggleGuestFiltersPanel());
            syncGuestFiltersToggleIcon();
        }

        dao = new PhongDAO(requireContext());

        refreshData();

        return view;
    }

    /** Gọi từ {@link MainActivity} khi SearchView đổi nội dung. */
    public void onToolbarSearchQueryChanged() {
        if (boundUsesGuestList) {
            applyGuestFilters();
        }
    }

    @Override
    public void onDestroyView() {
        guestSearchBound = false;
        guestFiltersExpanded = true;
        adapter = null;
        boundAdapterMode = null;
        boundUsesGuestList = false;
        super.onDestroyView();
    }

    private void toggleGuestFiltersPanel() {
        if (layoutGuestFilters == null || btnToggleGuestFilters == null) {
            return;
        }
        guestFiltersExpanded = !guestFiltersExpanded;
        layoutGuestFilters.setVisibility(guestFiltersExpanded ? View.VISIBLE : View.GONE);
        syncGuestFiltersToggleIcon();
    }

    private void syncGuestFiltersToggleIcon() {
        if (btnToggleGuestFilters == null) {
            return;
        }
        if (guestFiltersExpanded) {
            btnToggleGuestFilters.setImageResource(R.drawable.ic_expand_less_24);
            btnToggleGuestFilters.setContentDescription(getString(R.string.phong_guest_filters_collapse));
        } else {
            btnToggleGuestFilters.setImageResource(R.drawable.ic_expand_more_24);
            btnToggleGuestFilters.setContentDescription(getString(R.string.phong_guest_filters_expand));
        }
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
        dao.syncAllPhongTrangThaiTheoDon();
        list = dao.getAllPhongFull();
        SessionManager session = new SessionManager(requireContext());
        String mode = PhongAdapter.MODE_BOOK;
        if (session.isLoggedIn()) {
            if (session.isAdmin()) {
                mode = PhongAdapter.MODE_ADMIN;
            } else if (session.isNhanVien()) {
                mode = PhongAdapter.MODE_STAFF;
            }
        }
        boolean guestBook = PhongAdapter.MODE_BOOK.equals(mode)
                && (!session.isLoggedIn() || session.isKhach());

        if (cardGuestSearch != null) {
            cardGuestSearch.setVisibility(guestBook ? View.VISIBLE : View.GONE);
        }

        if (guestBook) {
            if (guestDisplayList == null) {
                guestDisplayList = new ArrayList<>();
            }
            boolean needNewAdapter = adapter == null
                    || !PhongAdapter.MODE_BOOK.equals(boundAdapterMode)
                    || !boundUsesGuestList;
            if (needNewAdapter) {
                adapter = new PhongAdapter(requireContext(), guestDisplayList, PhongAdapter.MODE_BOOK, session, pickRoomImages);
                listView.setAdapter(adapter);
                boundAdapterMode = PhongAdapter.MODE_BOOK;
                boundUsesGuestList = true;
                bindGuestSearchListeners();
            }
            applyGuestFilters();
        } else {
            adapter = new PhongAdapter(requireContext(), list, mode, session, pickRoomImages);
            listView.setAdapter(adapter);
            boundAdapterMode = mode;
            boundUsesGuestList = false;
        }
        updateHeaderStats(session);
        updateLichDatPhongTable(session);
        syncPhongListHeightForNestedScroll();
    }

    /**
     * ListView trong NestedScrollView cần chiều cao đo theo từng item, nếu không chỉ hiện một phần.
     */
    private void syncPhongListHeightForNestedScroll() {
        if (listView == null) {
            return;
        }
        listView.post(() -> {
            ListAdapter la = listView.getAdapter();
            if (la == null) {
                return;
            }
            int count = la.getCount();
            ViewGroup.LayoutParams lp = listView.getLayoutParams();
            if (count == 0) {
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                listView.setLayoutParams(lp);
                return;
            }
            int parentW = listView.getWidth();
            if (parentW <= 0) {
                parentW = getResources().getDisplayMetrics().widthPixels;
            }
            int horizontalPad = listView.getPaddingLeft() + listView.getPaddingRight();
            int widthSpec = MeasureSpec.makeMeasureSpec(
                    Math.max(0, parentW - horizontalPad), MeasureSpec.EXACTLY);
            int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            int total = listView.getPaddingTop() + listView.getPaddingBottom();
            for (int i = 0; i < count; i++) {
                View row = la.getView(i, null, listView);
                row.measure(widthSpec, heightSpec);
                total += row.getMeasuredHeight();
            }
            int div = listView.getDividerHeight();
            if (div > 0 && count > 1) {
                total += div * (count - 1);
            }
            lp.height = total;
            listView.setLayoutParams(lp);
        });
    }

    private void bindGuestSearchListeners() {
        if (guestSearchBound || chipGuestPrice == null || chipGuestSort == null) {
            return;
        }
        guestSearchBound = true;
        chipGuestPrice.setOnCheckedStateChangeListener((group, checkedIds) -> applyGuestFilters());
        chipGuestSort.setOnCheckedStateChangeListener((group, checkedIds) -> applyGuestFilters());
    }

    private void applyGuestFilters() {
        if (!boundUsesGuestList || guestDisplayList == null || list == null || adapter == null) {
            return;
        }
        String q = "";
        if (getActivity() instanceof MainActivity) {
            q = ((MainActivity) getActivity()).getGuestPhongSearchQuery();
        }
        String qn = VietSearch.normalize(q);
        int band = guestPriceBandFromChip();
        boolean cheap = chipGuestSort != null && chipGuestSort.getCheckedChipId() == R.id.chipSortCheap;

        guestDisplayList.clear();
        for (PhongFull p : list) {
            if (!VietSearch.matchesPhong(qn, p)) {
                continue;
            }
            if (!guestPriceMatches(p.getGiaNgay(), band)) {
                continue;
            }
            guestDisplayList.add(p);
        }
        if (cheap) {
            Collections.sort(guestDisplayList, Comparator.comparingDouble(PhongFull::getGiaNgay));
        }
        adapter.notifyDataSetChanged();
        updateGuestResultCount();
        syncPhongListHeightForNestedScroll();
    }

    private int guestPriceBandFromChip() {
        if (chipGuestPrice == null) {
            return 0;
        }
        int id = chipGuestPrice.getCheckedChipId();
        if (id == R.id.chipPriceLt400) {
            return 1;
        }
        if (id == R.id.chipPrice400700) {
            return 2;
        }
        if (id == R.id.chipPriceGt700) {
            return 3;
        }
        return 0;
    }

    private static boolean guestPriceMatches(double g, int band) {
        if (band == 0) {
            return true;
        }
        if (band == 1) {
            return g < GUEST_PRICE_LOW;
        }
        if (band == 2) {
            return g >= GUEST_PRICE_LOW && g <= GUEST_PRICE_HIGH;
        }
        return g > GUEST_PRICE_HIGH;
    }

    private void updateGuestResultCount() {
        if (txtGuestPhongResultCount == null || guestDisplayList == null) {
            return;
        }
        int n = guestDisplayList.size();
        if (n == 0) {
            txtGuestPhongResultCount.setText(R.string.phong_guest_result_none);
        } else {
            txtGuestPhongResultCount.setText(getString(R.string.phong_guest_result_count, n));
        }
    }

    private void updateLichDatPhongTable(@NonNull SessionManager session) {
        if (cardPhongLichBieu == null || tablePhongLich == null || list == null) {
            return;
        }
        if (!session.isAdmin() && !session.isNhanVien()) {
            cardPhongLichBieu.setVisibility(View.GONE);
            return;
        }
        cardPhongLichBieu.setVisibility(View.VISIBLE);
        tablePhongLich.removeAllViews();

        float density = getResources().getDisplayMetrics().density;
        int pad = Math.round(8 * density);
        DatPhongDAO dpDao = new DatPhongDAO(requireContext());
        List<DatPhong> allDon = dpDao.getAll();

        tablePhongLich.addView(buildLichHeaderRow(pad));
        for (int ri = 0; ri < list.size(); ri++) {
            PhongFull p = list.get(ri);
            boolean altStripe = (ri % 2) == 1;
            TableRow row = buildLichDataRow(p, allDon, pad, altStripe);
            row.setClickable(true);
            row.setFocusable(true);
            TypedValue tvRipple = new TypedValue();
            if (requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, tvRipple, true)) {
                row.setForeground(ContextCompat.getDrawable(requireContext(), tvRipple.resourceId));
            }
            row.setOnClickListener(v -> {
                if (adapter != null) {
                    adapter.openRoomQlDetail(p);
                }
            });
            tablePhongLich.addView(row);
        }
        fillLichCardsViews(allDon);
        fillLichTimelineViews();
        applyPhongLichTabVisibility(lastLichTabIndex);
    }

    private void setupPhongLichTabs() {
        if (tabPhongLich == null) {
            return;
        }
        tabPhongLich.removeAllTabs();
        tabPhongLich.addTab(tabPhongLich.newTab().setText(R.string.phong_lich_tab_table));
        tabPhongLich.addTab(tabPhongLich.newTab().setText(R.string.phong_lich_tab_cards));
        tabPhongLich.addTab(tabPhongLich.newTab().setText(R.string.phong_lich_tab_timeline));
        tabPhongLich.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                lastLichTabIndex = tab.getPosition();
                applyPhongLichTabVisibility(lastLichTabIndex);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        int idx = Math.max(0, Math.min(2, lastLichTabIndex));
        TabLayout.Tab sel = tabPhongLich.getTabAt(idx);
        if (sel != null) {
            sel.select();
        } else {
            lastLichTabIndex = 0;
            applyPhongLichTabVisibility(0);
        }
    }

    private void applyPhongLichTabVisibility(int index) {
        if (scrollPhongLichTable == null) {
            return;
        }
        int i = Math.max(0, Math.min(2, index));
        scrollPhongLichTable.setVisibility(i == 0 ? View.VISIBLE : View.GONE);
        if (layoutPhongLichCards != null) {
            layoutPhongLichCards.setVisibility(i == 1 ? View.VISIBLE : View.GONE);
        }
        if (layoutPhongLichTimeline != null) {
            layoutPhongLichTimeline.setVisibility(i == 2 ? View.VISIBLE : View.GONE);
        }
    }

    private void fillLichCardsViews(@NonNull List<DatPhong> allDon) {
        if (layoutPhongLichCards == null) {
            return;
        }
        layoutPhongLichCards.removeAllViews();
        if (list == null || list.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText(R.string.phong_lich_empty_rooms);
            empty.setTextColor(MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceVariant,
                    ContextCompat.getColor(requireContext(), R.color.app_on_surface_variant)));
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
            int pad = Math.round(8 * getResources().getDisplayMetrics().density);
            empty.setPadding(pad, pad, pad, pad);
            layoutPhongLichCards.addView(empty);
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (PhongFull p : list) {
            View card = inflater.inflate(R.layout.item_phong_lich_room_card, layoutPhongLichCards, false);
            TextView title = card.findViewById(R.id.txtPhongLichCardTitle);
            TextView sub = card.findViewById(R.id.txtPhongLichCardSub);
            LinearLayout blocks = card.findViewById(R.id.layoutPhongLichCardBlocks);
            if (title != null) {
                String name = p.getTenPhong() != null ? p.getTenPhong() : ("#" + p.getPhongID());
                title.setText(name);
            }
            if (sub != null) {
                sub.setText(p.getTrangThai() != null ? p.getTrangThai() : "");
            }
            if (blocks != null) {
                int pid = p.getPhongID();
                addLichCardStatusBlock(blocks, R.string.phong_lich_label_cho,
                        formatLichCellText(pickDonForPhongStatus(allDon, pid, DatPhongDAO.TT_CHO_XAC_NHAN)));
                addLichCardStatusBlock(blocks, R.string.phong_lich_label_da_xn,
                        formatLichCellText(pickDonForPhongStatus(allDon, pid, DatPhongDAO.TT_DA_XAC_NHAN)));
                addLichCardStatusBlock(blocks, R.string.phong_lich_label_dang_o,
                        formatLichCellText(pickDonForPhongStatus(allDon, pid, DatPhongDAO.TT_DANG_O)));
                addLichCardStatusBlock(blocks, R.string.phong_lich_label_da_tra,
                        formatLichCellText(pickDonForPhongStatus(allDon, pid, DatPhongDAO.TT_DA_TRA_PHONG)));
            }
            card.setOnClickListener(v -> {
                if (adapter != null) {
                    adapter.openRoomQlDetail(p);
                }
            });
            layoutPhongLichCards.addView(card);
        }
    }

    private void addLichCardStatusBlock(@NonNull LinearLayout parent, int titleRes, @NonNull String body) {
        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText(titleRes);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        tvTitle.setTypeface(tvTitle.getTypeface(), Typeface.BOLD);
        tvTitle.setTextColor(MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceVariant,
                    ContextCompat.getColor(requireContext(), R.color.app_on_surface_variant)));
        LinearLayout.LayoutParams lpTitle = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int top = Math.round(10 * getResources().getDisplayMetrics().density);
        lpTitle.topMargin = parent.getChildCount() == 0 ? 0 : top;
        tvTitle.setLayoutParams(lpTitle);
        parent.addView(tvTitle);

        TextView tvBody = new TextView(requireContext());
        tvBody.setText(body);
        tvBody.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        tvBody.setTextColor(MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurface,
                    ContextCompat.getColor(requireContext(), R.color.app_on_surface)));
        float extra = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics());
        tvBody.setLineSpacing(extra, 1f);
        LinearLayout.LayoutParams lpBody = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int gap = Math.round(4 * getResources().getDisplayMetrics().density);
        lpBody.topMargin = gap;
        tvBody.setLayoutParams(lpBody);
        parent.addView(tvBody);
    }

    private void fillLichTimelineViews() {
        if (layoutPhongLichTimeline == null || list == null) {
            return;
        }
        layoutPhongLichTimeline.removeAllViews();
        if (list.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText(R.string.phong_lich_empty_rooms);
            empty.setTextColor(MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceVariant,
                    ContextCompat.getColor(requireContext(), R.color.app_on_surface_variant)));
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
            int pad = Math.round(8 * getResources().getDisplayMetrics().density);
            empty.setPadding(pad, pad, pad, pad);
            layoutPhongLichTimeline.addView(empty);
            return;
        }
        Set<Integer> phongIds = new HashSet<>();
        for (PhongFull p : list) {
            phongIds.add(p.getPhongID());
        }
        DatPhongDAO dpDao = new DatPhongDAO(requireContext());
        List<DatPhong> joined = dpDao.getAllWithTenPhong();
        List<DatPhong> filtered = new ArrayList<>();
        for (DatPhong d : joined) {
            if (!phongIds.contains(d.getPhongID())) {
                continue;
            }
            if (DatPhongDAO.TT_DA_HUY.equals(DatPhongDAO.normalizeStatus(d.getTrangThai()))) {
                continue;
            }
            filtered.add(d);
        }
        if (filtered.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText(R.string.phong_lich_empty_orders);
            empty.setTextColor(MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceVariant,
                    ContextCompat.getColor(requireContext(), R.color.app_on_surface_variant)));
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
            int pad = Math.round(8 * getResources().getDisplayMetrics().density);
            empty.setPadding(pad, pad, pad, pad);
            layoutPhongLichTimeline.addView(empty);
            return;
        }
        Collections.sort(filtered, (a, b) -> {
            int c = compareYmd(a.getNgayNhan(), b.getNgayNhan());
            if (c != 0) {
                return c;
            }
            return Integer.compare(a.getDatPhongID(), b.getDatPhongID());
        });
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (DatPhong d : filtered) {
            View row = inflater.inflate(R.layout.item_phong_lich_timeline_row, layoutPhongLichTimeline, false);
            TextView ma = row.findViewById(R.id.txtPhongLichTlMa);
            TextView st = row.findViewById(R.id.txtPhongLichTlTrangThai);
            TextView phong = row.findViewById(R.id.txtPhongLichTlPhong);
            TextView tg = row.findViewById(R.id.txtPhongLichTlThoiGian);
            TextView khach = row.findViewById(R.id.txtPhongLichTlKhach);
            if (ma != null) {
                String m = d.getMaDatPhong() != null ? d.getMaDatPhong() : ("#" + d.getDatPhongID());
                ma.setText(m);
            }
            if (st != null) {
                String raw = d.getTrangThai() != null ? d.getTrangThai() : "";
                st.setText(raw);
                styleTimelineStatusBadge(st, DatPhongDAO.normalizeStatus(d.getTrangThai()));
            }
            if (phong != null) {
                String tp = d.getTenPhong() != null && !d.getTenPhong().isEmpty()
                        ? d.getTenPhong() : ("Phòng #" + d.getPhongID());
                phong.setText(tp);
            }
            if (tg != null) {
                tg.setText(formatLichCellText(d));
            }
            if (khach != null) {
                String name = d.getKhachTen() != null ? d.getKhachTen() : "—";
                String phone = d.getKhachDienThoai() != null ? d.getKhachDienThoai() : "";
                String line = phone.isEmpty() ? name : (name + " · " + phone);
                khach.setText(getString(R.string.phong_lich_tl_khach, line));
            }
            row.setOnClickListener(v -> {
                if (adapter == null) {
                    return;
                }
                PhongFull pf = findPhongFullById(d.getPhongID());
                if (pf != null) {
                    adapter.openRoomQlDetail(pf);
                }
            });
            layoutPhongLichTimeline.addView(row);
        }
    }

    private void styleTimelineStatusBadge(@NonNull TextView tv, @NonNull String normalized) {
        int bg = R.drawable.bg_room_badge_khac;
        int fgColor = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceVariant,
                ContextCompat.getColor(requireContext(), R.color.app_on_surface_variant));
        if (DatPhongDAO.TT_DA_XAC_NHAN.equals(normalized)) {
            bg = R.drawable.bg_room_badge_trong;
            fgColor = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnPrimaryContainer,
                    ContextCompat.getColor(requireContext(), R.color.theme_green_on_primary_container));
        } else if (DatPhongDAO.TT_DANG_O.equals(normalized)) {
            bg = R.drawable.bg_room_badge_dang;
            fgColor = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSecondaryContainer,
                    ContextCompat.getColor(requireContext(), R.color.app_on_surface_variant));
        } else if (DatPhongDAO.TT_DA_TRA_PHONG.equals(normalized)) {
            bg = R.drawable.bg_room_badge_khac;
            fgColor = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurfaceVariant,
                    ContextCompat.getColor(requireContext(), R.color.app_on_surface_variant));
        }
        tv.setBackgroundResource(bg);
        tv.setTextColor(fgColor);
    }

    @Nullable
    private PhongFull findPhongFullById(int phongId) {
        if (list == null) {
            return null;
        }
        for (PhongFull p : list) {
            if (p.getPhongID() == phongId) {
                return p;
            }
        }
        return null;
    }

    private TableRow buildLichHeaderRow(int pad) {
        TableRow row = new TableRow(requireContext());
        row.addView(lichHeaderCell(R.string.phong_lich_col_phong, pad, 1.2f));
        row.addView(lichHeaderCell(R.string.phong_lich_col_dang_dat, pad, 1f));
        row.addView(lichHeaderCell(R.string.phong_lich_col_da_dat, pad, 1f));
        row.addView(lichHeaderCell(R.string.phong_lich_col_dang_o, pad, 1f));
        row.addView(lichHeaderCell(R.string.phong_lich_col_da_tra, pad, 1f));
        return row;
    }

    private TextView lichHeaderCell(int stringRes, int pad, float weight) {
        TextView tv = new TextView(requireContext());
        tv.setText(stringRes);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setPadding(pad, pad, pad, pad);
        tv.setBackgroundResource(R.drawable.bg_phong_lich_header);
        tv.setTextColor(MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurface,
                    ContextCompat.getColor(requireContext(), R.color.app_on_surface)));
        tv.setMaxLines(3);
        tv.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics()), 1f);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
        lp.width = 0;
        tv.setLayoutParams(lp);
        tv.setMinWidth(Math.round(115 * getResources().getDisplayMetrics().density));
        return tv;
    }

    private TableRow buildLichDataRow(PhongFull p, List<DatPhong> allDon, int pad, boolean altStripe) {
        TableRow row = new TableRow(requireContext());
        int pid = p.getPhongID();
        int rowBg = altStripe ? R.drawable.bg_phong_lich_row_alt : R.drawable.bg_phong_lich_row;
        row.addView(lichDataCell(p.getTenPhong() != null ? p.getTenPhong() : ("#" + pid), pad, 1.2f, true, rowBg));
        row.addView(lichDataCell(formatLichCellText(pickDonForPhongStatus(allDon, pid, DatPhongDAO.TT_CHO_XAC_NHAN)), pad, 1f, false, rowBg));
        row.addView(lichDataCell(formatLichCellText(pickDonForPhongStatus(allDon, pid, DatPhongDAO.TT_DA_XAC_NHAN)), pad, 1f, false, rowBg));
        row.addView(lichDataCell(formatLichCellText(pickDonForPhongStatus(allDon, pid, DatPhongDAO.TT_DANG_O)), pad, 1f, false, rowBg));
        row.addView(lichDataCell(formatLichCellText(pickDonForPhongStatus(allDon, pid, DatPhongDAO.TT_DA_TRA_PHONG)), pad, 1f, false, rowBg));
        return row;
    }

    private TextView lichDataCell(String text, int pad, float weight, boolean nameCol, int rowBgRes) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        tv.setPadding(pad, pad, pad, pad);
        tv.setBackgroundResource(rowBgRes);
        tv.setTextColor(MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurface,
                    ContextCompat.getColor(requireContext(), R.color.app_on_surface)));
        tv.setMaxLines(5);
        tv.setEllipsize(android.text.TextUtils.TruncateAt.END);
        tv.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics()), 1f);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
        lp.width = 0;
        tv.setLayoutParams(lp);
        int minW = Math.round((nameCol ? 120 : 108) * getResources().getDisplayMetrics().density);
        tv.setMinWidth(minW);
        return tv;
    }

    /**
     * Chọn đơn hiển thị: với “đã trả” lấy lần trả gần nhất; các trạng thái khác lấy đơn có ngày nhận sớm nhất.
     */
    @Nullable
    private static DatPhong pickDonForPhongStatus(List<DatPhong> all, int phongId, String wantStatus) {
        DatPhong best = null;
        for (DatPhong d : all) {
            if (d.getPhongID() != phongId) {
                continue;
            }
            if (!DatPhongDAO.normalizeStatus(d.getTrangThai()).equals(wantStatus)) {
                continue;
            }
            if (DatPhongDAO.TT_DA_TRA_PHONG.equals(wantStatus)) {
                if (best == null) {
                    best = d;
                } else {
                    int c = compareYmd(d.getNgayTra(), best.getNgayTra());
                    if (c > 0 || (c == 0 && d.getDatPhongID() > best.getDatPhongID())) {
                        best = d;
                    }
                }
            } else {
                if (best == null) {
                    best = d;
                } else {
                    int c = compareYmd(d.getNgayNhan(), best.getNgayNhan());
                    if (c < 0 || (c == 0 && d.getDatPhongID() < best.getDatPhongID())) {
                        best = d;
                    }
                }
            }
        }
        return best;
    }

    private static int compareYmd(@Nullable String a, @Nullable String b) {
        String sa = a != null ? a : "";
        String sb = b != null ? b : "";
        return sa.compareTo(sb);
    }

    private String formatLichCellText(@Nullable DatPhong d) {
        if (d == null) {
            return "—";
        }
        String gn = d.getGioNhan() != null && !d.getGioNhan().isEmpty() ? d.getGioNhan() : "—";
        String gt = d.getGioTra() != null && !d.getGioTra().isEmpty() ? d.getGioTra() : "—";
        String dn = d.getNgayNhan() != null ? d.getNgayNhan() : "";
        String dt = d.getNgayTra() != null ? d.getNgayTra() : "";
        String range;
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat day = new SimpleDateFormat("dd/MM", Locale.getDefault());
            in.setLenient(false);
            String a = dn.length() >= 10 ? day.format(in.parse(dn)) : dn;
            String b = dt.length() >= 10 ? day.format(in.parse(dt)) : dt;
            range = a + " → " + b;
        } catch (Exception e) {
            range = dn + " → " + dt;
        }
        String ma = d.getMaDatPhong() != null ? d.getMaDatPhong() : "";
        return range + "\n" + gn + " - " + gt + (ma.isEmpty() ? "" : ("\n" + ma));
    }

    private void updateHeaderStats(@NonNull SessionManager session) {
        if (cardPhongHeader == null || list == null) {
            return;
        }
        if (!session.isAdmin() && !session.isNhanVien()) {
            cardPhongHeader.setVisibility(View.GONE);
            if (btnThemPhong != null) {
                btnThemPhong.setVisibility(View.GONE);
            }
            if (layoutPhongQlListHeader != null) {
                layoutPhongQlListHeader.setVisibility(View.GONE);
            }
            return;
        }
        cardPhongHeader.setVisibility(View.VISIBLE);
        if (layoutPhongQlListHeader != null) {
            layoutPhongQlListHeader.setVisibility(View.VISIBLE);
        }
        if (btnThemPhong != null) {
            if (session.isAdmin()) {
                btnThemPhong.setVisibility(View.VISIBLE);
                btnThemPhong.setOnClickListener(v -> {
                    if (adapter != null) {
                        adapter.openAddPhongDialog();
                    }
                });
            } else {
                btnThemPhong.setVisibility(View.GONE);
            }
        }
        if (txtPhongHeaderTitle != null && txtPhongHeaderSubtitle != null) {
            if (session.isNhanVien()) {
                txtPhongHeaderTitle.setText(R.string.phong_header_staff_title);
                txtPhongHeaderSubtitle.setText(R.string.phong_header_staff_subtitle);
            } else {
                txtPhongHeaderTitle.setText(R.string.phong_header_admin_title);
                txtPhongHeaderSubtitle.setText(R.string.phong_header_admin_subtitle);
            }
        }
        int tong = list.size();
        int trong = 0;
        for (PhongFull p : list) {
            if (isPhongTrong(p.getTrangThai())) {
                trong++;
            }
        }
        int dang = Math.max(0, tong - trong);
        txtPhongStatTong.setText(String.valueOf(tong));
        txtPhongStatTrong.setText(String.valueOf(trong));
        txtPhongStatDang.setText(String.valueOf(dang));
    }

    private static boolean isPhongTrong(@Nullable String s) {
        if (s == null) {
            return false;
        }
        return s.contains("Trống") || s.contains("trống") || s.contains("Trong") || s.contains("trong");
    }
}



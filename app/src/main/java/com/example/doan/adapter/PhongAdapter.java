package com.example.doan.adapter;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentActivity;

import com.example.doan.DAO.DanhGiaDAO;
import com.example.doan.DAO.DatPhongDAO;
import com.example.doan.DAO.DichVuDAO;
import com.example.doan.DAO.PhongDAO;
import com.example.doan.DAO.PhongGiaLeDAO;
import com.example.doan.DAO.TaiKhoanDAO;
import com.example.doan.MainActivity;
import com.example.doan.R;
import com.example.doan.SessionManager;
import com.example.doan.model.DanhGia;
import com.example.doan.model.DatPhong;
import com.example.doan.model.DichVu;
import com.example.doan.model.PhongFull;
import com.example.doan.model.PhongGiaLe;
import com.example.doan.model.TaiKhoan;
import com.example.doan.util.DatePickerHelper;
import com.example.doan.util.DatPhongIntervalUtil;
import com.example.doan.util.PeakPricingUtil;
import com.example.doan.util.RoomImageUtils;
import com.example.doan.util.TimePickerHelper;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class PhongAdapter extends BaseAdapter {

    public static final String MODE_ADMIN = "admin";
    public static final String MODE_STAFF = "staff";
    public static final String MODE_BOOK = "book";

    private final Context context;
    private final List<PhongFull> list;
    private final PhongDAO dao;
    private final String mode;
    private final SessionManager session;
    @Nullable
    private final ActivityResultLauncher<PickVisualMediaRequest> pickRoomImagesLauncher;

    private LinearLayout pendingAdminPreview;
    private java.util.List<String> pendingAdminPaths;

    public PhongAdapter(Context context, List<PhongFull> list, String mode, SessionManager session) {
        this(context, list, mode, session, null);
    }

    public PhongAdapter(Context context, List<PhongFull> list, String mode, SessionManager session,
                        @Nullable ActivityResultLauncher<PickVisualMediaRequest> pickRoomImagesLauncher) {
        this.context = context;
        this.list = list;
        this.dao = new PhongDAO(context);
        this.mode = mode != null ? mode : MODE_BOOK;
        this.session = session;
        this.pickRoomImagesLauncher = pickRoomImagesLauncher;
    }

    /** Gọi từ Fragment sau khi chọn ảnh từ thiết bị (thêm/sửa phòng). */
    public void onRoomImagesPicked(@Nullable java.util.List<Uri> uris) {
        if (pendingAdminPaths == null || pendingAdminPreview == null || uris == null) {
            return;
        }
        for (Uri u : uris) {
            try {
                pendingAdminPaths.add(RoomImageUtils.copyUriToRoomImagesDir(context, u));
            } catch (Exception e) {
                Toast.makeText(context, "Không lưu được ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        refreshAdminPreview();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_phong, parent, false);
        }

        TextView txtTen = view.findViewById(R.id.txtTenPhong);
        TextView txtMoTa = view.findViewById(R.id.txtMoTa);
        TextView txtGiaOverlay = view.findViewById(R.id.txtGiaOverlay);
        TextView txtBadgeTrangThai = view.findViewById(R.id.txtBadgeTrangThai);
        ImageView img = view.findViewById(R.id.imgPhong);
        HorizontalScrollView scrollMini = view.findViewById(R.id.scrollMiniGallery);
        LinearLayout layoutMini = view.findViewById(R.id.layoutMiniGallery);
        LinearLayout layoutQlMeta = view.findViewById(R.id.layoutPhongQlMeta);
        TextView txtQlMeta = view.findViewById(R.id.txtPhongQlMeta);
        TextView txtTapHint = view.findViewById(R.id.txtPhongTapHint);

        PhongFull p = list.get(i);

        txtTen.setText(p.getTenPhong());
        txtGiaOverlay.setText(String.format(Locale.getDefault(), "%,.0f đ/đêm", p.getGiaNgay()));
        txtMoTa.setText(p.getMoTa() != null ? p.getMoTa() : "");
        applyRoomStatusBadge(txtBadgeTrangThai, p.getTrangThai());

        java.util.List<String> imgs = p.resolveAllImageRefs();
        String first = imgs.isEmpty() ? p.getUrlAnh() : imgs.get(0);
        RoomImageUtils.loadInto(img, first != null ? first : "");

        if (layoutMini != null && scrollMini != null) {
            layoutMini.removeAllViews();
            if (imgs.size() > 1) {
                scrollMini.setVisibility(View.VISIBLE);
                LayoutInflater inf = LayoutInflater.from(context);
                for (String ref : imgs) {
                    View thumb = inf.inflate(R.layout.item_phong_mini_thumb, layoutMini, false);
                    ImageView iv = thumb.findViewById(R.id.imgPhongMiniThumb);
                    RoomImageUtils.loadInto(iv, ref);
                    layoutMini.addView(thumb);
                }
            } else {
                scrollMini.setVisibility(View.GONE);
            }
        }

        if (layoutQlMeta != null && txtQlMeta != null && txtTapHint != null) {
            if (MODE_ADMIN.equals(mode) || MODE_STAFF.equals(mode)) {
                layoutQlMeta.setVisibility(View.VISIBLE);
                int toiDa = Math.max(1, p.getSoNguoiToiDa());
                txtQlMeta.setText(context.getString(R.string.phong_card_ql_id_meta, p.getPhongID(), toiDa));
                txtTapHint.setText(MODE_ADMIN.equals(mode)
                        ? context.getString(R.string.phong_card_tap_admin)
                        : context.getString(R.string.phong_card_tap_staff));
            } else {
                layoutQlMeta.setVisibility(View.GONE);
            }
        }

        view.setOnClickListener(v -> {
            if (MODE_ADMIN.equals(mode)) {
                openAdminDialog(p);
            } else if (MODE_STAFF.equals(mode)) {
                openStaffReadDialog(p);
            } else {
                openBookingDialog(p);
            }
        });

        return view;
    }

    /** Admin / nhân viên: mở chi tiết phòng từ bảng lịch (cùng hành vi như chạm thẻ trong danh sách). */
    public void openRoomQlDetail(PhongFull p) {
        if (MODE_ADMIN.equals(mode)) {
            openAdminDialog(p);
        } else if (MODE_STAFF.equals(mode)) {
            openStaffReadDialog(p);
        }
    }

    /** Chỉ admin: form trống để thêm phòng (không cần mở phòng có sẵn). */
    public void openAddPhongDialog() {
        if (!MODE_ADMIN.equals(mode)) {
            return;
        }
        openAdminDialog(null);
    }

    private void applyRoomStatusBadge(TextView tv, String trangThai) {
        String t = trangThai != null ? trangThai.trim() : "";
        if (t.isEmpty()) {
            t = "—";
        }
        tv.setText(t);
        int fgColor;
        int bgDrawable;
        if (isPhongTrong(t)) {
            fgColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnPrimaryContainer,
                    ContextCompat.getColor(context, R.color.theme_green_on_primary_container));
            bgDrawable = R.drawable.bg_room_badge_trong;
        } else if (isPhongDang(t)) {
            fgColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSecondaryContainer,
                    ContextCompat.getColor(context, R.color.app_on_surface_variant));
            bgDrawable = R.drawable.bg_room_badge_dang;
        } else {
            fgColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant,
                    ContextCompat.getColor(context, R.color.app_on_surface_variant));
            bgDrawable = R.drawable.bg_room_badge_khac;
        }
        tv.setTextColor(fgColor);
        tv.setBackgroundResource(bgDrawable);
    }

    private static boolean isPhongTrong(String t) {
        return t.contains("Trống") || t.contains("trống") || t.contains("Trong") || t.contains("trong");
    }

    private static boolean isPhongDang(String t) {
        return t.contains("Đang") || t.contains("đang") || t.contains("Dang") || t.contains("dang");
    }

    private void openStaffReadDialog(PhongFull p) {
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_xem_phong, null);
        TextView txtTen = v.findViewById(R.id.txtXemTenPhong);
        TextView txtGia = v.findViewById(R.id.txtXemGia);
        TextView txtMoTa = v.findViewById(R.id.txtXemMoTa);
        TextView txtTt = v.findViewById(R.id.txtXemTrangThai);
        LinearLayout gallery = v.findViewById(R.id.layoutXemGalleryPhong);
        txtTen.setText(p.getTenPhong());
        txtGia.setText(String.format(Locale.getDefault(), "%,.0f đ/đêm", p.getGiaNgay()));
        txtMoTa.setText(p.getMoTa() != null ? p.getMoTa() : "");
        txtTt.setText(context.getString(R.string.phong_staff_trang_thai_fmt, p.getTrangThai()));
        fillHorizontalGallery(gallery, p.resolveAllImageRefs());
        fillDanhGiaSection(v, p.getPhongID(), true);
        new MaterialAlertDialogBuilder(context)
                .setView(v)
                .setPositiveButton(R.string.phong_detail_close, null)
                .show();
    }

    private void fillHorizontalGallery(@Nullable LinearLayout row, java.util.List<String> refs) {
        if (row == null) {
            return;
        }
        row.removeAllViews();
        if (refs == null || refs.isEmpty()) {
            return;
        }
        LayoutInflater inf = LayoutInflater.from(context);
        for (String ref : refs) {
            View cell = inf.inflate(R.layout.item_room_gallery_image, row, false);
            ImageView iv = cell.findViewById(R.id.imgRoomGallery);
            RoomImageUtils.loadInto(iv, ref);
            row.addView(cell);
        }
    }

    /** Khách / vãng lai: xem các khoảng đang có đơn (không hiện thông tin cá nhân). */
    private void showGuestRoomScheduleDialog(PhongFull p) {
        DatPhongDAO dpDao = new DatPhongDAO(context);
        List<DatPhong> active = dpDao.getActiveBookingsByPhongId(p.getPhongID());
        View scheduleView = LayoutInflater.from(context).inflate(R.layout.dialog_phong_lich_khach, null);
        TextView body = scheduleView.findViewById(R.id.txtPhongLichKhachBody);
        if (active.isEmpty()) {
            body.setText(context.getString(R.string.dat_phong_schedule_empty));
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < active.size(); i++) {
                sb.append("▮ ").append(formatGuestScheduleDateRange(active.get(i)));
                if (i < active.size() - 1) {
                    sb.append('\n');
                }
            }
            body.setText(sb);
        }
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dat_phong_schedule_title)
                .setView(scheduleView)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private String formatGuestScheduleDateRange(DatPhong d) {
        String dn = d.getNgayNhan() != null ? d.getNgayNhan() : "";
        String dt = d.getNgayTra() != null ? d.getNgayTra() : "";
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat day = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            in.setLenient(false);
            String a = dn.length() >= 10 ? day.format(in.parse(dn)) : dn;
            String b = dt.length() >= 10 ? day.format(in.parse(dt)) : dt;
            return a + " → " + b;
        } catch (Exception e) {
            return dn + " → " + dt;
        }
    }

    private void openBookingDialog(PhongFull p) {
        if (!(context instanceof FragmentActivity)) {
            Toast.makeText(context, "Không mở được lịch chọn ngày.", Toast.LENGTH_SHORT).show();
            return;
        }
        FragmentActivity act = (FragmentActivity) context;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_dat_phong, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        LinearLayout bookingGallery = view.findViewById(R.id.layoutDatPhongGalleryPhong);
        fillHorizontalGallery(bookingGallery, p.resolveAllImageRefs());

        view.findViewById(R.id.btnXemDanhGia).setOnClickListener(v -> {
            View content = LayoutInflater.from(context).inflate(R.layout.dialog_xem_danh_gia_dat_phong, null);
            fillDanhGiaSection(content, p.getPhongID(), false);
            new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.dat_phong_dialog_reviews_title)
                    .setView(content)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        });
        view.findViewById(R.id.btnXemGiaTheoGio).setOnClickListener(v -> showGiaTheoGioDialog(p));
        view.findViewById(R.id.btnXemLichPhong).setOnClickListener(v -> showGuestRoomScheduleDialog(p));

        LinearLayout layoutDichVuThem = view.findViewById(R.id.layoutDichVuThem);
        LinearLayout layoutQuickDateRanges = view.findViewById(R.id.layoutQuickDateRanges);
        View btnPickDateRange = view.findViewById(R.id.btnPickDateRange);
        View btnToggleDichVuThem = view.findViewById(R.id.btnToggleDichVuThem);
        View cardDichVuThem = view.findViewById(R.id.cardDichVuThem);
        TextView txtDichVuThemEmpty = view.findViewById(R.id.txtDichVuThemEmpty);
        DichVuDAO dichVuDAO = new DichVuDAO(context);
        List<DichVu> extraDv = dichVuDAO.getAll();
        final List<DichVuRow> dvRows = new ArrayList<>();

        EditText edtTen = view.findViewById(R.id.edtKhachTen);
        EditText edtSdt = view.findViewById(R.id.edtKhachSDT);
        EditText edtEmail = view.findViewById(R.id.edtKhachEmail);
        TextView txtNhan = view.findViewById(R.id.txtNgayNhan);
        TextView txtTra = view.findViewById(R.id.txtNgayTra);
        TextView txtGioNhan = view.findViewById(R.id.txtGioNhan);
        TextView txtGioTra = view.findViewById(R.id.txtGioTra);
        TextView txtTamTinh = view.findViewById(R.id.txtTamTinh);
        TextView txtCocHint = view.findViewById(R.id.txtCocHint);
        android.view.View cardPriceBreakdown = view.findViewById(R.id.cardPriceBreakdown);
        android.widget.LinearLayout layoutPriceNights = view.findViewById(R.id.layoutPriceNights);
        android.widget.LinearLayout layoutPriceServices = view.findViewById(R.id.layoutPriceServices);
        android.view.View divPriceServices = view.findViewById(R.id.divPriceServices);
        TextView txtPriceRoomTotal = view.findViewById(R.id.txtPriceRoomTotal);
        TextView txtPriceServicesTotal = view.findViewById(R.id.txtPriceServicesTotal);
        android.widget.LinearLayout rowPriceServicesTotal = view.findViewById(R.id.rowPriceServicesTotal);
        TextView txtPriceGrandTotal = view.findViewById(R.id.txtPriceGrandTotal);
        TextView txtPriceDeposit = view.findViewById(R.id.txtPriceDeposit);
        TextView txtPriceRemain = view.findViewById(R.id.txtPriceRemain);
        EditText edtSoNguoi = view.findViewById(R.id.edtSoNguoi);
        TextView txtSoNguoiToiDa = view.findViewById(R.id.txtSoNguoiToiDa);
        EditText edtGhiChu = view.findViewById(R.id.edtGhiChu);
        Button btn = view.findViewById(R.id.btnGuiDatPhong);

        final int toiDaPhong = Math.max(1, p.getSoNguoiToiDa());
        edtSoNguoi.setHint(context.getString(R.string.dat_phong_so_khach_hint, toiDaPhong));
        if (txtSoNguoiToiDa != null) {
            txtSoNguoiToiDa.setText(context.getString(R.string.dat_phong_so_khach_toi_da_label, toiDaPhong));
        }

        final Long[] checkInMs = {null};
        final Long[] checkOutMs = {null};
        final String[] gioNhan = {"14:00"};
        final String[] gioTra = {"12:00"};
        final Set<Long> blockedUtcDays = collectBlockedUtcDays(p.getPhongID());
        // Cache tập ngày lễ của phòng — load 1 lần khi mở dialog.
        final Set<String> ngayLeSet = new PhongGiaLeDAO(context).getNgayLeSetByPhongId(p.getPhongID());
        txtGioNhan.setText(gioNhan[0]);
        txtGioTra.setText(gioTra[0]);
        // Giờ nhận / trả là quy định của homestay (admin), khách chỉ xem và không được đổi.
        txtGioNhan.setEnabled(false);
        txtGioTra.setEnabled(false);
        txtGioNhan.setClickable(false);
        txtGioTra.setClickable(false);

        final Runnable[] updateQuickRangesRef = new Runnable[1];

        Runnable refreshTamTinh = () -> {
            if (txtTamTinh == null) {
                return;
            }
            if (txtCocHint != null) {
                txtCocHint.setVisibility(View.GONE);
            }
            double tongDv = 0;
            for (DichVuRow r : dvRows) {
                if (r.cb != null && r.cb.isChecked() && r.cb.getTag() instanceof DichVu) {
                    DichVu dv = (DichVu) r.cb.getTag();
                    int q = Math.max(1, r.qty);
                    tongDv += dv.getGia() * q;
                }
            }
            if (checkInMs[0] == null || checkOutMs[0] == null) {
                if (tongDv > 0) {
                    txtTamTinh.setVisibility(View.VISIBLE);
                    txtTamTinh.setText(String.format(Locale.getDefault(),
                            "%s\n(%s %,.0f đ)",
                            context.getString(R.string.dat_phong_total_pay_label),
                            context.getString(R.string.dat_phong_detail_services_only),
                            tongDv));
                } else {
                    txtTamTinh.setVisibility(View.GONE);
                }
                hidePriceBreakdown(cardPriceBreakdown);
                return;
            }
            String nhan = DatePickerHelper.formatYmdLocal(checkInMs[0]);
            String tra = DatePickerHelper.formatYmdLocal(checkOutMs[0]);
            int soDem = computeSoDem(nhan, tra);
            if (soDem < 1) {
                txtTamTinh.setVisibility(View.GONE);
                hidePriceBreakdown(cardPriceBreakdown);
                return;
            }
            // Tổng tiền phòng = tổng từng đêm, mỗi đêm áp giá lễ riêng nếu rơi vào ngày lễ.
            // Query fresh để luôn đúng dù ngày lễ được thêm sau khi mở dialog.
            Set<String> ngayLeFresh = new PhongGiaLeDAO(context).getNgayLeSetByPhongId(p.getPhongID());
            java.util.Map<String, Double> ngayLeHesoMap = new PhongGiaLeDAO(context).getNgayLeMapByPhongId(p.getPhongID());
            double tongPhong = PeakPricingUtil.tongTienPhong(
                    p, nhan, tra, gioNhan[0], gioTra[0], ngayLeFresh, 1.0, ngayLeHesoMap);
            double tong = tongPhong + tongDv;
            txtTamTinh.setVisibility(View.VISIBLE);
            String line = String.format(Locale.getDefault(),
                    "%s %,.0f đ\n(%s %,.0f đ + %s %,.0f đ)",
                    context.getString(R.string.dat_phong_total_pay_label),
                    tong,
                    context.getString(R.string.dat_phong_detail_room),
                    tongPhong,
                    context.getString(R.string.dat_phong_detail_services),
                    tongDv);
            // Ghi chú: có đêm là ngày lễ / giờ cao điểm.
            String note = buildPricingNote(p, nhan, tra, gioNhan[0], gioTra[0], ngayLeFresh);
            if (!note.isEmpty()) {
                line += "\n" + note;
            }
            txtTamTinh.setText(line);
            if (txtCocHint != null) {
                double coc = tong * 0.2;
                txtCocHint.setText(String.format(Locale.getDefault(),
                        "Cọc tối thiểu (20%%): %,.0f đ", coc));
                txtCocHint.setVisibility(View.VISIBLE);
            }
            // Render breakdown chi tiết.
            renderPriceBreakdown(
                    cardPriceBreakdown, layoutPriceNights, layoutPriceServices,
                    divPriceServices, txtPriceRoomTotal, txtPriceServicesTotal,
                    rowPriceServicesTotal, txtPriceGrandTotal, txtPriceDeposit, txtPriceRemain,
                    p, nhan, tra, gioNhan[0], gioTra[0], ngayLeFresh, dvRows);
        };

        if (extraDv.isEmpty()) {
            txtDichVuThemEmpty.setVisibility(View.VISIBLE);
            if (cardDichVuThem != null) {
                cardDichVuThem.setVisibility(View.GONE);
            }
            layoutDichVuThem.setVisibility(View.GONE);
            if (btnToggleDichVuThem != null) {
                btnToggleDichVuThem.setVisibility(View.GONE);
            }
        } else {
            txtDichVuThemEmpty.setVisibility(View.GONE);
            if (cardDichVuThem != null) {
                cardDichVuThem.setVisibility(View.VISIBLE);
            }
            layoutDichVuThem.setVisibility(View.VISIBLE);
            if (btnToggleDichVuThem instanceof com.google.android.material.button.MaterialButton) {
                com.google.android.material.button.MaterialButton t =
                        (com.google.android.material.button.MaterialButton) btnToggleDichVuThem;
                final boolean[] expanded = {true};
                t.setText(R.string.dat_phong_services_collapse);
                t.setOnClickListener(v -> {
                    expanded[0] = !expanded[0];
                    if (cardDichVuThem != null) {
                        cardDichVuThem.setVisibility(expanded[0] ? View.VISIBLE : View.GONE);
                    }
                    if (txtDichVuThemEmpty != null) {
                        txtDichVuThemEmpty.setVisibility(expanded[0] ? View.GONE : View.VISIBLE);
                    }
                    t.setText(expanded[0]
                            ? R.string.dat_phong_services_collapse
                            : R.string.dat_phong_services_expand);
                });
            }
            LayoutInflater infDv = LayoutInflater.from(context);
            for (DichVu dv : extraDv) {
                View rowV = infDv.inflate(R.layout.item_booking_dich_vu_checkbox, layoutDichVuThem, false);
                DichVuRow row = new DichVuRow(rowV);
                row.cb.setText(String.format(Locale.getDefault(), "%s — %,.0f đ",
                        dv.getTenDichVu(), dv.getGia()));
                row.cb.setTag(dv);
                row.setQty(0);

                row.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    row.setQty(isChecked ? Math.max(1, row.qty) : 0);
                    refreshTamTinh.run();
                });

                if (row.btnMinus != null) {
                    row.btnMinus.setOnClickListener(v -> {
                        if (!row.cb.isChecked()) {
                            row.cb.setChecked(true);
                        }
                        int next = Math.max(1, row.qty) - 1;
                        if (next <= 0) {
                            row.cb.setChecked(false);
                        } else {
                            row.setQty(next);
                            refreshTamTinh.run();
                        }
                    });
                }
                if (row.btnPlus != null) {
                    row.btnPlus.setOnClickListener(v -> {
                        if (!row.cb.isChecked()) {
                            row.cb.setChecked(true);
                        }
                        int next = Math.max(1, row.qty) + 1;
                        row.setQty(next);
                        refreshTamTinh.run();
                    });
                }

                layoutDichVuThem.addView(rowV);
                dvRows.add(row);
            }
        }

        java.util.function.BiConsumer<Long, Long> applyDateRangeMs = (startMs, endMs) -> {
            if (startMs == null || endMs == null) {
                return;
            }
            checkInMs[0] = startMs;
            checkOutMs[0] = endMs;
            txtNhan.setText(DatePickerHelper.formatYmdLocal(startMs));
            txtTra.setText(DatePickerHelper.formatYmdLocal(endMs));
            refreshTamTinh.run();
            if (updateQuickRangesRef[0] != null) {
                updateQuickRangesRef[0].run();
            }
        };

        if (layoutQuickDateRanges != null) {
            Runnable updateQuickRanges = () -> {
                layoutQuickDateRanges.removeAllViews();
                long dayMs = 24L * 60L * 60L * 1000L;
                long base = DatePickerHelper.startOfTodayUtcMillis();
                LayoutInflater chipInf = LayoutInflater.from(context);
                for (int k = 0; k < 3; k++) {
                    final long s = base + k * dayMs;
                    final long e = s + dayMs;
                    String label = formatShortDayLabel(s) + "-" + formatShortDayLabel(e);
                    com.google.android.material.button.MaterialButton b =
                            (com.google.android.material.button.MaterialButton) chipInf.inflate(
                                    R.layout.item_booking_quick_range_button, layoutQuickDateRanges, false);
                    b.setText(label);
                    String ymdS = DatePickerHelper.formatYmdLocal(s);
                    String ymdE = DatePickerHelper.formatYmdLocal(e);
                    boolean overlap = new DatPhongDAO(context).hasOverlap(
                            p.getPhongID(), ymdS, ymdE, gioNhan[0], gioTra[0], null);
                    b.setEnabled(!overlap);
                    if (!overlap) {
                        b.setOnClickListener(v -> applyDateRangeMs.accept(s, e));
                    } else {
                        b.setAlpha(0.45f);
                    }
                    if (checkInMs[0] != null && checkOutMs[0] != null
                            && checkInMs[0].longValue() == s
                            && checkOutMs[0].longValue() == e) {
                        b.setStrokeWidth(3);
                    }
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.setMarginEnd(8);
                    layoutQuickDateRanges.addView(b, lp);
                }
            };
            updateQuickRangesRef[0] = updateQuickRanges;
            updateQuickRanges.run();
        }

        if (btnPickDateRange != null) {
            btnPickDateRange.setOnClickListener(v -> {
                long min = DatePickerHelper.startOfTodayUtcMillis();
                Pair<Long, Long> init = null;
                if (checkInMs[0] != null && checkOutMs[0] != null) {
                    init = Pair.create(checkInMs[0], checkOutMs[0]);
                }
                Long openAt = checkInMs[0] != null ? checkInMs[0] : min;
                DatePickerHelper.showPickDateRange(
                        act,
                        context.getString(R.string.dat_phong_pick_range_title),
                        min,
                        init,
                        openAt,
                        blockedUtcDays,
                        (startUtc, endUtc) -> {
                            applyDateRangeMs.accept(startUtc, endUtc);
                        });
            });
        }

        TaiKhoanDAO tkDao = new TaiKhoanDAO(context);
        if (session != null && session.isLoggedIn() && session.isKhach()) {
            TaiKhoan u = tkDao.getById(session.getTaiKhoanId());
            if (u != null) {
                edtTen.setText(u.getTenNguoiDung() != null ? u.getTenNguoiDung() : "");
                edtSdt.setText(u.getDienThoai() != null ? u.getDienThoai() : "");
                edtEmail.setText(u.getEmail() != null ? u.getEmail() : "");
            } else {
                edtTen.setText(session.getDisplayName());
            }
        }

        txtNhan.setOnClickListener(v -> {
            long min = DatePickerHelper.startOfTodayUtcMillis();
            long openAt = checkInMs[0] != null ? checkInMs[0] : min;
            DatePickerHelper.showPickDate(act,
                    context.getString(R.string.dat_phong_pick_checkin_title),
                    min,
                    checkInMs[0],
                    openAt,
                    blockedUtcDays,
                    sel -> {
                        checkInMs[0] = sel;
                        checkOutMs[0] = null;
                        txtNhan.setText(DatePickerHelper.formatYmdLocal(sel));
                        txtTra.setText("");
                        refreshTamTinh.run();
                        if (updateQuickRangesRef[0] != null) {
                            updateQuickRangesRef[0].run();
                        }
                    });
        });
        txtTra.setOnClickListener(v -> {
            if (checkInMs[0] == null) {
                Toast.makeText(context, "Chọn ngày nhận trước", Toast.LENGTH_SHORT).show();
                return;
            }
            long min = checkInMs[0];
            long initial = checkOutMs[0] != null ? checkOutMs[0] : min;
            long openAt = checkOutMs[0] != null ? checkOutMs[0] : checkInMs[0];
            DatePickerHelper.showPickDate(act,
                    context.getString(R.string.dat_phong_pick_checkout_title),
                    min,
                    initial,
                    openAt,
                    blockedUtcDays,
                    sel -> {
                        checkOutMs[0] = sel;
                        txtTra.setText(DatePickerHelper.formatYmdLocal(sel));
                        refreshTamTinh.run();
                        if (updateQuickRangesRef[0] != null) {
                            updateQuickRangesRef[0].run();
                        }
                    });
        });

        btn.setOnClickListener(v -> {
            String ten = edtTen.getText().toString().trim();
            String sdt = edtSdt.getText().toString().trim();
            if (checkInMs[0] == null || checkOutMs[0] == null) {
                Toast.makeText(context, "Chọn đủ ngày nhận và ngày trả", Toast.LENGTH_SHORT).show();
                return;
            }
            String nhan = DatePickerHelper.formatYmdLocal(checkInMs[0]);
            String tra = DatePickerHelper.formatYmdLocal(checkOutMs[0]);
            if (ten.isEmpty() || sdt.isEmpty()) {
                Toast.makeText(context, "Điền họ tên và SĐT", Toast.LENGTH_SHORT).show();
                return;
            }
            int soNguoi = 1;
            try {
                soNguoi = Integer.parseInt(edtSoNguoi.getText().toString().trim());
                if (soNguoi < 1) soNguoi = 1;
            } catch (NumberFormatException ignored) {
                soNguoi = 1;
            }
            if (soNguoi > toiDaPhong) {
                Toast.makeText(context,
                        context.getString(R.string.dat_phong_so_khach_vuot_qua, toiDaPhong),
                        Toast.LENGTH_LONG).show();
                return;
            }
            int soDem = computeSoDem(nhan, tra);
            if (soDem < 1) {
                Toast.makeText(context, "Ngày trả phải sau hoặc cùng ngày nhận", Toast.LENGTH_SHORT).show();
                return;
            }
            if (DatPhongIntervalUtil.tryStayIntervalHalfOpen(nhan, tra, gioNhan[0], gioTra[0]) == null) {
                Toast.makeText(context, context.getString(R.string.dat_phong_invalid_stay_time), Toast.LENGTH_LONG).show();
                return;
            }
            Set<String> ngayLeFresh2 = new PhongGiaLeDAO(context).getNgayLeSetByPhongId(p.getPhongID());
            java.util.Map<String, Double> ngayLeHesoMap2 = new PhongGiaLeDAO(context).getNgayLeMapByPhongId(p.getPhongID());
            double tongPhong = PeakPricingUtil.tongTienPhong(
                    p, nhan, tra, gioNhan[0], gioTra[0], ngayLeFresh2, 1.0, ngayLeHesoMap2);
            double tongDv = 0;
            for (DichVuRow r : dvRows) {
                if (r.cb != null && r.cb.isChecked() && r.cb.getTag() instanceof DichVu) {
                    DichVu dv = (DichVu) r.cb.getTag();
                    int q = Math.max(1, r.qty);
                    tongDv += dv.getGia() * q;
                }
            }
            double tong = tongPhong + tongDv;

            DatPhong d = new DatPhong();
            d.setMaDatPhong("DP" + System.currentTimeMillis());
            d.setPhongID(p.getPhongID());
            d.setNgayNhan(nhan);
            d.setNgayTra(tra);
            d.setSoDem(soDem);
            d.setTongTien(tong);
            d.setGioNhan(gioNhan[0]);
            d.setGioTra(gioTra[0]);
            d.setTrangThai(DatPhongDAO.TT_CHO_XAC_NHAN);
            d.setKhachTen(ten);
            d.setKhachEmail(edtEmail.getText().toString().trim());
            d.setKhachDienThoai(sdt);
            d.setKhachCccd("");
            d.setSoNguoi(soNguoi);
            d.setGhiChu(edtGhiChu.getText().toString().trim());
            d.setNgayTao(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

            if (session != null && session.isLoggedIn() && session.isKhach()) {
                d.setTaiKhoanID(session.getTaiKhoanId());
                d.setLoaiDat("tai_khoan");
            } else {
                d.setTaiKhoanID(null);
                d.setLoaiDat("khach_vang_lai");
            }

            DatPhongDAO dpDao = new DatPhongDAO(context);
            long rowId = dpDao.insertIfNoOverlap(d);
            if (rowId == DatPhongDAO.INSERT_OVERLAP) {
                Toast.makeText(context, context.getString(R.string.dat_phong_overlap_error), Toast.LENGTH_LONG).show();
                return;
            }
            if (rowId == DatPhongDAO.INSERT_INVALID_STAY) {
                Toast.makeText(context, context.getString(R.string.dat_phong_invalid_stay_time), Toast.LENGTH_LONG).show();
                return;
            }
            if (rowId != -1) {
                for (DichVuRow r : dvRows) {
                    if (r.cb != null && r.cb.isChecked() && r.cb.getTag() instanceof DichVu) {
                        DichVu dv = (DichVu) r.cb.getTag();
                        int q = Math.max(1, r.qty);
                        dpDao.insertDatPhongDichVu((int) rowId, dv.getDichVuID(), q);
                    }
                }
                boolean loggedKhach = session != null && session.isLoggedIn() && session.isKhach();
                if (loggedKhach) {
                    double coc = tong * 0.2;
                    showBookingSuccessDialog(dialog,
                            context.getString(R.string.dat_phong_success_logged_in,
                                    d.getMaDatPhong(), tong, coc));
                } else {
                    TaiKhoan tkLink = null;
                    boolean accountNew = false;
                    String digits = TaiKhoanDAO.normalizePhoneDigits(sdt);
                    if (digits.length() >= 9) {
                        tkLink = tkDao.findKhachByPhoneDigits(digits);
                        if (tkLink == null) {
                            TaiKhoan nu = new TaiKhoan();
                            String user = digits;
                            if (tkDao.existsTenDangNhap(user)) {
                                user = "k" + digits;
                            }
                            nu.setTenDangNhap(user);
                            nu.setMatKhau("123");
                            nu.setRole("khach");
                            nu.setTenNguoiDung(ten);
                            nu.setDienThoai(sdt);
                            nu.setEmail(edtEmail.getText().toString().trim());
                            nu.setCccd("");
                            nu.setAnhDaiDien("");
                            long tid = tkDao.insert(nu);
                            if (tid != -1) {
                                tkLink = tkDao.getById((int) tid);
                                accountNew = true;
                            }
                        }
                        if (tkLink != null) {
                            dpDao.updateGanTaiKhoanKhach((int) rowId, tkLink.getTaiKhoanID());
                            SessionManager sm = new SessionManager(context);
                            sm.saveUser(tkLink);
                            sm.setMustChangePassword(accountNew);
                            if (context instanceof MainActivity) {
                                ((MainActivity) context).refreshSessionUi();
                            }
                        }
                    }
                    String msgBody;
                    double coc = tong * 0.2;
                    if (tkLink != null) {
                        if (accountNew) {
                            msgBody = context.getString(R.string.dat_phong_success_new_account,
                                    d.getMaDatPhong(), tong, coc,
                                    tkLink.getTenDangNhap(),
                                    tkLink.getTenNguoiDung(),
                                    tkLink.getDienThoai());
                        } else {
                            msgBody = context.getString(R.string.dat_phong_success_linked_account,
                                    d.getMaDatPhong(), tong, coc,
                                    tkLink.getTenDangNhap(),
                                    tkLink.getTenNguoiDung());
                        }
                    } else {
                        msgBody = context.getString(R.string.dat_phong_success_guest_no_login,
                                d.getMaDatPhong(), tong, coc);
                    }
                    showBookingSuccessDialog(dialog, msgBody);
                }
            } else {
                Toast.makeText(context, "Không thể lưu đặt phòng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showBookingSuccessDialog(AlertDialog bookingFormDialog, String message) {
        AlertDialog success = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dat_phong_success_title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (d1, w) -> bookingFormDialog.dismiss())
                .create();
        success.setOnDismissListener(di -> {
            if (context instanceof MainActivity) {
                ((MainActivity) context).getWindow().getDecorView().post(
                        () -> ((MainActivity) context).maybeShowFirstPasswordReminder());
            }
        });
        success.show();
    }

    /** Hiển thị đánh giá theo phòng trong dialog đặt / dialog xem (nhân viên). */
    private void fillDanhGiaSection(View root, int phongId, boolean layoutXemVariant) {
        LinearLayout layout = root.findViewById(layoutXemVariant ? R.id.layoutDanhGiaPhongXem : R.id.layoutDanhGiaPhong);
        TextView txtTom = root.findViewById(layoutXemVariant ? R.id.txtDanhGiaTomTatXem : R.id.txtDanhGiaTomTat);
        if (layout == null) {
            return;
        }
        layout.removeAllViews();
        DanhGiaDAO dgDao = new DanhGiaDAO(context);
        List<DanhGia> dgList = dgDao.getByPhongId(phongId);
        if (dgList.isEmpty()) {
            View empty = LayoutInflater.from(context).inflate(R.layout.item_review_list_empty, layout, false);
            layout.addView(empty);
            if (txtTom != null) {
                txtTom.setVisibility(View.GONE);
            }
            return;
        }
        double sum = 0;
        for (DanhGia d : dgList) {
            sum += d.getSoSao();
        }
        double avg = sum / dgList.size();
        if (txtTom != null) {
            txtTom.setVisibility(View.VISIBLE);
            txtTom.setText(context.getString(R.string.phong_reviews_summary, avg, dgList.size()));
        }
        LayoutInflater inf = LayoutInflater.from(context);
        for (DanhGia d : dgList) {
            View row = inf.inflate(R.layout.item_review_inline, layout, false);
            TextView h = row.findViewById(R.id.txtReviewHeader);
            TextView b = row.findViewById(R.id.txtReviewBody);
            String ngay = d.getNgayTao() != null ? d.getNgayTao() : "";
            h.setText(d.getTenHienThi() + " · " + starLine(d.getSoSao()) + " · " + ngay);
            b.setText(d.getNoiDung() != null ? d.getNoiDung() : "");
            layout.addView(row);
        }
    }

    private static String starLine(int n) {
        n = Math.max(0, Math.min(5, n));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append('★');
        }
        for (int i = n; i < 5; i++) {
            sb.append('☆');
        }
        return sb.toString();
    }

    private void showGiaTheoGioDialog(PhongFull p) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.getDefault(),
                "Giá thường: %,.0f đ/đêm\n", p.getGiaNgay()));
        if (p.getGiaCaoDiem() > 0 || p.getHeSoCaoDiem() > 1.0) {
            double giaCaoDiemHeSo = p.getGiaNgay() * p.getHeSoCaoDiem();
            sb.append(String.format(Locale.getDefault(),
                    "Giá giờ cao điểm: %,.0f đ/đêm (hệ số x%s)\n",
                    Math.max(p.getGiaCaoDiem(), giaCaoDiemHeSo), p.getHeSoCaoDiem()));
            String tu = p.getGioCaoDiemTu();
            String den = p.getGioCaoDiemDen();
            if (tu != null && den != null && !tu.isEmpty() && !den.isEmpty()) {
                sb.append(String.format(Locale.getDefault(),
                        "Khung giờ: %s → %s\n", tu, den));
            }
            sb.append("\nNếu giờ nhận hoặc giờ trả nằm trong khung trên, mỗi đêm áp giá cao điểm.");
        } else {
            sb.append("\nPhòng này chưa cấu hình giờ cao điểm — mọi đêm tính theo giá thường.");
        }

        // Danh sách ngày lễ sắp tới (tối đa 5).
        PhongGiaLeDAO leDao = new PhongGiaLeDAO(context);
        List<PhongGiaLe> dsLe = leDao.getByPhongId(p.getPhongID());
        if (!dsLe.isEmpty()) {
            sb.append("\n\nNgày lễ áp giá riêng:");
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            int shown = 0;
            for (PhongGiaLe g : dsLe) {
                if (g.getNgayLe() == null) continue;
                if (g.getNgayLe().compareTo(today) < 0) continue; // bỏ ngày đã qua
                shown++;
                if (shown > 5) break;
                String ten = g.getGhiChu() != null && !g.getGhiChu().isEmpty()
                        ? g.getGhiChu() : "Ngày lễ";
                sb.append(String.format(Locale.getDefault(),
                        "\n• %s — x%s (%s) → %,.0f đ/đêm",
                        g.getNgayLe(), g.getHeSoNhan(), ten,
                        p.getGiaNgay() * g.getHeSoNhan()));
            }
            if (shown == 0) {
                sb.append("\n(không có ngày lễ sắp tới)");
            }
        }

        sb.append("\n\nNgày lễ luôn đè giờ cao điểm.");
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dat_phong_dialog_price_title)
                .setMessage(sb.toString())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    /**
     * Ghi chú ngắn gọn cho tổng tiền: có bao nhiêu đêm lễ / đêm cao điểm.
     */
    private String buildPricingNote(PhongFull p, String nhan, String tra,
                                    String gioNhan, String gioTra, Set<String> ngayLeSet) {
        if (p == null) return "";
        int soDem = computeSoDem(nhan, tra);
        if (soDem < 1) return "";
        int demLe = 0;
        int demPeak = 0;
        for (int i = 0; i < soDem; i++) {
            String ngayDem = congNgayYmd(nhan, i);
            if (ngayLeSet != null && ngayLeSet.contains(ngayDem)) {
                demLe++;
            } else if (PeakPricingUtil.isInPeakWindow(gioNhan, p.getGioCaoDiemTu(), p.getGioCaoDiemDen())
                    || PeakPricingUtil.isInPeakWindow(gioTra, p.getGioCaoDiemTu(), p.getGioCaoDiemDen())) {
                demPeak++;
            }
        }
        StringBuilder note = new StringBuilder();
        if (demLe > 0) {
            note.append(String.format(Locale.getDefault(),
                    "• %d đêm là ngày lễ (giá riêng). ", demLe));
        }
        if (demPeak > 0) {
            note.append(String.format(Locale.getDefault(),
                    "• %d đêm rơi vào khung giờ cao điểm.", demPeak));
        }
        return note.toString().trim();
    }

    private static String congNgayYmd(String ymd, int offsetDays) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setLenient(false);
            Date d = sdf.parse(ymd);
            if (d == null) return ymd;
            Calendar c = Calendar.getInstance(Locale.US);
            c.setTime(d);
            c.add(Calendar.DAY_OF_MONTH, offsetDays);
            return sdf.format(c.getTime());
        } catch (Exception e) {
            return ymd;
        }
    }

    private static int computeSoDem(String d1, String d2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setLenient(false);
            Date a = sdf.parse(d1);
            Date b = sdf.parse(d2);
            if (a == null || b == null) return -1;
            if (b.before(a)) return -1;
            long diff = b.getTime() - a.getTime();
            int days = (int) TimeUnit.MILLISECONDS.toDays(diff);
            return Math.max(1, days);
        } catch (Exception e) {
            return -1;
        }
    }

    private static void hidePriceBreakdown(android.view.View card) {
        if (card != null) card.setVisibility(View.GONE);
    }

    /**
     * Render bảng chi tiết: mỗi đêm 1 dòng (ngày, loại, đơn giá, thành tiền) + danh sách dịch vụ +
     * tổng phòng + tổng DV + tổng cộng + cọc 20% + còn lại.
     */
    private void renderPriceBreakdown(
            android.view.View card,
            android.widget.LinearLayout layoutNights,
            android.widget.LinearLayout layoutServices,
            android.view.View divServices,
            TextView txtRoomTotal,
            TextView txtServicesTotal,
            android.widget.LinearLayout rowServicesTotal,
            TextView txtGrandTotal,
            TextView txtDeposit,
            TextView txtRemain,
            PhongFull p, String nhanYmd, String traYmd,
            String gioNhan, String gioTra, Set<String> ngayLeSet /* chỉ dùng cho breakdown note; ngày lễ luôn query lại để đảm bảo fresh */,
            List<DichVuRow> dvRows) {
        if (card == null || layoutNights == null) return;

        // Luôn query fresh ngày lễ từ DB — tránh cache cũ khi dialog đã mở.
        Set<String> freshNgayLeSet = new PhongGiaLeDAO(context).getNgayLeSetByPhongId(p.getPhongID());

        int soDem = computeSoDem(nhanYmd, traYmd);
        if (soDem < 1) {
            hidePriceBreakdown(card);
            return;
        }
        card.setVisibility(View.VISIBLE);
        LayoutInflater inf = LayoutInflater.from(context);

        // Query fresh ngày lễ + hệ số từ DB.
        java.util.Map<String, Double> ngayLeHesoMap = new PhongGiaLeDAO(context).getNgayLeMapByPhongId(p.getPhongID());

        // Từng đêm.
        layoutNights.removeAllViews();
        double tongPhong = 0;
        SimpleDateFormat inYmd = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat outDmy = new SimpleDateFormat("dd/MM", Locale.getDefault());
        for (int i = 0; i < soDem; i++) {
            String ngayDem = congNgayYmd(nhanYmd, i);
            double giaDem = PeakPricingUtil.demGiaMotDem(
                    p, gioNhan, gioTra, ngayDem, freshNgayLeSet, 1.0, ngayLeHesoMap);
            tongPhong += giaDem;

            // Loại đêm.
            int kindLabel;
            if (freshNgayLeSet != null && freshNgayLeSet.contains(ngayDem)) {
                kindLabel = R.string.dat_phong_breakdown_night_kind_le;
            } else if (PeakPricingUtil.isInPeakWindow(gioNhan, p.getGioCaoDiemTu(), p.getGioCaoDiemDen())
                    || PeakPricingUtil.isInPeakWindow(gioTra, p.getGioCaoDiemTu(), p.getGioCaoDiemDen())) {
                kindLabel = R.string.dat_phong_breakdown_night_kind_peak;
            } else {
                kindLabel = R.string.dat_phong_breakdown_night_kind_thuong;
            }
            String dmy = ngayDem;
            try {
                Date d = inYmd.parse(ngayDem);
                if (d != null) dmy = outDmy.format(d);
            } catch (Exception ignored) {}

            android.widget.LinearLayout row = new android.widget.LinearLayout(context);
            row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            android.widget.LinearLayout.LayoutParams rowLp = new android.widget.LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowLp.topMargin = dp(2);
            row.setLayoutParams(rowLp);

            TextView left = new TextView(context);
            android.widget.LinearLayout.LayoutParams leftLp = new android.widget.LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            left.setLayoutParams(leftLp);
            left.setText(context.getString(R.string.dat_phong_breakdown_night_fmt, dmy, context.getString(kindLabel)));
            left.setTextSize(13);

            TextView right = new TextView(context);
            right.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            right.setText(String.format(Locale.getDefault(), "%,.0f đ", giaDem));
            right.setTextSize(13);

            row.addView(left);
            row.addView(right);
            layoutNights.addView(row);
        }

        // Dịch vụ thêm.
        double tongDv = 0;
        boolean coDv = false;
        if (layoutServices != null) layoutServices.removeAllViews();
        for (DichVuRow r : dvRows) {
            if (r.cb == null || !r.cb.isChecked() || !(r.cb.getTag() instanceof DichVu)) continue;
            DichVu dv = (DichVu) r.cb.getTag();
            int q = Math.max(1, r.qty);
            double tien = dv.getGia() * q;
            tongDv += tien;
            coDv = true;
            if (layoutServices == null) continue;
            android.widget.LinearLayout row = new android.widget.LinearLayout(context);
            row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            row.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView left = new TextView(context);
            android.widget.LinearLayout.LayoutParams leftLp = new android.widget.LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            left.setLayoutParams(leftLp);
            left.setText(String.format(Locale.getDefault(), "%s × %d", dv.getTenDichVu(), q));
            left.setTextSize(13);
            TextView right = new TextView(context);
            right.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            right.setText(String.format(Locale.getDefault(), "%,.0f đ", tien));
            right.setTextSize(13);
            row.addView(left);
            row.addView(right);
            layoutServices.addView(row);
        }
        int dvVis = coDv ? View.VISIBLE : View.GONE;
        if (layoutServices != null) layoutServices.setVisibility(dvVis);
        if (divServices != null) divServices.setVisibility(dvVis);
        if (rowServicesTotal != null) rowServicesTotal.setVisibility(dvVis);

        // Tổng.
        double tong = tongPhong + tongDv;
        double coc = tong * 0.2;
        if (txtRoomTotal != null) {
            txtRoomTotal.setText(String.format(Locale.getDefault(), "%,.0f đ", tongPhong));
        }
        if (txtServicesTotal != null) {
            txtServicesTotal.setText(String.format(Locale.getDefault(), "%,.0f đ", tongDv));
        }
        if (txtGrandTotal != null) {
            txtGrandTotal.setText(String.format(Locale.getDefault(), "%,.0f đ", tong));
        }
        if (txtDeposit != null) {
            txtDeposit.setText(String.format(Locale.getDefault(), "%,.0f đ", coc));
        }
        if (txtRemain != null) {
            txtRemain.setText(String.format(Locale.getDefault(), "%,.0f đ", Math.max(0, tong - coc)));
        }
    }

    private int dp(int v) {
        float d = context.getResources().getDisplayMetrics().density;
        return (int) (v * d + 0.5f);
    }

    private static String formatShortDayLabel(long utcMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
        return sdf.format(new Date(utcMillis));
    }

    private Set<Long> collectBlockedUtcDays(int phongId) {
        Set<Long> out = new HashSet<>();
        DatPhongDAO dpDao = new DatPhongDAO(context);
        List<DatPhong> active = dpDao.getActiveBookingsByPhongId(phongId);
        for (DatPhong d : active) {
            addBlockedDays(out, d.getNgayNhan(), d.getNgayTra());
        }
        return out;
    }

    private static void addBlockedDays(Set<Long> target, String fromYmd, String toYmd) {
        Calendar start = parseYmdLocalStart(fromYmd);
        Calendar end = parseYmdLocalStart(toYmd);
        if (start == null || end == null) {
            return;
        }
        Calendar cur = (Calendar) start.clone();
        while (!cur.after(end)) {
            target.add(toUtcDayMillis(
                    cur.get(Calendar.YEAR),
                    cur.get(Calendar.MONTH),
                    cur.get(Calendar.DAY_OF_MONTH)));
            cur.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    @Nullable
    private static Calendar parseYmdLocalStart(String ymd) {
        if (ymd == null || ymd.trim().isEmpty()) {
            return null;
        }
        try {
            String[] p = ymd.trim().split("-");
            if (p.length != 3) {
                return null;
            }
            int y = Integer.parseInt(p[0]);
            int m = Integer.parseInt(p[1]);
            int d = Integer.parseInt(p[2]);
            Calendar c = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
            c.setLenient(false);
            c.set(Calendar.YEAR, y);
            c.set(Calendar.MONTH, m - 1);
            c.set(Calendar.DAY_OF_MONTH, d);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            c.getTimeInMillis();
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    private static long toUtcDayMillis(int year, int monthZeroBased, int dayOfMonth) {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.getDefault());
        utc.setLenient(false);
        utc.set(Calendar.YEAR, year);
        utc.set(Calendar.MONTH, monthZeroBased);
        utc.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        utc.set(Calendar.HOUR_OF_DAY, 0);
        utc.set(Calendar.MINUTE, 0);
        utc.set(Calendar.SECOND, 0);
        utc.set(Calendar.MILLISECOND, 0);
        return utc.getTimeInMillis();
    }

    private void openAdminDialog(@Nullable PhongFull p) {
        final boolean isNewRoom = (p == null);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(isNewRoom
                ? context.getString(R.string.phong_add_dialog_title)
                : p.getTenPhong());
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_phong, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();

        EditText edtTen = view.findViewById(R.id.edtTenPhong);
        EditText edtGia = view.findViewById(R.id.edtGia);
        com.google.android.material.textfield.TextInputEditText edtGiaCaoDiem =
                view.findViewById(R.id.edtGiaCaoDiem);
        com.google.android.material.textfield.TextInputEditText edtHeSoCaoDiem =
                view.findViewById(R.id.edtHeSoCaoDiem);
        TextView txtGioCaoDiemTu = view.findViewById(R.id.txtGioCaoDiemTu);
        TextView txtGioCaoDiemDen = view.findViewById(R.id.txtGioCaoDiemDen);
        EditText edtMoTa = view.findViewById(R.id.edtMoTa);
        EditText edtTrangThai = view.findViewById(R.id.edtTrangThai);
        com.google.android.material.textfield.TextInputEditText edtSoNguoiToiDa =
                view.findViewById(R.id.edtSoNguoiToiDa);
        EditText edtAnh = view.findViewById(R.id.edtAnh);
        LinearLayout layoutAnhPreview = view.findViewById(R.id.layoutAnhPreview);
        View btnChonAnh = view.findViewById(R.id.btnChonAnh);
        View btnXoaHetAnh = view.findViewById(R.id.btnXoaHetAnh);
        View btnQuanLyNgayLe = view.findViewById(R.id.btnQuanLyNgayLe);

        Button btnUpdate = view.findViewById(R.id.btnUpdate);
        Button btnDelete = view.findViewById(R.id.btnDelete);
        Button btnAdd = view.findViewById(R.id.btnAdd);

        pendingAdminPreview = layoutAnhPreview;
        if (isNewRoom) {
            pendingAdminPaths = new ArrayList<>();
            refreshAdminPreview();
            edtTen.setText("");
            edtGia.setText("");
            edtGiaCaoDiem.setText("");
            edtHeSoCaoDiem.setText("1.0");
            txtGioCaoDiemTu.setText("—");
            txtGioCaoDiemDen.setText("—");
            edtMoTa.setText("");
            edtTrangThai.setText("Trống");
            if (edtSoNguoiToiDa != null) {
                edtSoNguoiToiDa.setText("2");
            }
            btnUpdate.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            if (btnQuanLyNgayLe != null) {
                btnQuanLyNgayLe.setVisibility(View.GONE);
            }
        } else {
            btnUpdate.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            if (btnQuanLyNgayLe != null) {
                btnQuanLyNgayLe.setVisibility(View.VISIBLE);
            }
            pendingAdminPaths = new ArrayList<>(dao.getAnhUrlsByPhongId(p.getPhongID()));
            if (pendingAdminPaths.isEmpty() && p.getUrlAnh() != null && !p.getUrlAnh().trim().isEmpty()) {
                pendingAdminPaths.add(p.getUrlAnh().trim());
            }
            refreshAdminPreview();

            edtTen.setText(p.getTenPhong());
            edtGia.setText(String.valueOf(p.getGiaNgay()));
            if (p.getGiaCaoDiem() > 0) {
                edtGiaCaoDiem.setText(String.valueOf((long) p.getGiaCaoDiem()));
            } else {
                edtGiaCaoDiem.setText("");
            }
            edtHeSoCaoDiem.setText(String.format(Locale.getDefault(), "%.2f", p.getHeSoCaoDiem()));
            String tu = p.getGioCaoDiemTu() != null ? p.getGioCaoDiemTu() : "";
            String den = p.getGioCaoDiemDen() != null ? p.getGioCaoDiemDen() : "";
            txtGioCaoDiemTu.setText(tu.isEmpty() ? "—" : tu);
            txtGioCaoDiemDen.setText(den.isEmpty() ? "—" : den);
            edtMoTa.setText(p.getMoTa());
            edtTrangThai.setText(p.getTrangThai());
            if (edtSoNguoiToiDa != null) {
                edtSoNguoiToiDa.setText(String.valueOf(Math.max(1, p.getSoNguoiToiDa())));
            }
        }
        if (context instanceof FragmentActivity) {
            FragmentActivity fact = (FragmentActivity) context;
            txtGioCaoDiemTu.setOnClickListener(v -> {
                int[] cur = TimePickerHelper.parseHHmm("—".equals(txtGioCaoDiemTu.getText().toString()) ? "" : txtGioCaoDiemTu.getText().toString());
                TimePickerHelper.showPick24h(fact, context.getString(R.string.phong_peak_from),
                        cur[0], cur[1], (h, m) -> {
                            String s = TimePickerHelper.formatHHmm(h, m);
                            txtGioCaoDiemTu.setText(s);
                        });
            });
            txtGioCaoDiemDen.setOnClickListener(v -> {
                int[] cur = TimePickerHelper.parseHHmm("—".equals(txtGioCaoDiemDen.getText().toString()) ? "" : txtGioCaoDiemDen.getText().toString());
                TimePickerHelper.showPick24h(fact, context.getString(R.string.phong_peak_to),
                        cur[0], cur[1], (h, m) -> {
                            String s = TimePickerHelper.formatHHmm(h, m);
                            txtGioCaoDiemDen.setText(s);
                        });
            });
        }
        edtAnh.setText("");

        if (btnChonAnh != null) {
            btnChonAnh.setOnClickListener(v -> {
                if (pickRoomImagesLauncher != null) {
                    pickRoomImagesLauncher.launch(new PickVisualMediaRequest.Builder().build());
                } else {
                    Toast.makeText(context, R.string.phong_pick_images, Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (btnXoaHetAnh != null) {
            btnXoaHetAnh.setOnClickListener(v -> {
                if (pendingAdminPaths != null) {
                    pendingAdminPaths.clear();
                    refreshAdminPreview();
                }
            });
        }
        if (btnQuanLyNgayLe != null && p != null) {
            btnQuanLyNgayLe.setOnClickListener(v -> openHolidayManagerDialog(p));
        }

        dialog.setOnDismissListener(d -> {
            pendingAdminPreview = null;
            pendingAdminPaths = null;
        });

        final PhongFull phongSua = p;
        View.OnClickListener getDataAndSave = v -> {

            String tenPhong = edtTen.getText().toString().trim();
            if (tenPhong.isEmpty()) {
                Toast.makeText(context, R.string.phong_err_need_ten, Toast.LENGTH_SHORT).show();
                return;
            }
            String giaStr = edtGia.getText().toString().trim().replace(",", ".");
            if (giaStr.isEmpty()) {
                Toast.makeText(context, R.string.phong_err_need_gia, Toast.LENGTH_SHORT).show();
                return;
            }
            double giaNgay;
            try {
                giaNgay = Double.parseDouble(giaStr);
            } catch (NumberFormatException e) {
                Toast.makeText(context, R.string.phong_err_gia_sai, Toast.LENGTH_SHORT).show();
                return;
            }

            double giaCd = 0;
            try {
                String gs = edtGiaCaoDiem.getText().toString().trim().replace(",", ".");
                if (!gs.isEmpty()) {
                    giaCd = Double.parseDouble(gs);
                }
            } catch (NumberFormatException ignored) {
                giaCd = 0;
            }
            double heSoCd = 1.0;
            try {
                String hs = edtHeSoCaoDiem.getText().toString().trim().replace(",", ".");
                if (!hs.isEmpty()) {
                    heSoCd = Double.parseDouble(hs);
                }
            } catch (NumberFormatException ignored) {
                heSoCd = 1.0;
            }
            if (heSoCd < 1.0) heSoCd = 1.0;
            if (heSoCd > 10.0) heSoCd = 10.0;
            String gTu = txtGioCaoDiemTu.getText().toString().trim();
            String gDen = txtGioCaoDiemDen.getText().toString().trim();
            if ("—".equals(gTu)) {
                gTu = "";
            }
            if ("—".equals(gDen)) {
                gDen = "";
            }
            if (giaCd <= 0 && heSoCd <= 1.0) {
                gTu = "";
                gDen = "";
            }

            int soNguoiToiDa = 2;
            if (edtSoNguoiToiDa != null) {
                try {
                    String sn = edtSoNguoiToiDa.getText().toString().trim();
                    if (!sn.isEmpty()) {
                        soNguoiToiDa = Integer.parseInt(sn);
                    }
                } catch (NumberFormatException ignored) {
                    soNguoiToiDa = 2;
                }
                if (soNguoiToiDa < 1) {
                    soNguoiToiDa = 1;
                } else if (soNguoiToiDa > 99) {
                    soNguoiToiDa = 99;
                }
            }

            java.util.List<String> anhLuu = new ArrayList<>();
            if (pendingAdminPaths != null) {
                anhLuu.addAll(pendingAdminPaths);
            }
            String drawableName = edtAnh.getText().toString().trim();
            if (!drawableName.isEmpty() && !anhLuu.contains(drawableName)) {
                anhLuu.add(drawableName);
            }

            if (v.getId() == R.id.btnUpdate) {
                if (isNewRoom || phongSua == null) {
                    return;
                }
                dao.updatePhong(
                        phongSua.getPhongID(),
                        tenPhong,
                        giaNgay,
                        edtMoTa.getText().toString(),
                        edtTrangThai.getText().toString(),
                        soNguoiToiDa,
                        anhLuu,
                        giaCd,
                        gTu,
                        gDen,
                        heSoCd
                );

                Toast.makeText(context, "Đã cập nhật", Toast.LENGTH_SHORT).show();

            } else if (v.getId() == R.id.btnAdd) {

                dao.insertPhong(
                        tenPhong,
                        giaNgay,
                        edtMoTa.getText().toString(),
                        edtTrangThai.getText().toString(),
                        soNguoiToiDa,
                        anhLuu,
                        giaCd,
                        gTu,
                        gDen,
                        heSoCd
                );

                Toast.makeText(context, "Đã thêm", Toast.LENGTH_SHORT).show();
            }

            reload();
            dialog.dismiss();
        };

        btnUpdate.setOnClickListener(getDataAndSave);
        btnAdd.setOnClickListener(getDataAndSave);

        btnDelete.setOnClickListener(v -> {
            if (phongSua == null) {
                return;
            }
            dao.deletePhong(phongSua.getPhongID());
            reload();
            Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }

    private void refreshAdminPreview() {
        if (pendingAdminPreview == null || pendingAdminPaths == null) {
            return;
        }
        pendingAdminPreview.removeAllViews();
        LayoutInflater inf = LayoutInflater.from(context);
        for (int i = 0; i < pendingAdminPaths.size(); i++) {
            String path = pendingAdminPaths.get(i);
            View cell = inf.inflate(R.layout.item_admin_room_image_preview, pendingAdminPreview, false);
            ImageView iv = cell.findViewById(R.id.imgAdminRoomPreview);
            TextView btnRemove = cell.findViewById(R.id.btnRemoveAdminPreview);
            RoomImageUtils.loadInto(iv, path);
            final int idx = i;
            btnRemove.setOnClickListener(v -> {
                if (pendingAdminPaths != null && idx >= 0 && idx < pendingAdminPaths.size()) {
                    pendingAdminPaths.remove(idx);
                    refreshAdminPreview();
                }
            });
            pendingAdminPreview.addView(cell);
        }
    }

    private void reload() {
        list.clear();
        list.addAll(dao.getAllPhongFull());
        notifyDataSetChanged();
    }

    // ============== QUẢN LÝ NGÀY LỄ ==============

    /**
     * Mở dialog quản lý ngày lễ (chỉ admin): liệt kê, thêm, xoá các ngày lễ áp giá riêng cho 1 phòng.
     */
    private void openHolidayManagerDialog(PhongFull phong) {
        if (phong == null) return;
        PhongGiaLeDAO leDao = new PhongGiaLeDAO(context);
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_phong_holiday, null, false);
        RecyclerView recycler = v.findViewById(R.id.recyclerHoliday);
        TextView txtEmpty = v.findViewById(R.id.txtHolidayEmpty);
        com.google.android.material.textfield.TextInputEditText edtNgay = v.findViewById(R.id.edtHolidayNgay);
        com.google.android.material.textfield.TextInputEditText edtHeSo = v.findViewById(R.id.edtHolidayHeSo);
        com.google.android.material.textfield.TextInputEditText edtNote = v.findViewById(R.id.edtHolidayNote);
        View btnAdd = v.findViewById(R.id.btnHolidayAdd);
        View btnClose = v.findViewById(R.id.btnHolidayClose);

        java.util.List<PhongGiaLe> data = new ArrayList<>(leDao.getByPhongId(phong.getPhongID()));
        HolidayAdapter adapter = new HolidayAdapter(data);
        adapter.setOnDelete(item -> {
            leDao.deleteById(item.getId());
            data.remove(item);
            adapter.notifyDataSetChanged();
            if (data.isEmpty() && txtEmpty != null) txtEmpty.setVisibility(View.VISIBLE);
        });
        recycler.setLayoutManager(new LinearLayoutManager(context));
        recycler.setAdapter(adapter);
        if (txtEmpty != null) txtEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);

        edtNgay.setOnClickListener(x -> {
            if (!(context instanceof FragmentActivity)) return;
            java.util.Calendar cal = java.util.Calendar.getInstance();
            com.example.doan.util.DatePickerHelper.showPickDateUnbounded(
                    (FragmentActivity) context,
                    context.getString(R.string.phong_holiday_pick_date_title),
                    null,
                    cal.getTimeInMillis(),
                    sel -> {
                        edtNgay.setText(DatePickerHelper.formatYmdLocal(sel));
                    });
        });
        edtNgay.setFocusable(false);

        btnAdd.setOnClickListener(x -> {
            String ngay = edtNgay.getText() != null ? edtNgay.getText().toString().trim() : "";
            if (ngay.isEmpty()) {
                Toast.makeText(context, R.string.phong_holiday_err_need_date, Toast.LENGTH_SHORT).show();
                return;
            }
            double heSo = 1.5;
            try {
                String hs = edtHeSo.getText() != null ? edtHeSo.getText().toString().trim().replace(",", ".") : "";
                if (!hs.isEmpty()) heSo = Double.parseDouble(hs);
            } catch (NumberFormatException ignored) {
                heSo = 1.5;
            }
            if (heSo < 1.0) heSo = 1.0;
            if (heSo > 10.0) heSo = 10.0;
            String note = edtNote.getText() != null ? edtNote.getText().toString().trim() : "";
            PhongGiaLe g = new PhongGiaLe(phong.getPhongID(), ngay, heSo, note);
            long id = leDao.upsert(g);
            if (id == -1) {
                Toast.makeText(context, R.string.phong_holiday_err_save, Toast.LENGTH_SHORT).show();
                return;
            }
            g.setId(id);
            // Cập nhật list: thay thế nếu đã có cùng ngày.
            int found = -1;
            for (int i = 0; i < data.size(); i++) {
                if (ngay.equals(data.get(i).getNgayLe())) { found = i; break; }
            }
            if (found >= 0) {
                data.set(found, g);
            } else {
                java.util.Collections.sort(data, (a, b) -> {
                    String aa = a.getNgayLe() != null ? a.getNgayLe() : "";
                    String bb = b.getNgayLe() != null ? b.getNgayLe() : "";
                    return aa.compareTo(bb);
                });
                data.add(g);
                java.util.Collections.sort(data, (a, b) -> {
                    String aa = a.getNgayLe() != null ? a.getNgayLe() : "";
                    String bb = b.getNgayLe() != null ? b.getNgayLe() : "";
                    return aa.compareTo(bb);
                });
            }
            adapter.notifyDataSetChanged();
            if (txtEmpty != null) txtEmpty.setVisibility(View.GONE);
            edtNgay.setText("");
            edtNote.setText("");
            Toast.makeText(context, R.string.phong_holiday_added, Toast.LENGTH_SHORT).show();
        });

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.phong_holiday_dialog_title, phong.getTenPhong()))
                .setView(v)
                .create();
        btnClose.setOnClickListener(x -> dialog.dismiss());
        dialog.show();
    }

    /** Adapter nhỏ cho danh sách ngày lễ trong dialog. */
    private static class HolidayAdapter extends RecyclerView.Adapter<HolidayAdapter.VH> {
        interface OnDelete { void onDelete(PhongGiaLe item); }
        private final java.util.List<PhongGiaLe> data;
        private OnDelete onDelete;
        HolidayAdapter(java.util.List<PhongGiaLe> data) {
            this.data = data;
        }
        void setOnDelete(OnDelete onDelete) {
            this.onDelete = onDelete;
        }
        @Override
        public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_phong_holiday, parent, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(VH h, int position) {
            PhongGiaLe g = data.get(position);
            String ngay = g.getNgayLe() != null ? g.getNgayLe() : "—";
            // Hiển thị ngày dạng dd/MM/yyyy cho dễ đọc.
            String hien = ngay;
            try {
                SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                SimpleDateFormat out = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date d = in.parse(ngay);
                if (d != null) hien = out.format(d);
            } catch (Exception ignored) {}
            h.txtNgay.setText(hien);
            h.txtHeSo.setText(String.format(Locale.getDefault(), "Hệ số: x%.2f", g.getHeSoNhan()));
            String note = g.getGhiChu() != null && !g.getGhiChu().isEmpty() ? g.getGhiChu() : "—";
            h.txtNote.setText("Ghi chú: " + note);
            h.btnDelete.setOnClickListener(x -> {
                if (onDelete != null) onDelete.onDelete(g);
            });
        }
        @Override
        public int getItemCount() { return data.size(); }
        static class VH extends RecyclerView.ViewHolder {
            final TextView txtNgay, txtHeSo, txtNote;
            final com.google.android.material.button.MaterialButton btnDelete;
            VH(View itemView) {
                super(itemView);
                txtNgay = itemView.findViewById(R.id.txtHolidayItemNgay);
                txtHeSo = itemView.findViewById(R.id.txtHolidayItemHeSo);
                txtNote = itemView.findViewById(R.id.txtHolidayItemNote);
                btnDelete = itemView.findViewById(R.id.btnHolidayItemDelete);
            }
        }
    }

    /** Hàng dịch vụ trong dialog đặt phòng — static để các helper cùng file tham chiếu được. */
    static class DichVuRow {
        final View root;
        final CheckBox cb;
        final View qtyWrap;
        final TextView txtQty;
        final View btnMinus;
        final View btnPlus;
        int qty = 0;

        DichVuRow(View root) {
            this.root = root;
            this.cb = root.findViewById(R.id.cbDichVuBooking);
            this.qtyWrap = root.findViewById(R.id.layoutSoLuongDichVu);
            this.txtQty = root.findViewById(R.id.txtSoLuongDichVu);
            this.btnMinus = root.findViewById(R.id.btnGiamSoLuongDichVu);
            this.btnPlus = root.findViewById(R.id.btnTangSoLuongDichVu);
        }

        void setQty(int newQty) {
            qty = Math.max(0, newQty);
            if (txtQty != null) {
                txtQty.setText(String.valueOf(Math.max(1, qty)));
            }
        }
    }
}

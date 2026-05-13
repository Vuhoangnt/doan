package com.example.doan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.doan.DAO.DatPhongDAO;
import com.example.doan.DAO.PhongDAO;
import com.example.doan.R;
import com.example.doan.model.DatPhong;
import com.example.doan.model.PhongFull;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatPhongListAdapter extends BaseAdapter {

    public interface GuestCheckoutListener {
        void onGuestTraPhong(DatPhong d);
    }

    public interface GuestDepositListener {
        void onGuestGuiCoc(DatPhong d);
    }

    public interface GuestCancelListener {
        void onGuestHuyDon(DatPhong d);
    }

    private final Context context;
    private final List<DatPhong> list;
    /** Đã thu theo DatPhongID (admin / lễ tân); null = không hiển thị. */
    private Map<Integer, Double> daThuTheoDon;
    private boolean guestOrdersMode;
    @Nullable
    private GuestCheckoutListener guestCheckoutListener;
    @Nullable
    private GuestDepositListener guestDepositListener;
    @Nullable
    private GuestCancelListener guestCancelListener;
    private int guestTaiKhoanId;

    /** Cache ảnh theo PhongID để không query DB lặp lại mỗi lần scroll. */
    private final Map<Integer, String> anhPhongCache = new HashMap<>();

    public DatPhongListAdapter(Context context, List<DatPhong> list) {
        this(context, list, null);
    }

    public DatPhongListAdapter(Context context, List<DatPhong> list, Map<Integer, Double> daThuTheoDon) {
        this.context = context;
        this.list = list;
        this.daThuTheoDon = daThuTheoDon != null ? daThuTheoDon : new HashMap<>();
    }

    public void setDaThuMap(Map<Integer, Double> map) {
        this.daThuTheoDon = map != null ? map : new HashMap<>();
    }

    public void setGuestOrdersMode(boolean on, @Nullable GuestCheckoutListener listener) {
        guestOrdersMode = on;
        guestCheckoutListener = listener;
    }

    public void setGuestDepositListener(@Nullable GuestDepositListener listener) {
        guestDepositListener = listener;
    }

    public void setGuestCancelListener(int taiKhoanKhachId, @Nullable GuestCancelListener listener) {
        this.guestTaiKhoanId = taiKhoanKhachId;
        this.guestCancelListener = listener;
    }

    public void updateData(List<DatPhong> newList) {
        list.clear();
        list.addAll(newList);
        anhPhongCache.clear(); // xóa cache khi dữ liệu thay đổi
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // -----------------------------------------------------------------------
    // Lấy đường dẫn ảnh đại diện của phòng (có cache, tránh query DB liên tục)
    // -----------------------------------------------------------------------
    @Nullable
    private String getAnhPhong(int phongId) {
        if (anhPhongCache.containsKey(phongId)) {
            return anhPhongCache.get(phongId);
        }
        PhongFull p = new PhongDAO(context).getPhongFullById(phongId);
        String anh = null;
        if (p != null) {
            // Ưu tiên danh sách ảnh; fallback về urlAnh nếu list rỗng
            List<String> refs = p.resolveAllImageRefs();
            if (!refs.isEmpty()) {
                anh = refs.get(0);
            }
        }
        anhPhongCache.put(phongId, anh);
        return anh;
    }

    // -----------------------------------------------------------------------

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_datphong, parent, false);
        }
        DatPhong d = list.get(position);

        ImageView imgPhong    = convertView.findViewById(R.id.imgPhong);
        TextView txtMa        = convertView.findViewById(R.id.txtMaDat);
        TextView txtTenPhong  = convertView.findViewById(R.id.txtTenPhong);
        TextView txtKhach     = convertView.findViewById(R.id.txtKhach);
        TextView txtNgay      = convertView.findViewById(R.id.txtNgay);
        TextView txtTrangThai = convertView.findViewById(R.id.txtTrangThai);
        TextView txtThuHo     = convertView.findViewById(R.id.txtThuHo);
        TextView txtTongTien  = convertView.findViewById(R.id.txtTongTien);
        TextView txtNv        = convertView.findViewById(R.id.txtNhanVienXuLy);
        MaterialButton btnCoc = convertView.findViewById(R.id.btnGuestGuiCoc);
        MaterialButton btnHuy = convertView.findViewById(R.id.btnGuestHuyDon);
        MaterialButton btnTra = convertView.findViewById(R.id.btnGuestTraPhong);

        // ── Ảnh phòng (lưu dạng tên drawable, ví dụ: "don", "don2") ─────────
        if (imgPhong != null) {
            String tenDrawable = getAnhPhong(d.getPhongID());
            int resId = 0;
            if (tenDrawable != null && !tenDrawable.isEmpty()) {
                resId = context.getResources().getIdentifier(
                        tenDrawable, "drawable", context.getPackageName());
            }
            imgPhong.setImageResource(resId != 0
                    ? resId
                    : R.drawable.ic_room_placeholder);
        }

        // ── Thông tin đơn ───────────────────────────────────────────────────
        txtMa.setText(d.getMaDatPhong());

        String tenP = d.getTenPhong();
        txtTenPhong.setText(tenP != null && !tenP.isEmpty()
                ? ("Phòng: " + tenP)
                : ("Phòng ID: " + d.getPhongID()));

        txtKhach.setText(d.getKhachTen() + " • " + d.getKhachDienThoai());

        String gn = d.getGioNhan();
        String gt = d.getGioTra();
        String gio = "";
        if (gn != null && !gn.isEmpty() && gt != null && !gt.isEmpty()) {
            gio = " · " + gn + " → " + gt;
        }
        txtNgay.setText(d.getNgayNhan() + " → " + d.getNgayTra()
                + " (" + d.getSoDem() + " đêm)" + gio);

        // Badge trạng thái (không có prefix "Trạng thái:" vì hiển thị dạng chip ngắn)
        txtTrangThai.setText(DatPhongDAO.normalizeStatus(d.getTrangThai()));

        String nv = d.getTenNhanVienXuLy();
        if (nv != null && !nv.isEmpty()) {
            txtNv.setVisibility(View.VISIBLE);
            txtNv.setText("Xác nhận bởi: " + nv);
        } else {
            txtNv.setVisibility(View.GONE);
        }

        // Tổng tiền (không có prefix "Tổng đơn:" vì layout đã có nhãn tĩnh bên cạnh)
        txtTongTien.setText(String.format(Locale.getDefault(),
                "%,.0f đ", d.getTongTien()));

        Double daThu = daThuTheoDon.get(d.getDatPhongID());
        if (daThu != null) {
            txtThuHo.setVisibility(View.VISIBLE);
            double thucThu = daThu;
            double cocMin = d.getTongTien() * 0.2;
            boolean daCoc = thucThu + 1.0 >= cocMin;
            String head = daCoc ? "Đã thu cọc: " : "Đã thu: ";
            txtThuHo.setText(String.format(Locale.getDefault(),
                    head + "%,.0f đ — Còn: %,.0f đ",
                    thucThu, Math.max(0, d.getTongTien() - thucThu)));
        } else {
            txtThuHo.setVisibility(View.GONE);
        }

        // ── Nút hành động ───────────────────────────────────────────────────
        if (btnTra != null) {
            boolean dangO = DatPhongDAO.TT_DANG_O.equals(
                    DatPhongDAO.normalizeStatus(d.getTrangThai()));
            if (guestOrdersMode && guestCheckoutListener != null && dangO) {
                btnTra.setVisibility(View.VISIBLE);
                btnTra.setOnClickListener(v -> guestCheckoutListener.onGuestTraPhong(d));
            } else {
                btnTra.setVisibility(View.GONE);
            }
        }

        if (btnCoc != null) {
            String st = DatPhongDAO.normalizeStatus(d.getTrangThai());
            double thu = daThuTheoDon.getOrDefault(d.getDatPhongID(), 0d);
            double cocMin = d.getTongTien() * 0.2;
            boolean canGuiCoc = DatPhongDAO.TT_CHO_XAC_NHAN.equals(st)
                    && (thu + 1.0 < cocMin);
            if (guestOrdersMode && guestDepositListener != null && canGuiCoc) {
                btnCoc.setVisibility(View.VISIBLE);
                btnCoc.setOnClickListener(v -> guestDepositListener.onGuestGuiCoc(d));
            } else {
                btnCoc.setVisibility(View.GONE);
            }
        }

        if (btnHuy != null) {
            String st = DatPhongDAO.normalizeStatus(d.getTrangThai());
            boolean canHuy = guestOrdersMode
                    && guestCancelListener != null
                    && guestTaiKhoanId > 0
                    && d.getTaiKhoanID() != null
                    && d.getTaiKhoanID() == guestTaiKhoanId
                    && (DatPhongDAO.TT_CHO_XAC_NHAN.equals(st)
                    || DatPhongDAO.TT_DA_XAC_NHAN.equals(st));
            if (canHuy) {
                btnHuy.setVisibility(View.VISIBLE);
                btnHuy.setOnClickListener(v -> guestCancelListener.onGuestHuyDon(d));
            } else {
                btnHuy.setVisibility(View.GONE);
            }
        }

        return convertView;
    }
}
package com.example.doan.UI.Fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import com.example.doan.DAO.DanhGiaDAO;
import com.example.doan.DAO.HomestayThongTinDAO;
import com.example.doan.DAO.PhongDAO;
import com.example.doan.DAO.TinTucDAO;
import com.example.doan.DataRefreshable;
import com.example.doan.MainActivity;
import com.example.doan.R;
import com.example.doan.SessionManager;
import com.example.doan.model.DanhGia;
import com.example.doan.model.HomestayThongTin;
import com.example.doan.model.TinTuc;
import com.example.doan.util.MapOpenHelper;
import com.example.doan.util.RoomImageUtils;

import java.util.List;

public class Home extends Fragment implements DataRefreshable {

    /** Tránh lặp post vô hạn khi đo chiều cao banner. */
    private int heroBannerLayoutAttempts;

    private ImageButton btnScrollDown;
    private ScrollView scrollView;
    private TextView txtGreeting;
    private ImageView imgHeroBg;
    private TextView txtHeroTitle;
    private TextView txtHeroTagline;
    private TextView txtLocationAddress;
    private LinearLayout sectionQuick;
    private LinearLayout sectionTin;
    private LinearLayout sectionDg;
    private LinearLayout sectionServices;
    private LinearLayout sectionViTri;
    private LinearLayout tinContainer;
    private LinearLayout dgContainer;
    private View cardHomeMap;

    private HomestayThongTinDAO homestayDao;
    private TinTucDAO tinDao;
    private DanhGiaDAO danhGiaDao;
    private PhongDAO phongDao;

    public Home() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        btnScrollDown = view.findViewById(R.id.btnScrollDown);
        scrollView = view.findViewById(R.id.scrollViewHome);
        txtGreeting = view.findViewById(R.id.txtHomeGreeting);
        imgHeroBg = view.findViewById(R.id.imgHomeHeroBg);
        txtHeroTitle = view.findViewById(R.id.txtHomeHeroTitle);
        txtHeroTagline = view.findViewById(R.id.txtHomeHeroTagline);
        txtLocationAddress = view.findViewById(R.id.txtHomeLocationAddress);
        sectionQuick = view.findViewById(R.id.sectionHomeQuick);
        sectionTin = view.findViewById(R.id.sectionHomeTin);
        sectionDg = view.findViewById(R.id.sectionHomeDanhGia);
        sectionServices = view.findViewById(R.id.sectionHomeServices);
        sectionViTri = view.findViewById(R.id.sectionHomeViTri);
        tinContainer = view.findViewById(R.id.homeTinTucContainer);
        dgContainer = view.findViewById(R.id.homeDanhGiaContainer);
        cardHomeMap = view.findViewById(R.id.cardHomeMap);

        homestayDao = new HomestayThongTinDAO(requireContext());
        tinDao = new TinTucDAO(requireContext());
        danhGiaDao = new DanhGiaDAO(requireContext());
        phongDao = new PhongDAO(requireContext());

        View btnDat = view.findViewById(R.id.btnHomeDatPhong);
        View btnTin = view.findViewById(R.id.btnHomeTinTuc);
        View btnDg = view.findViewById(R.id.btnHomeDanhGia);
        View btnHs = view.findViewById(R.id.btnHomeHomestay);
        View btnXemTin = view.findViewById(R.id.btnHomeXemThemTin);
        View btnXemDg = view.findViewById(R.id.btnHomeXemThemDanhGia);
        if (getActivity() instanceof MainActivity) {
            MainActivity act = (MainActivity) getActivity();
            btnDat.setOnClickListener(v -> act.openFromHome(R.id.nav_phong));
            btnTin.setOnClickListener(v -> act.openFromHome(R.id.nav_tin_tuc));
            btnDg.setOnClickListener(v -> act.openFromHome(R.id.nav_danh_gia));
            btnHs.setOnClickListener(v -> act.openFromHome(R.id.nav_homestay));
            btnXemTin.setOnClickListener(v -> act.openFromHome(R.id.nav_tin_tuc));
            btnXemDg.setOnClickListener(v -> act.openFromHome(R.id.nav_danh_gia));
        }

        btnScrollDown.setOnClickListener(v -> {
            if (scrollView != null) {
                scrollView.post(() ->
                        scrollView.smoothScrollBy(0, scrollView.getHeight()));
            }
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
        if (txtGreeting == null || getContext() == null) {
            return;
        }
        SessionManager s = new SessionManager(requireContext());
        if (!s.isLoggedIn()) {
            txtGreeting.setText("Xin chào! Bạn có thể xem phòng và đặt phòng không cần tài khoản.");
        } else {
            String role = SessionManager.roleLabelVi(s.getRole());
            txtGreeting.setText("Xin chào " + (s.getDisplayName().isEmpty() ? s.getUsername() : s.getDisplayName())
                    + " — " + role);
        }

        HomestayThongTin h = homestayDao.getHomestay();
        applyHero(h);
        applySectionsVisibility(h);
        txtLocationAddress.setText(h.getDiaChi() != null ? h.getDiaChi() : "");

        if (cardHomeMap != null) {
            cardHomeMap.setOnClickListener(v -> MapOpenHelper.openHomestayLocation(requireContext(), h));
        }

        tinContainer.removeAllViews();
        dgContainer.removeAllViews();

        if (h.getTrangChuHienTin() != 0) {
            int nTin = h.getTrangChuSoTin();
            List<TinTuc> tins = tinDao.getLatest(nTin);
            LayoutInflater inf = LayoutInflater.from(requireContext());
            for (TinTuc t : tins) {
                View row = inf.inflate(R.layout.item_home_tin_row, tinContainer, false);
                TextView t1 = row.findViewById(R.id.txtHomeTinTitle);
                TextView t2 = row.findViewById(R.id.txtHomeTinDate);
                TextView t3 = row.findViewById(R.id.txtHomeTinExcerpt);
                t1.setText(t.getTieuDe() != null ? t.getTieuDe() : "");
                t2.setText(t.getNgayDang() != null ? t.getNgayDang() : "");
                t3.setText(excerpt(t.getNoiDung(), 220));
                tinContainer.addView(row);
            }
        }

        if (h.getTrangChuHienDanhGia() != 0) {
            int nDg = h.getTrangChuSoDanhGia();
            List<DanhGia> dgs = danhGiaDao.getApprovedNewestFirstLimit(nDg);
            LayoutInflater inf = LayoutInflater.from(requireContext());
            for (DanhGia d : dgs) {
                View row = inf.inflate(R.layout.item_home_dg_row, dgContainer, false);
                TextView ten = row.findViewById(R.id.txtHomeDgTen);
                TextView sao = row.findViewById(R.id.txtHomeDgSao);
                TextView np = row.findViewById(R.id.txtHomeDgNgayPhong);
                TextView nd = row.findViewById(R.id.txtHomeDgNoiDung);
                ten.setText(d.getTenHienThi());
                sao.setText(stars(d.getSoSao()));
                String phongTen = "";
                if (d.getPhongID() != null) {
                    String t = phongDao.getTenPhongById(d.getPhongID());
                    phongTen = t != null ? t : "";
                }
                String line = d.getNgayTao() != null ? d.getNgayTao() : "";
                if (!phongTen.isEmpty()) {
                    line = line.isEmpty() ? phongTen : line + " · " + phongTen;
                }
                np.setText(line);
                nd.setText(excerpt(d.getNoiDung(), 200));
                dgContainer.addView(row);
            }
        }
    }

    private void applyHero(HomestayThongTin h) {
        SessionManager s = new SessionManager(requireContext());
        boolean useNvHero = s.isLoggedIn() && s.isNhanVien();

        String title;
        String sub;
        if (useNvHero) {
            String tNv = h.getTrangChuTieuDeNv();
            title = (tNv != null && !tNv.trim().isEmpty()) ? tNv.trim() : null;
            if (title == null) {
                String t = h.getTrangChuTieuDe();
                title = (t != null && !t.trim().isEmpty()) ? t.trim() : null;
            }
            String sNv = h.getTrangChuCamKetNv();
            sub = (sNv != null && !sNv.trim().isEmpty()) ? sNv.trim() : null;
            if (sub == null) {
                String c = h.getTrangChuCamKet();
                sub = (c != null && !c.trim().isEmpty()) ? c.trim() : null;
            }
        } else {
            String t = h.getTrangChuTieuDe();
            title = (t != null && !t.trim().isEmpty()) ? t.trim() : null;
            String c = h.getTrangChuCamKet();
            sub = (c != null && !c.trim().isEmpty()) ? c.trim() : null;
        }

        if (title != null) {
            txtHeroTitle.setText(title);
        } else {
            txtHeroTitle.setText(R.string.home_hero_title);
        }
        if (sub != null) {
            txtHeroTagline.setText(sub);
        } else {
            txtHeroTagline.setText(R.string.home_hero_tagline);
        }

        String ref;
        if (useNvHero) {
            String rNv = h.getTrangChuAnhNenNv();
            ref = (rNv != null && !rNv.trim().isEmpty()) ? rNv.trim() : null;
            if (ref == null) {
                String r = h.getTrangChuAnhNen();
                ref = (r != null && !r.trim().isEmpty()) ? r.trim() : null;
            }
        } else {
            String r = h.getTrangChuAnhNen();
            ref = (r != null && !r.trim().isEmpty()) ? r.trim() : null;
        }

        if (ref == null || ref.isEmpty()) {
            imgHeroBg.setImageDrawable(null);
            imgHeroBg.setImageResource(R.drawable.bg_guest_hero);
            scheduleHeroPalette("bg_guest_hero");
            return;
        }
        scheduleHeroPalette(ref);
        applyHeroBannerImage(ref);
    }

    /**
     * FrameLayout hero (wrap_content) + ImageView match_parent đôi khi đo height = 0 → ảnh không hiện.
     * Đợi layout xong, gán height ImageView = chiều cao khung rồi mới load ảnh.
     */
    private void applyHeroBannerImage(String ref) {
        imgHeroBg.setImageDrawable(null);
        View heroRoot = getView() != null ? getView().findViewById(R.id.homeHeroRoot) : null;
        if (heroRoot == null) {
            RoomImageUtils.loadInto(imgHeroBg, ref);
            return;
        }
        heroBannerLayoutAttempts = 0;
        Runnable attempt = new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) {
                    return;
                }
                int h = heroRoot.getHeight();
                heroBannerLayoutAttempts++;
                if (h <= 0 && heroBannerLayoutAttempts < 30) {
                    heroRoot.post(this);
                    return;
                }
                if (h > 0 && imgHeroBg.getLayoutParams() != null) {
                    ViewGroup.LayoutParams lp = imgHeroBg.getLayoutParams();
                    lp.height = h;
                    imgHeroBg.setLayoutParams(lp);
                }
                RoomImageUtils.loadInto(imgHeroBg, ref);
            }
        };
        heroRoot.post(attempt);
    }

    private void scheduleHeroPalette(String ref) {
        if (!(getActivity() instanceof MainActivity)) {
            return;
        }
        final MainActivity act = (MainActivity) getActivity();
        final android.content.Context appCtx = requireContext().getApplicationContext();
        final String r = ref;
        new Thread(() -> {
            Bitmap bmp = RoomImageUtils.decodeBitmapForPalette(appCtx, r, 256);
            int fallback = ContextCompat.getColor(appCtx, R.color.primary);
            int seed = fallback;
            if (bmp != null) {
                Palette p = Palette.from(bmp).clearFilters().maximumColorCount(20).generate();
                bmp.recycle();
                int d = p.getDarkVibrantColor(0);
                int dv = p.getVibrantColor(0);
                int m = p.getMutedColor(0);
                int dm = p.getDarkMutedColor(0);
                if (dm != 0) {
                    seed = dm;
                } else if (d != 0) {
                    seed = d;
                } else if (m != 0) {
                    seed = m;
                } else if (dv != 0) {
                    seed = dv;
                }
            }
            int finalSeed = seed;
            if (act.isFinishing()) {
                return;
            }
            act.runOnUiThread(() -> {
                if (isAdded() && act.findViewById(R.id.fragmentContainer) != null) {
                    act.applyHomeGuestChromeFromHeroSeed(finalSeed);
                }
            });
        }).start();
    }

    private void applySectionsVisibility(HomestayThongTin h) {
        sectionQuick.setVisibility(h.getTrangChuHienNhanh() != 0 ? View.VISIBLE : View.GONE);
        sectionTin.setVisibility(h.getTrangChuHienTin() != 0 ? View.VISIBLE : View.GONE);
        sectionDg.setVisibility(h.getTrangChuHienDanhGia() != 0 ? View.VISIBLE : View.GONE);
        sectionServices.setVisibility(h.getTrangChuHienDichVu() != 0 ? View.VISIBLE : View.GONE);
        sectionViTri.setVisibility(h.getTrangChuHienViTri() != 0 ? View.VISIBLE : View.GONE);
    }

    private static String excerpt(String s, int maxLen) {
        if (s == null) {
            return "";
        }
        String t = s.trim().replace('\n', ' ');
        if (t.length() <= maxLen) {
            return t;
        }
        return t.substring(0, maxLen).trim() + "…";
    }

    private static String stars(int n) {
        if (n < 1) {
            n = 1;
        }
        if (n > 5) {
            n = 5;
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < n; i++) {
            b.append('★');
        }
        return b.toString();
    }
}

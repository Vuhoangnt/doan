package com.example.doan;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.doan.DAO.TaiKhoanDAO;
import com.example.doan.DAO.ThongBaoDAO;
import com.example.doan.model.TaiKhoan;
import com.example.doan.util.RoomImageUtils;
import com.example.doan.UI.Fragment.Home;
import com.example.doan.UI.Fragment.Phong;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNav;
    private SessionManager session;
    private String lastNavKey;
    /** Badge số thông báo chưa đọc trên icon toolbar. */
    private BadgeDrawable thongBaoBadge;
    /** Từ khóa tìm phòng (SearchView toolbar) — áp dụng khi đang ở màn Phòng. */
    private String guestPhongSearchQuery = "";
    /** Tránh listener bottom nav gọi handleNavItem khi đang setSelectedItemId sau điều hướng có Bundle (thông báo). */
    private boolean suppressBottomNavReopen;

    private int defaultToolbarColor;
    private int defaultToolbarTitleColor;
    private int defaultBottomNavBg;
    private ColorStateList defaultBottomNavTints;
    private ColorStateList defaultDrawerTints;
    private boolean homeGuestChromeOverride;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.post(this::ensureThongBaoBadgeAttached);

        defaultToolbarColor = MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary, Color.BLACK);
        defaultToolbarTitleColor = Color.WHITE;
        defaultBottomNavBg = ContextCompat.getColor(this, R.color.white);
        defaultBottomNavTints = AppCompatResources.getColorStateList(this, R.color.bottom_nav_guest_tint);
        defaultDrawerTints = AppCompatResources.getColorStateList(this, R.color.drawer_item_color);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        bottomNav = findViewById(R.id.topNav);

        session = new SessionManager(this);

        setupBottomNavigationIfNeeded(true);
        updateDrawerMenu();
        updateNavHeader();
        refreshMainBackground();

        openInitialFragment();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fm = getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                    fm.executePendingTransactions();
                    Fragment f = fm.findFragmentById(R.id.fragmentContainer);
                    if (f instanceof AdminDashboardFragment) {
                        setToolbarTitle(getString(R.string.nav_admin_dashboard));
                    }
                    syncChromeAfterFragmentChange();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_login) {
                startActivity(new Intent(this, login.class));
            } else if (id == R.id.nav_register) {
                startActivity(new Intent(this, register.class));
            } else if (id == R.id.nav_logout) {
                lastNavKey = null;

                session.logout();

                // QUAN TRỌNG: khởi tạo lại session sau logout
                session = new SessionManager(this);

                setupBottomNavigationIfNeeded(true);

                // refresh lại menu drawer
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.menu_drawer); // menu chính của bạn

                updateDrawerMenu();
                updateNavHeader();

                Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();

                clearBackStack();
                openFragment(new Home());
                setToolbarTitle(getString(R.string.nav_home));
                bottomNav.setSelectedItemId(R.id.nav_home);
                refreshMainBackground();

            } else if (id == R.id.nav_drawer_cai_dat) {
                openFragment(new SettingsFragment());
                setToolbarTitle(getString(R.string.settings_title));
            } else {
                navigateFromDrawer(id);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
        MenuItem searchItem = menu.findItem(R.id.action_tim_phong);
        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            if (searchView != null) {
                searchView.setQueryHint(getString(R.string.phong_guest_search_field_hint));
                searchView.setMaxWidth(Integer.MAX_VALUE);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        guestPhongSearchQuery = query != null ? query : "";
                        notifyPhongGuestSearchChanged();
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        guestPhongSearchQuery = newText != null ? newText : "";
                        notifyPhongGuestSearchChanged();
                        return true;
                    }
                });
                searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                        if (!(f instanceof Phong)) {
                            navigateToPhongTabFromToolbarSearch();
                        }
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        guestPhongSearchQuery = "";
                        notifyPhongGuestSearchChanged();
                        return true;
                    }
                });
            }
        }
        return true;
    }

    public String getGuestPhongSearchQuery() {
        return guestPhongSearchQuery != null ? guestPhongSearchQuery : "";
    }

    private void notifyPhongGuestSearchChanged() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (f instanceof Phong) {
            ((Phong) f).onToolbarSearchQueryChanged();
        }
    }

    private void navigateToPhongTabFromToolbarSearch() {
        clearBackStack();
        suppressBottomNavReopen = true;
        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            applyFragmentAnimFade(ft);
            ft.replace(R.id.fragmentContainer, new Phong()).commit();
            if (bottomNav.getMenu().findItem(R.id.nav_phong) != null) {
                bottomNav.setSelectedItemId(R.id.nav_phong);
            }
            setToolbarTitle(getString(R.string.nav_phong));
        } finally {
            bottomNav.post(() -> suppressBottomNavReopen = false);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem it = menu.findItem(R.id.action_thong_bao);
        if (it != null) {
            it.setTitle(R.string.thong_bao_title);
            SessionManager sm = new SessionManager(this);
            updateThongBaoBadge(sm);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void ensureThongBaoBadgeAttached() {
        if (thongBaoBadge != null || isFinishing()) {
            return;
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) {
            return;
        }
        thongBaoBadge = BadgeDrawable.create(this);
        thongBaoBadge.setBackgroundColor(ContextCompat.getColor(this, R.color.badge_notification));
        thongBaoBadge.setBadgeTextColor(Color.WHITE);
        thongBaoBadge.setMaxCharacterCount(3);
        BadgeUtils.attachBadgeDrawable(thongBaoBadge, toolbar, R.id.action_thong_bao);
        updateThongBaoBadge(new SessionManager(this));
    }

    private void updateThongBaoBadge(SessionManager sm) {
        if (thongBaoBadge == null) {
            return;
        }
        if (!sm.isLoggedIn()) {
            thongBaoBadge.setVisible(false);
            return;
        }
        int u = new ThongBaoDAO(this).countUnread(sm);
        if (u > 0) {
            thongBaoBadge.setNumber(u);
            thongBaoBadge.setVisible(true);
        } else {
            thongBaoBadge.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_thong_bao) {
            openFragmentWithBackStack(new ThongBaoFragment(), getString(R.string.thong_bao_title));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Mở tab quản lý đơn và hiện chi tiết đơn (từ thông báo). */
    public void openQlDatPhongAndDetail(int datPhongId) {
        clearBackStack();
        suppressBottomNavReopen = true;
        try {
            QLDatPhongFragment f = new QLDatPhongFragment();
            Bundle b = new Bundle();
            b.putInt(QLDatPhongFragment.ARG_OPEN_DAT_PHONG_ID, datPhongId);
            f.setArguments(b);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            applyFragmentAnimFade(ft);
            ft.replace(R.id.fragmentContainer, f).commit();
            if (bottomNav.getMenu().findItem(R.id.nav_ql_dat_phong) != null) {
                bottomNav.setSelectedItemId(R.id.nav_ql_dat_phong);
            }
            setToolbarTitle(getString(R.string.nav_ql_dat_phong));
        } finally {
            bottomNav.post(() -> suppressBottomNavReopen = false);
        }
        invalidateOptionsMenu();
    }

    /** Mở Đơn của tôi; có thể cuộn tới đơn tương ứng thông báo. */
    public void openDonCuaToiAndHighlight(Integer datPhongId) {
        clearBackStack();
        suppressBottomNavReopen = true;
        try {
            DonCuaToiFragment f = new DonCuaToiFragment();
            if (datPhongId != null && datPhongId > 0) {
                Bundle b = new Bundle();
                b.putInt(DonCuaToiFragment.ARG_OPEN_DAT_PHONG_ID, datPhongId);
                f.setArguments(b);
            }
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            applyFragmentAnimFade(ft);
            ft.replace(R.id.fragmentContainer, f).commit();
            if (bottomNav.getMenu().findItem(R.id.nav_don_cua_toi) != null) {
                bottomNav.setSelectedItemId(R.id.nav_don_cua_toi);
            }
            setToolbarTitle(getString(R.string.nav_don_cua_toi));
        } finally {
            bottomNav.post(() -> suppressBottomNavReopen = false);
        }
        invalidateOptionsMenu();
    }

    /** Điều hướng từ menu cạnh — đồng bộ bottom nav khi có tab tương ứng. */
    private void navigateFromDrawer(int id) {
        clearBackStack();
        if (id == R.id.nav_drawer_home) {
            handleNavItem(R.id.nav_home);
            bottomNav.setSelectedItemId(R.id.nav_home);
            return;
        }
        if (id == R.id.nav_drawer_phong) {
            handleNavItem(R.id.nav_phong);
            bottomNav.setSelectedItemId(R.id.nav_phong);
            return;
        }
        if (id == R.id.nav_drawer_tin_tuc) {
            if (!session.isLoggedIn() || session.isKhach()) {
                handleNavItem(R.id.nav_tin_tuc);
                bottomNav.setSelectedItemId(R.id.nav_tin_tuc);
            } else {
                openFragment(new TinTucFragment());
                setToolbarTitle(getString(R.string.nav_tin_tuc));
            }
            return;
        }
        if (id == R.id.nav_drawer_homestay) {
            openFromHome(R.id.nav_homestay);
            return;
        }
        if (id == R.id.nav_drawer_danh_gia) {
            openFromHome(R.id.nav_danh_gia);
            return;
        }
        if (id == R.id.nav_drawer_don_toi) {
            handleNavItem(R.id.nav_don_cua_toi);
            bottomNav.setSelectedItemId(R.id.nav_don_cua_toi);
            return;
        }
        if (id == R.id.nav_drawer_ql_dat) {
            handleNavItem(R.id.nav_ql_dat_phong);
            bottomNav.setSelectedItemId(R.id.nav_ql_dat_phong);
            return;
        }
        if (id == R.id.nav_drawer_ql_tk) {
            handleNavItem(R.id.nav_ql_tai_khoan);
            bottomNav.setSelectedItemId(R.id.nav_ql_tai_khoan);
            return;
        }
        if (id == R.id.nav_drawer_ql_dv) {
            openFragment(new QLDichVuFragment());
            setToolbarTitle(getString(R.string.nav_ql_dich_vu));
            return;
        }
        if (id == R.id.nav_drawer_ql_nv) {
            openFragment(new QLNhanVienFragment());
            setToolbarTitle(getString(R.string.nav_ql_nhan_vien));
            return;
        }
        if (id == R.id.nav_drawer_thong_ke) {
            openFragment(new ThongKeChartFragment());
            setToolbarTitle(getString(R.string.nav_thong_ke));
            return;
        }
        if (id == R.id.nav_drawer_trang_chu_cai_dat) {
            openFragment(new TrangChuCaiDatFragment());
            setToolbarTitle(getString(R.string.nav_trang_chu_cai_dat));
            return;
        }
        if (id == R.id.nav_drawer_duyet_danh_gia) {
            openFragment(new DuyetDanhGiaFragment());
            setToolbarTitle(getString(R.string.nav_duyet_danh_gia));
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        session = new SessionManager(this);
        setupBottomNavigationIfNeeded(false);
        updateDrawerMenu();
        updateNavHeader();
        refreshMainBackground();
        refreshHomeDataIfVisible();
        if (session.isLoggedIn() && (session.isAdmin() || session.isNhanVien())) {
            ThongBaoDAO.notifyOverdueStaysIfNeeded(this);
        }
        invalidateOptionsMenu();
        maybeShowFirstPasswordReminder();
    }

    /** Sau khi đặt phòng tự tạo tài khoản & đăng nhập (từ dialog đặt phòng). */
    public void refreshSessionUi() {
        session = new SessionManager(this);
        setupBottomNavigationIfNeeded(true);
        updateDrawerMenu();
        updateNavHeader();
        invalidateOptionsMenu();
        refreshVisibleFragmentFromDb();
        refreshMainBackground();
        refreshHomeDataIfVisible();
    }

    /** Làm mới màn Fragment đang hiển thị (dữ liệu mới nhất từ CSDL). */
    public void refreshVisibleFragmentFromDb() {
        getSupportFragmentManager().executePendingTransactions();
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (f instanceof DataRefreshable) {
            ((DataRefreshable) f).refreshData();
        }
    }

    /**
     * Nếu đang mở Trang chủ — nạp lại hero/nền từ CSDL (sau khi admin lưu cấu hình, tránh vẫn thấy ảnh cũ).
     */
    public void refreshHomeDataIfVisible() {
        getSupportFragmentManager().executePendingTransactions();
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (f instanceof Home) {
            ((Home) f).refreshData();
        }
    }

    /** Sau khi lưu Trang chủ — cài đặt: cập nhật nền app + hero (nếu đang ở Trang chủ). */
    public void notifyHomestayConfigChanged() {
        refreshMainBackground();
        refreshHomeDataIfVisible();
        View root = findViewById(R.id.mainContentRoot);
        if (root != null) {
            root.post(() -> {
                refreshMainBackground();
                refreshHomeDataIfVisible();
            });
        }
    }

    /** Nền vùng nội dung theo ảnh cấu hình (khách / nhân viên / admin). */
    public void refreshMainBackground() {
        View root = findViewById(R.id.mainContentRoot);
        if (root != null) {
            root.post(() -> AppBackgroundHelper.applyForSession(MainActivity.this, root));
        }
    }

    /** Nhắc đổi mật khẩu mặc định (123) — gọi từ onResume hoặc sau đặt phòng. */
    public void maybeShowFirstPasswordReminder() {
        if (isFinishing()) {
            return;
        }
        session = new SessionManager(this);
        if (!session.isLoggedIn() || !session.isKhach() || !session.mustChangePassword()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.first_password_change_title)
                .setMessage(R.string.first_password_change_msg)
                .setPositiveButton(R.string.first_password_change_ok, (d, w) -> {
                    session.clearMustChangePassword();
                })
                .show();
    }

    private void openInitialFragment() {
        if (session.isLoggedIn() && session.isAdmin()) {
            openFragment(new AdminDashboardFragment());
            setToolbarTitle(getString(R.string.nav_admin_dashboard));
        } else {
            openFragment(new Home());
            setToolbarTitle(getString(R.string.nav_home));
        }
    }

    private void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void clearBackStack() {
        FragmentManager fm = getSupportFragmentManager();
        while (fm.getBackStackEntryCount() > 0) {
            fm.popBackStackImmediate();
        }
    }

    private void setupBottomNavigationIfNeeded(boolean force) {
        String key = !session.isLoggedIn() ? "guest" : session.getRole();
        if (!force && lastNavKey != null && lastNavKey.equals(key)) {
            return;
        }
        lastNavKey = key;

        bottomNav.getMenu().clear();
        int menuRes;
        if (!session.isLoggedIn()) {
            menuRes = R.menu.menu_bottom_guest;
        } else if (session.isAdmin()) {
            menuRes = R.menu.menu_bottom_admin;
        } else if (session.isNhanVien()) {
            menuRes = R.menu.menu_bottom_nhanvien;
        } else {
            menuRes = R.menu.menu_bottom_khach;
        }
        getMenuInflater().inflate(menuRes, bottomNav.getMenu());

        bottomNav.setOnItemSelectedListener(item -> {
            if (suppressBottomNavReopen) {
                return true;
            }
            clearBackStack();
            handleNavItem(item.getItemId());
            return true;
        });
    }

    private void handleNavItem(int id) {
        if (id == R.id.nav_home) {
            if (session.isAdmin()) {
                openFragment(new AdminDashboardFragment());
                setToolbarTitle(getString(R.string.nav_admin_dashboard));
            } else {
                openFragment(new Home());
                setToolbarTitle(getString(R.string.nav_home));
            }
        } else if (id == R.id.nav_phong) {
            openFragment(new Phong());
            setToolbarTitle(getString(R.string.nav_phong));
        } else if (id == R.id.nav_tin_tuc) {
            openFragment(new TinTucFragment());
            setToolbarTitle(getString(R.string.nav_tin_tuc));
        } else if (id == R.id.nav_danh_gia) {
            openFragment(new DanhGiaFragment());
            setToolbarTitle(getString(R.string.nav_danh_gia));
        } else if (id == R.id.nav_don_cua_toi) {
            openFragment(new DonCuaToiFragment());
            setToolbarTitle(getString(R.string.nav_don_cua_toi));
        } else if (id == R.id.nav_ql_dat_phong) {
            openFragment(new QLDatPhongFragment());
            setToolbarTitle(getString(R.string.nav_ql_dat_phong));
        } else if (id == R.id.nav_ql_tai_khoan) {
            openFragment(new QLTaiKhoan());
            setToolbarTitle(getString(R.string.nav_ql_tai_khoan));
        } else if (id == R.id.nav_menu) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    public void adminNavigateTo(int bottomNavItemId) {
        clearBackStack();
        bottomNav.setSelectedItemId(bottomNavItemId);
    }

    /**
     * Điều hướng từ trang chủ / menu cạnh — đồng bộ bottom nav khi có tab tương ứng.
     * {@code R.id.nav_homestay} mở màn hình giới thiệu (không có tab thanh dưới).
     */
    public void openFromHome(int destinationId) {
        clearBackStack();
        if (destinationId == R.id.nav_homestay) {
            openFragment(new HomestayInfoFragment());
            setToolbarTitle(getString(R.string.nav_homestay));
            return;
        }
        handleNavItem(destinationId);
        if (bottomNav.getMenu().findItem(destinationId) != null) {
            bottomNav.setSelectedItemId(destinationId);
        }
    }

    public void openFragmentWithBackStack(Fragment fragment, String title) {
        setToolbarTitle(title);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        applyFragmentAnimPush(ft);
        ft.replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
        syncChromeAfterFragmentChange();
    }

    private void updateDrawerMenu() {
        Menu menu = navigationView.getMenu();

        if (menu == null) return;

        boolean in = session.isLoggedIn();
        boolean admin = in && session.isAdmin();
        boolean staff = in && session.isNhanVien();
        boolean khach = in && session.isKhach();

        menu.findItem(R.id.nav_login).setVisible(!in);
        menu.findItem(R.id.nav_register).setVisible(!in);
        menu.findItem(R.id.nav_logout).setVisible(in);

        menu.findItem(R.id.nav_drawer_don_toi).setVisible(in && khach);

        boolean qlDonOrStats = admin || staff;
        menu.findItem(R.id.nav_drawer_ql_dat).setVisible(qlDonOrStats);
        menu.findItem(R.id.nav_drawer_thong_ke).setVisible(qlDonOrStats);

        menu.findItem(R.id.nav_drawer_ql_tk).setVisible(admin);
        menu.findItem(R.id.nav_drawer_ql_dv).setVisible(admin);
        menu.findItem(R.id.nav_drawer_ql_nv).setVisible(admin);

        boolean trangChuAdmin = admin;
        menu.findItem(R.id.nav_drawer_trang_chu_cai_dat).setVisible(trangChuAdmin);
        menu.findItem(R.id.nav_drawer_duyet_danh_gia).setVisible(trangChuAdmin);
    }

    private void updateNavHeader() {
        View header = navigationView.getHeaderView(0);
        TextView tvName = header.findViewById(R.id.navHeaderName);
        TextView tvRole = header.findViewById(R.id.navHeaderRole);
        ImageView imgAvatar = header.findViewById(R.id.imgAvatar);
        if (session.isLoggedIn()) {
            tvName.setText(session.getDisplayName().isEmpty() ? session.getUsername() : session.getDisplayName());
            tvRole.setText(SessionManager.roleLabelVi(session.getRole()));
            TaiKhoan tk = new TaiKhoanDAO(this).getById(session.getTaiKhoanId());
            if (tk != null && tk.getAnhDaiDien() != null && !tk.getAnhDaiDien().isEmpty()) {
                RoomImageUtils.loadInto(imgAvatar, tk.getAnhDaiDien());
            } else {
                imgAvatar.setImageResource(R.mipmap.ic_launcher_round);
            }
        } else {
            tvName.setText("Khách");
            tvRole.setText("Chưa đăng nhập");
            imgAvatar.setImageResource(R.mipmap.ic_launcher_round);
        }
    }

    /** Gọi sau khi đổi ảnh đại diện trong Cài đặt (không recreate activity). */
    public void refreshDrawerHeader() {
        session = new SessionManager(this);
        updateNavHeader();
    }

    /** Trang chủ: toolbar + bottom nav + drawer lấy tông từ ảnh nền hero. */
    public void applyHomeGuestChromeFromHeroSeed(int seedColor) {
        if (isFinishing()) {
            return;
        }
        homeGuestChromeOverride = true;
        Toolbar tb = findViewById(R.id.toolbar);
        float[] hsv = new float[3];
        Color.colorToHSV(seedColor, hsv);
        hsv[1] = Math.min(1f, hsv[1] * 1.08f);
        int toolbarCol = Color.HSVToColor(hsv);
        boolean darkBar = ColorUtils.calculateLuminance(toolbarCol) < 0.45;
        int title = darkBar ? Color.WHITE : Color.BLACK;
        tb.setBackgroundColor(toolbarCol);
        tb.setTitleTextColor(title);
        int bottomBg = ColorUtils.blendARGB(toolbarCol, Color.WHITE, 0.52f);
        bottomNav.setBackgroundColor(bottomBg);
        // Tab đang chọn dùng màu từ ảnh — nếu quá nhạt trên nền bottom sáng, chữ/icon gần như biến mất (đặc biệt menu nhân viên).
        int selected = adjustSelectedColorForBottomBar(seedColor, bottomBg);
        // Nền bottom nav luôn khá sáng (pha trắng) — không dùng chữ trắng mờ khi toolbar tối (sẽ mờ trên nền sáng).
        boolean bottomIsLight = ColorUtils.calculateLuminance(bottomBg) > 0.45f;
        int unselectedNav = bottomIsLight
                ? ContextCompat.getColor(this, R.color.guest_nav_inactive)
                : ContextCompat.getColor(this, R.color.white);
        ColorStateList cslNav = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked}
                },
                new int[]{selected, unselectedNav}
        );
        bottomNav.setItemIconTintList(cslNav);
        bottomNav.setItemTextColor(cslNav);
        // Drawer nền sáng: luôn dùng chữ đậm cho mục không chọn (tránh lẫn với tint theo toolbar tối).
        int unselectedDrawer = ContextCompat.getColor(this, R.color.guest_nav_inactive);
        ColorStateList cslDrawer = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked}
                },
                new int[]{selected, unselectedDrawer}
        );
        navigationView.setItemIconTintList(cslDrawer);
        navigationView.setItemTextColor(cslDrawer);
    }

    /** Đảm bảo màu mục đang chọn đọc được trên nền thanh dưới (tránh seed quá sáng / trùng nền). */
    private static int adjustSelectedColorForBottomBar(int seed, int bottomBg) {
        double lBg = ColorUtils.calculateLuminance(bottomBg);
        double lSeed = ColorUtils.calculateLuminance(seed);
        if (Math.abs(lSeed - lBg) < 0.14f) {
            return ColorUtils.blendARGB(seed, lBg > 0.5f ? Color.BLACK : Color.WHITE, 0.5f);
        }
        if (lBg > 0.52f && lSeed > 0.58f) {
            return ColorUtils.blendARGB(seed, Color.BLACK, 0.42f);
        }
        if (lBg < 0.42f && lSeed < 0.4f) {
            return ColorUtils.blendARGB(seed, Color.WHITE, 0.38f);
        }
        return seed;
    }

    public void resetHomeGuestChrome() {
        if (!homeGuestChromeOverride) {
            return;
        }
        homeGuestChromeOverride = false;
        Toolbar tb = findViewById(R.id.toolbar);
        tb.setBackgroundColor(defaultToolbarColor);
        tb.setTitleTextColor(defaultToolbarTitleColor);
        bottomNav.setBackgroundColor(defaultBottomNavBg);
        if (defaultBottomNavTints != null) {
            bottomNav.setItemIconTintList(defaultBottomNavTints);
            bottomNav.setItemTextColor(defaultBottomNavTints);
        }
        if (defaultDrawerTints != null) {
            navigationView.setItemIconTintList(defaultDrawerTints);
            navigationView.setItemTextColor(defaultDrawerTints);
        }
    }

    private void syncChromeAfterFragmentChange() {
        getSupportFragmentManager().executePendingTransactions();
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (!(f instanceof Home)) {
            resetHomeGuestChrome();
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        applyFragmentAnimFade(ft);
        ft.replace(R.id.fragmentContainer, fragment).commit();
        syncChromeAfterFragmentChange();
    }

    /** Chuyển tab / màn chính: mờ dần nhẹ. */
    private static void applyFragmentAnimFade(FragmentTransaction ft) {
        ft.setCustomAnimations(
                R.anim.fragment_fade_in,
                R.anim.fragment_fade_out,
                R.anim.fragment_fade_in,
                R.anim.fragment_fade_out);
    }

    /** Màn xếp chồng (có nút back): trượt ngang + mờ. */
    private static void applyFragmentAnimPush(FragmentTransaction ft) {
        ft.setCustomAnimations(
                R.anim.fragment_slide_in_right,
                R.anim.fragment_slide_out_left,
                R.anim.fragment_slide_in_left,
                R.anim.fragment_slide_out_right);
    }
}

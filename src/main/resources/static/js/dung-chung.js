/** Tiện ích dùng chung hệ thống Sangrimar Cafe & Lounge */
(function (window) {
  'use strict';

  var USER_KEY = 'mocan_java_user';
  var CARTS_KEY = 'mocan_java_table_carts';

  var MENU_IMAGE_MAP = {
    1: 'assets/images/cafe_den_da_1794937559459.jpg',
    2: 'assets/images/cafe_sua_da_1794937558363.jpg',
    3: 'assets/images/bac_xiu_cafe_1794969775813.jpg',
    4: 'assets/images/bac_xiu_cafe_1794969775813.jpg',
    5: 'assets/images/cafe_den_da_1794937559459.jpg',
    6: 'assets/images/tra_dao_cam_sa_1794937558193.jpg',
    7: 'assets/v_i_l_i_518c68ba.png',
    8: 'assets/__long_senn_v_ng_52d1415b.png',
    9: 'assets/tr__s_a_9eee8433.png',
    10: 'assets/tr__chanh_9341e931.png',
    11: 'assets/images/matcha_croissant_1794937559936.jpg',
    12: 'assets/images/cafe_sua_da_1794937558363.jpg',
    13: 'assets/images/bac_xiu_cafe_1794969775813.jpg',
    14: 'assets/images/croissant_pastry_1794969797589.jpg',
    15: 'assets/Gemini_Generated_Image_n16uibn16uibn16u_b4ca1ca1.png',
    16: 'assets/cam_f4973615.png',
    17: 'assets/d_a_h_u_a8a5c321.png',
    18: 'assets/t_o_8fe1ad66.png',
    19: 'assets/Gemini_Generated_Image_5apdl45apdl45apd_a4b90edb.png',
    20: 'assets/Gemini_Generated_Image_83j0q883j0q883j0_7d4a1b30.png'
  };

  function escapeHtml(val) {
    if (val === null || val === undefined) return '';
    return String(val)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  function getCurrentUser() {
    try {
      var raw = localStorage.getItem(USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch (e) {
      return null;
    }
  }

  function setCurrentUser(user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  function logout() {
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(CARTS_KEY);
    fetch('/api/auth/logout', { method: 'POST', credentials: 'same-origin' })
      .catch(function () { })
      .finally(function () { window.location.href = 'index.html'; });
  }

  function requireAuth() {
    var user = getCurrentUser();
    if (!user || !user.tenDangNhap) {
      window.location.href = 'index.html';
      return null;
    }
    return user;
  }

  function isPrivileged(user) {
    if (!user || !user.vaiTro) return false;
    var role = String(user.vaiTro).toUpperCase();
    return role === 'QUAN_LY' || role === 'ADMIN' || role === 'MANAGER';
  }

  function getRoleTitle(user) {
    var role = user && user.vaiTro ? String(user.vaiTro).toUpperCase() : 'PHUC_VU';
    if (role === 'QUAN_LY' || role === 'ADMIN') return 'Quản lý · Toàn quyền';
    if (role === 'THU_NGAN' || role === 'MANAGER') return 'Thu ngân · Quầy POS';
    return 'Phục vụ · Gọi món';
  }

  function getRoleShort(user) {
    var role = user && user.vaiTro ? String(user.vaiTro).toUpperCase() : 'PHUC_VU';
    if (role === 'QUAN_LY' || role === 'ADMIN') return 'Quản lý';
    if (role === 'THU_NGAN' || role === 'MANAGER') return 'Thu ngân';
    return 'Phục vụ';
  }

  function getInitials(name) {
    var parts = String(name || 'NV').trim().split(/\s+/);
    if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  }

  async function apiFetch(url, options) {
    var opts = options || {};
    var headers = Object.assign({}, opts.headers || {});
    var response = await fetch(url, Object.assign({}, opts, {
      headers: headers,
      credentials: 'same-origin'
    }));
    var data = null;
    try {
      data = await response.json();
    } catch (e) {
      data = {};
    }
    if (!response.ok) {
      var err = new Error(data.error || 'Yêu cầu thất bại.');
      err.status = response.status;
      err.data = data;
      if (response.status === 401) {
        localStorage.removeItem(USER_KEY);
        localStorage.removeItem(CARTS_KEY);
        try { sessionStorage.clear(); } catch (e) { }
        if (!window.location.pathname.endsWith('index.html')) {
          window.location.href = 'index.html?expired=1';
        }
      }
      throw err;
    }
    return data;
  }

  function formatVnd(value) {
    var num = Number(value) || 0;
    return new Intl.NumberFormat('vi-VN').format(Math.round(num)) + ' ₫';
  }

  function formatTimeShort(val) {
    if (!val) return '--:--:--';
    var str = String(val);
    if (/^\d{2}:\d{2}:\d{2}$/.test(str)) return str;
    var parsed = new Date(str.replace(' ', 'T'));
    if (!isNaN(parsed.getTime())) {
      return parsed.toLocaleTimeString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      });
    }
    var match = str.match(/(\d{2}:\d{2}:\d{2})/);
    return match ? match[1] : str;
  }

  function formatDateVi(val) {
    if (!val) return '';
    var d = new Date(String(val).replace(' ', 'T'));
    if (isNaN(d.getTime())) return String(val);
    return d.toLocaleDateString('vi-VN');
  }

  function resolveMenuImage(menuItem) {
    if (menuItem && (menuItem.hinhAnh || menuItem.hinh_anh)) {
      var p = String(menuItem.hinhAnh || menuItem.hinh_anh);
      return p.startsWith('/') ? p.slice(1) : p;
    }
    var id = Number(menuItem && (menuItem.maMon || menuItem.ma_mon));
    return MENU_IMAGE_MAP[id] || 'assets/images/cafe_sua_da_1794937558363.jpg';
  }

  async function loadSessionUser() {
    var sessionUser = await apiFetch('/api/auth/me');
    var cached = getCurrentUser() || {};
    var user = Object.assign({}, cached, sessionUser);
    if (!user.hoTen) user.hoTen = user.tenDangNhap || 'Nhân viên';
    setCurrentUser(user);
    return user;
  }

  function loadTableCarts() {
    try {
      var raw = localStorage.getItem(CARTS_KEY);
      return raw ? JSON.parse(raw) : {};
    } catch (e) {
      return {};
    }
  }

  function saveTableCarts(carts) {
    localStorage.setItem(CARTS_KEY, JSON.stringify(carts || {}));
  }

  function inferIngredientCategory(tenNl) {
    var name = String(tenNl || '').toLowerCase();
    if (name.includes('cà phê')) return 'Cà phê';
    if (name.includes('sữa') || name.includes('kem')) return 'Sữa & Kem';
    if (name.includes('trà') || name.includes('siro') || name.includes('matcha')) return 'Trà & Siro';
    return 'Topping & Đường';
  }

  function renderAppShell(config) {
    config = config || {};
    var activeRoute = config.activeRoute || '';
    var user = config.user || getCurrentUser() || { hoTen: 'Nhân viên', tenDangNhap: 'staff', vaiTro: 'PHUC_VU' };
    var lowStockCount = Number(config.lowStockCount || 0);
    var onRefresh = config.onRefresh;

    var routeTitles = {
      banhang: 'Bán hàng & Quầy POS',
      kho: 'Kho nguyên liệu & KPI',
      nhansu: 'Nhân sự & Chấm công'
    };

    var isQuanLy = isPrivileged(user);
    var navItems = [
      { route: 'banhang', href: 'banhang.html', label: 'Bán hàng (POS)', icon: 'bi-bag-check' }
    ];
    if (isQuanLy) {
      navItems.push(
        { route: 'kho', href: 'kho.html', label: 'Kho & KPI', icon: 'bi-box-seam', badge: lowStockCount },
        { route: 'nhansu', href: 'nhansu.html', label: 'Nhân sự & Chấm công', icon: 'bi-people' }
      );
    } else {
      navItems.push(
        { route: 'nhansu', href: 'nhansu.html', label: 'Chấm công & Lịch ca', icon: 'bi-calendar-check' }
      );
    }

    var sidebarContainer = document.getElementById('app-sidebar-root');
    if (sidebarContainer) {
      sidebarContainer.innerHTML = '';
      sidebarContainer.style.display = 'none';
    }

    var topbarContainer = document.getElementById('app-topbar-root');
    if (topbarContainer) {
      var displayName = (user && (user.hoTen || user.tenDangNhap)) || 'Nhân viên';
      topbarContainer.innerHTML =
        '<header class="app-topbar px-3 px-sm-4 d-flex align-items-center justify-content-between gap-3">' +
        '<div class="d-flex align-items-center gap-3">' +
        '<a href="' + (isQuanLy ? 'kho.html' : 'banhang.html') + '" class="d-flex align-items-center gap-2 text-decoration-none">' +
        '<span class="brand-badge"><i class="bi bi-cup-hot-fill"></i></span>' +
        '<span class="font-display fw-bold fs-5" style="letter-spacing:-0.02em;color:var(--brand-primary,#5D4037);">Sangrimar Cafe & Lounge</span>' +
        '</a>' +
        '</div>' +

        '<nav class="d-none d-md-flex align-items-center gap-2">' +
        navItems.map(function (item) {
          var isActive = activeRoute === item.route;
          var badgeHtml = item.badge > 0
            ? '<span class="badge bg-danger rounded-pill px-2 py-1 ms-1 tabular-nums" style="font-size:11px;">' + item.badge + '</span>'
            : '';
          return '<a href="' + item.href + '" class="topbar-nav-link ' + (isActive ? 'active' : '') + '">' +
            '<i class="bi ' + item.icon + '"></i>' +
            '<span>' + item.label + '</span>' +
            badgeHtml +
            '</a>';
        }).join('') +
        '</nav>' +

        '<div class="d-flex align-items-center gap-2 gap-sm-3">' +
        '<div class="d-none d-sm-flex align-items-center gap-2 px-3 py-1 rounded-3" style="background-color:var(--bg-surface-muted);">' +
        '<span class="badge ' + (isPrivileged(user) ? 'bg-primary' : 'bg-success') + ' text-white text-uppercase" style="font-size:10px;letter-spacing:0.04em;">' +
        escapeHtml(getRoleShort(user)) +
        '</span>' +
        '<span class="fw-semibold text-dark" style="font-size:0.82rem;">' +
        escapeHtml(displayName) +
        '</span>' +
        '</div>' +
        '<button type="button" id="btn-refresh-mysql" class="btn btn-sm btn-outline-secondary d-none d-md-flex align-items-center gap-1" title="Đồng bộ dữ liệu từ MySQL">' +
        '<i class="bi bi-arrow-clockwise"></i><span class="d-none d-lg-inline">Đồng bộ</span>' +
        '</button>' +
        '<button type="button" id="btn-topbar-logout" class="btn btn-sm btn-outline-danger d-flex align-items-center gap-1" title="Đăng xuất khỏi hệ thống">' +
        '<i class="bi bi-box-arrow-right"></i><span class="d-none d-sm-inline">Đăng xuất</span>' +
        '</button>' +
        '<button class="btn btn-sm d-md-none border" type="button" data-bs-toggle="collapse" data-bs-target="#mobileNavDrawer" aria-label="Mở menu">' +
        '<i class="bi bi-list fs-5"></i>' +
        '</button>' +
        '</div>' +
        '</header>' +

        '<div class="collapse d-md-none bg-white border-bottom px-3 py-3 shadow-sm" id="mobileNavDrawer" style="border-color:var(--border-subtle) !important;">' +
        '<div class="d-flex flex-column gap-1 mb-3">' +
        navItems.map(function (item) {
          var isActive = activeRoute === item.route;
          return '<a href="' + item.href + '" class="topbar-nav-link ' + (isActive ? 'active' : '') + '">' +
            '<i class="bi ' + item.icon + '"></i><span>' + item.label + '</span>' +
            (item.badge > 0 ? '<span class="badge bg-danger ms-auto">' + item.badge + '</span>' : '') +
            '</a>';
        }).join('') +
        '</div>' +
        '<div class="d-flex align-items-center justify-content-between pt-2 border-top" style="border-color:var(--border-subtle) !important;">' +
        '<span style="font-size:0.78rem;color:var(--text-secondary);">' + escapeHtml(displayName) + ' (' + escapeHtml(getRoleShort(user)) + ')</span>' +
        '<button type="button" id="btn-mobile-logout" class="btn btn-outline-danger btn-sm">Đăng xuất</button>' +
        '</div>' +
        '</div>';
    }

    var logoutBtns = ['btn-topbar-logout', 'btn-mobile-logout'];
    logoutBtns.forEach(function (id) {
      var el = document.getElementById(id);
      if (el) el.addEventListener('click', logout);
    });

    var refreshBtn = document.getElementById('btn-refresh-mysql');
    if (refreshBtn && typeof onRefresh === 'function') {
      refreshBtn.addEventListener('click', function () {
        onRefresh();
      });
    }
  }

  window.MocAn = {
    escapeHtml: escapeHtml,
    getCurrentUser: getCurrentUser,
    setCurrentUser: setCurrentUser,
    logout: logout,
    requireAuth: requireAuth,
    isPrivileged: isPrivileged,
    apiFetch: apiFetch,
    formatVnd: formatVnd,
    formatTimeShort: formatTimeShort,
    formatDateVi: formatDateVi,
    resolveMenuImage: resolveMenuImage,
    loadTableCarts: loadTableCarts,
    saveTableCarts: saveTableCarts,
    inferIngredientCategory: inferIngredientCategory,
    loadSessionUser: loadSessionUser,
    renderAppShell: renderAppShell
  };
})(window);

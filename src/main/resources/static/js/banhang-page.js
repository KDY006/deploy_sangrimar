/** Module Bán hàng & Quầy POS (Sangrimar Cafe & Lounge) */
(function () {
  'use strict';

  var user = null;
  var state = { tables: [], menu: [], orders: [], ingredients: [] };
  var selected = '1'; // Default selected table: Bàn 01
  var currentOrder = null; // Open order of selected table
  var area = 'Tất cả';
  var category = 'Tất cả';
  var query = '';
  var payment = 'TIEN_MAT';
  var discount = 0;
  var carts = MocAn.loadTableCarts();

  var barModal = null;
  var precheckModal = null;
  var checkoutModal = null;
  var receiptModal = null;

  function genId() {
    return crypto.randomUUID ? crypto.randomUUID() : (Date.now() + '-' + Math.random().toString(36).substring(2, 9));
  }

  function esc(v) {
    return MocAn.escapeHtml(v);
  }

  function err(message) {
    var e = document.getElementById('pos-error');
    if (e) {
      e.textContent = message;
      e.classList.remove('d-none');
    }
    var info = document.getElementById('pos-info');
    if (info) info.classList.add('d-none');
  }

  function info(message) {
    var i = document.getElementById('pos-info');
    if (i) {
      i.textContent = message;
      i.classList.remove('d-none');
    }
    var e = document.getElementById('pos-error');
    if (e) e.classList.add('d-none');
  }

  function clearAlerts() {
    var e = document.getElementById('pos-error');
    if (e) e.classList.add('d-none');
    var i = document.getElementById('pos-info');
    if (i) i.classList.add('d-none');
  }

  function itemsFromOrder(o) {
    if (!o || !o.danhSachChiTiet) return [];
    return o.danhSachChiTiet.map(function (x) {
      return {
        maMon: x.monNuoc ? x.monNuoc.maMon : x.maMon,
        name: x.monNuoc ? x.monNuoc.tenMon : ('Món #' + x.maMon),
        price: x.donGiaBan,
        quantity: x.soLuong,
        soLuongDaInBar: x.soLuongDaInBar || 0,
        note: x.ghiChu || ''
      };
    });
  }

  function allTables() {
    var list = state.tables.map(function (t) {
      return {
        key: String(t.maBan),
        id: t.maBan,
        name: t.tenBan,
        area: t.viTri || 'Khu vực',
        soGhe: t.soGhe || 4,
        status: t.trangThai || 'TRONG',
        maHoaDonHienTai: t.maHoaDonHienTai,
        tongTienTamTinh: Number(t.tongTienTamTinh) || 0,
        thoiGianMoBan: t.thoiGianMoBan
      };
    });
    list.push({
      key: 'takeaway',
      id: null,
      name: 'Mang đi',
      area: 'Quầy bar',
      soGhe: 0,
      status: 'TRONG',
      maHoaDonHienTai: null,
      tongTienTamTinh: 0,
      thoiGianMoBan: null
    });
    return list;
  }

  function currentTable() {
    return allTables().find(function (t) { return t.key === selected; }) || allTables()[0];
  }

  function cart() {
    return carts[selected] || [];
  }

  function saveCart(value) {
    carts[selected] = value;
    MocAn.saveTableCarts(carts);
    renderTicket();
    renderTables();
  }

  function cartTotal() {
    return cart().reduce(function (sum, item) {
      return sum + Number(item.price) * Number(item.quantity);
    }, 0);
  }

  function isQuanLy() {
    return user && user.vaiTro === 'QUAN_LY';
  }

  function isThuNgan() {
    return user && (user.vaiTro === 'THU_NGAN' || user.vaiTro === 'QUAN_LY');
  }

  function isPhucVu() {
    return user && user.vaiTro === 'PHUC_VU';
  }

  async function load() {
    try {
      user = await MocAn.loadSessionUser();
      var promises = [
        MocAn.apiFetch('/api/ban'),
        MocAn.apiFetch('/api/mon-nuoc'),
        MocAn.apiFetch('/api/order/danh-sach')
      ];
      if (isQuanLy()) {
        promises.push(MocAn.apiFetch('/api/kho/nguyen-lieu').catch(function () { return []; }));
      }
      var r = await Promise.all(promises);
      state.tables = r[0] || [];
      state.menu = r[1] || [];
      state.orders = r[2] || [];
      state.ingredients = r[3] || [];

      if (!allTables().some(function (t) { return t.key === selected; })) {
        selected = state.tables.length ? String(state.tables[0].maBan) : 'takeaway';
      }

      await syncSelectedTableOrder();
      render();
    } catch (e) {
      if (e.status === 401) {
        location.href = 'index.html';
      } else {
        err(e.message || 'Lỗi tải dữ liệu.');
      }
    }
  }

  async function syncSelectedTableOrder() {
    var t = currentTable();
    currentOrder = null;
    if (t.id && t.status !== 'TRONG') {
      try {
        var res = await fetch('/api/order/theo-ban/' + t.id, { credentials: 'same-origin' });
        if (res.status === 200) {
          currentOrder = await res.json();
          carts[selected] = itemsFromOrder(currentOrder);
          MocAn.saveTableCarts(carts);
        }
      } catch (e) {
        console.error('Lỗi nạp đơn theo bàn:', e);
      }
    } else if (t.status === 'TRONG' && !carts[selected]) {
      carts[selected] = [];
    }
  }

  function renderTabs() {
    var areas = ['Tất cả'].concat(Array.from(new Set(allTables().map(function (x) { return x.area; }))));
    document.getElementById('area-tabs').innerHTML = areas.map(function (x) {
      return '<button class="moss-tab-btn ' + (area === x ? 'active' : '') + '" data-area="' + esc(x) + '">' + esc(x) + '</button>';
    }).join('');

    var cats = ['Tất cả'].concat(Array.from(new Set(state.menu.map(function (x) { return x.danhMuc; }))));
    document.getElementById('category-tabs').innerHTML = cats.map(function (x) {
      return '<button class="moss-tab-btn ' + (category === x ? 'active' : '') + '" data-cat="' + esc(x) + '">' + esc(x) + '</button>';
    }).join('');
  }

  function renderTables() {
    var filtered = allTables().filter(function (x) {
      return area === 'Tất cả' || x.area === area;
    });

    document.getElementById('tables-grid').innerHTML = filtered.map(function (t) {
      var statusClass = 'status-trong';
      var statusBadgeText = 'Trống';
      var statusIcon = '<i class="bi bi-circle me-1" style="color:#0d6efd;"></i>';
      var amountHtml = '';

      if (t.status === 'DANG_DUNG_NUOC') {
        statusClass = 'status-dang-dung-nuoc';
        statusBadgeText = 'Đang dùng nước';
        statusIcon = '<i class="bi bi-cup-hot-fill me-1 text-success"></i>';
        if (t.tongTienTamTinh > 0) {
          amountHtml = '<span class="table-amount text-success"><i class="bi bi-cash-stack me-1"></i>' + MocAn.formatVnd(t.tongTienTamTinh) + '</span>';
        }
      } else if (t.status === 'CHO_THANH_TOAN') {
        statusClass = 'status-cho-thanh-toan';
        statusBadgeText = 'Chờ thanh toán';
        statusIcon = '<i class="bi bi-cash-coin me-1" style="color:#d97706;"></i>';
        if (t.tongTienTamTinh > 0) {
          amountHtml = '<span class="table-amount" style="color:#d97706;"><i class="bi bi-cash-coin me-1"></i>' + MocAn.formatVnd(t.tongTienTamTinh) + '</span>';
        }
      }

      var isSelected = (selected === t.key);

      return '<div class="col-6 col-sm-4 col-md-3">' +
        '<button class="pos-table-btn ' + statusClass + (isSelected ? ' selected' : '') + '" data-table="' + t.key + '">' +
          '<div class="d-flex align-items-center justify-content-between mb-1">' +
            '<span class="fw-bold" style="font-size:0.95rem;">' + esc(t.name) + '</span>' +
            '<span class="text-muted" style="font-size:0.75rem;">' + esc(t.area) + '</span>' +
          '</div>' +
          '<div class="d-flex align-items-center justify-content-between mt-auto pt-1 gap-1" style="font-size:0.75rem;">' +
            '<span class="table-badge text-truncate">' + statusIcon + statusBadgeText + '</span>' +
            amountHtml +
          '</div>' +
        '</button>' +
      '</div>';
    }).join('');
  }

  function renderMenu() {
    var q = query.toLowerCase();
    var t = currentTable();
    var data = state.menu.filter(function (m) {
      return (category === 'Tất cả' || m.danhMuc === category) &&
        (!q || (m.tenMon + ' ' + m.danhMuc).toLowerCase().includes(q)) &&
        m.trangThai !== 'HET_HANG';
    });

    document.getElementById('menu-grid').innerHTML = data.map(function (m) {
      var count = typeof m.soLyCoTheLam === 'number' ? m.soLyCoTheLam : 0;
      var stockBadge = '';
      var isOutOfStock = (count === 0);

      if (count > 5) {
        stockBadge = '<span class="stock-badge-ok"><i class="bi bi-check2-circle me-1"></i>Còn ' + count + ' ly</span>';
      } else if (count >= 1 && count <= 5) {
        stockBadge = '<span class="stock-badge-low"><i class="bi bi-exclamation-triangle me-1"></i>Còn ' + count + ' ly</span>';
      } else {
        stockBadge = '<span class="stock-badge-out"><i class="bi bi-x-circle me-1"></i>HẾT MÓN</span>';
      }

      var actionButton = isOutOfStock
        ? '<button class="btn btn-secondary w-100 disabled" disabled style="font-size:0.82rem;"><i class="bi bi-slash-circle me-1"></i>Tạm hết món</button>'
        : '<button class="btn btn-forest w-100" data-add="' + m.maMon + '" style="font-size:0.82rem;"><i class="bi bi-plus-lg me-1"></i>Thêm vào ' + esc(t.name) + '</button>';

      return '<div class="col-12 col-sm-6 col-md-4">' +
        '<div class="menu-card ' + (isOutOfStock ? 'out-of-stock' : '') + '">' +
          '<div class="menu-card-media">' +
            '<img src="' + esc(MocAn.resolveMenuImage(m)) + '" alt="">' +
            '<div class="menu-card-overlay">' +
              '<span>' + esc(m.danhMuc) + '</span>' +
              '<span class="font-mono">' + MocAn.formatVnd(m.donGia) + '</span>' +
            '</div>' +
          '</div>' +
          '<div class="p-3">' +
            '<div class="d-flex align-items-center justify-content-between mb-2">' +
              '<h3 class="font-display fw-semibold mb-0" style="font-size:.95rem">' + esc(m.tenMon) + '</h3>' +
              stockBadge +
            '</div>' +
            actionButton +
          '</div>' +
        '</div>' +
      '</div>';
    }).join('') || '<div class="col-12 text-center py-4 text-muted">Không tìm thấy món phù hợp.</div>';
  }

  function hasCartChanges() {
    if (!currentOrder || !currentOrder.danhSachChiTiet) return true;
    var c = cart();
    var oldItems = currentOrder.danhSachChiTiet;
    if (c.length !== oldItems.length) return true;
    for (var i = 0; i < c.length; i++) {
      var item = c[i];
      var match = oldItems.find(function (x) {
        var mId = x.monNuoc ? x.monNuoc.maMon : x.maMon;
        return mId === item.maMon;
      });
      if (!match || match.soLuong !== item.quantity) return true;
    }
    return false;
  }

  function renderTicket() {
    var t = currentTable();
    var c = cart();
    var sub = cartTotal();
    var isPriv = isQuanLy();
    var disc = isPriv ? Math.min(discount, sub) : 0;

    document.getElementById('selected-table-label').textContent = t.name + ' · ' + t.area;
    document.getElementById('ticket-title').textContent = 'Phiếu gọi món · ' + t.name;

    var statusBadge = '';
    if (t.status === 'DANG_DUNG_NUOC') {
      statusBadge = '<span class="badge bg-success-subtle text-success border border-success-subtle" style="font-size:0.7rem;">Đang dùng nước</span>';
    } else if (t.status === 'CHO_THANH_TOAN') {
      statusBadge = '<span class="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle" style="font-size:0.7rem;">Chờ thanh toán</span>';
    } else {
      statusBadge = '<span class="badge bg-light text-secondary border" style="font-size:0.7rem;">Bàn trống</span>';
    }
    document.getElementById('ticket-subtitle').outerHTML = '<span id="ticket-subtitle">' + statusBadge + '</span>';
    document.getElementById('btn-clear-cart').classList.toggle('d-none', !c.length || t.status === 'CHO_THANH_TOAN');

    document.getElementById('cart-items-container').innerHTML = c.length
      ? c.map(function (x) {
          var isReadOnly = (t.status === 'CHO_THANH_TOAN' && isPhucVu());
          var buttonsHtml = isReadOnly
            ? '<b class="tabular-nums font-mono px-2">' + x.quantity + '</b>'
            : '<button class="btn btn-sm border" data-qty="' + x.maMon + '" data-delta="-1">−</button>' +
              '<b class="tabular-nums font-mono px-1">' + x.quantity + '</b>' +
              '<button class="btn btn-sm border" data-qty="' + x.maMon + '" data-delta="1">+</button>';

          return '<div class="d-flex justify-content-between align-items-center gap-2 py-2 border-bottom">' +
            '<div class="min-w-0 flex-grow-1">' +
              '<div class="fw-semibold text-truncate">' + esc(x.name) + '</div>' +
              '<small class="text-muted">' + MocAn.formatVnd(x.price) + ' × ' + x.quantity + '</small>' +
            '</div>' +
            '<div class="d-flex gap-1 align-items-center flex-shrink-0">' +
              buttonsHtml +
            '</div>' +
          '</div>';
        }).join('')
      : '<div class="text-center py-5" style="color:var(--text-secondary)">Chưa có món trong phiếu.</div>';

    var lockIcon = document.getElementById('discount-lock-icon');
    if (lockIcon) lockIcon.classList.toggle('d-none', isPriv);

    document.getElementById('discount-control-wrap').innerHTML = isPriv
      ? '<input id="pos-discount" type="number" min="0" class="form-control form-control-sm moss-input text-end font-mono" value="' + (discount || '') + '" placeholder="0" style="width:130px">'
      : '<span class="badge bg-light text-danger border py-1 px-2" style="font-size:0.7rem;"><i class="bi bi-lock-fill me-1"></i>Chỉ Quản lý</span>';

    document.getElementById('ticket-subtotal').textContent = MocAn.formatVnd(sub);
    document.getElementById('ticket-discount-row').classList.toggle('d-none', !disc);
    document.getElementById('ticket-discount').textContent = '-' + MocAn.formatVnd(disc);
    document.getElementById('ticket-total').textContent = MocAn.formatVnd(sub - disc);

    document.querySelectorAll('[data-pay]').forEach(function (b) {
      b.classList.toggle('btn-forest', b.dataset.pay === payment);
      b.classList.toggle('bg-light', b.dataset.pay !== payment);
    });

    renderDynamicTicketActions();
  }

  function renderDynamicTicketActions() {
    var container = document.getElementById('ticket-actions-container');
    if (!container) return;

    var t = currentTable();
    var c = cart();
    var hasItems = c.length > 0;
    var cartChanged = hasCartChanges();
    var html = '';

    if (t.status === 'TRONG') {
      html += '<button id="btn-create-bar-order" type="button" class="btn btn-moss w-100 py-3 fw-bold d-flex align-items-center justify-content-center gap-2" ' + (!hasItems ? 'disabled' : '') + ' style="font-size:0.92rem;">' +
        '<i class="bi bi-printer"></i><span>Xác nhận &amp; In Bill Bar</span>' +
      '</button>';
    } else if (t.status === 'DANG_DUNG_NUOC') {
      if (cartChanged && hasItems) {
        html += '<button id="btn-update-bar-order" type="button" class="btn btn-forest w-100 py-2 fw-semibold d-flex align-items-center justify-content-center gap-2 mb-1">' +
          '<i class="bi bi-plus-circle"></i><span>Cập nhật &amp; In món mới (Bar)</span>' +
        '</button>';
      }
      html += '<button id="btn-request-payment" type="button" class="btn btn-warning w-100 py-3 fw-bold text-dark d-flex align-items-center justify-content-center gap-2" style="font-size:0.92rem;">' +
        '<i class="bi bi-bell-fill"></i><span>Yêu cầu thanh toán</span>' +
      '</button>';

      if (isThuNgan() || isQuanLy()) {
        html += '<button id="btn-cancel-order" type="button" class="btn btn-outline-danger btn-sm w-100 mt-1">' +
          '<i class="bi bi-x-circle me-1"></i>Hủy đơn hàng' +
        '</button>';
      }
    } else if (t.status === 'CHO_THANH_TOAN') {
      if (isPhucVu()) {
        html += '<div class="alert alert-warning py-2 px-3 mb-2 font-mono text-center" style="font-size:0.78rem;">' +
          '<i class="bi bi-hourglass-split me-1"></i>Đang chờ Thu ngân in bill &amp; xác nhận thanh toán' +
        '</div>' +
        '<button id="btn-back-to-serve" type="button" class="btn btn-outline-secondary w-100 py-2">' +
          '<i class="bi bi-arrow-counterclockwise me-1"></i>Quay lại phục vụ (Gọi thêm món)' +
        '</button>';
      } else {
        html += '<button id="btn-print-precheck" type="button" class="btn btn-outline-warning text-dark fw-bold border-warning w-100 py-2 d-flex align-items-center justify-content-center gap-2">' +
          '<i class="bi bi-receipt"></i><span>In Bill Tạm Tính (Pre-check)</span>' +
        '</button>' +
        '<button id="btn-open-checkout-modal" type="button" class="btn btn-moss w-100 py-3 fw-bold d-flex align-items-center justify-content-center gap-2" style="font-size:0.95rem;">' +
          '<i class="bi bi-cash-stack"></i><span>Xác nhận Thanh toán &amp; Đóng bàn</span>' +
        '</button>' +
        '<div class="d-flex gap-2 mt-1">' +
          '<button id="btn-back-to-serve" type="button" class="btn btn-outline-secondary btn-sm flex-fill">' +
            '<i class="bi bi-arrow-counterclockwise me-1"></i>Quay lại phục vụ' +
          '</button>' +
          '<button id="btn-cancel-order" type="button" class="btn btn-outline-danger btn-sm flex-fill">' +
            '<i class="bi bi-x-circle me-1"></i>Hủy đơn' +
          '</button>' +
        '</div>';
      }
    }

    container.innerHTML = html;
  }

  function renderOrders() {
    document.getElementById('orders-count-label').textContent = state.orders.length + ' đơn';
    document.getElementById('recent-orders-tbody').innerHTML = state.orders.slice(0, 10).map(function (o) {
      return '<tr>' +
        '<td><code class="fw-bold">HD-' + o.maHoaDon + '</code></td>' +
        '<td><strong>' + esc(o.ban ? o.ban.tenBan : 'Mang đi') + '</strong></td>' +
        '<td>' + esc(itemsFromOrder(o).map(function (x) { return x.quantity + ' ' + x.name; }).join(', ')) + '</td>' +
        '<td><span class="badge bg-light text-dark border">' + esc(o.phuongThucTT || '') + '</span></td>' +
        '<td>' + esc(o.nhanVien ? o.nhanVien.hoTen : '--') + '</td>' +
        '<td>' + esc(o.nhanVienThuNgan ? o.nhanVienThuNgan.hoTen : '--') + '</td>' +
        '<td class="font-mono">' + MocAn.formatTimeShort(o.thoiGianTao) + '</td>' +
        '<td class="text-end font-mono fw-bold text-success">' + MocAn.formatVnd(o.tongTienThanhToan) + '</td>' +
      '</tr>';
    }).join('') || '<tr><td colspan="8" class="text-center py-4 text-muted">Chưa có đơn hàng nào hôm nay.</td></tr>';
  }

  function render() {
    MocAn.renderAppShell({
      activeRoute: 'banhang',
      user: user,
      lowStockCount: state.ingredients.filter(function (x) { return Number(x.soLuongTon) <= Number(x.mucTonToiThieu); }).length,
      onRefresh: load
    });
    renderTabs();
    renderTables();
    renderMenu();
    renderTicket();
    renderOrders();
  }

  async function selectTable(key) {
    selected = key;
    clearAlerts();
    await syncSelectedTableOrder();
    render();
  }

  async function createBarOrder() {
    clearAlerts();
    var t = currentTable();
    var c = cart();
    if (!c.length) return err('Vui lòng chọn món trước khi tạo đơn.');

    var btn = document.getElementById('btn-create-bar-order');
    if (btn) btn.disabled = true;

    try {
      var payload = {
        maBan: t.id,
        loaiDon: t.id ? 'TAI_BAN' : 'MANG_DI',
        yeuCauTao: genId(),
        danhSachMon: c.map(function (x) {
          return { maMon: x.maMon, soLuong: x.quantity, ghiChu: x.note || '' };
        })
      };

      var order = await MocAn.apiFetch('/api/order/tao-don', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      var barItems = (order.phieuInBar && order.phieuInBar.length > 0)
        ? order.phieuInBar.map(function (item) {
            return {
              maMon: item.monNuoc ? item.monNuoc.maMon : item.maMon,
              name: item.monNuoc ? item.monNuoc.tenMon : ('Món #' + item.maMon),
              price: item.donGiaBan,
              quantity: item.soLuong,
              note: item.ghiChu || ''
            };
          })
        : c;

      info('Mở bàn thành công! Đã chuyển đơn xuống Quầy Bar.');
      showBarModal(order, barItems, 'Món mới mở bàn');
      await load();
    } catch (e) {
      err(e.message || 'Lỗi tạo đơn quầy bar.');
    } finally {
      if (btn) btn.disabled = false;
    }
  }

  async function updateBarOrder() {
    clearAlerts();
    if (!currentOrder) return err('Không tìm thấy thông tin đơn hàng hiện tại.');
    var t = currentTable();
    var c = cart();

    var btn = document.getElementById('btn-update-bar-order');
    if (btn) btn.disabled = true;

    try {
      var payload = {
        maBan: t.id,
        loaiDon: t.id ? 'TAI_BAN' : 'MANG_DI',
        phienBan: currentOrder.phienBan,
        danhSachMon: c.map(function (x) {
          return { maMon: x.maMon, soLuong: x.quantity, ghiChu: x.note || '' };
        })
      };

      var updated = await MocAn.apiFetch('/api/order/' + currentOrder.maHoaDon + '/mon', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      var deltaItems = [];
      if (updated.phieuInBarBoSung && updated.phieuInBarBoSung.length > 0) {
        deltaItems = updated.phieuInBarBoSung.map(function (item) {
          return {
            maMon: item.monNuoc ? item.monNuoc.maMon : item.maMon,
            name: item.monNuoc ? item.monNuoc.tenMon : ('Món #' + item.maMon),
            price: item.donGiaBan,
            quantity: item.soLuong,
            note: item.ghiChu || ''
          };
        });
      } else {
        var oldItems = currentOrder.danhSachChiTiet || [];
        c.forEach(function (ci) {
          var prev = oldItems.find(function (oi) {
            var mId = oi.monNuoc ? oi.monNuoc.maMon : oi.maMon;
            return mId === ci.maMon;
          });
          var delta = ci.quantity - (prev ? prev.soLuong : 0);
          if (delta > 0) {
            deltaItems.push(Object.assign({}, ci, { quantity: delta }));
          }
        });
      }

      info('Đã cập nhật đơn và gửi món bổ sung xuống Bar!');
      showBarModal(updated, deltaItems.length ? deltaItems : c, 'Món gọi bổ sung');
      await load();
    } catch (e) {
      err(e.message || 'Lỗi cập nhật món.');
    } finally {
      if (btn) btn.disabled = false;
    }
  }

  async function requestPayment() {
    clearAlerts();
    if (!currentOrder) return err('Không tìm thấy đơn hàng để yêu cầu thanh toán.');

    var btn = document.getElementById('btn-request-payment');
    if (btn) btn.disabled = true;

    try {
      await MocAn.apiFetch('/api/order/' + currentOrder.maHoaDon + '/yeu-cau-thanh-toan', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phienBan: currentOrder.phienBan })
      });

      info('Đã gửi yêu cầu thanh toán! Bàn chuyển sang trạng thái "Chờ thanh toán" (viền Vàng).');
      await load();
    } catch (e) {
      err(e.message || 'Lỗi yêu cầu thanh toán.');
    } finally {
      if (btn) btn.disabled = false;
    }
  }

  async function backToServe() {
    clearAlerts();
    if (!currentOrder) return err('Không tìm thấy đơn hàng.');

    try {
      await MocAn.apiFetch('/api/order/' + currentOrder.maHoaDon + '/quay-lai-phuc-vu', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phienBan: currentOrder.phienBan })
      });

      info('Đã chuyển bàn về trạng thái đang phục vụ.');
      await load();
    } catch (e) {
      err(e.message || 'Lỗi chuyển trạng thái bàn.');
    }
  }

  async function cancelOrder() {
    clearAlerts();
    if (!currentOrder) return err('Không tìm thấy đơn hàng để hủy.');
    if (!confirm('Bạn có chắc chắn muốn hủy đơn hàng HD-' + currentOrder.maHoaDon + ' của bàn này không?')) return;

    try {
      await MocAn.apiFetch('/api/order/' + currentOrder.maHoaDon + '/huy', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phienBan: currentOrder.phienBan })
      });

      carts[selected] = [];
      MocAn.saveTableCarts(carts);
      info('Đã hủy đơn hàng thành công.');
      await load();
    } catch (e) {
      err(e.message || 'Lỗi hủy đơn.');
    }
  }

  function showBarModal(order, barItems, ticketType) {
    var t = currentTable();
    document.getElementById('bar-ticket-type').textContent = ticketType || 'Món mới';
    document.getElementById('bar-table-name').textContent = t.name + ' (' + t.area + ')';
    document.getElementById('bar-order-code').textContent = 'HD-' + order.maHoaDon;
    document.getElementById('bar-order-time').textContent = MocAn.formatTimeShort(order.thoiGianTao || new Date());
    document.getElementById('bar-server-name').textContent = (order.nhanVien ? order.nhanVien.hoTen : (user ? user.hoTen : 'Phục vụ'));

    document.getElementById('bar-items-tbody').innerHTML = (barItems || []).map(function (item) {
      return '<tr>' +
        '<td class="fw-bold">' + esc(item.name) + '</td>' +
        '<td class="text-center font-mono fw-bold fs-6">' + item.quantity + '</td>' +
        '<td class="text-muted">' + esc(item.note || 'Pha chế chuẩn') + '</td>' +
      '</tr>';
    }).join('');

    barModal.show();
  }

  function showPrecheckModal(order) {
    var t = currentTable();
    var o = order || currentOrder;
    if (!o) return;

    var orderItems = itemsFromOrder(o);
    var sub = Number(o.tongTienGoc) || cartTotal();
    var disc = Number(o.chietKhau) || (isQuanLy() ? Math.min(discount, sub) : 0);
    var total = sub - disc;

    document.getElementById('precheck-table-name').textContent = t.name + ' · ' + t.area;
    document.getElementById('precheck-order-code').textContent = 'HD-' + o.maHoaDon;
    document.getElementById('precheck-open-time').textContent = MocAn.formatTimeShort(o.thoiGianTao || new Date());
    document.getElementById('precheck-print-time').textContent = MocAn.formatTimeShort(new Date());
    document.getElementById('precheck-server-name').textContent = o.nhanVien ? o.nhanVien.hoTen : (user ? user.hoTen : '--');

    document.getElementById('precheck-items-tbody').innerHTML = orderItems.map(function (item) {
      return '<tr>' +
        '<td>' + esc(item.name) + '</td>' +
        '<td class="text-center font-mono">' + item.quantity + '</td>' +
        '<td class="text-end font-mono">' + MocAn.formatVnd(item.price) + '</td>' +
        '<td class="text-end font-mono fw-semibold">' + MocAn.formatVnd(item.price * item.quantity) + '</td>' +
      '</tr>';
    }).join('');

    document.getElementById('precheck-subtotal').textContent = MocAn.formatVnd(sub);
    document.getElementById('precheck-discount-row').classList.toggle('d-none', !disc);
    document.getElementById('precheck-discount').textContent = '-' + MocAn.formatVnd(disc);
    document.getElementById('precheck-total').textContent = MocAn.formatVnd(total);

    var qrUrl = 'https://img.vietqr.io/image/MB-0987654321-compact2.png?amount=' + total + '&addInfo=' + encodeURIComponent('Sangrimar ' + t.name);
    document.getElementById('precheck-qr-img').src = qrUrl;

    precheckModal.show();
  }

  var checkoutPayMethod = 'TIEN_MAT';
  function openCheckoutModal() {
    clearAlerts();
    if (!currentOrder) return err('Không tìm thấy đơn hàng cần thanh toán.');

    var sub = Number(currentOrder.tongTienGoc) || cartTotal();
    var disc = isQuanLy() ? Math.min(discount, sub) : 0;
    var total = sub - disc;

    document.getElementById('checkout-modal-total').textContent = MocAn.formatVnd(total);
    document.getElementById('modal-tien-khach-dua').value = total;
    document.getElementById('modal-tien-thua').textContent = '0 ₫';

    document.getElementById('modal-ma-tham-chieu').value = 'POS-' + Date.now();
    document.getElementById('modal-transfer-qr-img').src = 'https://img.vietqr.io/image/MB-0987654321-compact2.png?amount=' + total + '&addInfo=' + encodeURIComponent('Sangrimar HD' + currentOrder.maHoaDon);

    var errEl = document.getElementById('modal-checkout-error');
    if (errEl) errEl.classList.add('d-none');

    setCheckoutMethod('TIEN_MAT');
    checkoutModal.show();
  }

  function setCheckoutMethod(method) {
    checkoutPayMethod = method;
    var isCash = (method === 'TIEN_MAT');

    document.getElementById('modal-pay-cash-btn').className = 'btn flex-fill py-2 d-flex align-items-center justify-content-center gap-2 ' + (isCash ? 'btn-forest' : 'btn-outline-secondary');
    document.getElementById('modal-pay-transfer-btn').className = 'btn flex-fill py-2 d-flex align-items-center justify-content-center gap-2 ' + (!isCash ? 'btn-forest' : 'btn-outline-secondary');

    document.getElementById('modal-cash-section').classList.toggle('d-none', !isCash);
    document.getElementById('modal-transfer-section').classList.toggle('d-none', isCash);
  }

  function updateChangeCalculation() {
    var sub = Number(currentOrder.tongTienGoc) || cartTotal();
    var disc = isQuanLy() ? Math.min(discount, sub) : 0;
    var total = sub - disc;
    var cash = Number(document.getElementById('modal-tien-khach-dua').value) || 0;
    var change = cash - total;

    var changeEl = document.getElementById('modal-tien-thua');
    if (change < 0) {
      changeEl.textContent = 'Thiếu ' + MocAn.formatVnd(Math.abs(change));
      changeEl.className = 'font-display fw-bold fs-4 text-danger font-mono';
    } else {
      changeEl.textContent = MocAn.formatVnd(change);
      changeEl.className = 'font-display fw-bold fs-4 text-success font-mono';
    }
  }

  async function confirmCheckout() {
    if (!currentOrder) return;
    var errEl = document.getElementById('modal-checkout-error');
    errEl.classList.add('d-none');

    var sub = Number(currentOrder.tongTienGoc) || cartTotal();
    var effectiveDiscount = isQuanLy() ? Math.min(discount, sub) : 0;
    var total = sub - effectiveDiscount;

    var payPayload = {
      chietKhau: effectiveDiscount,
      phuongThucTT: checkoutPayMethod,
      phienBan: currentOrder.phienBan,
      yeuCauThanhToan: genId()
    };

    if (checkoutPayMethod === 'TIEN_MAT') {
      var cash = Number(document.getElementById('modal-tien-khach-dua').value) || 0;
      if (cash < total) {
        errEl.textContent = 'Tiền khách đưa chưa đủ để thanh toán.';
        errEl.classList.remove('d-none');
        return;
      }
      payPayload.tienKhachDua = cash;
    } else {
      var ref = (document.getElementById('modal-ma-tham-chieu').value || '').trim();
      if (!ref) {
        errEl.textContent = 'Vui lòng nhập mã tham chiếu giao dịch chuyển khoản.';
        errEl.classList.remove('d-none');
        return;
      }
      payPayload.xacNhanChuyenKhoan = true;
      payPayload.maThamChieu = ref;
      payPayload.minhChungChuyenKhoan = (document.getElementById('modal-minh-chung').value || '').trim();
    }

    var doBtn = document.getElementById('btn-modal-do-checkout');
    if (doBtn) doBtn.disabled = true;

    try {
      var paidOrder = await MocAn.apiFetch('/api/order/' + currentOrder.maHoaDon + '/thanh-toan', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payPayload)
      });

      checkoutModal.hide();

      carts[selected] = [];
      MocAn.saveTableCarts(carts);
      discount = 0;

      showReceiptModal(paidOrder);
      await load();
    } catch (e) {
      errEl.textContent = e.message || 'Lỗi thanh toán đơn hàng.';
      errEl.classList.remove('d-none');
    } finally {
      if (doBtn) doBtn.disabled = false;
    }
  }

  function showReceiptModal(o) {
    document.getElementById('receipt-meta').textContent = 'Hóa đơn HD-' + o.maHoaDon + ' đã thanh toán thành công.';

    var orderItems = itemsFromOrder(o);
    var detailsHtml = '<div class="d-flex justify-content-between pb-1 border-bottom">' +
      '<span class="fw-semibold">Cửa hàng:</span><strong>Sangrimar Cafe &amp; Lounge</strong>' +
    '</div>' +
    '<div class="d-flex justify-content-between"><span>Vị trí phục vụ:</span><strong>' + (o.ban ? o.ban.tenBan : 'Mang đi') + '</strong></div>' +
    '<div class="d-flex justify-content-between"><span>Phục vụ mở bàn:</span><span>' + (o.nhanVien ? o.nhanVien.hoTen : '--') + '</span></div>' +
    '<div class="d-flex justify-content-between"><span>Thu ngân chốt đơn:</span><strong class="text-primary">' + (o.nhanVienThuNgan ? o.nhanVienThuNgan.hoTen : (user ? user.hoTen : '--')) + '</strong></div>' +
    '<div class="d-flex justify-content-between"><span>Thời gian thanh toán:</span><span class="font-mono">' + MocAn.formatTimeShort(o.thoiGianThanhToan || new Date()) + '</span></div>' +
    '<div class="border-top my-2 pt-2">' +
      orderItems.map(function (it) {
        return '<div class="d-flex justify-content-between text-muted" style="font-size:0.8rem;">' +
          '<span>' + esc(it.name) + ' × ' + it.quantity + '</span>' +
          '<span class="font-mono">' + MocAn.formatVnd(it.price * it.quantity) + '</span>' +
        '</div>';
      }).join('') +
    '</div>' +
    '<div class="d-flex justify-content-between"><span>Phương thức:</span><strong>' + (o.phuongThucTT === 'TIEN_MAT' ? 'Tiền mặt' : 'QR Chuyển khoản') + '</strong></div>' +
    (o.chietKhau > 0 ? '<div class="d-flex justify-content-between text-success"><span>Giảm giá:</span><span class="font-mono">-' + MocAn.formatVnd(o.chietKhau) + '</span></div>' : '') +
    '<div class="d-flex justify-content-between align-items-baseline pt-1 border-top">' +
      '<span class="fw-bold">Tổng thanh toán:</span><strong class="text-success font-mono fs-4">' + MocAn.formatVnd(o.tongTienThanhToan) + '</strong>' +
    '</div>';

    if (o.phuongThucTT === 'TIEN_MAT') {
      detailsHtml += '<div class="d-flex justify-content-between text-muted"><span>Tiền khách đưa:</span><span class="font-mono">' + MocAn.formatVnd(o.tienKhachDua) + '</span></div>' +
        '<div class="d-flex justify-content-between text-muted"><span>Tiền thối lại:</span><span class="font-mono fw-bold text-dark">' + MocAn.formatVnd(o.tienThua) + '</span></div>';
    } else if (o.maThamChieu) {
      detailsHtml += '<div class="d-flex justify-content-between text-muted"><span>Mã GD:</span><code class="font-mono">' + esc(o.maThamChieu) + '</code></div>';
    }

    document.getElementById('receipt-details').innerHTML = detailsHtml;

    var khoBtn = document.getElementById('receipt-btn-kho');
    if (khoBtn) khoBtn.classList.toggle('d-none', !isQuanLy());

    receiptModal.show();
  }

  document.addEventListener('click', function (e) {
    var b = e.target.closest('[data-area],[data-cat],[data-table],[data-add],[data-qty],[data-pay],#btn-create-bar-order,#btn-update-bar-order,#btn-request-payment,#btn-back-to-serve,#btn-cancel-order,#btn-print-precheck,#btn-open-checkout-modal,#btn-modal-do-checkout,#modal-pay-cash-btn,#modal-pay-transfer-btn,.quick-money-btn');
    if (!b) return;

    if (b.dataset.area) {
      area = b.dataset.area;
      renderTabs();
      renderTables();
    }
    if (b.dataset.cat) {
      category = b.dataset.cat;
      renderTabs();
      renderMenu();
    }
    if (b.dataset.table) {
      selectTable(b.dataset.table);
    }
    if (b.dataset.add) {
      var t = currentTable();
      if (t.status === 'CHO_THANH_TOAN') {
        return err('Bàn đang chờ thanh toán. Vui lòng bấm "Quay lại phục vụ" trước khi gọi thêm món.');
      }
      var m = state.menu.find(function (x) { return String(x.maMon) === b.dataset.add; });
      if (m) {
        var count = typeof m.soLyCoTheLam === 'number' ? m.soLyCoTheLam : 99;
        if (count <= 0) {
          return err('Món "' + m.tenMon + '" hiện đã tạm hết nguyên liệu pha chế!');
        }
        var c = cart().slice();
        var x = c.find(function (z) { return z.maMon === m.maMon; });
        if (x) {
          if (x.quantity + 1 > count) {
            return err('Món "' + m.tenMon + '" chỉ còn khả dụng tối đa ' + count + ' ly!');
          }
          x.quantity++;
        } else {
          c.push({ maMon: m.maMon, name: m.tenMon, price: m.donGia, quantity: 1, note: '' });
        }
        saveCart(c);
      }
    }
    if (b.dataset.qty) {
      var t2 = currentTable();
      if (t2.status === 'CHO_THANH_TOAN') {
        return err('Bàn đang chờ thanh toán. Bấm "Quay lại phục vụ" để chỉnh sửa.');
      }
      var c2 = cart().map(function (x) { return Object.assign({}, x); });
      var y = c2.find(function (x) { return String(x.maMon) === b.dataset.qty; });
      if (y) {
        var delta = Number(b.dataset.delta);
        if (delta > 0) {
          var mItem = state.menu.find(function (z) { return z.maMon === y.maMon; });
          var maxLy = mItem && typeof mItem.soLyCoTheLam === 'number' ? mItem.soLyCoTheLam : 99;
          if (y.quantity + delta > maxLy) {
            return err('Món "' + y.name + '" chỉ còn khả dụng tối đa ' + maxLy + ' ly!');
          }
        }
        y.quantity += delta;
        saveCart(c2.filter(function (x) { return x.quantity > 0; }));
      }
    }
    if (b.dataset.pay) {
      payment = b.dataset.pay;
      renderTicket();
    }

    if (b.id === 'btn-create-bar-order') createBarOrder();
    if (b.id === 'btn-update-bar-order') updateBarOrder();
    if (b.id === 'btn-request-payment') requestPayment();
    if (b.id === 'btn-back-to-serve') backToServe();
    if (b.id === 'btn-cancel-order') cancelOrder();
    if (b.id === 'btn-print-precheck') showPrecheckModal(currentOrder);
    if (b.id === 'btn-open-checkout-modal') openCheckoutModal();
    if (b.id === 'btn-modal-do-checkout') confirmCheckout();

    if (b.id === 'modal-pay-cash-btn') setCheckoutMethod('TIEN_MAT');
    if (b.id === 'modal-pay-transfer-btn') setCheckoutMethod('CHUYEN_KHOAN');

    if (b.classList.contains('quick-money-btn')) {
      var sub = Number(currentOrder.tongTienGoc) || cartTotal();
      var disc = isQuanLy() ? Math.min(discount, sub) : 0;
      var total = sub - disc;
      var cashInput = document.getElementById('modal-tien-khach-dua');

      if (b.dataset.mode === 'exact') {
        cashInput.value = total;
      } else if (b.dataset.add) {
        cashInput.value = (Number(cashInput.value) || total) + Number(b.dataset.add);
      } else if (b.dataset.val) {
        cashInput.value = Number(b.dataset.val);
      }
      updateChangeCalculation();
    }
  });

  document.getElementById('menu-search').addEventListener('input', function (e) {
    query = e.target.value;
    renderMenu();
  });

  document.getElementById('btn-clear-cart').addEventListener('click', function () {
    var t = currentTable();
    if (t.status === 'CHO_THANH_TOAN' && isPhucVu()) return;
    saveCart([]);
  });

  document.addEventListener('input', function (e) {
    if (e.target.id === 'pos-discount') {
      if (isQuanLy()) {
        discount = Math.max(0, Number(e.target.value) || 0);
        renderTicket();
      }
    }
    if (e.target.id === 'modal-tien-khach-dua') {
      updateChangeCalculation();
    }
  });

  barModal = new bootstrap.Modal(document.getElementById('barModal'));
  precheckModal = new bootstrap.Modal(document.getElementById('precheckModal'));
  checkoutModal = new bootstrap.Modal(document.getElementById('checkoutConfirmModal'));
  receiptModal = new bootstrap.Modal(document.getElementById('receiptModal'));

  load();
})();

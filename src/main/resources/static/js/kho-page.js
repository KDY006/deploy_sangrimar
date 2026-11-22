/** Module Quản lý Kho, BOM & Báo cáo KPI (Sangrimar Cafe & Lounge) */
(function () {
  'use strict';

  var user = null;
  var currentPeriod = 'HOM_NAY'; // HOM_NAY | BAY_NGAY | THANG_NAY | TAT_CA
  var currentTab = 'tab-inventory';

  var ingredients = [];
  var bomList = [];
  var historyList = [];
  var dashboardData = {};

  var lowOnly = false;
  var ingQuery = '';
  var bomQuery = '';
  var historyType = '';
  var historyNL = '';

  var targetNL = null;
  var targetDrink = null;

  var restockModal = null;
  var auditModal = null;
  var recipeModal = null;

  function esc(x) {
    return MocAn.escapeHtml(x);
  }

  function formatVnd(num) {
    return MocAn.formatVnd(num);
  }

  function isLow(nl) {
    return Number(nl.soLuongTon) <= Number(nl.mucTonToiThieu);
  }

  function notice(id, text, isError) {
    var el = document.getElementById(id);
    if (!el) return;
    el.innerHTML = '<div class="d-flex align-items-center gap-2">' +
      '<i class="bi ' + (isError ? 'bi-exclamation-triangle-fill text-danger' : 'bi-check-circle-fill text-success') + '"></i>' +
      '<span>' + esc(text) + '</span>' +
      '</div>' +
      '<button type="button" class="btn-close ms-auto" aria-label="Đóng" onclick="this.parentElement.classList.add(\'d-none\')"></button>';
    el.classList.remove('d-none');
    setTimeout(function () {
      if (el) el.classList.add('d-none');
    }, 6000);
  }

  async function loadAll() {
    try {
      user = await MocAn.loadSessionUser();

      if (!MocAn.isPrivileged(user)) {
        alert('Tài khoản của bạn không có quyền Quản lý Kho & KPI! Đang chuyển hướng về Bán hàng...');
        window.location.href = 'banhang.html';
        return;
      }

      var results = await Promise.all([
        MocAn.apiFetch('/api/kho/nguyen-lieu'),
        MocAn.apiFetch('/api/kho/bang-cong-thuc'),
        MocAn.apiFetch('/api/kho/dashboard?phamVi=' + currentPeriod),
        MocAn.apiFetch('/api/kho/lich-su?phamVi=' + currentPeriod)
      ]);

      ingredients = results[0] || [];
      bomList = results[1] || [];
      dashboardData = results[2] || {};
      historyList = results[3] || [];

      renderAll();
    } catch (e) {
      if (e.status === 401) {
        window.location.href = 'index.html';
      } else {
        notice('kho-error', e.message || 'Lỗi khi tải dữ liệu từ máy chủ', true);
      }
    }
  }

  async function reloadHistory() {
    try {
      var queryParams = [];
      if (currentPeriod) queryParams.push('phamVi=' + encodeURIComponent(currentPeriod));
      if (historyType) queryParams.push('loaiGD=' + encodeURIComponent(historyType));
      if (historyNL) queryParams.push('maNL=' + encodeURIComponent(historyNL));

      var url = '/api/kho/lich-su' + (queryParams.length ? '?' + queryParams.join('&') : '');
      historyList = await MocAn.apiFetch(url) || [];
      renderHistory();
    } catch (e) {
      notice('kho-error', 'Lỗi khi tải nhật ký biến động kho: ' + (e.message || ''), true);
    }
  }

  function renderAll() {
    var lows = ingredients.filter(isLow);

    MocAn.renderAppShell({
      activeRoute: 'kho',
      user: user,
      lowStockCount: lows.length,
      onRefresh: loadAll
    });

    renderKPIs();
    renderInventory();
    renderBOM();
    renderHistory();
    renderTopSellers();
    populateHistoryNLDropdown();
  }

  function renderKPIs() {
    var d = dashboardData || {};
    var lows = ingredients.filter(isLow);

    document.getElementById('kpi-revenue').textContent = formatVnd(d.tongDoanhThu || 0);
    document.getElementById('kpi-orders-discount').textContent =
      (d.tongDonHang || 0) + ' đơn hàng · Chiết khấu: ' + formatVnd(d.tongChietKhau || 0);

    document.getElementById('kpi-profit').textContent = formatVnd(d.loiNhuanGop || 0);
    document.getElementById('kpi-cogs-loss').textContent =
      'Giá vốn: ' + formatVnd(d.giaVonBanHang || 0) + ' · Hao hụt: ' + formatVnd(d.giaTriHaoHut || 0);

    var tm = d.doanhThuTienMat || 0;
    var ck = d.doanhThuChuyenKhoan || 0;
    var totalPayment = tm + ck;
    var tmPct = totalPayment > 0 ? Math.round((tm / totalPayment) * 100) : 50;
    var ckPct = totalPayment > 0 ? (100 - tmPct) : 50;

    document.getElementById('kpi-cash-val').textContent = formatVnd(tm);
    document.getElementById('kpi-cash-count').textContent = '(' + (d.soDonTienMat || 0) + ')';
    document.getElementById('kpi-transfer-val').textContent = formatVnd(ck);
    document.getElementById('kpi-transfer-count').textContent = '(' + (d.soDonChuyenKhoan || 0) + ')';

    document.getElementById('kpi-cash-bar').style.width = tmPct + '%';
    document.getElementById('kpi-transfer-bar').style.width = ckPct + '%';

    document.getElementById('kpi-stock-val').textContent = formatVnd(d.tongGiaTriTonKho || 0);
    document.getElementById('kpi-stock-count').textContent = (ingredients.length || 13) + ' nguyên liệu pha chế';

    document.getElementById('kpi-restock-cost').textContent = formatVnd(d.chiPhiNhapKho || 0);

    var lowValEl = document.getElementById('kpi-low-stock-val');
    var lowBadgeEl = document.getElementById('kpi-low-badge');
    var lowCardEl = document.getElementById('kpi-low-stock-card');
    var lowDescEl = document.getElementById('kpi-low-stock-desc');

    lowValEl.textContent = lows.length + ' / ' + ingredients.length;
    if (lows.length > 0) {
      lowValEl.className = 'kpi-value font-mono text-danger';
      lowBadgeEl.className = 'badge bg-danger-subtle text-danger border border-danger-subtle';
      lowBadgeEl.textContent = 'Cảnh báo (' + lows.length + ')';
      lowCardEl.style.borderColor = '#fca5a5';
      lowDescEl.textContent = 'Có ' + lows.length + ' nguyên liệu dưới ngưỡng tối thiểu!';
    } else {
      lowValEl.className = 'kpi-value font-mono text-success';
      lowBadgeEl.className = 'badge bg-success-subtle text-success border border-success-subtle';
      lowBadgeEl.textContent = 'Đủ hàng';
      lowCardEl.style.borderColor = 'var(--border-subtle, #e5e7eb)';
      lowDescEl.textContent = 'Tồn kho trên ngưỡng tối thiểu';
    }

    var banner = document.getElementById('low-stock-banner');
    if (banner) {
      if (lows.length > 0) {
        banner.className = 'alert alert-danger py-2 px-3 mb-0 rounded-3 d-flex align-items-center gap-2';
        banner.innerHTML = '<i class="bi bi-exclamation-triangle-fill fs-5"></i>' +
          '<span><strong>Cảnh báo sắp hết hàng:</strong> ' +
          esc(lows.map(function (x) { return x.tenNL + ' (còn ' + x.soLuongTon + ' ' + x.donViTinh + ')'; }).join(', ')) +
          '. Vui lòng nhập thêm hàng để tránh gián đoạn quầy POS.</span>';
      } else {
        banner.className = 'd-none';
      }
    }

    var filterCount = document.getElementById('low-filter-count');
    if (filterCount) filterCount.textContent = lows.length;
  }

  function renderInventory() {
    var tbody = document.getElementById('ingredients-tbody');
    if (!tbody) return;

    var filtered = ingredients.filter(function (item) {
      var matchLow = !lowOnly || isLow(item);
      var matchQ = !ingQuery || item.tenNL.toLowerCase().includes(ingQuery.toLowerCase());
      return matchLow && matchQ;
    });

    if (filtered.length === 0) {
      tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-muted">Không tìm thấy nguyên liệu phù hợp.</td></tr>';
      return;
    }

    tbody.innerHTML = filtered.map(function (nl) {
      var low = isLow(nl);
      var totalValue = (nl.soLuongTon || 0) * (nl.donGiaNhap || 0);
      var cat = MocAn.inferIngredientCategory(nl.tenNL);

      return '<tr class="' + (low ? 'table-warning bg-opacity-25' : '') + '">' +
        '<td class="fw-bold text-dark">' + esc(nl.tenNL) + '</td>' +
        '<td><span class="badge bg-light text-secondary border">' + esc(cat) + '</span></td>' +
        '<td class="text-end font-mono fw-bold ' + (low ? 'text-danger' : 'text-dark') + '">' +
          nl.soLuongTon + ' <small class="text-muted">' + esc(nl.donViTinh) + '</small>' +
        '</td>' +
        '<td class="text-end font-mono text-muted">&le; ' + nl.mucTonToiThieu + ' ' + esc(nl.donViTinh) + '</td>' +
        '<td class="text-end font-mono">' + formatVnd(nl.donGiaNhap) + '</td>' +
        '<td class="text-end font-mono fw-semibold text-primary">' + formatVnd(totalValue) + '</td>' +
        '<td>' +
          (low
            ? '<span class="badge bg-danger-subtle text-danger border border-danger-subtle"><i class="bi bi-exclamation-circle me-1"></i>SẮP HẾT</span>'
            : '<span class="badge bg-success-subtle text-success border border-success-subtle"><i class="bi bi-check-circle me-1"></i>Đủ hàng</span>') +
        '</td>' +
        '<td class="text-end">' +
          '<div class="btn-group btn-group-sm">' +
            '<button type="button" class="btn btn-outline-primary" data-action="restock" data-id="' + nl.maNL + '">' +
              '<i class="bi bi-box-arrow-in-down me-1"></i>+ Nhập kho' +
            '</button>' +
            '<button type="button" class="btn btn-outline-warning text-dark" data-action="audit" data-id="' + nl.maNL + '">' +
              '<i class="bi bi-clipboard-check me-1"></i>Kiểm kê' +
            '</button>' +
          '</div>' +
        '</td>' +
      '</tr>';
    }).join('');
  }

  function renderBOM() {
    var tbody = document.getElementById('bom-tbody');
    if (!tbody) return;

    var filtered = bomList.filter(function (b) {
      if (!bomQuery) return true;
      var q = bomQuery.toLowerCase();
      return (b.tenMon || '').toLowerCase().includes(q) || (b.danhMuc || '').toLowerCase().includes(q);
    });

    if (filtered.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">Không tìm thấy công thức đồ uống nào.</td></tr>';
      return;
    }

    tbody.innerHTML = filtered.map(function (b) {
      var boms = b.danhSachNguyenLieu || [];
      var bomDesc = boms.map(function (r) {
        return '<span class="badge bg-light text-dark border me-1 mb-1 font-mono" style="font-size:0.75rem;">' +
          esc(r.tenNL) + ': <strong>' + r.dinhMuc + ' ' + esc(r.donViTinh) + '</strong>' +
          '</span>';
      }).join('') || '<em class="text-muted" style="font-size:0.78rem;">Chưa cài đặt định mức</em>';

      var soLyClass = 'text-success fw-bold';
      if (b.soLyCoTheLam <= 0) soLyClass = 'text-danger fw-bold';
      else if (b.soLyCoTheLam <= 5) soLyClass = 'text-warning text-dark fw-bold';

      return '<tr>' +
        '<td>' +
          '<div class="fw-bold text-dark">' + esc(b.tenMon) + '</div>' +
          '<small class="text-muted">' + esc(b.danhMuc || 'Đồ uống') + '</small>' +
        '</td>' +
        '<td class="text-end font-mono fw-semibold">' + formatVnd(b.donGia) + '</td>' +
        '<td style="max-width:300px;"><div class="d-flex flex-wrap">' + bomDesc + '</div></td>' +
        '<td class="text-end font-mono text-secondary">' + formatVnd(b.giaVonLy) + '</td>' +
        '<td class="text-end font-mono fw-bold ' + (b.bienLoiNhuan >= 50 ? 'text-success' : 'text-primary') + '">' +
          (b.bienLoiNhuan || 0) + '%' +
        '</td>' +
        '<td class="text-center font-mono ' + soLyClass + '" style="font-size:0.95rem;">' +
          (b.soLyCoTheLam > 0 ? b.soLyCoTheLam + ' ly' : '<span class="badge bg-danger">Hết NL</span>') +
        '</td>' +
        '<td class="text-end">' +
          '<button type="button" class="btn btn-sm btn-outline-secondary" data-action="edit-bom" data-id="' + b.maMon + '">' +
            '<i class="bi bi-pencil me-1"></i>Sửa định mức' +
          '</button>' +
        '</td>' +
      '</tr>';
    }).join('');
  }

  function renderHistory() {
    var tbody = document.getElementById('history-tbody');
    if (!tbody) return;

    if (historyList.length === 0) {
      tbody.innerHTML = '<tr><td colspan="9" class="text-center py-4 text-muted">Chưa có giao dịch biến động kho nào trong kỳ đã chọn.</td></tr>';
      return;
    }

    tbody.innerHTML = historyList.map(function (row) {
      var badgeHtml = '';
      var thayDoiHtml = '';

      if (row.loaiGD === 'NHAP_KHO') {
        badgeHtml = '<span class="badge badge-audit-nhap"><i class="bi bi-box-arrow-in-down me-1"></i>Nhập kho</span>';
        thayDoiHtml = '<span class="text-success fw-bold font-mono">+' + row.soLuongThayDoi + ' ' + esc(row.donViTinh || '') + '</span>';
      } else if (row.loaiGD === 'XUAT_BAN_HANG') {
        badgeHtml = '<span class="badge badge-audit-xuat"><i class="bi bi-cart-check me-1"></i>Xuất bán</span>';
        thayDoiHtml = '<span class="text-primary fw-bold font-mono">' + row.soLuongThayDoi + ' ' + esc(row.donViTinh || '') + '</span>';
      } else {
        badgeHtml = '<span class="badge badge-audit-haohut"><i class="bi bi-exclamation-triangle me-1"></i>Kiểm kê hao hụt</span>';
        thayDoiHtml = '<span class="text-danger fw-bold font-mono">' + (row.soLuongThayDoi > 0 ? '+' : '') + row.soLuongThayDoi + ' ' + esc(row.donViTinh || '') + '</span>';
      }

      var docInfo = esc(row.ghiChu || '');
      if (row.maHoaDon) {
        docInfo = '<strong class="text-primary">#HD-' + row.maHoaDon + '</strong> ' + docInfo;
      } else if (row.nhaCungCap) {
        docInfo = '<span class="text-muted">NCC:</span> <strong>' + esc(row.nhaCungCap) + '</strong> ' + (docInfo ? '· ' + docInfo : '');
      }

      var timeStr = row.thoiGian ? String(row.thoiGian).replace('T', ' ').substring(0, 19) : '';

      return '<tr>' +
        '<td class="font-mono text-muted text-nowrap" style="font-size:0.75rem;">' + esc(timeStr) + '</td>' +
        '<td>' + badgeHtml + '</td>' +
        '<td class="fw-semibold">' + esc(row.tenNL || ('NL #' + row.maNL)) + '</td>' +
        '<td class="text-end">' + thayDoiHtml + '</td>' +
        '<td class="text-end font-mono text-muted" style="font-size:0.78rem;">' +
          row.tonTruoc + ' &rarr; <strong>' + row.tonSau + '</strong>' +
        '</td>' +
        '<td class="text-end font-mono text-secondary">' + formatVnd(row.donGia) + '</td>' +
        '<td class="text-end font-mono fw-bold text-dark">' + formatVnd(row.thanhTien) + '</td>' +
        '<td><span class="badge bg-light text-dark border">' + esc(row.tenNV || 'Hệ thống') + '</span></td>' +
        '<td style="max-width:240px;"><small>' + docInfo + '</small></td>' +
      '</tr>';
    }).join('');
  }

  function renderTopSellers() {
    var container = document.getElementById('top-selling-list');
    var label = document.getElementById('top-seller-period-label');
    if (!container) return;

    var periodNames = {
      HOM_NAY: 'Hôm nay',
      BAY_NGAY: '7 ngày qua',
      THANG_NAY: 'Tháng này',
      TAT_CA: 'Tất cả các kỳ'
    };
    if (label) label.textContent = periodNames[currentPeriod] || currentPeriod;

    var list = (dashboardData && dashboardData.topMonBanChay) || [];
    if (list.length === 0) {
      container.innerHTML = '<div class="text-center py-5 text-muted" style="font-size:0.85rem;">' +
        '<i class="bi bi-cup-straw fs-2 d-block mb-2 text-muted opacity-50"></i>' +
        'Chưa có đơn hàng nào phát sinh doanh thu trong kỳ này.' +
        '</div>';
      return;
    }

    var maxQty = list[0].tongSoLuong || 1;

    container.innerHTML = list.map(function (item, idx) {
      var rank = idx + 1;
      var rankBadge = rank === 1 ? 'bg-warning text-dark' : (rank === 2 ? 'bg-secondary text-white' : (rank === 3 ? 'bg-info text-dark' : 'bg-light text-dark border'));
      var pct = Math.max(10, Math.round((item.tongSoLuong / maxQty) * 100));

      return '<div class="p-2 border rounded-3 bg-white d-flex flex-column gap-1">' +
        '<div class="d-flex align-items-center justify-content-between">' +
          '<div class="d-flex align-items-center gap-2">' +
            '<span class="badge rounded-pill ' + rankBadge + ' font-mono" style="width:24px;height:24px;display:flex;align-items:center;justify-content:center;font-size:0.75rem;">' + rank + '</span>' +
            '<div>' +
              '<div class="fw-bold text-dark" style="font-size:0.85rem;">' + esc(item.tenMon) + '</div>' +
              '<small class="text-muted" style="font-size:0.72rem;">' + esc(item.danhMuc || '') + '</small>' +
            '</div>' +
          '</div>' +
          '<div class="text-end">' +
            '<div class="fw-bold font-mono text-primary" style="font-size:0.85rem;">' + item.tongSoLuong + ' ly</div>' +
            '<small class="font-mono text-muted" style="font-size:0.75rem;">' + formatVnd(item.tongDoanhThu) + '</small>' +
          '</div>' +
        '</div>' +
        '<div class="progress" style="height:4px;">' +
          '<div class="progress-bar ' + (rank === 1 ? 'bg-warning' : 'bg-primary') + '" style="width:' + pct + '%;"></div>' +
        '</div>' +
      '</div>';
    }).join('');
  }

  function populateHistoryNLDropdown() {
    var select = document.getElementById('filter-history-nl');
    if (!select) return;
    var currentVal = select.value;
    select.innerHTML = '<option value="">Tất cả nguyên liệu</option>' +
      ingredients.map(function (x) {
        return '<option value="' + x.maNL + '" ' + (String(x.maNL) === currentVal ? 'selected' : '') + '>' + esc(x.tenNL) + '</option>';
      }).join('');
  }

  document.querySelectorAll('.nav-tab-btn').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var tabId = this.dataset.tab;
      document.querySelectorAll('.nav-tab-btn').forEach(function (b) { b.classList.remove('active'); });
      this.classList.add('active');

      document.querySelectorAll('.tab-pane-content').forEach(function (p) { p.classList.add('d-none'); });
      var targetPane = document.getElementById(tabId + '-pane');
      if (targetPane) targetPane.classList.remove('d-none');
      currentTab = tabId;
    });
  });

  document.querySelectorAll('.period-pill').forEach(function (pill) {
    pill.addEventListener('click', function () {
      document.querySelectorAll('.period-pill').forEach(function (p) { p.classList.remove('active'); });
      this.classList.add('active');
      currentPeriod = this.dataset.period;
      loadAll();
    });
  });

  var ingSearch = document.getElementById('ing-search');
  if (ingSearch) {
    ingSearch.addEventListener('input', function (e) {
      ingQuery = e.target.value || '';
      renderInventory();
    });
  }

  var bomSearch = document.getElementById('bom-search');
  if (bomSearch) {
    bomSearch.addEventListener('input', function (e) {
      bomQuery = e.target.value || '';
      renderBOM();
    });
  }

  var btnFilterLow = document.getElementById('btn-filter-low');
  if (btnFilterLow) {
    btnFilterLow.addEventListener('click', function () {
      lowOnly = !lowOnly;
      this.classList.toggle('btn-danger', lowOnly);
      this.classList.toggle('btn-outline-secondary', !lowOnly);
      renderInventory();
    });
  }

  var filterType = document.getElementById('filter-history-type');
  if (filterType) {
    filterType.addEventListener('change', function (e) {
      historyType = e.target.value;
      reloadHistory();
    });
  }

  var filterNL = document.getElementById('filter-history-nl');
  if (filterNL) {
    filterNL.addEventListener('change', function (e) {
      historyNL = e.target.value;
      reloadHistory();
    });
  }

  var btnRefreshHistory = document.getElementById('btn-refresh-history');
  if (btnRefreshHistory) {
    btnRefreshHistory.addEventListener('click', reloadHistory);
  }

  var ingTable = document.getElementById('ingredients-tbody');
  if (ingTable) {
    ingTable.addEventListener('click', function (e) {
      var btn = e.target.closest('[data-action]');
      if (!btn) return;

      var action = btn.dataset.action;
      var id = Number(btn.dataset.id);
      targetNL = ingredients.find(function (x) { return x.maNL === id; });
      if (!targetNL) return;

      if (action === 'restock') {
        openRestockModal(targetNL);
      } else if (action === 'audit') {
        openAuditModal(targetNL);
      }
    });
  }

  var bomTable = document.getElementById('bom-tbody');
  if (bomTable) {
    bomTable.addEventListener('click', function (e) {
      var btn = e.target.closest('[data-action="edit-bom"]');
      if (!btn) return;
      var id = Number(btn.dataset.id);
      targetDrink = bomList.find(function (x) { return x.maMon === id; });
      if (!targetDrink) return;
      openRecipeModal(targetDrink);
    });
  }

  function openRestockModal(nl) {
    document.getElementById('restock-modal-title').textContent = 'Nhập kho · ' + nl.tenNL;
    document.getElementById('restock-modal-info').innerHTML =
      'Tồn hiện tại: <strong>' + nl.soLuongTon + ' ' + esc(nl.donViTinh) + '</strong> · ' +
      'Đơn giá vốn hiện tại: <strong>' + formatVnd(nl.donGiaNhap) + '</strong>';
    document.getElementById('restock-unit-label').textContent = nl.donViTinh;

    var amountInput = document.getElementById('restock-amount');
    var priceInput = document.getElementById('restock-price');
    var suppInput = document.getElementById('restock-supplier');
    var noteInput = document.getElementById('restock-note');

    amountInput.value = '';
    priceInput.value = nl.donGiaNhap || '';
    suppInput.value = '';
    noteInput.value = '';

    updateRestockPreview();
    restockModal.show();
    setTimeout(function () { amountInput.focus(); }, 300);
  }

  function updateRestockPreview() {
    var amt = Number(document.getElementById('restock-amount').value) || 0;
    var prc = Number(document.getElementById('restock-price').value) || (targetNL ? targetNL.donGiaNhap : 0) || 0;
    var total = amt * prc;
    document.getElementById('restock-preview-total').textContent = formatVnd(total);
  }

  document.getElementById('restock-amount').addEventListener('input', updateRestockPreview);
  document.getElementById('restock-price').addEventListener('input', updateRestockPreview);

  document.querySelectorAll('#restockModal .preset-btn').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var val = Number(this.dataset.preset);
      var current = Number(document.getElementById('restock-amount').value) || 0;
      document.getElementById('restock-amount').value = current + val;
      updateRestockPreview();
    });
  });

  document.getElementById('restock-form').addEventListener('submit', async function (e) {
    e.preventDefault();
    if (!targetNL) return;

    var amt = Number(document.getElementById('restock-amount').value);
    if (!amt || amt <= 0) {
      alert('Số lượng nhập phải lớn hơn 0');
      return;
    }
    var price = Number(document.getElementById('restock-price').value);
    var supp = document.getElementById('restock-supplier').value.trim();
    var note = document.getElementById('restock-note').value.trim();

    try {
      await MocAn.apiFetch('/api/kho/nhap-kho', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          maNL: targetNL.maNL,
          soLuongNhap: amt,
          donGiaNhapMoi: price > 0 ? price : null,
          nhaCungCap: supp || null,
          ghiChu: note || null
        })
      });

      restockModal.hide();
      notice('kho-feedback', 'Đã nhập thành công ' + amt + ' ' + targetNL.donViTinh + ' ' + targetNL.tenNL + ' vào kho.', false);
      loadAll();
    } catch (err) {
      notice('kho-error', err.message || 'Lỗi khi nhập kho', true);
    }
  });

  function openAuditModal(nl) {
    document.getElementById('audit-modal-title').textContent = 'Kiểm kê kho · ' + nl.tenNL;
    document.getElementById('audit-modal-info').innerHTML =
      'Tồn hệ thống: <strong>' + nl.soLuongTon + ' ' + esc(nl.donViTinh) + '</strong> · ' +
      'Giá vốn: <strong>' + formatVnd(nl.donGiaNhap) + '</strong>';
    document.getElementById('audit-unit-label').textContent = nl.donViTinh;

    var actualInput = document.getElementById('audit-actual-amount');
    var reasonInput = document.getElementById('audit-reason');

    actualInput.value = '';
    reasonInput.value = '';

    updateAuditPreview();
    auditModal.show();
    setTimeout(function () { actualInput.focus(); }, 300);
  }

  function updateAuditPreview() {
    if (!targetNL) return;
    var current = Number(targetNL.soLuongTon) || 0;
    var actualStr = document.getElementById('audit-actual-amount').value;
    var actual = actualStr === '' ? current : Number(actualStr);
    var delta = actual - current;
    var lossVal = Math.abs(delta) * (targetNL.donGiaNhap || 0);

    var deltaEl = document.getElementById('audit-delta');
    var lossEl = document.getElementById('audit-loss-value');

    if (delta === 0) {
      deltaEl.textContent = '0 (Khớp hệ thống)';
      deltaEl.className = 'font-mono text-muted';
      lossEl.textContent = '0 đ';
      lossEl.className = 'font-mono text-muted';
    } else if (delta < 0) {
      deltaEl.textContent = delta + ' ' + targetNL.donViTinh + ' (Hao hụt)';
      deltaEl.className = 'font-mono text-danger fw-bold';
      lossEl.textContent = formatVnd(lossVal) + ' (Tổn thất)';
      lossEl.className = 'font-mono text-danger fw-bold';
    } else {
      deltaEl.textContent = '+' + delta + ' ' + targetNL.donViTinh + ' (Tăng tồn)';
      deltaEl.className = 'font-mono text-success fw-bold';
      lossEl.textContent = '+' + formatVnd(lossVal) + ' (Thặng dư)';
      lossEl.className = 'font-mono text-success fw-bold';
    }
  }

  document.getElementById('audit-actual-amount').addEventListener('input', updateAuditPreview);

  document.getElementById('audit-form').addEventListener('submit', async function (e) {
    e.preventDefault();
    if (!targetNL) return;

    var actual = Number(document.getElementById('audit-actual-amount').value);
    if (isNaN(actual) || actual < 0) {
      alert('Số lượng thực tế không hợp lệ');
      return;
    }

    var reason = document.getElementById('audit-reason').value.trim();
    if (!reason) {
      alert('Vui lòng nhập lý do kiểm kê điều chỉnh');
      return;
    }

    if (actual === Number(targetNL.soLuongTon)) {
      alert('Số lượng thực tế khớp tồn kho hệ thống, không cần điều chỉnh');
      return;
    }

    try {
      await MocAn.apiFetch('/api/kho/kiem-ke', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          maNL: targetNL.maNL,
          soLuongThucTe: actual,
          ghiChu: reason
        })
      });

      auditModal.hide();
      notice('kho-feedback', 'Đã điều chỉnh tồn kho ' + targetNL.tenNL + ' thành ' + actual + ' ' + targetNL.donViTinh + ' thành công.', false);
      loadAll();
    } catch (err) {
      notice('kho-error', err.message || 'Lỗi khi kiểm kê kho', true);
    }
  });

  function openRecipeModal(drink) {
    document.getElementById('recipe-modal-drink-name').textContent = 'Món nước: ' + drink.tenMon + ' (' + formatVnd(drink.donGia) + ')';
    var select = document.getElementById('recipe-select-nl');

    select.innerHTML = ingredients.map(function (nl) {
      return '<option value="' + nl.maNL + '" data-unit="' + esc(nl.donViTinh) + '">' +
        esc(nl.tenNL) + ' (' + esc(nl.donViTinh) + ' - ' + formatVnd(nl.donGiaNhap) + ')' +
        '</option>';
    }).join('');

    var updateHint = function () {
      var opt = select.options[select.selectedIndex];
      var unit = opt ? opt.dataset.unit : 'ml';
      document.getElementById('recipe-unit-hint').textContent = unit;
    };

    select.onchange = updateHint;
    updateHint();

    document.getElementById('recipe-amount').value = '';
    document.getElementById('recipe-note').value = '';

    recipeModal.show();
  }

  document.getElementById('recipe-form').addEventListener('submit', async function (e) {
    e.preventDefault();
    if (!targetDrink) return;

    var maNL = Number(document.getElementById('recipe-select-nl').value);
    var amt = Number(document.getElementById('recipe-amount').value);
    var note = document.getElementById('recipe-note').value.trim();

    if (!amt || amt <= 0) {
      alert('Định mức công thức phải lớn hơn 0');
      return;
    }

    try {
      await MocAn.apiFetch('/api/kho/cong-thuc', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          maMon: targetDrink.maMon,
          maNL: maNL,
          dinhMuc: amt,
          ghiChu: note || null
        })
      });

      recipeModal.hide();
      notice('kho-feedback', 'Đã cập nhật định mức công thức cho món ' + targetDrink.tenMon + ' thành công.', false);
      loadAll();
    } catch (err) {
      notice('kho-error', err.message || 'Lỗi khi lưu định mức công thức', true);
    }
  });

  restockModal = new bootstrap.Modal(document.getElementById('restockModal'));
  auditModal = new bootstrap.Modal(document.getElementById('auditModal'));
  recipeModal = new bootstrap.Modal(document.getElementById('recipeModal'));

  loadAll();
})();

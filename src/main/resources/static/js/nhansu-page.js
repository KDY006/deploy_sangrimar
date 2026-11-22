/** Module Quản lý Nhân sự, Ca làm & Chấm công (Sangrimar Cafe & Lounge) */
(function () {
  'use strict';

  var user = MocAn.requireAuth();
  if (!user) return;
  var shifts = [];
  var staff = [];
  var currentWeekStart = null;

  MocAn.renderAppShell({
    activeRoute: 'nhansu',
    user: user,
    lowStockCount: 0,
    onRefresh: function () { refreshAll(); }
  });

  var adjustModal = null;
  var swapShiftModal = null;
  var scheduleModal = null;
  var addStaffModal = null;
  var editStaffModal = null;

  function esc(x) {
    return MocAn.escapeHtml(x);
  }

  function role() {
    return String((user && user.vaiTro) || '').toUpperCase();
  }

  function privileged() {
    return role() === 'QUAN_LY' || role() === 'ADMIN' || role() === 'MANAGER';
  }

  function showMsg(id, text, autoHide) {
    var el = document.getElementById(id);
    if (el) {
      el.textContent = text;
      el.classList.remove('d-none');
      if (autoHide) {
        setTimeout(function () {
          el.classList.add('d-none');
        }, 5000);
      }
    }
  }

  function hideMsg(id) {
    var el = document.getElementById(id);
    if (el) el.classList.add('d-none');
  }

  function getMonday(d) {
    var date = new Date(d);
    var day = date.getDay();
    var diff = date.getDate() - day + (day === 0 ? -6 : 1);
    var monday = new Date(date.setDate(diff));
    return toIsoDate(monday);
  }

  function toIsoDate(d) {
    var year = d.getFullYear();
    var month = String(d.getMonth() + 1).padStart(2, '0');
    var day = String(d.getDate()).padStart(2, '0');
    return year + '-' + month + '-' + day;
  }

  function addDays(isoDateStr, days) {
    var parts = isoDateStr.split('-');
    var d = new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]));
    d.setDate(d.getDate() + days);
    return toIsoDate(d);
  }

  function getRoleBadge(vaiTro) {
    var r = String(vaiTro || '').toUpperCase();
    if (r === 'QUAN_LY' || r === 'ADMIN' || r === 'MANAGER') {
      return '<span class="badge bg-dark">Quản lý</span>';
    }
    if (r === 'THU_NGAN') {
      return '<span class="badge bg-primary">Thu ngân</span>';
    }
    return '<span class="badge bg-success">Phục vụ</span>';
  }

  function getStatusBadge(trangThai) {
    switch (trangThai) {
      case 'DUNG_GIO':
        return '<span class="badge bg-success">Đúng giờ</span>';
      case 'DI_TRE':
        return '<span class="badge bg-danger">Đi trễ</span>';
      case 'VE_SOM':
        return '<span class="badge bg-warning text-dark">Về sớm</span>';
      case 'DI_TRE_VE_SOM':
        return '<span class="badge" style="background-color:#7C3AED;color:#fff;">Trễ &amp; Về sớm</span>';
      case 'QUEN_CHECK_OUT':
        return '<span class="badge bg-warning text-dark border border-warning">Quên Check-out</span>';
      case 'VANG':
        return '<span class="badge bg-secondary">Vắng mặt</span>';
      case 'CHUA_CHECK_IN':
        return '<span class="badge bg-light text-secondary border">Chưa Check-in</span>';
      default:
        return '<span class="badge bg-light text-dark border">' + esc(trangThai) + '</span>';
    }
  }


  async function init() {
    try {
      user = await MocAn.loadSessionUser();
      currentWeekStart = getMonday(new Date());

      var adjEl = document.getElementById('adjustModal');
      if (adjEl) adjustModal = new bootstrap.Modal(adjEl);

      var swapEl = document.getElementById('swapShiftModal');
      if (swapEl) swapShiftModal = new bootstrap.Modal(swapEl);

      var schEl = document.getElementById('scheduleModal');
      if (schEl) scheduleModal = new bootstrap.Modal(schEl);

      var addStEl = document.getElementById('addStaffModal');
      if (addStEl) addStaffModal = new bootstrap.Modal(addStEl);

      var editStEl = document.getElementById('editStaffModal');
      if (editStEl) editStaffModal = new bootstrap.Modal(editStEl);

      startLiveClocks();

      shifts = await MocAn.apiFetch('/api/nhan-su/ca-lam').catch(function () { return []; });

      var lowStockCount = 0;
      if (privileged()) {
        staff = await MocAn.apiFetch('/api/nhan-su/nhan-vien').catch(function () { return []; });
        var ings = await MocAn.apiFetch('/api/kho/nguyen-lieu').catch(function () { return []; });
        lowStockCount = ings.filter(function (x) {
          return Number(x.soLuongTon) <= Number(x.mucTonToiThieu);
        }).length;
      }

      MocAn.renderAppShell({
        activeRoute: 'nhansu',
        user: user,
        lowStockCount: lowStockCount,
        onRefresh: refreshAll
      });

      if (privileged()) {
        document.getElementById('manager-mode-container').classList.remove('d-none');
        document.getElementById('personal-mode-container').classList.add('d-none');
        initManagerMode();
      } else {
        document.getElementById('personal-mode-container').classList.remove('d-none');
        document.getElementById('manager-mode-container').classList.add('d-none');
        initPersonalMode();
      }
    } catch (e) {
      if (e.status === 401) {
        location.href = 'index.html';
      } else {
        showMsg('hr-error', e.message || 'Lỗi kết nối máy chủ');
      }
    }
  }

  function startLiveClocks() {
    function update() {
      var timeStr = new Date().toLocaleTimeString('vi-VN');
      var clockEls = document.querySelectorAll('.live-clock');
      clockEls.forEach(function (el) {
        el.textContent = timeStr;
      });
    }
    update();
    setInterval(update, 1000);
  }

  async function refreshAll() {
    if (privileged()) {
      await loadManagerSupervisionTab();
      await loadManagerScheduleTab();
      await loadManagerTimesheetTab();
      await loadManagerDirectoryTab();
    } else {
      await loadPersonalTodayShifts();
      await loadPersonalWeeklySchedule();
      await loadPersonalTimesheetAndHistory();
    }
  }


  async function initPersonalMode() {
    document.getElementById('personal-user-info').innerHTML =
      'Đang đăng nhập: <strong>' + esc(user.hoTen) + '</strong> (' + esc(user.tenDangNhap) + ') · Vai trò: ' + getRoleBadge(user.vaiTro);

    await loadPersonalTodayShifts();
    await loadPersonalWeeklySchedule();
    await loadPersonalTimesheetAndHistory();
  }

  async function loadPersonalTodayShifts() {
    var container = document.getElementById('personal-today-shifts-list');
    try {
      var data = await MocAn.apiFetch('/api/cham-cong/ca-hom-nay');
      if (!data || data.length === 0) {
        container.innerHTML =
          '<div class="col-12"><div class="p-3 bg-light rounded-3 text-muted text-center" style="font-size:0.85rem;">' +
          '<i class="bi bi-info-circle me-1"></i> Hôm nay bạn không có lịch phân ca làm việc nào.' +
          '</div></div>';
        return;
      }

      container.innerHTML = data.map(function (s) {
        var statusBadge = getStatusBadge(s.trangThai);
        var rateBadge = s.heSoLuong > 1.0
          ? '<span class="badge bg-warning text-dark ms-1">Hệ số x' + s.heSoLuong + '</span>'
          : '<span class="badge bg-light text-secondary border ms-1">x' + s.heSoLuong + '</span>';

        var actionHtml = '';
        if (s.coTheCheckIn) {
          actionHtml =
            '<button type="button" class="btn btn-moss btn-sm px-3 py-2 btn-do-checkin d-flex align-items-center gap-1" data-phanca="' + s.maPhanCa + '">' +
            '<i class="bi bi-box-arrow-in-right"></i><span>Check-in Vào Ca</span>' +
            '</button>';
        } else if (s.coTheCheckOut) {
          actionHtml =
            '<button type="button" class="btn btn-outline-danger btn-sm px-3 py-2 btn-do-checkout d-flex align-items-center gap-1" data-id="' + s.maChamCong + '">' +
            '<i class="bi bi-box-arrow-right"></i><span>Check-out Ra Ca</span>' +
            '</button>';
        } else if (s.gioCheckOut) {
          actionHtml = '<span class="text-success fw-medium small"><i class="bi bi-check-circle me-1"></i>Đã hoàn thành ca</span>';
        } else if (s.trangThai === 'VANG') {
          actionHtml = '<span class="text-danger fw-medium small"><i class="bi bi-x-circle me-1"></i>Đã chốt vắng</span>';
        } else if (s.trangThai === 'QUEN_CHECK_OUT') {
          actionHtml = '<span class="text-warning fw-medium small"><i class="bi bi-exclamation-triangle me-1"></i>Quên check-out</span>';
        } else {
          actionHtml = '<span class="text-muted small">' + esc(s.thongBaoCuaSo || 'Chưa mở check-in') + '</span>';
        }

        var detailsHtml = '';
        if (s.gioCheckIn) {
          detailsHtml =
            '<div class="d-flex flex-wrap gap-3 mt-2 pt-2 border-top text-secondary" style="font-size:0.78rem;">' +
            '<div>Vào: <strong class="font-mono text-dark">' + MocAn.formatTimeShort(s.gioCheckIn) + '</strong>' +
            (s.soPhutTre > 0 ? ' <span class="text-danger">(Trễ ' + s.soPhutTre + 'p)</span>' : '') + '</div>' +
            '<div>Ra: <strong class="font-mono text-dark">' + (s.gioCheckOut ? MocAn.formatTimeShort(s.gioCheckOut) : 'Đang làm') + '</strong>' +
            (s.soPhutVeSom > 0 ? ' <span class="text-warning">(Sớm ' + s.soPhutVeSom + 'p)</span>' : '') + '</div>' +
            (s.soGioLam > 0 ? '<div>Thực tế: <strong class="text-dark">' + s.soGioLam.toFixed(2) + 'h</strong></div>' : '') +
            (s.gioCongQuyDoi > 0 ? '<div>Quy đổi: <strong class="text-success">' + s.gioCongQuyDoi.toFixed(2) + ' công</strong></div>' : '') +
            '</div>';
        }

        return '<div class="col-12 col-md-6">' +
          '<div class="p-3 rounded-3 border bg-white d-flex flex-column justify-content-between h-100" style="border-color:var(--border-subtle) !important;">' +
            '<div>' +
              '<div class="d-flex align-items-center justify-content-between mb-2">' +
                '<div class="fw-bold fs-6">' + esc(s.tenCa) + rateBadge + '</div>' +
                '<div>' + statusBadge + '</div>' +
              '</div>' +
              '<div class="text-muted font-mono mb-2" style="font-size:0.82rem;">' +
                '<i class="bi bi-clock me-1"></i>' + String(s.gioBatDau).slice(0, 5) + ' - ' + String(s.gioKetThuc).slice(0, 5) +
              '</div>' +
              '<div class="text-secondary small mb-2"><i class="bi bi-info-circle me-1"></i>' + esc(s.thongBaoCuaSo || '') + '</div>' +
              detailsHtml +
            '</div>' +
            '<div class="mt-3 pt-2 d-flex justify-content-end align-items-center">' +
              actionHtml +
            '</div>' +
          '</div>' +
        '</div>';
      }).join('');

      container.querySelectorAll('.btn-do-checkin').forEach(function (btn) {
        btn.addEventListener('click', async function () {
          var maPhanCa = Number(this.dataset.phanca);
          try {
            await MocAn.apiFetch('/api/cham-cong/check-in', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ maPhanCa: maPhanCa })
            });
            showMsg('hr-notice', 'Check-in vào ca thành công!', true);
            await loadPersonalTodayShifts();
            await loadPersonalTimesheetAndHistory();
          } catch (e) {
            showMsg('hr-error', e.message || 'Lỗi khi check-in');
          }
        });
      });

      container.querySelectorAll('.btn-do-checkout').forEach(function (btn) {
        btn.addEventListener('click', async function () {
          var maChamCong = Number(this.dataset.id);
          try {
            await MocAn.apiFetch('/api/cham-cong/check-out/' + maChamCong, { method: 'POST' });
            showMsg('hr-notice', 'Check-out hoàn tất ca làm việc thành công!', true);
            await loadPersonalTodayShifts();
            await loadPersonalTimesheetAndHistory();
          } catch (e) {
            showMsg('hr-error', e.message || 'Lỗi khi check-out');
          }
        });
      });
    } catch (e) {
      container.innerHTML = '<div class="col-12 text-danger">Lỗi tải dữ liệu ca hôm nay.</div>';
    }
  }

  async function loadPersonalWeeklySchedule() {
    var gridEl = document.getElementById('personal-week-grid');
    var labelEl = document.getElementById('personal-week-label');
    try {
      var endWeek = addDays(currentWeekStart, 6);
      if (labelEl) labelEl.textContent = 'Tuần: ' + currentWeekStart + ' → ' + endWeek;

      var data = await MocAn.apiFetch('/api/phan-ca/tuan?ngayBatDau=' + currentWeekStart);
      var dayNames = ['Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'Chủ nhật'];

      var html = '';
      for (var i = 0; i < 7; i++) {
        var dayDate = addDays(currentWeekStart, i);
        var dayShifts = (data || []).filter(function (x) { return x.ngayLam === dayDate; });

        var shiftPills = dayShifts.map(function (s) {
          var c = s.caLam || {};
          return '<div class="p-2 mb-1 rounded border bg-light" style="font-size:0.75rem;">' +
            '<div class="fw-bold text-dark">' + esc(c.tenCa) + '</div>' +
            '<div class="font-mono text-muted">' + String(c.gioBatDau).slice(0, 5) + '-' + String(c.gioKetThuc).slice(0, 5) + '</div>' +
            '<div>' + getStatusBadge(s.trangThaiChamCong) + '</div>' +
          '</div>';
        }).join('') || '<div class="text-muted small py-2 text-center">Nghỉ</div>';

        html += '<div class="col" style="min-width:130px;">' +
          '<div class="p-2 rounded border bg-white h-100">' +
            '<div class="fw-bold text-center border-bottom pb-1 mb-2" style="font-size:0.78rem;">' +
              dayNames[i] + '<br><small class="text-muted font-mono">' + dayDate.slice(5) + '</small>' +
            '</div>' +
            shiftPills +
          '</div>' +
        '</div>';
      }
      gridEl.innerHTML = html;
    } catch (e) {
      gridEl.innerHTML = '<div class="col-12 text-danger">Lỗi tải lịch tuần.</div>';
    }
  }

  async function loadPersonalTimesheetAndHistory() {
    try {
      var today = toIsoDate(new Date());
      var firstDayOfMonth = today.slice(0, 8) + '01';

      var timesheets = await MocAn.apiFetch('/api/cham-cong/bang-cong?tuNgay=' + firstDayOfMonth + '&denNgay=' + today).catch(function () { return []; });
      var mySummary = (timesheets && timesheets.length > 0) ? timesheets[0] : null;

      var kpiContainer = document.getElementById('personal-kpi-strip');
      if (mySummary && kpiContainer) {
        kpiContainer.innerHTML = [
          ['Tổng ca làm', mySummary.tongSoCa],
          ['Đúng giờ', mySummary.soCaDungGio],
          ['Đi trễ / Về sớm', (mySummary.soCaDiTre + mySummary.soCaVeSom)],
          ['Giờ thực tế', (mySummary.tongGioThucTe || 0).toFixed(2) + ' h'],
          ['Giờ quy đổi hệ số', (mySummary.tongGioQuyDoi || 0).toFixed(2) + ' công']
        ].map(function (x) {
          return '<div class="col">' +
            '<div class="p-3 rounded-3 border bg-light text-center">' +
              '<div style="font-size:0.72rem;color:var(--text-secondary);">' + x[0] + '</div>' +
              '<div class="font-display fw-bold fs-4 mt-1" style="color:var(--accent-primary);">' + x[1] + '</div>' +
            '</div>' +
          '</div>';
        }).join('');
      }

      var history = await MocAn.apiFetch('/api/cham-cong/lich-su?tuNgay=' + firstDayOfMonth + '&denNgay=' + today).catch(function () { return []; });
      var tbody = document.getElementById('personal-history-tbody');
      if (!history || history.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center py-4 text-muted">Chưa có bản ghi chấm công nào trong tháng này.</td></tr>';
        return;
      }

      tbody.innerHTML = history.map(function (h) {
        var c = h.caLam || {};
        return '<tr>' +
          '<td class="font-mono">' + esc(h.ngayLam) + '</td>' +
          '<td class="fw-semibold">' + esc(c.tenCa) + '</td>' +
          '<td>x' + (c.heSoLuong || 1.0) + '</td>' +
          '<td class="font-mono">' + (h.gioCheckIn ? MocAn.formatTimeShort(h.gioCheckIn) : '--:--') + '</td>' +
          '<td class="font-mono">' + (h.gioCheckOut ? MocAn.formatTimeShort(h.gioCheckOut) : '<span class="text-primary">Đang làm</span>') + '</td>' +
          '<td>' + (h.soGioLam != null ? Number(h.soGioLam).toFixed(2) + ' h' : '--') + '</td>' +
          '<td class="fw-bold text-success">' + (h.gioCongQuyDoi != null ? Number(h.gioCongQuyDoi).toFixed(2) : '--') + '</td>' +
          '<td>' + getStatusBadge(h.trangThai) + '</td>' +
          '<td class="text-muted small">' + esc(h.ghiChu || '') + '</td>' +
        '</tr>';
      }).join('');
    } catch (e) {
      console.error(e);
    }
  }


  function initManagerMode() {
    document.getElementById('manager-user-info').innerHTML =
      'Đang trực quản trị: <strong>' + esc(user.hoTen) + '</strong> (' + esc(user.tenDangNhap) + ') · Vai trò: ' + getRoleBadge(user.vaiTro);

    var tabBtns = document.querySelectorAll('.tab-nav-btn');
    tabBtns.forEach(function (btn) {
      btn.addEventListener('click', function () {
        tabBtns.forEach(function (b) { b.classList.remove('active'); });
        this.classList.add('active');

        var targetTabId = this.dataset.tab;
        document.querySelectorAll('.tab-content-panel').forEach(function (panel) {
          panel.classList.add('d-none');
        });
        var targetPanel = document.getElementById(targetTabId);
        if (targetPanel) targetPanel.classList.remove('d-none');

        if (targetTabId === 'tab-supervision') loadManagerSupervisionTab();
        else if (targetTabId === 'tab-schedule') loadManagerScheduleTab();
        else if (targetTabId === 'tab-timesheet') loadManagerTimesheetTab();
        else if (targetTabId === 'tab-directory') loadManagerDirectoryTab();
      });
    });

    var btnChotCa = document.getElementById('btn-chot-ca-btn');
    if (btnChotCa) {
      btnChotCa.addEventListener('click', async function () {
        if (!confirm('Hệ thống sẽ quét toàn bộ các ca đã qua mốc kết thúc + 30 phút:\n- Suất phân ca chưa check-in -> Đánh dấu VẮNG MẶT\n- Nhân viên quên check-out -> Đánh dấu QUÊN CHECK-OUT\n\nBạn có chắc chắn muốn chốt ca ngay bây giờ?')) {
          return;
        }
        try {
          var res = await MocAn.apiFetch('/api/cham-cong/chot-ca', { method: 'POST' });
          showMsg('hr-notice', 'Chốt ca quá hạn thành công: ' + res.soCaDanhDauVang + ' ca Vắng mặt, ' + res.soCaQuenCheckOut + ' ca Quên check-out.', true);
          await loadManagerSupervisionTab();
        } catch (e) {
          showMsg('hr-error', e.message || 'Lỗi khi chốt ca');
        }
      });
    }

    var fDate = document.getElementById('filter-att-date');
    if (fDate) {
      fDate.value = toIsoDate(new Date());
      fDate.addEventListener('change', loadManagerAttendanceTable);
    }
    var fStaff = document.getElementById('filter-att-staff');
    if (fStaff) fStaff.addEventListener('change', loadManagerAttendanceTable);
    var fStatus = document.getElementById('filter-att-status');
    if (fStatus) fStatus.addEventListener('change', loadManagerAttendanceTable);

    var adjustForm = document.getElementById('adjust-form');
    if (adjustForm) {
      adjustForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        hideMsg('adjust-error');
        var id = document.getElementById('adjust-id').value;
        var inVal = document.getElementById('adjust-checkin').value;
        var outVal = document.getElementById('adjust-checkout').value;
        var reasonVal = document.getElementById('adjust-reason').value.trim();

        if (!reasonVal) {
          showMsg('adjust-error', 'Vui lòng nhập lý do điều chỉnh.');
          return;
        }

        try {
          await MocAn.apiFetch('/api/cham-cong/' + id + '/dieu-chinh', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              gioCheckIn: inVal ? inVal + ':00' : null,
              gioCheckOut: outVal ? outVal + ':00' : null,
              lyDoDieuChinh: reasonVal
            })
          });
          if (adjustModal) adjustModal.hide();
          showMsg('hr-notice', 'Đã điều chỉnh chấm công & tự động tính lại giờ công thành công!', true);
          await loadManagerSupervisionTab();
        } catch (x) {
          showMsg('adjust-error', x.message || 'Lỗi khi điều chỉnh');
        }
      });
    }

    var swapForm = document.getElementById('swap-shift-form');
    if (swapForm) {
      swapForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        hideMsg('swap-error');
        var shiftId = document.getElementById('swap-shift-id').value;
        var newStaffId = Number(document.getElementById('swap-new-staff-select').value);
        var reasonVal = document.getElementById('swap-reason-input').value.trim();

        if (!newStaffId) {
          showMsg('swap-error', 'Vui lòng chọn nhân sự thay thế.');
          return;
        }

        try {
          await MocAn.apiFetch('/api/phan-ca/' + shiftId + '/doi-ca', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              maNVMoi: newStaffId,
              lyDoDoiCa: reasonVal
            })
          });
          if (swapShiftModal) swapShiftModal.hide();
          showMsg('hr-notice', 'Đổi ca thành công!', true);
          await loadManagerScheduleTab();
        } catch (x) {
          showMsg('swap-error', x.message || 'Lỗi khi đổi ca');
        }
      });
    }

    var btnPrev = document.getElementById('btn-prev-week');
    if (btnPrev) {
      btnPrev.addEventListener('click', function () {
        currentWeekStart = addDays(currentWeekStart, -7);
        loadManagerScheduleTab();
      });
    }
    var btnNext = document.getElementById('btn-next-week');
    if (btnNext) {
      btnNext.addEventListener('click', function () {
        currentWeekStart = addDays(currentWeekStart, 7);
        loadManagerScheduleTab();
      });
    }

    var btnOpenSchedule = document.getElementById('btn-open-schedule-modal');
    if (btnOpenSchedule) {
      btnOpenSchedule.addEventListener('click', function () {
        populateScheduleModalDropdowns();
        if (scheduleModal) scheduleModal.show();
      });
    }

    var scheduleForm = document.getElementById('schedule-form');
    if (scheduleForm) {
      scheduleForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        hideMsg('schedule-error');
        var staffId = Number(document.getElementById('schedule-staff-select').value);
        var shiftId = Number(document.getElementById('schedule-shift-select').value);
        var dateVal = document.getElementById('schedule-date-input').value;
        var noteVal = document.getElementById('schedule-note-input').value.trim();

        try {
          await MocAn.apiFetch('/api/phan-ca', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              maNV: staffId,
              maCa: shiftId,
              ngayLam: dateVal,
              ghiChu: noteVal
            })
          });
          if (scheduleModal) scheduleModal.hide();
          showMsg('hr-notice', 'Phân ca thành công!', true);
          await loadManagerScheduleTab();
        } catch (x) {
          showMsg('schedule-error', x.message || 'Lỗi khi phân ca');
        }
      });
    }

    var btnPeriodWeek = document.getElementById('btn-period-week');
    var btnPeriodMonth = document.getElementById('btn-period-month');
    var inputFrom = document.getElementById('timesheet-from');
    var inputTo = document.getElementById('timesheet-to');
    var btnLoadTimesheet = document.getElementById('btn-load-timesheet');

    var todayStr = toIsoDate(new Date());
    if (inputTo) inputTo.value = todayStr;
    if (inputFrom) inputFrom.value = currentWeekStart;

    if (btnPeriodWeek) {
      btnPeriodWeek.addEventListener('click', function () {
        btnPeriodWeek.classList.add('active');
        if (btnPeriodMonth) btnPeriodMonth.classList.remove('active');
        inputFrom.value = currentWeekStart;
        inputTo.value = todayStr;
        loadManagerTimesheetTab();
      });
    }
    if (btnPeriodMonth) {
      btnPeriodMonth.addEventListener('click', function () {
        btnPeriodMonth.classList.add('active');
        if (btnPeriodWeek) btnPeriodWeek.classList.remove('active');
        inputFrom.value = todayStr.slice(0, 8) + '01';
        inputTo.value = todayStr;
        loadManagerTimesheetTab();
      });
    }
    if (btnLoadTimesheet) {
      btnLoadTimesheet.addEventListener('click', function () {
        loadManagerTimesheetTab();
      });
    }

    var btnOpenAdd = document.getElementById('btn-open-add-staff');
    if (btnOpenAdd) {
      btnOpenAdd.addEventListener('click', function () {
        if (addStaffModal) addStaffModal.show();
      });
    }

    var addStaffForm = document.getElementById('add-staff-form');
    if (addStaffForm) {
      addStaffForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        hideMsg('add-staff-error');
        try {
          await MocAn.apiFetch('/api/nhan-su/nhan-vien', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              tenDangNhap: document.getElementById('new-staff-username').value.trim(),
              hoTen: document.getElementById('new-staff-fullname').value.trim(),
              matKhau: document.getElementById('new-staff-password').value,
              vaiTro: document.getElementById('new-staff-role').value,
              soDienThoai: document.getElementById('new-staff-phone').value.trim(),
              trangThai: 'DANG_LAM'
            })
          });
          if (addStaffModal) addStaffModal.hide();
          showMsg('hr-notice', 'Tạo tài khoản nhân viên mới thành công!', true);
          staff = await MocAn.apiFetch('/api/nhan-su/nhan-vien').catch(function () { return []; });
          await loadManagerDirectoryTab();
        } catch (x) {
          showMsg('add-staff-error', x.message || 'Lỗi khi tạo nhân viên');
        }
      });
    }

    var editStaffForm = document.getElementById('edit-staff-form');
    if (editStaffForm) {
      editStaffForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        hideMsg('edit-staff-error');
        var id = document.getElementById('edit-staff-id').value;
        var pass = document.getElementById('edit-staff-password').value;

        var payload = {
          tenDangNhap: document.getElementById('edit-staff-username').value.trim(),
          hoTen: document.getElementById('edit-staff-fullname').value.trim(),
          vaiTro: document.getElementById('edit-staff-role').value,
          soDienThoai: document.getElementById('edit-staff-phone').value.trim(),
          trangThai: document.getElementById('edit-staff-status').value
        };
        if (pass && pass.trim()) payload.matKhau = pass.trim();

        try {
          await MocAn.apiFetch('/api/nhan-su/nhan-vien/' + id, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
          });
          if (editStaffModal) editStaffModal.hide();
          showMsg('hr-notice', 'Cập nhật nhân viên thành công!', true);
          staff = await MocAn.apiFetch('/api/nhan-su/nhan-vien').catch(function () { return []; });
          await loadManagerDirectoryTab();
        } catch (x) {
          showMsg('edit-staff-error', x.message || 'Lỗi khi cập nhật nhân viên');
        }
      });
    }

    loadManagerSupervisionTab();
  }

  async function loadManagerSupervisionTab() {
    var myShifts = await MocAn.apiFetch('/api/cham-cong/ca-hom-nay').catch(function () { return []; });
    var myContainer = document.getElementById('manager-today-shifts-list');
    if (myContainer) {
      if (!myShifts || myShifts.length === 0) {
        myContainer.innerHTML = '<div class="col-12 text-muted small py-1">Hôm nay Quản lý không có lịch ca trực cố định (Toàn quyền quản trị).</div>';
      } else {
        myContainer.innerHTML = myShifts.map(function (s) {
          return '<div class="col-12 col-md-6">' +
            '<div class="p-2 rounded border bg-white d-flex align-items-center justify-content-between" style="font-size:0.82rem;">' +
              '<div><strong>' + esc(s.tenCa) + '</strong> (' + String(s.gioBatDau).slice(0, 5) + '-' + String(s.gioKetThuc).slice(0, 5) + ') ' + getStatusBadge(s.trangThai) + '</div>' +
              '<div>' +
                (s.coTheCheckIn
                  ? '<button class="btn btn-moss btn-xs px-2 py-1 btn-mgr-checkin" data-phanca="' + s.maPhanCa + '">Check-in</button>'
                  : (s.coTheCheckOut
                    ? '<button class="btn btn-outline-danger btn-xs px-2 py-1 btn-mgr-checkout" data-id="' + s.maChamCong + '">Check-out</button>'
                    : '<span class="text-muted small">' + esc(s.thongBaoCuaSo || '') + '</span>')) +
              '</div>' +
            '</div>' +
          '</div>';
        }).join('');

        myContainer.querySelectorAll('.btn-mgr-checkin').forEach(function (btn) {
          btn.addEventListener('click', async function () {
            try {
              await MocAn.apiFetch('/api/cham-cong/check-in', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ maPhanCa: Number(this.dataset.phanca) })
              });
              showMsg('hr-notice', 'Quản lý đã check-in thành công!', true);
              loadManagerSupervisionTab();
            } catch (e) {
              showMsg('hr-error', e.message);
            }
          });
        });

        myContainer.querySelectorAll('.btn-mgr-checkout').forEach(function (btn) {
          btn.addEventListener('click', async function () {
            try {
              await MocAn.apiFetch('/api/cham-cong/check-out/' + this.dataset.id, { method: 'POST' });
              showMsg('hr-notice', 'Quản lý đã check-out thành công!', true);
              loadManagerSupervisionTab();
            } catch (e) {
              showMsg('hr-error', e.message);
            }
          });
        });
      }
    }

    var fStaff = document.getElementById('filter-att-staff');
    if (fStaff && fStaff.options.length <= 1 && staff.length > 0) {
      staff.forEach(function (s) {
        var opt = document.createElement('option');
        opt.value = s.maNV;
        opt.textContent = s.hoTen + ' (' + s.vaiTro + ')';
        fStaff.appendChild(opt);
      });
    }

    await loadManagerAttendanceTable();
  }

  async function loadManagerAttendanceTable() {
    var tbody = document.getElementById('manager-attendance-tbody');
    var kpiContainer = document.getElementById('manager-kpi-strip');
    var dVal = document.getElementById('filter-att-date') ? document.getElementById('filter-att-date').value : toIsoDate(new Date());
    var sVal = document.getElementById('filter-att-staff') ? document.getElementById('filter-att-staff').value : '';
    var stVal = document.getElementById('filter-att-status') ? document.getElementById('filter-att-status').value : '';

    var qs = '?tuNgay=' + dVal + '&denNgay=' + dVal;
    if (sVal) qs += '&maNV=' + sVal;
    if (stVal) qs += '&trangThai=' + stVal;

    try {
      var records = await MocAn.apiFetch('/api/cham-cong/lich-su' + qs);

      if (kpiContainer) {
        var workingCount = (records || []).filter(function (r) { return r.gioCheckIn && !r.gioCheckOut && r.trangThai !== 'QUEN_CHECK_OUT' && r.trangThai !== 'VANG'; }).length;
        var exceptionCount = (records || []).filter(function (r) { return r.trangThai === 'QUEN_CHECK_OUT' || r.trangThai === 'VANG'; }).length;

        kpiContainer.innerHTML = [
          ['Tổng lượt chấm công', records.length],
          ['Đang trong ca làm', workingCount],
          ['Cần xử lý ngoại lệ', exceptionCount],
          ['Tổng nhân sự quán', staff.length || 3]
        ].map(function (x) {
          return '<div class="col-6 col-lg-3">' +
            '<div class="moss-card p-3">' +
              '<div style="font-size:0.75rem;color:var(--text-secondary);">' + x[0] + '</div>' +
              '<div class="font-display fw-bold fs-3 mt-1" style="color:var(--accent-primary);">' + x[1] + '</div>' +
            '</div>' +
          '</div>';
        }).join('');
      }

      if (!records || records.length === 0) {
        tbody.innerHTML = '<tr><td colspan="11" class="text-center py-4 text-muted">Không có dữ liệu chấm công cho bộ lọc này.</td></tr>';
        return;
      }

      tbody.innerHTML = records.map(function (r) {
        var n = r.nhanVien || {};
        var c = r.caLam || {};
        var isWorking = r.gioCheckIn && !r.gioCheckOut && r.trangThai !== 'QUEN_CHECK_OUT' && r.trangThai !== 'VANG';

        var auditInfo = '';
        if (r.nguoiDieuChinh) {
          auditInfo = '<div class="text-danger small" title="' + esc(r.lyDoDieuChinh || '') + '"><i class="bi bi-shield-check me-1"></i>Điều chỉnh bởi: ' + esc(r.nguoiDieuChinh.hoTen || r.nguoiDieuChinh.tenDangNhap) + '<br><span class="text-muted">' + esc(r.lyDoDieuChinh) + '</span></div>';
        }

        var actions = [];
        if (isWorking) {
          actions.push('<button class="btn btn-xs btn-outline-danger py-1 px-2 btn-mgr-force-checkout" data-id="' + r.maChamCong + '"><i class="bi bi-box-arrow-right me-1"></i>Check-out hộ</button>');
        }
        actions.push('<button class="btn btn-xs btn-outline-primary py-1 px-2 btn-open-adjust" data-record=\'' + JSON.stringify(r).replace(/'/g, '&#39;') + '\'><i class="bi bi-pencil-square me-1"></i>Điều chỉnh</button>');

        return '<tr>' +
          '<td><div class="fw-semibold">' + esc(n.hoTen) + '</div><small class="text-muted">' + esc(n.tenDangNhap) + '</small></td>' +
          '<td>' + getRoleBadge(n.vaiTro) + '</td>' +
          '<td>' + esc(c.tenCa) + ' <small class="text-muted">(x' + (c.heSoLuong || 1.0) + ')</small></td>' +
          '<td class="font-mono">' + esc(r.ngayLam) + '</td>' +
          '<td class="font-mono">' + (r.gioCheckIn ? MocAn.formatTimeShort(r.gioCheckIn) : '<span class="text-muted">--:--</span>') +
            (r.soPhutTre > 0 ? ' <small class="text-danger">+' + r.soPhutTre + 'p</small>' : '') + '</td>' +
          '<td class="font-mono">' + (r.gioCheckOut ? MocAn.formatTimeShort(r.gioCheckOut) : (isWorking ? '<span class="text-primary fw-medium">Đang làm</span>' : '<span class="text-muted">--:--</span>')) +
            (r.soPhutVeSom > 0 ? ' <small class="text-warning">-' + r.soPhutVeSom + 'p</small>' : '') + '</td>' +
          '<td class="font-mono">' + (r.soGioLam != null ? Number(r.soGioLam).toFixed(2) + ' h' : '--') + '</td>' +
          '<td class="font-mono fw-bold text-success">' + (r.gioCongQuyDoi != null ? Number(r.gioCongQuyDoi).toFixed(2) : '--') + '</td>' +
          '<td>' + getStatusBadge(r.trangThai) + '</td>' +
          '<td>' + auditInfo + '</td>' +
          '<td class="text-end">' +
            '<div class="d-flex justify-content-end gap-1">' + actions.join('') + '</div>' +
          '</td>' +
        '</tr>';
      }).join('');

      tbody.querySelectorAll('.btn-mgr-force-checkout').forEach(function (btn) {
        btn.addEventListener('click', async function () {
          var id = this.dataset.id;
          if (!confirm('Bạn có chắc chắn muốn Check-out hộ cho nhân viên này?')) return;
          try {
            await MocAn.apiFetch('/api/cham-cong/check-out/' + id, { method: 'POST' });
            showMsg('hr-notice', 'Check-out hộ thành công!', true);
            loadManagerAttendanceTable();
          } catch (e) {
            showMsg('hr-error', e.message);
          }
        });
      });

      tbody.querySelectorAll('.btn-open-adjust').forEach(function (btn) {
        btn.addEventListener('click', function () {
          var r = JSON.parse(this.dataset.record);
          document.getElementById('adjust-id').value = r.maChamCong;
          document.getElementById('adjust-staff-text').textContent = (r.nhanVien ? r.nhanVien.hoTen : 'NV') + ' (' + (r.nhanVien ? r.nhanVien.vaiTro : '') + ')';
          document.getElementById('adjust-shift-text').textContent = (r.caLam ? r.caLam.tenCa : 'Ca') + ' (' + r.ngayLam + ')';
          document.getElementById('adjust-old-status-badge').innerHTML = getStatusBadge(r.trangThai);

          document.getElementById('adjust-checkin').value = r.gioCheckIn ? r.gioCheckIn.slice(0, 16) : '';
          document.getElementById('adjust-checkout').value = r.gioCheckOut ? r.gioCheckOut.slice(0, 16) : '';
          document.getElementById('adjust-reason').value = r.lyDoDieuChinh || '';

          hideMsg('adjust-error');
          if (adjustModal) adjustModal.show();
        });
      });
    } catch (e) {
      tbody.innerHTML = '<tr><td colspan="11" class="text-danger text-center py-3">Lỗi tải dữ liệu chấm công.</td></tr>';
    }
  }

  async function loadManagerScheduleTab() {
    var rangeLabel = document.getElementById('schedule-week-range-label');
    var endWeek = addDays(currentWeekStart, 6);
    if (rangeLabel) rangeLabel.textContent = currentWeekStart + ' → ' + endWeek;

    var dayNames = ['Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'Chủ nhật'];
    for (var i = 0; i < 7; i++) {
      var d = addDays(currentWeekStart, i);
      var th = document.getElementById('th-day-0' ? 'th-day-' + i : null);
      if (th) th.innerHTML = dayNames[i] + '<br><span class="fw-normal font-mono text-muted" style="font-size:0.75rem;">' + d.slice(5) + '</span>';
    }

    var tbody = document.getElementById('schedule-matrix-tbody');
    try {
      var scheduleData = await MocAn.apiFetch('/api/phan-ca/tuan?ngayBatDau=' + currentWeekStart);

      tbody.innerHTML = shifts.map(function (sh) {
        var rowCells = '';
        for (var col = 0; col < 7; col++) {
          var targetDate = addDays(currentWeekStart, col);
          var cellShifts = (scheduleData || []).filter(function (x) {
            return x.ngayLam === targetDate && ((x.caLam && x.caLam.maCa === sh.maCa) || x.maCa === sh.maCa);
          });

          var isMissingCashier = cellShifts.some(function (x) { return x.thieuThuNgan === true; });
          var missingCashierBadge = isMissingCashier
            ? '<div class="badge badge-amber-warning mb-1 w-100 text-center"><i class="bi bi-exclamation-triangle-fill me-1"></i>Thiếu Thu ngân</div>'
            : '';

          var cellContent = cellShifts.map(function (item) {
            var nv = item.nhanVien || {};
            var canSwap = !item.daChamCong;

            var swapBtn = canSwap
              ? '<button class="btn btn-xs btn-outline-primary py-0 px-1 btn-open-swap-shift" data-shift=\'' + JSON.stringify(item).replace(/'/g, '&#39;') + '\' title="Đổi người trực ca đột xuất"><i class="bi bi-arrow-left-right"></i> Đổi ca</button>'
              : '<span class="text-muted" style="font-size:0.7rem;"><i class="bi bi-check2"></i> Đã chấm công</span>';

            var delBtn = canSwap
              ? '<button class="btn btn-xs btn-outline-danger py-0 px-1 btn-delete-shift" data-id="' + item.maPhanCa + '" title="Xóa suất phân ca"><i class="bi bi-trash"></i></button>'
              : '';

            return '<div class="shift-assignment-item mb-1">' +
              '<div class="d-flex align-items-center justify-content-between">' +
                '<span class="fw-semibold text-dark text-truncate">' + esc(nv.hoTen || item.hoTen) + '</span>' +
                getRoleBadge(nv.vaiTro || item.vaiTro) +
              '</div>' +
              (item.ghiChu ? '<div class="text-muted text-truncate" style="font-size:0.7rem;">' + esc(item.ghiChu) + '</div>' : '') +
              '<div class="d-flex justify-content-between align-items-center mt-1 pt-1 border-top" style="font-size:0.72rem;">' +
                swapBtn + delBtn +
              '</div>' +
            '</div>';
          }).join('');

          rowCells += '<td class="' + (isMissingCashier ? 'cell-missing-cashier' : '') + '" style="vertical-align:top;width:14%;">' +
            '<div class="schedule-cell ' + (isMissingCashier ? 'cell-missing-cashier' : '') + '">' +
              missingCashierBadge +
              cellContent +
            '</div>' +
          '</td>';
        }

        return '<tr>' +
          '<td class="align-middle bg-light fw-bold text-center">' +
            esc(sh.tenCa) + '<br>' +
            '<span class="font-mono text-muted fw-normal" style="font-size:0.75rem;">' + String(sh.gioBatDau).slice(0, 5) + ' - ' + String(sh.gioKetThuc).slice(0, 5) + '</span><br>' +
            (sh.heSoLuong > 1.0 ? '<span class="badge bg-warning text-dark mt-1">Hệ số x' + sh.heSoLuong + '</span>' : '') +
          '</td>' +
          rowCells +
        '</tr>';
      }).join('');

      tbody.querySelectorAll('.btn-open-swap-shift').forEach(function (btn) {
        btn.addEventListener('click', function () {
          var s = JSON.parse(this.dataset.shift);
          document.getElementById('swap-shift-id').value = s.maPhanCa;
          var nv = s.nhanVien || {};
          var c = s.caLam || {};
          document.getElementById('swap-shift-info-text').textContent = (c.tenCa || s.tenCa) + ' ngày ' + s.ngayLam;
          document.getElementById('swap-current-staff-text').textContent = (nv.hoTen || s.hoTen) + ' (' + (nv.vaiTro || s.vaiTro) + ')';

          var select = document.getElementById('swap-new-staff-select');
          select.innerHTML = '<option value="">-- Chọn nhân viên thay thế --</option>';
          staff.filter(function (st) { return st.trangThai === 'DANG_LAM' && st.maNV !== (nv.maNV || s.maNV); }).forEach(function (st) {
            var opt = document.createElement('option');
            opt.value = st.maNV;
            opt.textContent = st.hoTen + ' (' + st.vaiTro + ')';
            select.appendChild(opt);
          });

          document.getElementById('swap-reason-input').value = '';
          hideMsg('swap-error');
          if (swapShiftModal) swapShiftModal.show();
        });
      });

      tbody.querySelectorAll('.btn-delete-shift').forEach(function (btn) {
        btn.addEventListener('click', async function () {
          var id = this.dataset.id;
          if (!confirm('Bạn có chắc chắn muốn xóa suất phân ca này?')) return;
          try {
            await MocAn.apiFetch('/api/phan-ca/' + id, { method: 'DELETE' });
            showMsg('hr-notice', 'Xóa suất phân ca thành công!', true);
            loadManagerScheduleTab();
          } catch (e) {
            showMsg('hr-error', e.message || 'Lỗi khi xóa phân ca');
          }
        });
      });
    } catch (e) {
      tbody.innerHTML = '<tr><td colspan="8" class="text-danger text-center py-3">Lỗi tải dữ liệu lịch phân ca.</td></tr>';
    }
  }

  function populateScheduleModalDropdowns() {
    var staffSelect = document.getElementById('schedule-staff-select');
    var shiftSelect = document.getElementById('schedule-shift-select');
    var dateInput = document.getElementById('schedule-date-input');

    if (staffSelect) {
      staffSelect.innerHTML = '<option value="">-- Chọn nhân viên --</option>';
      staff.filter(function (s) { return s.trangThai === 'DANG_LAM'; }).forEach(function (s) {
        var opt = document.createElement('option');
        opt.value = s.maNV;
        opt.textContent = s.hoTen + ' (' + s.vaiTro + ')';
        staffSelect.appendChild(opt);
      });
    }

    if (shiftSelect) {
      shiftSelect.innerHTML = '<option value="">-- Chọn ca làm --</option>';
      shifts.forEach(function (sh) {
        var opt = document.createElement('option');
        opt.value = sh.maCa;
        opt.textContent = sh.tenCa + ' (' + String(sh.gioBatDau).slice(0, 5) + ' - ' + String(sh.gioKetThuc).slice(0, 5) + ')';
        shiftSelect.appendChild(opt);
      });
    }

    if (dateInput) {
      var today = toIsoDate(new Date());
      dateInput.value = today;
      dateInput.min = today;
    }
  }

  async function loadManagerTimesheetTab() {
    var from = document.getElementById('timesheet-from').value;
    var to = document.getElementById('timesheet-to').value;
    var tbody = document.getElementById('timesheet-tbody');
    var kpiStrip = document.getElementById('timesheet-kpi-strip');

    if (!from || !to) return;

    try {
      var data = await MocAn.apiFetch('/api/cham-cong/bang-cong?tuNgay=' + from + '&denNgay=' + to);

      var totalWorkedHours = 0;
      var totalConvertedHours = 0;
      var totalOnTime = 0;
      var totalViolations = 0;

      data.forEach(function (d) {
        totalWorkedHours += (d.tongGioThucTe || 0);
        totalConvertedHours += (d.tongGioQuyDoi || 0);
        totalOnTime += (d.soCaDungGio || 0);
        totalViolations += (d.soCaDiTre + d.soCaVeSom + d.soCaVang + d.soCaQuenCheckOut);
      });

      if (kpiStrip) {
        kpiStrip.innerHTML = [
          ['Tổng giờ thực tế toàn quán', totalWorkedHours.toFixed(2) + ' h'],
          ['Tổng giờ công quy đổi hệ số', totalConvertedHours.toFixed(2) + ' công'],
          ['Tổng lượt đúng giờ', totalOnTime],
          ['Tổng lượt vi phạm giờ/vắng', totalViolations]
        ].map(function (x) {
          return '<div class="col-6 col-lg-3">' +
            '<div class="moss-card p-3 text-center">' +
              '<div style="font-size:0.75rem;color:var(--text-secondary);">' + x[0] + '</div>' +
              '<div class="font-display fw-bold fs-3 mt-1" style="color:var(--accent-primary);">' + x[1] + '</div>' +
            '</div>' +
          '</div>';
        }).join('');
      }

      if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="12" class="text-center py-4 text-muted">Chưa có dữ liệu bảng công trong khoảng thời gian này.</td></tr>';
        return;
      }

      tbody.innerHTML = data.map(function (d) {
        return '<tr>' +
          '<td class="font-mono">' + d.maNV + '</td>' +
          '<td class="fw-semibold">' + esc(d.hoTen) + '</td>' +
          '<td><code>' + esc(d.tenDangNhap) + '</code></td>' +
          '<td>' + getRoleBadge(d.vaiTro) + '</td>' +
          '<td class="text-center fw-medium">' + d.tongSoCa + '</td>' +
          '<td class="text-center text-success">' + d.soCaDungGio + '</td>' +
          '<td class="text-center ' + (d.soCaDiTre > 0 ? 'text-danger fw-bold' : 'text-muted') + '">' + d.soCaDiTre + '</td>' +
          '<td class="text-center ' + (d.soCaVeSom > 0 ? 'text-warning fw-bold' : 'text-muted') + '">' + d.soCaVeSom + '</td>' +
          '<td class="text-center ' + (d.soCaVang > 0 ? 'text-danger fw-bold' : 'text-muted') + '">' + d.soCaVang + '</td>' +
          '<td class="text-center ' + (d.soCaQuenCheckOut > 0 ? 'text-warning fw-bold' : 'text-muted') + '">' + d.soCaQuenCheckOut + '</td>' +
          '<td class="text-end font-mono">' + (d.tongGioThucTe || 0).toFixed(2) + ' h</td>' +
          '<td class="text-end font-mono fw-bold text-success fs-6">' + (d.tongGioQuyDoi || 0).toFixed(2) + '</td>' +
        '</tr>';
      }).join('');
    } catch (e) {
      tbody.innerHTML = '<tr><td colspan="12" class="text-danger text-center py-3">Lỗi tải bảng công.</td></tr>';
    }
  }

  async function loadManagerDirectoryTab() {
    var tbody = document.getElementById('directory-tbody');
    try {
      staff = await MocAn.apiFetch('/api/nhan-su/nhan-vien');
      if (!staff || staff.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">Chưa có nhân viên.</td></tr>';
        return;
      }

      tbody.innerHTML = staff.map(function (s) {
        var isSelf = (s.maNV === user.maNV);
        var statusLabel = (s.trangThai === 'DANG_LAM')
          ? '<span class="text-success fw-medium"><i class="bi bi-check-circle me-1"></i>Đang làm</span>'
          : '<span class="text-danger fw-medium"><i class="bi bi-x-circle me-1"></i>Đã nghỉ</span>';

        return '<tr>' +
          '<td class="font-mono">' + s.maNV + '</td>' +
          '<td class="fw-semibold">' + esc(s.hoTen) + (isSelf ? ' <span class="badge bg-secondary ms-1">Bạn</span>' : '') + '</td>' +
          '<td><code>' + esc(s.tenDangNhap) + '</code></td>' +
          '<td>' + getRoleBadge(s.vaiTro) + '</td>' +
          '<td class="font-mono">' + esc(s.soDienThoai || '--') + '</td>' +
          '<td>' + statusLabel + '</td>' +
          '<td class="text-end">' +
            '<button class="btn btn-xs btn-outline-secondary py-1 px-2 btn-open-edit-staff me-1" data-staff=\'' + JSON.stringify(s).replace(/'/g, '&#39;') + '\'><i class="bi bi-pencil me-1"></i>Sửa</button>' +
            (!isSelf && s.trangThai === 'DANG_LAM'
              ? '<button class="btn btn-xs btn-outline-danger py-1 px-2 btn-stop-staff" data-id="' + s.maNV + '"><i class="bi bi-person-x me-1"></i>Nghỉ việc</button>'
              : '') +
          '</td>' +
        '</tr>';
      }).join('');

      tbody.querySelectorAll('.btn-open-edit-staff').forEach(function (btn) {
        btn.addEventListener('click', function () {
          var s = JSON.parse(this.dataset.staff);
          document.getElementById('edit-staff-id').value = s.maNV;
          document.getElementById('edit-staff-username').value = s.tenDangNhap;
          document.getElementById('edit-staff-fullname').value = s.hoTen;
          document.getElementById('edit-staff-password').value = '';
          document.getElementById('edit-staff-role').value = s.vaiTro;
          document.getElementById('edit-staff-phone').value = s.soDienThoai || '';
          document.getElementById('edit-staff-status').value = s.trangThai;

          hideMsg('edit-staff-error');
          if (editStaffModal) editStaffModal.show();
        });
      });

      tbody.querySelectorAll('.btn-stop-staff').forEach(function (btn) {
        btn.addEventListener('click', async function () {
          var id = this.dataset.id;
          if (!confirm('Bạn có chắc chắn muốn đánh dấu nhân viên này đã nghỉ việc?')) return;
          try {
            await MocAn.apiFetch('/api/nhan-su/nhan-vien/' + id, { method: 'DELETE' });
            showMsg('hr-notice', 'Đã chuyển trạng thái nhân viên sang Đã nghỉ việc!', true);
            loadManagerDirectoryTab();
          } catch (e) {
            showMsg('hr-error', e.message);
          }
        });
      });
    } catch (e) {
      tbody.innerHTML = '<tr><td colspan="7" class="text-danger text-center py-3">Lỗi tải danh sách nhân viên.</td></tr>';
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();

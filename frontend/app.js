const state = {
  users: [],
  selectedUserId: null,
  profile: null,
  periods: [],
  predictions: null,
  calendarMonth: new Date().getMonth(),
  calendarYear: new Date().getFullYear(),
};

const API = (localStorage.getItem('api_base') || 'http://localhost:8080') + '/v1';

const $ = id => document.getElementById(id);
const userSelect = $('user-select');
const onboarding = $('onboarding');
const dashboard = $('dashboard');
const calBody = $('cal-body');
const calLabel = $('cal-label');
const toast = $('toast');
let toastTimer = null;

function iso(d) { return typeof d === 'string' ? d : d.toISOString().slice(0,10); }
function parseDate(s) { const [y,m,d]=s.split('-').map(Number); return new Date(y,m-1,d); }
function fmtDate(s) { const d = parseDate(s); return d.toLocaleDateString('en-US', {month:'short', day:'numeric', year:'numeric'}); }
function fmtShort(s) { const d = parseDate(s); return d.toLocaleDateString('en-US', {month:'short', day:'numeric'}); }

function showToast(msg, type='success', duration=3000) {
  clearTimeout(toastTimer);
  toast.textContent = msg;
  toast.className = 'toast ' + type;
  toastTimer = setTimeout(() => toast.classList.add('hidden'), duration);
}

async function api(path, opts={}) {
  const url = API + path;
  const res = await fetch(url, {
    headers: { 'Content-Type': 'application/json', ...opts.headers },
    ...opts,
  });
  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (!res.ok) {
    const msg = data?.error?.message || `HTTP ${res.status}`;
    const err = new Error(msg);
    err.status = res.status;
    err.code = data?.error?.code;
    err.data = data;
    throw err;
  }
  return data;
}

async function loadUsers() {
  const users = await api('/users');
  state.users = users;
  userSelect.innerHTML = '<option value="">— Select user —</option>'
    + users.map(u => `<option value="${u.id}">${u.displayName}</option>`).join('');
  if (users.length > 0) {
    userSelect.value = users[0].id;
    await onUserChange(users[0].id);
  }
}

userSelect.addEventListener('change', () => {
  const id = parseInt(userSelect.value);
  if (id) onUserChange(id);
});

async function onUserChange(userId) {
  state.selectedUserId = userId;
  state.periods = [];
  state.predictions = null;
  try {
    state.profile = await api('/profile?userId=' + userId);
    if (!state.profile.onboardingCompleted) {
      showOnboarding();
    } else {
      showDashboard();
    }
  } catch (err) {
    if (err.status === 404) {
      state.profile = null;
      showOnboarding();
    } else {
      showToast(err.message, 'error');
    }
  }
}

function showOnboarding() {
  onboarding.classList.remove('hidden');
  dashboard.classList.add('hidden');
  const d = new Date(); d.setDate(d.getDate() - 14);
  $('last-period').value = iso(d);
}

$('onboarding-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const body = {
    userId: state.selectedUserId,
    typicalCycleLengthDays: parseInt($('cycle-length').value),
    typicalPeriodDurationDays: parseInt($('period-duration').value),
    lastPeriodStartDate: $('last-period').value,
  };
  try {
    state.profile = await api('/profile', {
      method: 'PUT',
      body: JSON.stringify(body),
    });
    showToast('Profile saved!');
    showDashboard();
  } catch (err) {
    showToast(err.message, 'error');
  }
});

async function showDashboard() {
  onboarding.classList.add('hidden');
  dashboard.classList.remove('hidden');
  state.calendarMonth = new Date().getMonth();
  state.calendarYear = new Date().getFullYear();
  await Promise.all([loadPeriods(), loadPredictions()]);
  renderCalendar();
}

async function loadPeriods() {
  if (!state.selectedUserId) return;
  try {
    const resp = await api('/periods?userId=' + state.selectedUserId + '&size=100');
    state.periods = resp.data || [];
    renderPeriodHistory();
  } catch (err) {
    console.error('Failed to load periods:', err);
  }
}

async function loadPredictions() {
  if (!state.selectedUserId) return;
  try {
    state.predictions = await api('/predictions?userId=' + state.selectedUserId);
    renderPredictions();
  } catch (err) {
    state.predictions = null;
    renderPredictions();
  }
}

function renderCalendar() {
  const { calendarMonth: m, calendarYear: y } = state;
  calLabel.textContent = new Date(y, m).toLocaleDateString('en-US', { month:'long', year:'numeric' });

  const periodDays = new Set();
  const predictedDays = new Set();
  const fertileDays = new Set();

  for (const p of state.periods) {
    if (!p.startDate) continue;
    const end = p.endDate || p.startDate;
    let cur = parseDate(p.startDate);
    const endD = parseDate(end);
    while (cur <= endD) {
      periodDays.add(iso(cur));
      cur.setDate(cur.getDate() + 1);
    }
  }

  if (state.predictions) {
    const next = state.predictions.nextPeriod;
    if (next?.confidenceBand) {
      const start = parseDate(next.confidenceBand.earliest);
      const end = parseDate(next.confidenceBand.latest);
      let cur = new Date(start);
      while (cur <= end) {
        predictedDays.add(iso(cur));
        cur.setDate(cur.getDate() + 1);
      }
    }
    const ovu = state.predictions.ovulation;
    if (ovu?.fertileWindow) {
      const start = parseDate(ovu.fertileWindow.start);
      const end = parseDate(ovu.fertileWindow.end);
      let cur = new Date(start);
      while (cur <= end) {
        fertileDays.add(iso(cur));
        cur.setDate(cur.getDate() + 1);
      }
    }
  }

  const first = new Date(y, m, 1);
  const startDay = first.getDay();
  const daysInMonth = new Date(y, m + 1, 0).getDate();
  const today = iso(new Date());

  let html = '';
  let row = '<tr>';
  for (let i = 0; i < startDay; i++) row += '<td></td>';

  for (let d = 1; d <= daysInMonth; d++) {
    const date = iso(new Date(y, m, d));
    let cls = '';
    if (date === today) cls += ' today';
    if (periodDays.has(date)) cls += ' period-day';
    else if (predictedDays.has(date)) cls += ' predicted-day';
    if (fertileDays.has(date) && !periodDays.has(date)) cls += ' fertile-day';
    row += `<td class="${cls.trim()}">${d}</td>`;
    if ((startDay + d) % 7 === 0 && d < daysInMonth) {
      row += '</tr><tr>';
    }
  }
  const total = startDay + daysInMonth;
  const rem = 7 - (total % 7);
  if (rem < 7) for (let i = 0; i < rem; i++) row += '<td></td>';
  row += '</tr>';
  calBody.innerHTML = row;
}

$('cal-prev').addEventListener('click', () => {
  state.calendarMonth--;
  if (state.calendarMonth < 0) { state.calendarMonth = 11; state.calendarYear--; }
  renderCalendar();
});
$('cal-next').addEventListener('click', () => {
  state.calendarMonth++;
  if (state.calendarMonth > 11) { state.calendarMonth = 0; state.calendarYear++; }
  renderCalendar();
});

function renderPeriodHistory() {
  const el = $('period-history');
  if (!state.periods.length) {
    el.innerHTML = '<p class="empty-state">No periods logged yet.</p>';
    return;
  }
  const sorted = [...state.periods].sort((a,b) => b.startDate.localeCompare(a.startDate));
  el.innerHTML = sorted.map(p => {
    const end = p.endDate || '—';
    const cycle = p.cycleLengthDays != null ? `${p.cycleLengthDays}d cycle` : '';
    const intensity = p.flowIntensity ? `Flow: ${p.flowIntensity}/5` : '';
    const meta = [cycle, intensity].filter(Boolean).join(' · ');
    return `<div class="period-row">
      <div class="pr-dates">${fmtDate(p.startDate)}${p.endDate ? ' – ' + fmtDate(p.endDate) : ''}</div>
      <div class="pr-meta">${meta}</div>
    </div>`;
  }).join('');
}

$('log-flow').addEventListener('click', (e) => {
  if (!e.target.dataset.value) return;
  $('log-flow').querySelectorAll('button').forEach(b => b.classList.remove('selected'));
  e.target.classList.add('selected');
});

$('log-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const fb = $('log-feedback');
  fb.classList.add('hidden');

  const selectedFlow = $('log-flow').querySelector('.selected');
  const body = {
    userId: state.selectedUserId,
    startDate: $('log-start').value,
    endDate: $('log-end').value || null,
    flowIntensity: selectedFlow ? parseInt(selectedFlow.dataset.value) : null,
    notes: $('log-notes').value || null,
  };

  try {
    await api('/periods', { method: 'POST', body: JSON.stringify(body) });
    showToast('Period logged!');
    $('log-form').reset();
    $('log-flow').querySelectorAll('button').forEach(b => b.classList.remove('selected'));
    fb.classList.add('hidden');
    await Promise.all([loadPeriods(), loadPredictions()]);
    renderCalendar();
  } catch (err) {
    showToast(err.message, 'error');
  }
});

$('log-start').valueAsDate = new Date();

function renderPredictions() {
  const p = state.predictions;
  const content = $('predictions-content');
  const empty = $('predictions-empty');

  if (!p) {
    content.classList.add('hidden');
    empty.classList.remove('hidden');
    return;
  }
  content.classList.remove('hidden');
  empty.classList.add('hidden');

  $('pred-date').textContent = p.nextPeriod ? fmtDate(p.nextPeriod.predictedStartDate) : '—';
  if (p.nextPeriod?.confidenceBand) {
    $('pred-band').textContent = fmtShort(p.nextPeriod.confidenceBand.earliest)
      + ' – ' + fmtShort(p.nextPeriod.confidenceBand.latest);
  } else {
    $('pred-band').textContent = '';
  }

  $('ovu-date').textContent = p.ovulation ? fmtDate(p.ovulation.predictedDate) : '—';
  if (p.ovulation?.fertileWindow) {
    $('ovu-window').textContent = 'Fertile: ' + fmtShort(p.ovulation.fertileWindow.start)
      + ' – ' + fmtShort(p.ovulation.fertileWindow.end);
  } else {
    $('ovu-window').textContent = '';
  }

  $('conf-note').textContent = p.explanation?.confidenceNote || '';

  if (p.explanation) {
    const e = p.explanation;
    const rows = [
      ['Method', e.method || '—'],
      ['Data source', e.dataSource || '—'],
      ['Sample size', String(e.sampleSize ?? '—')],
      ['Avg cycle length', e.avgCycleLengthDays ? `${e.avgCycleLengthDays.toFixed(1)} days` : '—'],
      ['Std deviation', e.cycleLengthStdDev != null ? `${e.cycleLengthStdDev.toFixed(1)} days` : '—'],
      ['Onboarding baseline', `${e.onboardingBaselineDays} days`],
      ['Luteal phase', `${e.lutealPhaseDays} days`],
      ['Last period start', e.lastPeriodStart ? fmtDate(e.lastPeriodStart) : '—'],
    ];
    $('explanation-body').innerHTML = '<div class="exp-body">'
      + rows.map(r => `<div class="exp-row"><span class="exp-label">${r[0]}</span><span class="exp-value">${r[1]}</span></div>`).join('')
      + '</div>';
  }
}

loadUsers().catch(err => {
  showToast('Failed to connect to backend: ' + err.message, 'error');
  userSelect.innerHTML = '<option value="">Connection error</option>';
});

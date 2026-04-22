/* ============================================
   Dashboard — Checklist App
   - Sincronizado com style.css e dashboard.html
   ============================================ */

let state = {
  usuario: null,
  categorias: [],
  tags: [],
  tarefas: [],
  tarefa_tags: [],
  logs: []
};

let currentCategoryFilter = null;
let currentTagFilter = null;
let currentSearch = '';

// ---------- Auth & Data ----------
async function checkAuth() {
  try {
    const response = await fetch('/api/auth/me');
    if (response.ok) {
      state.usuario = await response.json();
      document.getElementById('user-name').textContent = state.usuario.nome;
      document.getElementById('user-email').textContent = state.usuario.email;
      return true;
    }
  } catch (e) { console.error('Erro ao verificar auth', e); }
  window.location.href = 'index.html';
  return false;
}

async function loadData() {
  try {
    const endpoints = ['/api/categorias', '/api/tags', '/api/tarefas', '/api/logs', '/api/tarefa-tags'];
    const results = await Promise.all(endpoints.map(url => fetch(url)));
    
    const [cats, tags, tasks, logs, ttags] = await Promise.all(results.map(r => r.ok ? r.json() : []));
    
    state.categorias = cats || [];
    state.tags = tags || [];
    state.tarefas = tasks || [];
    state.logs = logs || [];
    state.tarefa_tags = ttags || [];

    // Normalização Global de IDs
    state.tarefas.forEach(t => { 
      if (!t.id_tarefa && t.id) t.id_tarefa = t.id; 
      if (!t.id && t.id_tarefa) t.id = t.id_tarefa; 
    });

    render();
  } catch (e) {
    console.error('Erro ao carregar dados:', e);
  }
}

// ---------- UI Rendering ----------
function render() {
  renderCategories();
  renderTagsCloud();
  renderTarefas();
  renderStats();
  renderLogs();
}

function renderCategories() {
  const container = document.getElementById('categories-list');
  const select = document.getElementById('task-category');
  container.innerHTML = '';
  select.innerHTML = '<option value="">Selecionar categoria...</option>';

  // "Todas"
  const liAll = document.createElement('li');
  liAll.className = 'filter-item' + (currentCategoryFilter === null ? ' filter-item--active' : '');
  liAll.innerHTML = `
    <button type="button" class="filter-item__btn">
      <span class="filter-item__dot"></span>
      <span class="filter-item__label">Todas as tarefas</span>
      <span class="filter-item__count">${state.tarefas.length}</span>
    </button>`;
  liAll.onclick = () => { currentCategoryFilter = null; render(); };
  container.appendChild(liAll);

  state.categorias.forEach(c => {
    const id = c.id_categoria || c.id;
    const count = state.tarefas.filter(t => t.id_categoria === id).length;
    const li = document.createElement('li');
    li.className = 'filter-item' + (currentCategoryFilter === id ? ' filter-item--active' : '');
    li.innerHTML = `
      <button type="button" class="filter-item__btn">
        <span class="filter-item__dot" style="background:${colorFromId(id)}"></span>
        <span class="filter-item__label">${escapeHtml(c.nome)}</span>
        <span class="filter-item__count">${count}</span>
      </button>
      <button type="button" class="filter-item__delete" data-action="delete">✕</button>`;
    
    li.querySelector('.filter-item__btn').onclick = () => {
      currentCategoryFilter = id;
      render();
    };
    li.querySelector('.filter-item__delete').onclick = (e) => {
      e.stopPropagation();
      deleteCategoria(id);
    };
    container.appendChild(li);

    const opt = document.createElement('option');
    opt.value = id;
    opt.textContent = c.nome;
    select.appendChild(opt);
  });
}

function renderTagsCloud() {
  const container = document.getElementById('tags-cloud');
  const picker = document.getElementById('task-tags-picker');
  container.innerHTML = '';
  picker.innerHTML = '';

  // "Todas"
  const btnAll = document.createElement('button');
  btnAll.className = 'tag-chip' + (currentTagFilter === null ? ' tag-chip--active' : '');
  btnAll.innerHTML = `<span class="tag-chip__btn">#todas</span>`;
  btnAll.onclick = () => { currentTagFilter = null; render(); };
  container.appendChild(btnAll);

  state.tags.forEach(t => {
    const id = t.id_tag || t.id;
    const chip = document.createElement('div');
    chip.className = 'tag-chip' + (currentTagFilter === id ? ' tag-chip--active' : '');
    chip.innerHTML = `
      <button class="tag-chip__btn">#${escapeHtml(t.nome)}</button>
      <button class="tag-chip__remove" data-action="delete">×</button>`;
    
    chip.querySelector('.tag-chip__btn').onclick = () => {
      currentTagFilter = id;
      render();
    };
    chip.querySelector('.tag-chip__remove').onclick = (e) => {
      e.stopPropagation();
      deleteTag(id);
    };
    container.appendChild(chip);

    const label = document.createElement('label');
    label.className = 'tag-pick';
    label.innerHTML = `<input type="checkbox" value="${id}"> ${escapeHtml(t.nome)}`;
    picker.appendChild(label);
  });
}

function renderTarefas() {
  const list = document.getElementById('tasks-list');
  const empty = document.getElementById('empty-state');
  list.innerHTML = '';

  let tarefas = state.tarefas.filter(t => t.status !== 'deletado');

  if (currentCategoryFilter !== null) {
    tarefas = tarefas.filter(t => (t.id_categoria || t.id) === currentCategoryFilter);
  }
  if (currentTagFilter !== null) {
    const tids = state.tarefa_tags.filter(tt => (tt.id_tag || tt.idTag) === currentTagFilter).map(tt => tt.id_tarefa || tt.idTarefa);
    tarefas = tarefas.filter(t => tids.includes(t.id_tarefa || t.id));
  }
  if (currentSearch.trim()) {
    const q = currentSearch.toLowerCase();
    tarefas = tarefas.filter(t => t.titulo.toLowerCase().includes(q));
  }

  tarefas.sort((a, b) => {
    if (a.status !== b.status) return a.status === 'pendente' ? -1 : 1;
    return (a.data_vencimento || '9999').localeCompare(b.data_vencimento || '9999');
  });

  if (!tarefas.length) { empty.hidden = false; return; }
  empty.hidden = true;

  tarefas.forEach(t => {
    const tid = t.id_tarefa || t.id;
    const cat = state.categorias.find(c => (c.id_categoria || c.id) === t.id_categoria);
    const ttagIds = state.tarefa_tags.filter(tt => (tt.id_tarefa || tt.idTarefa) === tid).map(tt => tt.id_tag || tt.idTag);
    const ttags = state.tags.filter(tg => ttagIds.includes(tg.id_tag || tg.id));

    const card = document.createElement('article');
    card.className = 'task' + (t.status === 'concluida' ? ' task--done' : '');
    if (isOverdue(t)) card.classList.add('task--overdue');

    card.innerHTML = `
      <button class="task__check" data-action="toggle">
        ${t.status === 'concluida' ? '✓' : ''}
      </button>
      <div class="task__body">
        <div class="task__head">
          <h3 class="task__title">${escapeHtml(t.titulo)}</h3>
          <span class="task__id">#${tid}</span>
        </div>
        <p class="task__desc">${escapeHtml(t.descricao || '')}</p>
        <div class="task__meta">
          ${cat ? `<span class="meta-pill"><span class="meta-pill__dot" style="background:${colorFromId(cat.id_categoria)}"></span>${escapeHtml(cat.nome)}</span>` : ''}
          ${t.data_vencimento ? `<span class="meta-pill meta-pill--muted">📅 ${formatDate(t.data_vencimento)}</span>` : ''}
          ${ttags.map(tg => `<span class="meta-tag">#${escapeHtml(tg.nome)}</span>`).join('')}
        </div>
      </div>
      <div class="task__actions">
        <button class="icon-btn" data-action="edit" title="Editar">✎</button>
        <button class="icon-btn icon-btn--danger" data-action="delete" title="Excluir">🗑</button>
      </div>`;

    card.querySelector('[data-action="toggle"]').onclick = () => toggleTarefa(tid);
    card.querySelector('[data-action="edit"]').onclick = () => openTaskModal(tid);
    card.querySelector('[data-action="delete"]').onclick = () => deleteTarefa(tid);
    list.appendChild(card);
  });
}

function renderStats() {
  const ts = state.tarefas;
  document.getElementById('stat-total').textContent = ts.length;
  document.getElementById('stat-pending').textContent = ts.filter(t => t.status === 'pendente').length;
  document.getElementById('stat-done').textContent = ts.filter(t => t.status === 'concluida').length;
  document.getElementById('stat-overdue').textContent = ts.filter(isOverdue).length;
}

function renderLogs() {
  const list = document.getElementById('logs-list');
  const section = document.getElementById('logs-section');
  if (!state.logs.length) { section.hidden = true; return; }
  section.hidden = false;
  list.innerHTML = '';
  state.logs.slice().reverse().forEach(log => {
    const li = document.createElement('li');
    li.className = 'log';
    li.innerHTML = `
      <div class="log__head"><span class="log__id">LOG #${log.id_log || log.id}</span> <span>${log.data_conclusao}</span></div>
      <p class="log__title">Tarefa #${log.id_tarefa} concluída</p>
    `;
    list.appendChild(li);
  });
}

// ---------- Actions ----------
async function submitTaskForm(e) {
  e.preventDefault();
  const id = document.getElementById('task-id').value;
  const titulo = document.getElementById('task-title').value.trim();
  const descricao = document.getElementById('task-desc').value.trim();
  const id_categoria = Number(document.getElementById('task-category').value) || 0;
  const data_vencimento = document.getElementById('task-due').value;
  const tagIds = Array.from(document.querySelectorAll('#task-tags-picker input:checked')).map(cb => Number(cb.value));

  if (!titulo) return alert('Título obrigatório');

  const payload = { titulo, descricao, id_categoria, data_vencimento, status: 'pendente' };
  const url = id ? `/api/tarefas/${id}` : '/api/tarefas';
  const method = id ? 'PUT' : 'POST';

  const res = await fetch(url, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });

  if (res.ok) {
    const saved = await res.json();
    const actualId = id ? Number(id) : (saved.id_tarefa || saved.id);
    for (const tid of tagIds) {
      await fetch('/api/tarefa-tags', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id_tag: tid, id_tarefa: actualId })
      });
    }
    closeModal('task-modal');
    loadData();
  }
}

async function toggleTarefa(id) {
  const t = state.tarefas.find(x => (x.id_tarefa || x.id) === id);
  const status = t.status === 'pendente' ? 'concluida' : 'pendente';
  await fetch(`/api/tarefas/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ ...t, status })
  });
  loadData();
}

async function deleteTarefa(id) {
  if (confirm('Excluir esta tarefa?')) { await fetch(`/api/tarefas/${id}`, { method: 'DELETE' }); loadData(); }
}

async function deleteCategoria(id) {
  if (confirm('Excluir esta categoria?')) {
    const res = await fetch(`/api/categorias/${id}`, { method: 'DELETE' });
    if (res.ok) loadData(); else alert('Erro ao excluir (verifique se há tarefas)');
  }
}

async function deleteTag(id) {
  if (confirm('Excluir esta tag?')) {
    const res = await fetch(`/api/tags/${id}`, { method: 'DELETE' });
    if (res.ok) loadData(); else alert('Erro ao excluir');
  }
}

// ---------- Helpers ----------
function openModal(id) { const m = document.getElementById(id); m.classList.add('modal--open'); m.hidden = false; }
function closeModal(id) { const m = document.getElementById(id); m.classList.remove('modal--open'); m.hidden = true; }
function escapeHtml(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
function formatDate(iso) { if (!iso) return ''; const [y,m,d] = iso.split('-'); return `${d}/${m}`; }
function isOverdue(t) { 
  if (t.status === 'concluida' || !t.data_vencimento) return false;
  return new Date(t.data_vencimento + 'T23:59:59').getTime() < Date.now();
}
function colorFromId(id) {
  const colors = ['#5b5bf0', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899'];
  return colors[id % colors.length];
}

// ---------- Event Listeners ----------
document.querySelectorAll('[data-close-modal]').forEach(b => {
  b.onclick = () => closeModal(b.dataset.closeModal);
});

document.getElementById('add-task-btn').onclick = () => {
  document.getElementById('task-form').reset();
  document.getElementById('task-id').value = '';
  document.getElementById('task-modal-title').textContent = 'Nova tarefa';
  openModal('task-modal');
};

function openTaskModal(id) {
  const t = state.tarefas.find(x => (x.id_tarefa || x.id) === id);
  if(!t) return;
  document.getElementById('task-id').value = id;
  document.getElementById('task-title').value = t.titulo;
  document.getElementById('task-desc').value = t.descricao || '';
  document.getElementById('task-category').value = t.id_categoria || '';
  document.getElementById('task-due').value = t.data_vencimento || '';
  document.getElementById('task-modal-title').textContent = `Editar tarefa #${id}`;
  
  const ttagIds = state.tarefa_tags.filter(tt => (tt.id_tarefa || tt.idTarefa) === id).map(tt => tt.id_tag || tt.idTag);
  document.querySelectorAll('#task-tags-picker input').forEach(cb => { cb.checked = ttagIds.includes(Number(cb.value)); });
  
  openModal('task-modal');
}

document.getElementById('task-form').onsubmit = submitTaskForm;
document.getElementById('add-category-btn').onclick = () => { document.getElementById('category-form').reset(); openModal('category-modal'); };
document.getElementById('category-form').onsubmit = async (e) => {
  e.preventDefault();
  const nome = document.getElementById('category-name').value.trim();
  if(!nome) return;
  await fetch('/api/categorias', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ nome }) });
  closeModal('category-modal');
  loadData();
};

document.getElementById('add-tag-btn').onclick = () => { document.getElementById('tag-form').reset(); openModal('tag-modal'); };
document.getElementById('tag-form').onsubmit = async (e) => {
  e.preventDefault();
  const nome = document.getElementById('tag-name').value.trim();
  if(!nome) return;
  await fetch('/api/tags', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ nome }) });
  closeModal('tag-modal');
  loadData();
};

document.getElementById('logout-btn').onclick = async () => { await fetch('/api/auth/logout', { method: 'POST' }); window.location.href = 'index.html'; };
document.getElementById('search-input').oninput = (e) => { currentSearch = e.target.value; renderTarefas(); };
document.getElementById('toggle-logs').onclick = () => { document.getElementById('logs-list').classList.toggle('logs__list--collapsed'); };

async function init() {
  if (await checkAuth()) await loadData();
}

init();

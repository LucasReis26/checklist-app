/* ============================================
   Dashboard — Checklist App
   Mock front-end seguindo o DER do projeto:
   - Usuario, Categoria, Tag, Tarefa, TarefaTag, LogConclusao
   - Persistência via localStorage (substituir por
     chamada ao backend Java/arquivos binários depois)
   ============================================ */

const STORAGE_KEY = 'checklist-app-data';

// ---------- Estado ----------
let state = {
  usuario: null,
  categorias: [],
  tags: [],
  tarefas: [],
  tarefa_tags: [],
  logs: []
};
let currentCategoryFilter = null; // id_categoria ou null
let currentTagFilter = null;      // id_tag ou null
let currentSearch = '';

// ---------- Persistência (Substituída por API) ----------
async function checkAuth() {
  try {
    const response = await fetch('/api/auth/me');
    if (response.ok) {
      state.usuario = await response.json();
      return true;
    }
  } catch (e) { console.error('Erro ao verificar auth', e); }
  window.location.href = 'index.html';
  return false;
}

async function loadData() {
  try {
    const [cats, tags, tasks, logs, ttags] = await Promise.all([
      fetch('/api/categorias').then(r => r.json()),
      fetch('/api/tags').then(r => r.json()),
      fetch('/api/tarefas').then(r => r.json()),
      fetch('/api/logs').then(r => r.json()),
      fetch('/api/tarefa-tags').then(r => r.json())
    ]);
    state.categorias = cats;
    state.tags = tags;
    state.tarefas = tasks;
    state.logs = logs;
    state.tarefa_tags = ttags;
    render();
  } catch (e) {
    showToast('Erro ao carregar dados', 'error');
  }
}

// Removendo funções de mock que não são mais necessárias
// saveState, nextId, addDays...


// ---------- Helpers ----------
function activeCategorias() {
  return (state.categorias || []);
}
function activeTags() {
  return (state.tags || []);
}
function activeTarefas() {
  return (state.tarefas || []);
}
function tagsDeTarefa(idTarefa) {
  if (!state.tarefa_tags) return [];
  const tagIds = state.tarefa_tags
    .filter(tt => tt.id_tarefa === idTarefa)
    .map(tt => tt.id_tag);
  return (state.tags || []).filter(t => tagIds.includes(t.id_tag));
}
function categoriaDeTarefa(idCategoria) {
  return (state.categorias || []).find(c => c.id_categoria === idCategoria);
}

function formatDate(ts) {
  if (!ts) return '';
  // Se for string YYYY-MM-DD
  if (typeof ts === 'string' && ts.includes('-')) {
    const [y, m, d] = ts.split('-');
    return `${d}/${m}`;
  }
  const d = new Date(ts);
  return d.toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' });
}
function formatDateTime(ts) {
  if (!ts) return '';
  return new Date(ts).toLocaleString('pt-BR', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}
function isOverdue(t) {
  if (t.status === 'concluida' || !t.data_vencimento) return false;
  const venc = new Date(t.data_vencimento + 'T23:59:59').getTime();
  return venc < Date.now();
}
function dueLabel(t) {
  if (!t.data_vencimento) return null;
  if (t.status === 'concluida') return 'Concluída';
  const venc = new Date(t.data_vencimento + 'T23:59:59').getTime();
  const days = Math.ceil((venc - Date.now()) / 86400000);
  if (days < 0) return `Atrasada • ${Math.abs(days)}d`;
  if (days === 0) return 'Vence hoje';
  if (days === 1) return 'Vence amanhã';
  return `Em ${days}d`;
}

// ---------- Toast ----------
function showToast(msg, type = 'success') {
  const old = document.querySelector('.toast');
  if (old) old.remove();
  const t = document.createElement('div');
  t.className = `toast toast--${type}`;
  t.textContent = msg;
  document.body.appendChild(t);
  requestAnimationFrame(() => t.classList.add('toast--show'));
  setTimeout(() => {
    t.classList.remove('toast--show');
    setTimeout(() => t.remove(), 300);
  }, 2500);
}

// ---------- Render ----------
function render() {
  if (!state.usuario) return; // Aguarda carregar usuário
  renderUser();
  renderCategorias();
  renderTags();
  renderTarefas();
  renderStats();
  renderLogs();
}

function renderUser() {
  const u = state.usuario;
  if (!u) return;
  document.getElementById('user-name').textContent = u.nome || 'Usuário';
  document.getElementById('user-email').textContent = u.email || '';
  document.getElementById('user-avatar').textContent = (u.nome || 'U').charAt(0).toUpperCase();
}

function renderCategorias() {
  const list = document.getElementById('categories-list');
  const select = document.getElementById('task-category');
  list.innerHTML = '';
  select.innerHTML = '<option value="">Selecione uma categoria</option>';

  // "Todas"
  const all = document.createElement('li');
  all.className = 'filter-item' + (currentCategoryFilter === null ? ' filter-item--active' : '');
  all.innerHTML = `
    <button type="button" class="filter-item__btn">
      <span class="filter-item__dot"></span>
      <span class="filter-item__label">Todas</span>
      <span class="filter-item__count">${activeTarefas().length}</span>
    </button>`;
  all.querySelector('button').addEventListener('click', () => {
    currentCategoryFilter = null;
    render();
  });
  list.appendChild(all);

  activeCategorias().forEach(c => {
    const count = activeTarefas().filter(t => t.id_categoria === c.id_categoria).length;
    const li = document.createElement('li');
    li.className = 'filter-item' + (currentCategoryFilter === c.id_categoria ? ' filter-item--active' : '');
    li.innerHTML = `
      <button type="button" class="filter-item__btn">
        <span class="filter-item__dot" style="background:${colorFromId(c.id_categoria)}"></span>
        <span class="filter-item__label">${escapeHtml(c.nome)}</span>
        <span class="filter-item__count">${count}</span>
      </button>
      <button type="button" class="filter-item__delete" title="Excluir categoria" aria-label="Excluir categoria">
        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6"/><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
      </button>`;
    li.querySelector('.filter-item__btn').addEventListener('click', () => {
      currentCategoryFilter = c.id_categoria;
      render();
    });
    li.querySelector('.filter-item__delete').addEventListener('click', (e) => {
      e.stopPropagation();
      deleteCategoria(c.id_categoria);
    });
    list.appendChild(li);

    const opt = document.createElement('option');
    opt.value = c.id_categoria;
    opt.textContent = c.nome;
    select.appendChild(opt);
  });
}

function renderTags() {
  const cloud = document.getElementById('tags-cloud');
  cloud.innerHTML = '';

  activeTags().forEach(t => {
    const chip = document.createElement('div');
    chip.className = 'tag-chip' + (currentTagFilter === t.id_tag ? ' tag-chip--active' : '');
    chip.innerHTML = `
      <button type="button" class="tag-chip__btn">#${escapeHtml(t.nome)}</button>
      <button type="button" class="tag-chip__remove" title="Excluir tag" aria-label="Excluir tag">
        <svg xmlns="http://www.w3.org/2000/svg" width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
      </button>`;
    chip.querySelector('.tag-chip__btn').addEventListener('click', () => {
      currentTagFilter = currentTagFilter === t.id_tag ? null : t.id_tag;
      render();
    });
    chip.querySelector('.tag-chip__remove').addEventListener('click', (e) => {
      e.stopPropagation();
      deleteTag(t.id_tag);
    });
    cloud.appendChild(chip);
  });

  if (!activeTags().length) {
    cloud.innerHTML = '<p class="empty-hint">Nenhuma tag criada ainda.</p>';
  }

  // Tag picker no modal
  const picker = document.getElementById('task-tags-picker');
  picker.innerHTML = '';
  if (!activeTags().length) {
    picker.innerHTML = '<p class="empty-hint">Crie tags na barra lateral primeiro.</p>';
    return;
  }
  activeTags().forEach(t => {
    const lbl = document.createElement('label');
    lbl.className = 'tag-pick';
    lbl.innerHTML = `
      <input type="checkbox" value="${t.id_tag}" />
      <span>#${escapeHtml(t.nome)}</span>`;
    picker.appendChild(lbl);
  });
}

function renderTarefas() {
  const list = document.getElementById('tasks-list');
  const empty = document.getElementById('empty-state');
  const title = document.getElementById('main-title');
  const subtitle = document.getElementById('main-subtitle');

  list.innerHTML = '';

  // Título dinâmico
  if (currentCategoryFilter !== null) {
    const c = categoriaDeTarefa(currentCategoryFilter);
    title.textContent = c ? c.nome : 'Categoria';
    subtitle.textContent = 'Tarefas desta categoria';
  } else if (currentTagFilter !== null) {
    const t = state.tags.find(x => x.id_tag === currentTagFilter);
    title.textContent = t ? `#${t.nome}` : 'Tag';
    subtitle.textContent = 'Tarefas com esta tag';
  } else {
    title.textContent = 'Todas as tarefas';
    subtitle.textContent = 'Organize seu dia';
  }

  let tarefas = activeTarefas();
  if (currentCategoryFilter !== null) {
    tarefas = tarefas.filter(t => t.id_categoria === currentCategoryFilter);
  }
  if (currentTagFilter !== null) {
    const ids = state.tarefa_tags
      .filter(tt => tt.id_tag === currentTagFilter)
      .map(tt => tt.id_tarefa);
    tarefas = tarefas.filter(t => ids.includes(t.id_tarefa));
  }
  if (currentSearch.trim()) {
    const q = currentSearch.trim().toLowerCase();
    tarefas = tarefas.filter(t =>
      t.titulo.toLowerCase().includes(q) ||
      String(t.id_tarefa) === q
    );
  }

  // Ordena: pendentes primeiro, por vencimento
  tarefas.sort((a, b) => {
    if (a.status !== b.status) return a.status === 'pendente' ? -1 : 1;
    const valA = a.data_vencimento || '9999-99-99';
    const valB = b.data_vencimento || '9999-99-99';
    return valA.localeCompare(valB);
  });

  if (!tarefas.length) {
    empty.hidden = false;
    return;
  }
  empty.hidden = true;

  tarefas.forEach(t => list.appendChild(renderTarefaCard(t)));
}

function renderTarefaCard(t) {
  const cat = categoriaDeTarefa(t.id_categoria);
  const tags = tagsDeTarefa(t.id_tarefa);
  const concluida = t.status === 'concluida';
  const overdue = isOverdue(t);
  const due = dueLabel(t);

  const card = document.createElement('article');
  card.className = 'task' + (concluida ? ' task--done' : '') + (overdue ? ' task--overdue' : '');

  card.innerHTML = `
    <button type="button" class="task__check" aria-label="Marcar como concluída" data-action="toggle">
      ${concluida
        ? `<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>`
        : ''}
    </button>
    <div class="task__body">
      <div class="task__head">
        <h3 class="task__title">${escapeHtml(t.titulo)}</h3>
        <span class="task__id">#${t.id_tarefa}</span>
      </div>
      ${t.descricao ? `<p class="task__desc">${escapeHtml(t.descricao)}</p>` : ''}
      <div class="task__meta">
        ${cat ? `<span class="meta-pill">
          <span class="meta-pill__dot" style="background:${colorFromId(cat.id_categoria)}"></span>
          ${escapeHtml(cat.nome)}
        </span>` : ''}
        ${due ? `<span class="meta-pill meta-pill--${overdue ? 'danger' : 'muted'}">
          <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="4" rx="2"/><path d="M16 2v4"/><path d="M8 2v4"/><path d="M3 10h18"/></svg>
          ${due}${t.data_vencimento ? ' • ' + formatDate(t.data_vencimento) : ''}
        </span>` : ''}
        ${tags.map(tag => `<span class="meta-tag">#${escapeHtml(tag.nome)}</span>`).join('')}
      </div>
    </div>
    <div class="task__actions">
      <button type="button" class="icon-btn" data-action="edit" title="Editar" aria-label="Editar">
        <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/></svg>
      </button>
      <button type="button" class="icon-btn icon-btn--danger" data-action="delete" title="Excluir" aria-label="Excluir">
        <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6"/><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
      </button>
    </div>`;

  card.querySelector('[data-action="toggle"]').addEventListener('click', () => toggleTarefa(t.id_tarefa));
  card.querySelector('[data-action="edit"]').addEventListener('click', () => openTaskModal(t.id_tarefa));
  card.querySelector('[data-action="delete"]').addEventListener('click', () => deleteTarefa(t.id_tarefa));

  return card;
}

function renderStats() {
  const tarefas = activeTarefas();
  document.getElementById('stat-total').textContent = tarefas.length;
  document.getElementById('stat-pending').textContent = tarefas.filter(t => t.status === 'pendente').length;
  document.getElementById('stat-done').textContent = tarefas.filter(t => t.status === 'concluida').length;
  document.getElementById('stat-overdue').textContent = tarefas.filter(isOverdue).length;
}

function renderLogs() {
  const section = document.getElementById('logs-section');
  const list = document.getElementById('logs-list');
  if (!state.logs || !state.logs.length) {
    section.hidden = true;
    return;
  }
  section.hidden = false;
  list.innerHTML = '';
  state.logs.slice().reverse().forEach(log => {
    const li = document.createElement('li');
    li.className = 'log';
    li.innerHTML = `
      <div class="log__head">
        <span class="log__id">LOG #${log.id_log}</span>
        <span class="log__date">${log.data_conclusao}</span>
      </div>
      <p class="log__title">Tarefa #${log.id_tarefa} concluída</p>
      ${log.resumo_tags ? `<p class="log__tags">Tags: ${escapeHtml(log.resumo_tags)}</p>` : ''}
    `;
    list.appendChild(li);
  });
}

// ---------- CRUD ----------
async function openTaskModal(idTarefa = null) {
  const modal = document.getElementById('task-modal');
  const title = document.getElementById('task-modal-title');
  const form = document.getElementById('task-form');
  form.reset();
  document.getElementById('task-title-error').textContent = '';
  document.getElementById('task-category-error').textContent = '';
  renderTags(); // garante picker atualizado

  if (idTarefa) {
    const t = state.tarefas.find(x => x.id_tarefa === idTarefa);
    if (!t) return;
    title.textContent = `Editar tarefa #${t.id_tarefa}`;
    document.getElementById('task-id').value = t.id_tarefa;
    document.getElementById('task-title').value = t.titulo;
    document.getElementById('task-desc').value = t.descricao || '';
    document.getElementById('task-category').value = t.id_categoria;
    if (t.data_vencimento) {
      document.getElementById('task-due').value = t.data_vencimento;
    }
    // Tags mapping might need more work if we have a TaskTag relationship on backend
    // For now, let's keep it simple as the original code was mostly mock
  } else {
    title.textContent = 'Nova tarefa';
    document.getElementById('task-id').value = '';
  }
  openModal('task-modal');
}

async function submitTaskForm(e) {
  e.preventDefault();
  const id = document.getElementById('task-id').value;
  const titulo = document.getElementById('task-title').value.trim();
  const descricao = document.getElementById('task-desc').value.trim();
  const categorySelect = document.getElementById('task-category');
  const id_categoria = categorySelect.value === "" ? null : Number(categorySelect.value);
  const data_vencimento = document.getElementById('task-due').value;
  const tagIds = Array.from(document.querySelectorAll('#task-tags-picker input:checked')).map(cb => Number(cb.value));

  let ok = true;
  if (!titulo) { document.getElementById('task-title-error').textContent = 'Título é obrigatório'; ok = false; }
  else { document.getElementById('task-title-error').textContent = ''; }
  
  if (categorySelect.value === "") { 
    document.getElementById('task-category-error').textContent = 'Selecione uma categoria'; 
    ok = false; 
  } else { 
    document.getElementById('task-category-error').textContent = ''; 
  }
  
  if (!ok) return;

  const payload = {
    titulo,
    descricao,
    id_categoria,
    data_vencimento,
    status: 'pendente',
    data_criacao: new Date().toISOString().slice(0, 10)
  };

  try {
    const url = id ? `/api/tarefas/${id}` : '/api/tarefas';
    const method = id ? 'PUT' : 'POST';
    const response = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (response.ok) {
      const tarefaSalva = await response.json();
      const tarefaId = id ? Number(id) : tarefaSalva.id;

      // Se for edição, idealmente deveríamos limpar as tags antigas no backend primeiro
      // mas como o backend atual é simples, vamos apenas adicionar as novas
      for (const tid of tagIds) {
        await fetch('/api/tarefa-tags', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ id_tag: tid, id_tarefa: tarefaId })
        });
      }

      showToast(id ? 'Tarefa atualizada' : 'Tarefa criada');
      closeModal('task-modal');
      loadData();
    } else {
      showToast('Erro ao salvar tarefa', 'error');
    }
  } catch (e) {
    showToast('Erro de conexão', 'error');
  }
}

async function toggleTarefa(idTarefa) {
  const t = state.tarefas.find(x => x.id_tarefa === idTarefa);
  if (!t) return;
  
  const novoStatus = t.status === 'pendente' ? 'concluida' : 'pendente';
  const payload = { ...t, status: novoStatus };

  try {
    const response = await fetch(`/api/tarefas/${idTarefa}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (response.ok) {
      showToast(novoStatus === 'concluida' ? 'Tarefa concluída' : 'Tarefa reaberta');
      loadData();
    }
  } catch (e) { showToast('Erro ao atualizar status', 'error'); }
}

async function deleteTarefa(idTarefa) {
  if (!confirm('Excluir esta tarefa?')) return;
  try {
    const response = await fetch(`/api/tarefas/${idTarefa}`, { method: 'DELETE' });
    if (response.ok) {
      showToast('Tarefa excluída');
      loadData();
    }
  } catch (e) { showToast('Erro ao excluir tarefa', 'error'); }
}

async function deleteCategoria(idCategoria) {
  if (!confirm('Excluir esta categoria?')) return;
  try {
    const response = await fetch(`/api/categorias/${idCategoria}`, { method: 'DELETE' });
    if (response.ok) {
      showToast('Categoria excluída');
      if (currentCategoryFilter === idCategoria) currentCategoryFilter = null;
      loadData();
    } else {
      const msg = await response.text();
      showToast(msg || 'Erro ao excluir categoria (verifique se há tarefas associadas)', 'error');
    }
  } catch (e) { showToast('Erro ao excluir categoria', 'error'); }
}

async function deleteTag(idTag) {
  if (!confirm('Excluir esta tag?')) return;
  try {
    const response = await fetch(`/api/tags/${idTag}`, { method: 'DELETE' });
    if (response.ok) {
      showToast('Tag excluída');
      if (currentTagFilter === idTag) currentTagFilter = null;
      loadData();
    } else {
      const msg = await response.text();
      showToast(msg || 'Erro ao excluir tag', 'error');
    }
  } catch (e) { showToast('Erro ao excluir tag', 'error'); }
}

// ---------- Modal helpers ----------
function openModal(id) {
  const m = document.getElementById(id);
  m.hidden = false;
  requestAnimationFrame(() => m.classList.add('modal--open'));
}
function closeModal(id) {
  const m = document.getElementById(id);
  m.classList.remove('modal--open');
  setTimeout(() => { m.hidden = true; }, 200);
}
document.querySelectorAll('[data-close-modal]').forEach(el => {
  el.addEventListener('click', () => closeModal(el.dataset.closeModal));
});
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    document.querySelectorAll('.modal:not([hidden])').forEach(m => closeModal(m.id));
  }
});

// ---------- Util ----------
function escapeHtml(str) {
  return String(str ?? '').replace(/[&<>"']/g, ch => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[ch]));
}
function colorFromId(id) {
  const palette = ['#5b5bf0', '#10b981', '#f59e0b', '#e11d48', '#0ea5e9', '#8b5cf6', '#ec4899'];
  return palette[id % palette.length];
}

// ---------- Eventos e Inicialização ----------
document.getElementById('add-task-btn').addEventListener('click', () => openTaskModal());
document.getElementById('task-form').addEventListener('submit', submitTaskForm);

document.getElementById('add-category-btn').addEventListener('click', () => {
  document.getElementById('category-form').reset();
  document.getElementById('category-name-error').textContent = '';
  openModal('category-modal');
});
document.getElementById('category-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const nome = document.getElementById('category-name').value.trim();
  if (!nome) { document.getElementById('category-name-error').textContent = 'Nome é obrigatório'; return; }
  
  try {
    const response = await fetch('/api/categorias', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nome })
    });
    if (response.ok) {
      showToast('Categoria criada');
      closeModal('category-modal');
      loadData();
    } else {
      showToast('Erro ao criar categoria', 'error');
    }
  } catch (e) { showToast('Erro de conexão', 'error'); }
});

document.getElementById('add-tag-btn').addEventListener('click', () => {
  document.getElementById('tag-form').reset();
  document.getElementById('tag-name-error').textContent = '';
  openModal('tag-modal');
});
document.getElementById('tag-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const nome = document.getElementById('tag-name').value.trim();
  if (!nome) { document.getElementById('tag-name-error').textContent = 'Nome é obrigatório'; return; }
  
  try {
    const response = await fetch('/api/tags', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nome })
    });
    if (response.ok) {
      showToast('Tag criada');
      closeModal('tag-modal');
      loadData();
    } else {
      showToast('Erro ao criar tag', 'error');
    }
  } catch (e) { showToast('Erro de conexão', 'error'); }
});

document.getElementById('search-input').addEventListener('input', (e) => {
  currentSearch = e.target.value;
  renderTarefas();
});

document.getElementById('logout-btn').addEventListener('click', async () => {
  await fetch('/api/auth/logout', { method: 'POST' });
  window.location.href = 'index.html';
});

document.getElementById('toggle-logs').addEventListener('click', () => {
  document.getElementById('logs-list').classList.toggle('logs__list--collapsed');
});

// Inicialização
async function init() {
  if (await checkAuth()) {
    await loadData();
  }
}

init();


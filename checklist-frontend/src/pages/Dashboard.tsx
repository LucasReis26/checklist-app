import { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';

interface DashboardProps {
  user: any;
  onLogout: () => void;
}

const Dashboard = ({ user, onLogout }: DashboardProps) => {
  const [categories, setCategories] = useState<any[]>([]);
  const [tags, setTags] = useState<any[]>([]);
  const [tasks, setTasks] = useState<any[]>([]);
  const [taskTags, setTaskTags] = useState<any[]>([]);
  const [logs, setLogs] = useState<any[]>([]);
  
  const [currentCategoryFilter, setCurrentCategoryFilter] = useState<number | null>(null);
  const [currentTagFilter, setCurrentTagFilter] = useState<number | null>(null);
  const [currentSearch, setCurrentSearch] = useState('');
  
  const [isTaskModalOpen, setIsTaskModalOpen] = useState(false);
  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false);
  const [isTagModalOpen, setIsTagModalOpen] = useState(false);
  
  const [isPatternSearchModalOpen, setIsPatternSearchModalOpen] = useState(false);
  const [patternQuery, setPatternQuery] = useState('');
  const [patternAlgorithm, setPatternAlgorithm] = useState('KMP');
  const [patternResults, setPatternResults] = useState<any[]>([]);
  const [isSearchingPattern, setIsSearchingPattern] = useState(false);
  const [patternSearchError, setPatternSearchError] = useState('');
  
  const [editingTask, setEditingTask] = useState<any>(null);
  const [newTaskTitle, setNewTaskTitle] = useState('');
  const [newTaskDesc, setNewTaskDesc] = useState('');
  const [newTaskCategory, setNewTaskCategory] = useState<string>('');
  const [newTaskDue, setNewTaskDue] = useState('');
  const [newTaskTags, setNewTaskTags] = useState<number[]>([]);

  const [newCategoryName, setNewCategoryName] = useState('');
  const [newTagName, setNewTagName] = useState('');

  const loadData = async () => {
    try {
      const endpoints = [
        '/api/categorias',
        '/api/tags',
        '/api/tarefas',
        '/api/logs',
        '/api/tarefa-tags'
      ];

      const responses = await Promise.all(endpoints.map(url => fetch(url)));
      
      const data = await Promise.all(responses.map(async (res, i) => {
        if (res.ok) {
          try {
            return await res.json();
          } catch {
            console.error(`Error parsing JSON from ${endpoints[i]}`);
            return [];
          }
        }
        console.error(`API Error ${res.status} from ${endpoints[i]}`);
        return [];
      }));

      const [cats, tgs, tsks, lgs, ttags] = data;

      setCategories(Array.isArray(cats) ? cats : []);
      setTags(Array.isArray(tgs) ? tgs : []);
      setTasks((Array.isArray(tsks) ? tsks : []).map((t: any) => ({ ...t, id: t.id_tarefa || t.id })));
      setLogs(Array.isArray(lgs) ? lgs : []);
      setTaskTags(Array.isArray(ttags) ? ttags : []);
    } catch (e) {
      console.error('Error loading dashboard data', e);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleLogout = async () => {
    await fetch('/api/auth/logout', { method: 'POST' });
    onLogout();
  };

  const isAdmin = useMemo(() => {
    if (!user) return false;
    console.log('Current user debug:', user);
    return user.role === 'ADMIN' || (user.email && user.email.toLowerCase() === 'admin@checklist.com');
  }, [user]);

  const filteredTasks = useMemo(() => {
    let ts = tasks.filter(t => t.status !== 'deletado');
    if (currentCategoryFilter !== null) {
      ts = ts.filter(t => (t.id_categoria || t.id) === currentCategoryFilter);
    }
    if (currentTagFilter !== null) {
      const tids = taskTags.filter(tt => (tt.id_tag || tt.idTag) === currentTagFilter).map(tt => tt.id_tarefa || tt.idTarefa);
      ts = ts.filter(t => tids.includes(t.id));
    }
    if (currentSearch.trim()) {
      const q = currentSearch.toLowerCase();
      ts = ts.filter(t => t.titulo.toLowerCase().includes(q));
    }
    return ts.sort((a, b) => {
      if (a.status !== b.status) return a.status === 'pendente' ? -1 : 1;
      return (a.data_vencimento || '9999').localeCompare(b.data_vencimento || '9999');
    });
  }, [tasks, currentCategoryFilter, currentTagFilter, currentSearch, taskTags]);

  const stats = useMemo(() => ({
    total: tasks.length,
    pending: tasks.filter(t => t.status === 'pendente').length,
    done: tasks.filter(t => t.status === 'concluida').length,
    overdue: tasks.filter(t => {
      if (t.status === 'concluida' || !t.data_vencimento) return false;
      return new Date(t.data_vencimento + 'T23:59:59').getTime() < Date.now();
    }).length
  }), [tasks]);

  const handleToggleTask = async (id: number) => {
    const t = tasks.find(x => x.id === id);
    const status = t.status === 'pendente' ? 'concluida' : 'pendente';
    await fetch(`/api/tarefas/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ...t, status })
    });
    loadData();
  };

  const handleDeleteTask = async (id: number) => {
    if (confirm('Excluir esta tarefa?')) {
      await fetch(`/api/tarefas/${id}`, { method: 'DELETE' });
      loadData();
    }
  };

  const handleSubmitTask = async (e: React.FormEvent) => {
    e.preventDefault();
    const payload = { 
      titulo: newTaskTitle, 
      descricao: newTaskDesc, 
      id_categoria: Number(newTaskCategory) || 0, 
      data_vencimento: newTaskDue, 
      status: editingTask ? editingTask.status : 'pendente' 
    };
    const url = editingTask ? `/api/tarefas/${editingTask.id}` : '/api/tarefas';
    const method = editingTask ? 'PUT' : 'POST';

    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (res.ok) {
      const saved = await res.json();
      const actualId = editingTask ? editingTask.id : (saved.id_tarefa || saved.id);
      
      // Handle tags (this is a bit simplified compared to original which did sequential posts)
      // Original logic just added tags, didn't clear them. Replicating that for now.
      for (const tid of newTaskTags) {
        await fetch('/api/tarefa-tags', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ id_tag: tid, id_tarefa: actualId })
        });
      }
      setIsTaskModalOpen(false);
      loadData();
    }
  };

  const handleOpenEditModal = (t: any) => {
    setEditingTask(t);
    setNewTaskTitle(t.titulo);
    setNewTaskDesc(t.descricao || '');
    setNewTaskCategory(String(t.id_categoria || ''));
    setNewTaskDue(t.data_vencimento || '');
    const ttagIds = taskTags.filter(tt => (tt.id_tarefa || tt.idTarefa) === t.id).map(tt => tt.id_tag || tt.idTag);
    setNewTaskTags(ttagIds);
    setIsTaskModalOpen(true);
  };

  const handleCreateCategory = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCategoryName.trim()) return;
    await fetch('/api/categorias', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nome: newCategoryName })
    });
    setNewCategoryName('');
    setIsCategoryModalOpen(false);
    loadData();
  };

  const handleCreateTag = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTagName.trim()) return;
    await fetch('/api/tags', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nome: newTagName })
    });
    setNewTagName('');
    setIsTagModalOpen(false);
    loadData();
  };

  const colorFromId = (id: number) => {
    const colors = ['#5b5bf0', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899'];
    return colors[id % colors.length];
  };

  const handlePatternSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!patternQuery.trim()) return;

    setIsSearchingPattern(true);
    setPatternSearchError('');
    try {
      const response = await fetch(`/api/search?pattern=${encodeURIComponent(patternQuery)}&algorithm=${patternAlgorithm}`);
      if (response.ok) {
        const data = await response.json();
        setPatternResults(Array.isArray(data) ? data : []);
      } else {
        setPatternSearchError('Erro ao realizar busca no servidor');
      }
    } catch (err) {
      console.error(err);
      setPatternSearchError('Erro de conexão com o servidor');
    } finally {
      setIsSearchingPattern(false);
    }
  };

  const escapeRegExp = (str: string) => {
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  };

  const highlightPattern = (text: string, pattern: string) => {
    if (!text || !pattern) return text;
    const parts = text.split(new RegExp(`(${escapeRegExp(pattern)})`, 'gi'));
    return (
      <>
        {parts.map((part, i) => 
          part.toLowerCase() === pattern.toLowerCase() 
            ? <mark key={i} style={{ backgroundColor: 'rgba(91, 91, 240, 0.2)', color: '#5b5bf0', padding: '1px 3px', borderRadius: '4px', fontWeight: 'bold' }}>{part}</mark>
            : part
        )}
      </>
    );
  };

  return (
    <div className="app-body">
      <header className="app-header">
        <div className="app-header__inner">
          <div className="app-header__brand">
            <span className="app-header__logo" aria-hidden="true">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="3" rx="2"/><path d="m9 12 2 2 4-4"/></svg>
            </span>
            <span>Checklist App</span>
          </div>

          <div className="app-header__user">
            {isAdmin && (
              <Link to="/admin" className="btn btn--outline btn--sm" style={{ marginRight: '8px' }}>
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"/><circle cx="12" cy="12" r="3"/></svg>
                <span className="btn__label">Admin</span>
              </Link>
            )}
            <button 
              onClick={() => { setIsPatternSearchModalOpen(true); setPatternQuery(''); setPatternResults([]); setPatternSearchError(''); }} 
              className="btn btn--outline btn--sm" 
              style={{ marginRight: '8px' }}
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
              <span className="btn__label">Pesquisar Padrão (KMP/BM)</span>
            </button>
            <div className="user-chip">
              <div className="user-chip__avatar">{user.nome[0].toUpperCase()}</div>
              <div className="user-chip__meta">
                <span className="user-chip__name">{user.nome}</span>
                <span className="user-chip__email">{user.email}</span>
              </div>
            </div>
            <button onClick={handleLogout} className="btn btn--outline btn--sm" title="Sair">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" x2="9" y1="12" y2="12"/></svg>
              <span className="btn__label">Sair</span>
            </button>
          </div>
        </div>
      </header>

      <div className="app-shell">
        <aside className="sidebar">
          <section className="sidebar__section">
            <header className="sidebar__header">
              <h2 className="sidebar__title">Categorias</h2>
              <button onClick={() => setIsCategoryModalOpen(true)} className="icon-btn" title="Nova categoria">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="M12 5v14"/></svg>
              </button>
            </header>
            <ul className="filter-list">
              <li className={`filter-item ${currentCategoryFilter === null ? 'filter-item--active' : ''}`}>
                <button onClick={() => setCurrentCategoryFilter(null)} className="filter-item__btn">
                  <span className="filter-item__dot"></span>
                  <span className="filter-item__label">Todas as tarefas</span>
                  <span className="filter-item__count">{tasks.length}</span>
                </button>
              </li>
              {categories.map(c => (
                <li key={c.id} className={`filter-item ${currentCategoryFilter === c.id ? 'filter-item--active' : ''}`}>
                  <button onClick={() => setCurrentCategoryFilter(c.id)} className="filter-item__btn">
                    <span className="filter-item__dot" style={{background: colorFromId(c.id)}}></span>
                    <span className="filter-item__label">{c.nome}</span>
                    <span className="filter-item__count">{tasks.filter(t => t.id_categoria === c.id).length}</span>
                  </button>
                </li>
              ))}
            </ul>
          </section>

          <section className="sidebar__section">
            <header className="sidebar__header">
              <h2 className="sidebar__title">Tags</h2>
              <button onClick={() => setIsTagModalOpen(true)} className="icon-btn" title="Nova tag">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="M12 5v14"/></svg>
              </button>
            </header>
            <div className="tag-cloud">
              <button onClick={() => setCurrentTagFilter(null)} className={`tag-chip ${currentTagFilter === null ? 'tag-chip--active' : ''}`}>
                <span className="tag-chip__btn">#todas</span>
              </button>
              {tags.map(t => (
                <button key={t.id} onClick={() => setCurrentTagFilter(t.id)} className={`tag-chip ${currentTagFilter === t.id ? 'tag-chip--active' : ''}`}>
                  <span className="tag-chip__btn">#{t.nome}</span>
                </button>
              ))}
            </div>
          </section>
        </aside>

        <main className="main">
          <div className="toolbar">
            <div className="toolbar__head">
              <h1 className="toolbar__title">{currentCategoryFilter ? categories.find(c => c.id === currentCategoryFilter)?.nome : 'Todas as tarefas'}</h1>
              <p className="toolbar__subtitle">Organize seu dia</p>
            </div>
            <div className="toolbar__actions">
              <div className="search">
                <svg className="search__icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
                <input 
                  type="search" 
                  className="search__input" 
                  placeholder="Buscar..." 
                  value={currentSearch}
                  onChange={(e) => setCurrentSearch(e.target.value)}
                />
              </div>
              <button onClick={() => { setEditingTask(null); setNewTaskTitle(''); setNewTaskDesc(''); setIsTaskModalOpen(true); }} className="btn btn--primary">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="M12 5v14"/></svg>
                <span className="btn__label">Nova tarefa</span>
              </button>
            </div>
          </div>

          <div className="stats">
            <div className="stat">
              <span className="stat__value">{stats.total}</span>
              <span className="stat__label">Total</span>
            </div>
            <div className="stat">
              <span className="stat__value">{stats.pending}</span>
              <span className="stat__label">Pendentes</span>
            </div>
            <div className="stat">
              <span className="stat__value">{stats.done}</span>
              <span className="stat__label">Concluídas</span>
            </div>
            <div className="stat">
              <span className="stat__value">{stats.overdue}</span>
              <span className="stat__label">Atrasadas</span>
            </div>
          </div>

          <section className="tasks">
            {filteredTasks.map(t => {
              const cat = categories.find(c => c.id === t.id_categoria);
              const ttagIds = taskTags.filter(tt => (tt.id_tarefa || tt.idTarefa) === t.id).map(tt => tt.id_tag || tt.idTag);
              const ttags = tags.filter(tg => ttagIds.includes(tg.id));
              const isOverdue = t.status !== 'concluida' && t.data_vencimento && new Date(t.data_vencimento + 'T23:59:59').getTime() < Date.now();

              return (
                <article key={t.id} className={`task ${t.status === 'concluida' ? 'task--done' : ''} ${isOverdue ? 'task--overdue' : ''}`}>
                  <button onClick={() => handleToggleTask(t.id)} className="task__check">
                    {t.status === 'concluida' && '✓'}
                  </button>
                  <div className="task__body">
                    <div className="task__head">
                      <h3 className="task__title">{t.titulo}</h3>
                      <span className="task__id">#{t.id}</span>
                    </div>
                    <p className="task__desc">{t.descricao}</p>
                    <div className="task__meta">
                      {cat && <span className="meta-pill"><span className="meta-pill__dot" style={{background: colorFromId(cat.id)}}></span>{cat.nome}</span>}
                      {t.data_vencimento && <span className="meta-pill meta-pill--muted">📅 {t.data_vencimento.split('-').reverse().slice(0, 2).join('/')}</span>}
                      {ttags.map(tg => <span key={tg.id} className="meta-tag">#{tg.nome}</span>)}
                    </div>
                  </div>
                  <div className="task__actions">
                    <button onClick={() => handleOpenEditModal(t)} className="icon-btn" title="Editar">✎</button>
                    <button onClick={() => handleDeleteTask(t.id)} className="icon-btn icon-btn--danger" title="Excluir">🗑</button>
                  </div>
                </article>
              );
            })}
            {filteredTasks.length === 0 && (
              <div className="empty">
                <div className="empty__icon">
                  <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="3" rx="2"/><path d="m9 12 2 2 4-4"/></svg>
                </div>
                <h3 className="empty__title">Nenhuma tarefa por aqui</h3>
              </div>
            )}
          </section>

          {logs.length > 0 && (
            <section className="logs">
              <header className="logs__header">
                <h2 className="logs__title">Logs de conclusão</h2>
              </header>
              <ul className="logs__list">
                {logs.slice().reverse().map(log => (
                  <li key={log.id} className="log">
                    <div className="log__head"><span className="log__id">LOG #{log.id}</span> <span>{log.data_conclusao}</span></div>
                    <p className="log__title">Tarefa #{log.id_tarefa} concluída</p>
                  </li>
                ))}
              </ul>
            </section>
          )}
        </main>
      </div>

      {/* Modals */}
      {isTaskModalOpen && (
        <div className="modal modal--open">
          <div className="modal__backdrop" onClick={() => setIsTaskModalOpen(false)}></div>
          <div className="modal__panel">
            <header className="modal__header">
              <h2 className="modal__title">{editingTask ? 'Editar tarefa' : 'Nova tarefa'}</h2>
              <button className="icon-btn" onClick={() => setIsTaskModalOpen(false)}>✕</button>
            </header>
            <form onSubmit={handleSubmitTask} className="form">
              <div className="field">
                <label className="field__label">Título</label>
                <input type="text" className="field__input field__input--plain" value={newTaskTitle} onChange={e => setNewTaskTitle(e.target.value)} required />
              </div>
              <div className="field">
                <label className="field__label">Descrição</label>
                <textarea className="field__input field__textarea" value={newTaskDesc} onChange={e => setNewTaskDesc(e.target.value)} rows={3}></textarea>
              </div>
              <div className="field-row">
                <div className="field">
                  <label className="field__label">Categoria</label>
                  <select className="field__input field__input--plain" value={newTaskCategory} onChange={e => setNewTaskCategory(e.target.value)}>
                    <option value="">Selecionar...</option>
                    {categories.map(c => <option key={c.id} value={c.id}>{c.nome}</option>)}
                  </select>
                </div>
                <div className="field">
                  <label className="field__label">Vencimento</label>
                  <input type="date" className="field__input field__input--plain" value={newTaskDue} onChange={e => setNewTaskDue(e.target.value)} />
                </div>
              </div>
              <div className="field">
                <span className="field__label">Tags</span>
                <div className="tag-picker">
                  {tags.map(t => (
                    <label key={t.id} className="tag-pick">
                      <input 
                        type="checkbox" 
                        checked={newTaskTags.includes(t.id)} 
                        onChange={e => {
                          if (e.target.checked) setNewTaskTags([...newTaskTags, t.id]);
                          else setNewTaskTags(newTaskTags.filter(id => id !== t.id));
                        }}
                      /> {t.nome}
                    </label>
                  ))}
                </div>
              </div>
              <footer className="modal__footer">
                <button type="button" className="btn btn--outline" onClick={() => setIsTaskModalOpen(false)}>Cancelar</button>
                <button type="submit" className="btn btn--primary">Salvar</button>
              </footer>
            </form>
          </div>
        </div>
      )}

      {isCategoryModalOpen && (
        <div className="modal modal--open">
          <div className="modal__backdrop" onClick={() => setIsCategoryModalOpen(false)}></div>
          <div className="modal__panel modal__panel--sm">
            <header className="modal__header">
              <h2 className="modal__title">Nova categoria</h2>
              <button className="icon-btn" onClick={() => setIsCategoryModalOpen(false)}>✕</button>
            </header>
            <form onSubmit={handleCreateCategory} className="form">
              <div className="field">
                <label className="field__label">Nome</label>
                <input type="text" className="field__input field__input--plain" value={newCategoryName} onChange={e => setNewCategoryName(e.target.value)} required />
              </div>
              <footer className="modal__footer">
                <button type="button" className="btn btn--outline" onClick={() => setIsCategoryModalOpen(false)}>Cancelar</button>
                <button type="submit" className="btn btn--primary">Criar</button>
              </footer>
            </form>
          </div>
        </div>
      )}

      {isTagModalOpen && (
        <div className="modal modal--open">
          <div className="modal__backdrop" onClick={() => setIsTagModalOpen(false)}></div>
          <div className="modal__panel modal__panel--sm">
            <header className="modal__header">
              <h2 className="modal__title">Nova tag</h2>
              <button className="icon-btn" onClick={() => setIsTagModalOpen(false)}>✕</button>
            </header>
            <form onSubmit={handleCreateTag} className="form">
              <div className="field">
                <label className="field__label">Nome</label>
                <input type="text" className="field__input field__input--plain" value={newTagName} onChange={e => setNewTagName(e.target.value)} required />
              </div>
              <footer className="modal__footer">
                <button type="button" className="btn btn--outline" onClick={() => setIsTagModalOpen(false)}>Cancelar</button>
                <button type="submit" className="btn btn--primary">Criar</button>
              </footer>
            </form>
          </div>
        </div>
      )}

      {isPatternSearchModalOpen && (
        <div className="modal modal--open">
          <div className="modal__backdrop" onClick={() => setIsPatternSearchModalOpen(false)}></div>
          <div className="modal__panel" style={{ maxWidth: '600px', width: '95%', maxHeight: '90vh', display: 'flex', flexDirection: 'column' }}>
            <header className="modal__header">
              <h2 className="modal__title">Pesquisar por Padrão (KMP / BM)</h2>
              <button className="icon-btn" onClick={() => setIsPatternSearchModalOpen(false)}>✕</button>
            </header>
            <form onSubmit={handlePatternSearch} className="form" style={{ marginBottom: '1.25rem', flexShrink: 0 }}>
              <div className="field-row">
                <div className="field" style={{ flex: 2 }}>
                  <label className="field__label">Padrão de Busca</label>
                  <input 
                    type="text" 
                    className="field__input field__input--plain" 
                    placeholder="Digite a palavra ou frase a buscar..." 
                    value={patternQuery} 
                    onChange={e => setPatternQuery(e.target.value)} 
                    required 
                  />
                </div>
                <div className="field" style={{ flex: 1 }}>
                  <label className="field__label">Algoritmo</label>
                  <select 
                    className="field__input field__input--plain" 
                    value={patternAlgorithm} 
                    onChange={e => setPatternAlgorithm(e.target.value)}
                  >
                    <option value="KMP">KMP (Knuth-Morris-Pratt)</option>
                    <option value="BM">Boyer-Moore</option>
                  </select>
                </div>
              </div>
              <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '1rem' }}>
                <button type="button" className="btn btn--outline" style={{ marginRight: '8px' }} onClick={() => setIsPatternSearchModalOpen(false)}>
                  Fechar
                </button>
                <button type="submit" className="btn btn--primary" disabled={isSearchingPattern}>
                  {isSearchingPattern ? 'Pesquisando...' : 'Buscar'}
                </button>
              </div>
            </form>

            <div className="pattern-results" style={{ flex: 1, overflowY: 'auto', paddingRight: '4px' }}>
              <h3 style={{ fontSize: '0.95rem', fontWeight: 600, marginBottom: '0.75rem', color: 'var(--foreground)' }}>
                Resultados Encontrados ({patternResults.length})
              </h3>
              
              {patternSearchError && (
                <div style={{ color: 'var(--destructive)', padding: '0.5rem 0', fontSize: '0.875rem' }}>
                  {patternSearchError}
                </div>
              )}

              {patternResults.map(t => {
                const cat = categories.find(c => c.id === t.id_categoria);
                return (
                  <div key={t.id} style={{ 
                    border: '1px solid var(--input-border)', 
                    borderRadius: '8px', 
                    padding: '0.75rem', 
                    marginBottom: '0.5rem',
                    background: 'var(--card)' 
                  }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.25rem' }}>
                      <h4 style={{ margin: 0, fontSize: '0.9rem', fontWeight: 600, color: 'var(--foreground)' }}>
                        {highlightPattern(t.titulo, patternQuery)}
                      </h4>
                      <span className="task__id" style={{ fontSize: '0.8rem' }}>#{t.id}</span>
                    </div>
                    <p style={{ margin: '0.25rem 0 0.5rem 0', fontSize: '0.85rem', color: 'var(--muted-foreground)', lineHeight: '1.4' }}>
                      {t.descricao ? highlightPattern(t.descricao, patternQuery) : <em style={{ opacity: 0.6 }}>Sem descrição</em>}
                    </p>
                    <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
                      {cat && (
                        <span className="meta-pill" style={{ fontSize: '0.7rem' }}>
                          <span className="meta-pill__dot" style={{background: colorFromId(cat.id)}}></span>
                          {cat.nome}
                        </span>
                      )}
                      <span className="meta-pill meta-pill--muted" style={{ fontSize: '0.7rem' }}>
                        Status: {t.status}
                      </span>
                    </div>
                  </div>
                );
              })}

              {!isSearchingPattern && patternResults.length === 0 && patternQuery && (
                <div style={{ textAlign: 'center', padding: '2rem 0', color: 'var(--muted-foreground)', fontSize: '0.875rem' }}>
                  Nenhum registro correspondente ao padrão "{patternQuery}" foi encontrado.
                </div>
              )}

              {!patternQuery && (
                <div style={{ textAlign: 'center', padding: '2rem 0', color: 'var(--muted-foreground)', fontSize: '0.875rem' }}>
                  Digite um padrão e clique em "Buscar" para pesquisar.
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;

import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';

interface AdminProps {
  user: any;
  onLogout: () => void;
}

const Admin = ({ user, onLogout }: AdminProps) => {
  const [backups, setBackups] = useState<any[]>([]);
  const [usersList, setUsersList] = useState<any[]>([]);
  const [activeTab, setActiveTab] = useState<'backups' | 'users'>('backups');
  const [loading, setLoading] = useState(false);
  const [selectedStats, setSelectedStats] = useState<any>(null);
  const [isStatsModalOpen, setIsStatsModalOpen] = useState(false);
  const navigate = useNavigate();

  const loadBackups = async () => {
    try {
      const res = await fetch('/api/backup/list');
      if (res.ok) {
        const data = await res.json();
        setBackups(data);
      } else if (res.status === 403) {
        alert('Acesso negado');
        navigate('/dashboard');
      }
    } catch (e) {
      console.error('Error loading backups', e);
    }
  };

  const loadUsers = async () => {
    try {
      const res = await fetch('/api/auth/users');
      if (res.ok) {
        const data = await res.json();
        setUsersList(data);
      }
    } catch (e) {
      console.error('Error loading users', e);
    }
  };

  useEffect(() => {
    if (!user || (!user.role?.includes('ADMIN') && user.email !== 'admin@checklist.com')) {
      navigate('/dashboard');
      return;
    }
    loadBackups();
    loadUsers();
  }, [user, navigate]);

  const handleBackup = async (type: 'huffman' | 'lzw') => {
    setLoading(true);
    try {
      const res = await fetch(`/api/backup/${type}`, { method: 'POST' });
      const data = await res.json();
      if (res.ok) {
        alert('Backup realizado com sucesso!');
        loadBackups();
        if (data.stats) {
          setSelectedStats(data.stats);
          setIsStatsModalOpen(true);
        }
      } else {
        alert('Erro: ' + (data.error || 'Falha ao realizar backup'));
      }
    } catch (e) {
      alert('Erro de conexão');
    } finally {
      setLoading(false);
    }
  };

  const handleShowStats = async (backupName: string) => {
    try {
      const res = await fetch(`/api/backup/stats?name=${encodeURIComponent(backupName)}`);
      if (res.ok) {
        const data = await res.json();
        setSelectedStats(data);
        setIsStatsModalOpen(true);
      } else {
        alert('Estatísticas não disponíveis para este backup antigo.');
      }
    } catch (e) {
      alert('Erro ao buscar estatísticas');
    }
  };

  const handleRestore = async (backupName: string) => {
    if (!confirm(`⚠️ ATENÇÃO: Deseja restaurar o backup "${backupName}"?\n\nIsso irá substituir todos os dados atuais do sistema. Esta ação não pode ser desfeita.`)) {
      return;
    }

    setLoading(true);
    try {
      const res = await fetch(`/api/backup/restore?name=${encodeURIComponent(backupName)}`, { method: 'POST' });
      const data = await res.json();
      if (res.ok) {
        alert('Dados restaurados com sucesso! O sistema será reiniciado em instantes.');
        // No ambiente web real, poderíamos forçar um reload para garantir que os DAOs recarreguem os arquivos
        window.location.reload();
      } else {
        alert('Erro: ' + (data.error || 'Falha na restauração'));
      }
    } catch (e) {
      alert('Erro de conexão ao restaurar');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = async () => {
    await fetch('/api/auth/logout', { method: 'POST' });
    onLogout();
  };

  return (
    <div className="app-body">
      <header className="app-header">
        <div className="app-header__inner">
          <Link to="/dashboard" className="app-header__brand" style={{ textDecoration: 'none', color: 'inherit' }}>
            <span className="app-header__logo" aria-hidden="true">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="3" rx="2"/><path d="m9 12 2 2 4-4"/></svg>
            </span>
            <span>Checklist Admin</span>
          </Link>

          <div className="app-header__user">
            <div className="user-chip">
              <div className="user-chip__avatar" style={{ backgroundColor: 'var(--primary)' }}>{user.nome[0].toUpperCase()}</div>
              <div className="user-chip__meta">
                <span className="user-chip__name">{user.nome} (Admin)</span>
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
              <h2 className="sidebar__title">Menu Admin</h2>
            </header>
            <ul className="filter-list">
              <li className={`filter-item ${activeTab === 'backups' ? 'filter-item--active' : ''}`}>
                <button onClick={() => setActiveTab('backups')} className="filter-item__btn">
                  <span className="filter-item__dot" style={{ background: activeTab === 'backups' ? 'var(--primary)' : 'transparent' }}></span>
                  <span className="filter-item__label">Backups do Sistema</span>
                  <span className="filter-item__count">{backups.length}</span>
                </button>
              </li>
              <li className={`filter-item ${activeTab === 'users' ? 'filter-item--active' : ''}`}>
                <button onClick={() => setActiveTab('users')} className="filter-item__btn">
                  <span className="filter-item__dot" style={{ background: activeTab === 'users' ? 'var(--primary)' : 'transparent' }}></span>
                  <span className="filter-item__label">Contas de Usuários</span>
                  <span className="filter-item__count">{usersList.length}</span>
                </button>
              </li>
              <li className="filter-item">
                <Link to="/dashboard" className="filter-item__btn" style={{ textDecoration: 'none' }}>
                  <span className="filter-item__dot"></span>
                  <span className="filter-item__label">Voltar ao App</span>
                </Link>
              </li>
            </ul>
          </section>
        </aside>

        <main className="main">
          {activeTab === 'users' ? (
            <>
              <div className="toolbar">
                <div className="toolbar__head">
                  <h1 className="toolbar__title">Contas de Usuários</h1>
                  <p className="toolbar__subtitle">Gerencie os acessos e veja as senhas criptografadas com XOR no banco de dados</p>
                </div>
              </div>

              <div className="stats" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))' }}>
                <div className="stat">
                  <span className="stat__value">{usersList.length}</span>
                  <span className="stat__label">Usuários Cadastrados</span>
                </div>
                <div className="stat">
                  <span className="stat__value">
                    {usersList.filter(u => u.role === 'ADMIN').length}
                  </span>
                  <span className="stat__label">Administradores</span>
                </div>
                <div className="stat">
                  <span className="stat__value">
                    {usersList.filter(u => u.role !== 'ADMIN').length}
                  </span>
                  <span className="stat__label">Contas Comuns</span>
                </div>
              </div>

              <section className="tasks">
                <header className="sidebar__header" style={{ marginBottom: '16px' }}>
                  <h2 className="sidebar__title">Listagem de Contas</h2>
                </header>
                
                <div className="user-accounts-list" style={{ display: 'grid', gap: '16px' }}>
                  {usersList.map((u) => (
                    <article 
                      key={u.id} 
                      className="task" 
                      style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '12px' }}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '8px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                          <div className="user-chip__avatar" style={{ 
                            width: '40px', 
                            height: '40px', 
                            borderRadius: '50%', 
                            backgroundColor: u.role === 'ADMIN' ? 'var(--primary)' : '#10b981',
                            color: '#fff',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontWeight: 'bold',
                            fontSize: '1.1rem'
                          }}>
                            {u.nome[0].toUpperCase()}
                          </div>
                          <div>
                            <h3 style={{ margin: 0, fontSize: '1rem', fontWeight: 600, color: 'var(--foreground)' }}>
                              {u.nome}
                            </h3>
                            <span style={{ fontSize: '0.8rem', color: 'var(--muted-foreground)' }}>ID: #{u.id}</span>
                          </div>
                        </div>

                        <span className="meta-pill" style={{ 
                          backgroundColor: u.role === 'ADMIN' ? '#dbeafe' : '#dcfce7',
                          color: u.role === 'ADMIN' ? '#1e40af' : '#166534',
                          fontWeight: 'bold',
                          fontSize: '0.75rem'
                        }}>
                          {u.role}
                        </span>
                      </div>

                      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginTop: '4px', flexWrap: 'wrap' }}>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                          <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--muted-foreground)', textTransform: 'uppercase' }}>
                            Endereço de E-mail
                          </span>
                          <span style={{ fontSize: '0.9rem', color: 'var(--foreground)', fontWeight: 500 }}>
                            {u.email}
                          </span>
                        </div>

                        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                          <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--muted-foreground)', textTransform: 'uppercase' }}>
                            Senha Criptografada (XOR Base64)
                          </span>
                          <div style={{ 
                            display: 'flex', 
                            alignItems: 'center', 
                            gap: '8px', 
                            backgroundColor: 'var(--card)', 
                            border: '1px solid var(--input-border)',
                            padding: '6px 12px',
                            borderRadius: '6px',
                            fontFamily: 'monospace',
                            fontSize: '0.85rem',
                            color: '#ef4444', 
                            fontWeight: 600,
                            overflowX: 'auto'
                          }}>
                            🔒 {u.senhaCriptografada || 'N/A'}
                          </div>
                        </div>
                      </div>
                    </article>
                  ))}
                </div>
              </section>
            </>
          ) : (
            <>
              <div className="toolbar">
                <div className="toolbar__head">
                  <h1 className="toolbar__title">Administração de Backups</h1>
                  <p className="toolbar__subtitle">Gerencie as cópias de segurança do banco de dados</p>
                </div>
                <div className="toolbar__actions">
                  <button 
                    onClick={() => handleBackup('huffman')} 
                    className="btn btn--outline"
                    disabled={loading}
                  >
                    Gerar Backup Huffman
                  </button>
                  <button 
                    onClick={() => handleBackup('lzw')} 
                    className="btn btn--primary"
                    disabled={loading}
                  >
                    Gerar Backup LZW
                  </button>
                </div>
              </div>

              <div className="stats" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))' }}>
                <div className="stat">
                  <span className="stat__value">{backups.length}</span>
                  <span className="stat__label">Total de Backups</span>
                </div>
                <div className="stat">
                  <span className="stat__value">
                    {backups.filter(b => b.name.endsWith('.huf')).length}
                  </span>
                  <span className="stat__label">Arquivos Huffman</span>
                </div>
                <div className="stat">
                  <span className="stat__value">
                    {backups.filter(b => b.name.endsWith('.lzw')).length}
                  </span>
                  <span className="stat__label">Arquivos LZW</span>
                </div>
              </div>

              <section className="tasks">
                <header className="sidebar__header" style={{ marginBottom: '16px' }}>
                  <h2 className="sidebar__title">Backups Disponíveis em Disco</h2>
                </header>
                
                <div className="backup-list" style={{ display: 'grid', gap: '12px' }}>
                  {backups.slice().reverse().map((b, i) => (
                    <article 
                      key={i} 
                      className="task" 
                      style={{ padding: '16px', cursor: 'pointer', transition: 'transform 0.1s' }}
                      onClick={() => handleShowStats(b.name)}
                      onMouseEnter={(e) => e.currentTarget.style.transform = 'translateY(-2px)'}
                      onMouseLeave={(e) => e.currentTarget.style.transform = 'translateY(0)'}
                    >
                      <div className="task__body">
                        <div className="task__head">
                          <h3 className="task__title" style={{ fontFamily: 'monospace' }}>{b.name}</h3>
                          <span className="meta-pill meta-pill--muted">
                            {(b.size / 1024).toFixed(2)} KB
                          </span>
                        </div>
                        <div className="task__meta" style={{ marginTop: '8px' }}>
                          <span className="meta-pill">
                            🕒 {new Date(b.lastModified).toLocaleString()}
                          </span>
                          <span className={`meta-pill ${b.name.endsWith('.lzw') ? 'meta-pill--primary' : 'meta-pill--secondary'}`} 
                                style={{ 
                                  backgroundColor: b.name.endsWith('.lzw') ? '#dcfce7' : '#dbeafe',
                                  color: b.name.endsWith('.lzw') ? '#166534' : '#1e40af'
                                }}>
                            {b.name.endsWith('.lzw') ? 'Algoritmo LZW' : 'Algoritmo Huffman'}
                          </span>
                        </div>
                      </div>
                      <div className="task__actions" style={{ marginLeft: '16px' }}>
                        <button 
                          onClick={(e) => { e.stopPropagation(); handleRestore(b.name); }} 
                          className="btn btn--outline btn--sm" 
                          style={{ color: '#dc2626', borderColor: '#fca5a5' }}
                          title="Restaurar este backup"
                        >
                          Restaurar
                        </button>
                      </div>
                    </article>
                  ))}

                  {backups.length === 0 && (
                    <div className="empty">
                      <div className="empty__icon">📁</div>
                      <h3 className="empty__title">Nenhum backup encontrado</h3>
                      <p>Inicie um novo backup para proteger seus dados.</p>
                    </div>
                  )}
                </div>
              </section>
              
              <div style={{ marginTop: '32px', padding: '20px', backgroundColor: '#f0fdf4', borderRadius: '12px', border: '1px solid #bbf7d0' }}>
                <h3 style={{ color: '#166534', marginBottom: '8px', fontSize: '1rem' }}>✅ Restauração Ativada</h3>
                <p style={{ color: '#15803d', fontSize: '0.875rem' }}>
                  Agora você pode restaurar backups diretamente pela interface. 
                  <strong>Aviso:</strong> A restauração substituirá todos os dados (usuários, tarefas e tags) 
                  pelos dados contidos no arquivo selecionado. O navegador será recarregado após o processo.
                </p>
              </div>
            </>
          )}
        </main>
      </div>

      {isStatsModalOpen && selectedStats && (
        <div className="modal modal--open">
          <div className="modal__backdrop" onClick={() => setIsStatsModalOpen(false)}></div>
          <div className="modal__panel">
            <header className="modal__header">
              <h2 className="modal__title">📊 Estatísticas de Compressão</h2>
              <button className="icon-btn" onClick={() => setIsStatsModalOpen(false)}>✕</button>
            </header>
            <div className="stats" style={{ gridTemplateColumns: '1fr 1fr', gap: '16px', padding: '0 0 16px 0' }}>
              <div className="stat">
                <span className="stat__label">Algoritmo</span>
                <span className="stat__value" style={{ fontSize: '1.25rem' }}>{selectedStats.algorithm}</span>
              </div>
              <div className="stat">
                <span className="stat__label">Arquivos</span>
                <span className="stat__value" style={{ fontSize: '1.25rem' }}>{selectedStats.fileCount}</span>
              </div>
            </div>
            
            <div style={{ display: 'grid', gap: '12px', marginBottom: '20px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px', background: '#f8fafc', borderRadius: '8px' }}>
                <span style={{ color: '#64748b' }}>Tamanho Original:</span>
                <span style={{ fontWeight: '600' }}>{(selectedStats.originalSize / 1024).toFixed(2)} KB</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px', background: '#f0f9ff', borderRadius: '8px' }}>
                <span style={{ color: '#0369a1' }}>Tamanho Compactado:</span>
                <span style={{ fontWeight: '600', color: '#0369a1' }}>{(selectedStats.compressedSize / 1024).toFixed(2)} KB</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px', background: '#f0fdf4', borderRadius: '8px' }}>
                <span style={{ color: '#166534' }}>Taxa de Compressão:</span>
                <span style={{ fontWeight: '700', color: '#166534' }}>{Number(selectedStats.compressionRatio).toFixed(2)}%</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px', background: '#f1f5f9', borderRadius: '8px' }}>
                <span style={{ color: '#64748b' }}>Tempo de Execução:</span>
                <span style={{ fontWeight: '500' }}>{Number(selectedStats.executionTimeMs).toFixed(2)} ms</span>
              </div>
            </div>

            <footer className="modal__footer">
              <button className="btn btn--primary" style={{ width: '100%' }} onClick={() => setIsStatsModalOpen(false)}>
                Fechar Detalhes
              </button>
            </footer>
          </div>
        </div>
      )}
    </div>
  );
};

export default Admin;

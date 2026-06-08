import { useState } from 'react';
import { Link } from 'react-router-dom';

interface RegisterProps {
  onRegister: (user: any) => void;
}

const Register = ({ onRegister }: RegisterProps) => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nome: name, email, senha: password })
      });

      if (response.ok) {
        const user = await response.json();
        onRegister(user);
      } else {
        const errorText = await response.text();
        setError(errorText || 'Erro ao criar conta');
      }
    } catch (error) {
      setError('Erro de conexão com o servidor');
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="auth">
      <header className="auth__header">
        <div className="auth__logo" aria-hidden="true">
          <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect width="18" height="18" x="3" y="3" rx="2" />
            <path d="m9 12 2 2 4-4" />
          </svg>
        </div>
        <h1 className="auth__brand">Checklist App</h1>
        <p className="auth__tagline">Organize suas tarefas com facilidade</p>
      </header>

      <section className="auth__card">
        <h2 className="auth__title">Criar nova conta</h2>
        <p className="auth__subtitle">Preencha seus dados para começar</p>

        <form onSubmit={handleSubmit} className="form">
          <div className="field">
            <label htmlFor="name" className="field__label">Nome</label>
            <div className="field__input-wrap">
              <span className="field__icon" aria-hidden="true">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              </span>
              <input
                type="text"
                id="name"
                className="field__input"
                placeholder="Seu nome completo"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="field">
            <label htmlFor="email" className="field__label">Email</label>
            <div className="field__input-wrap">
              <span className="field__icon" aria-hidden="true">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="20" height="16" x="2" y="4" rx="2"/><path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/></svg>
              </span>
              <input
                type="email"
                id="email"
                className="field__input"
                placeholder="voce@exemplo.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="field">
            <label htmlFor="password" className="field__label">Senha</label>
            <div className="field__input-wrap">
              <span className="field__icon" aria-hidden="true">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="11" x="3" y="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
              </span>
              <input
                type="password"
                id="password"
                className="field__input"
                placeholder="Mínimo 6 caracteres"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            {error && <span className="field__error" role="alert">{error}</span>}
          </div>

          <button type="submit" className="btn btn--primary btn--block" disabled={loading}>
            {loading ? 'Criando conta...' : 'Criar conta'}
          </button>
        </form>

        <footer className="auth__footer">
          Já tem uma conta? <Link to="/login">Entrar</Link>
        </footer>
      </section>

      <p className="auth__credits">Projeto AEDS-III • Felipe, Lucas e Thayná</p>
    </main>
  );
};

export default Register;

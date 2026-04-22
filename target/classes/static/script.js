// ===== Toast helper =====
function showToast(message, type = 'success') {
  const existing = document.querySelector('.toast');
  if (existing) existing.remove();

  const toast = document.createElement('div');
  toast.className = `toast toast--${type}`;
  toast.textContent = message;
  document.body.appendChild(toast);

  // Force reflow then animate in
  requestAnimationFrame(() => toast.classList.add('toast--show'));

  setTimeout(() => {
    toast.classList.remove('toast--show');
    setTimeout(() => toast.remove(), 300);
  }, 2500);
}

// ===== Validation =====
function validateEmail(email) {
  const trimmed = email.trim();
  if (!trimmed) return 'Email é obrigatório';
  if (trimmed.length > 255) return 'Email muito longo';
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!re.test(trimmed)) return 'Email inválido';
  return null;
}

function validatePassword(password) {
  if (!password) return 'Senha é obrigatória';
  if (password.length < 6) return 'Senha deve ter ao menos 6 caracteres';
  if (password.length > 100) return 'Senha muito longa';
  return null;
}

function validateName(name) {
  const trimmed = name.trim();
  if (!trimmed) return 'Nome é obrigatório';
  if (trimmed.length < 2) return 'Nome deve ter ao menos 2 caracteres';
  if (trimmed.length > 100) return 'Nome muito longo';
  return null;
}

function setError(fieldId, message) {
  const errorEl = document.getElementById(`${fieldId}-error`);
  if (errorEl) errorEl.textContent = message || '';
}

function setLoading(button, loading, loadingText) {
  if (loading) {
    button.dataset.originalText = button.innerHTML;
    button.disabled = true;
    button.innerHTML = `<span class="spinner"></span> ${loadingText}`;
  } else {
    button.disabled = false;
    button.innerHTML = button.dataset.originalText || button.innerHTML;
  }
}

// ===== Login form =====
const loginForm = document.getElementById('login-form');
if (loginForm) {
  loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    setError('email', '');
    setError('password', '');

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    const emailErr = validateEmail(email);
    const passErr = validatePassword(password);

    if (emailErr) setError('email', emailErr);
    if (passErr) setError('password', passErr);
    if (emailErr || passErr) return;

    const submitBtn = loginForm.querySelector('button[type="submit"]');
    setLoading(submitBtn, true, 'Entrando...');

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, senha: password })
      });

      if (response.ok) {
        showToast('Login realizado com sucesso!', 'success');
        setTimeout(() => { window.location.href = 'dashboard.html'; }, 800);
      } else {
        const errorText = await response.text();
        showToast(errorText || 'Erro ao realizar login', 'error');
        setError('password', errorText);
      }
    } catch (error) {
      showToast('Erro de conexão com o servidor', 'error');
    } finally {
      setLoading(submitBtn, false);
    }
  });
}

// ===== Register form =====
const registerForm = document.getElementById('register-form');
if (registerForm) {
  registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    setError('name', '');
    setError('email', '');
    setError('password', '');

    const name = document.getElementById('name').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    const nameErr = validateName(name);
    const emailErr = validateEmail(email);
    const passErr = validatePassword(password);

    if (nameErr) setError('name', nameErr);
    if (emailErr) setError('email', emailErr);
    if (passErr) setError('password', passErr);
    if (nameErr || emailErr || passErr) return;

    const submitBtn = registerForm.querySelector('button[type="submit"]');
    setLoading(submitBtn, true, 'Criando conta...');

    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nome: name, email, senha: password })
      });

      if (response.ok) {
        showToast('Conta criada com sucesso!', 'success');
        setTimeout(() => { window.location.href = 'login.html'; }, 800);
      } else {
        const errorText = await response.text();
        showToast(errorText || 'Erro ao criar conta', 'error');
      }
    } catch (error) {
      showToast('Erro de conexão com o servidor', 'error');
    } finally {
      setLoading(submitBtn, false);
    }
  });
}

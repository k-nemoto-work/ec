// ============================================
// AUTH.JS — Login + Register views
// ============================================
import { api, Auth } from './api.js';
import { updateNav }  from './app.js';

export function mountAuth(container, mode) {
  const isLogin = mode !== 'register';

  container.innerHTML = `
    <div class="view-auth">
      <div class="auth-left">
        <div class="auth-left-watermark">REUSE</div>
        <div class="auth-left-content">
          <h2 class="auth-left-title">TOKYO<br><em>THRIFT</em></h2>
          <p class="auth-left-sub">あなたの不用品に<br>新しい物語を</p>
        </div>
      </div>
      <div class="auth-right">
        <div class="auth-form-wrap">
          <div class="auth-tabs">
            <button class="auth-tab ${isLogin ? 'active' : ''}" id="tab-login">LOG IN</button>
            <button class="auth-tab ${!isLogin ? 'active' : ''}" id="tab-register">SIGN UP</button>
          </div>

          <div id="auth-form-container"></div>
        </div>
      </div>
    </div>
  `;

  document.getElementById('tab-login').addEventListener('click', () => {
    window.location.hash = '#/login';
  });
  document.getElementById('tab-register').addEventListener('click', () => {
    window.location.hash = '#/register';
  });

  if (isLogin) {
    renderLoginForm();
  } else {
    renderRegisterForm();
  }
}

function renderLoginForm() {
  const fc = document.getElementById('auth-form-container');
  if (!fc) return;

  fc.innerHTML = `
    <form id="login-form" class="auth-form" novalidate>
      <h2>ログイン</h2>

      <div class="form-group">
        <label class="form-label" for="login-email">メールアドレス</label>
        <input class="form-input" type="email" id="login-email" name="email"
               placeholder="you@example.com" autocomplete="email" required>
      </div>

      <div class="form-group">
        <label class="form-label" for="login-password">パスワード</label>
        <input class="form-input" type="password" id="login-password" name="password"
               placeholder="••••••••" autocomplete="current-password" required>
      </div>

      <div id="login-error" class="form-error" style="display:none"></div>

      <button type="submit" class="btn-primary" id="login-submit">
        <span>LOG IN</span>
      </button>

      <p class="auth-form-footer">
        アカウントをお持ちでない方は
        <a href="#/register">新規登録</a>
      </p>
    </form>
  `;

  document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const email    = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;
    const errEl    = document.getElementById('login-error');
    const btn      = document.getElementById('login-submit');
    const span     = btn.querySelector('span');

    if (!email || !password) {
      showError(errEl, 'メールアドレスとパスワードを入力してください');
      return;
    }

    btn.disabled = true;
    span.textContent = 'LOGGING IN...';
    errEl.style.display = 'none';

    try {
      const result = await api.login(email, password);
      if (!result) return;
      Auth.save(result.accessToken);
      await updateNav();
      window.toast('ログインしました', 'success');
      window.location.hash = '#/';
    } catch (err) {
      btn.disabled = false;
      span.textContent = 'LOG IN';
      showError(errEl, err.message || 'メールアドレスまたはパスワードが正しくありません');
    }
  });
}

function renderRegisterForm() {
  const fc = document.getElementById('auth-form-container');
  if (!fc) return;

  fc.innerHTML = `
    <form id="register-form" class="auth-form" novalidate>
      <h2>新規登録</h2>

      <div class="form-group">
        <label class="form-label" for="reg-name">お名前</label>
        <input class="form-input" type="text" id="reg-name" name="name"
               placeholder="山田 太郎" autocomplete="name" required>
      </div>

      <div class="form-group">
        <label class="form-label" for="reg-email">メールアドレス</label>
        <input class="form-input" type="email" id="reg-email" name="email"
               placeholder="you@example.com" autocomplete="email" required>
      </div>

      <div class="form-group">
        <label class="form-label" for="reg-password">パスワード</label>
        <input class="form-input" type="password" id="reg-password" name="password"
               placeholder="8文字以上" autocomplete="new-password" required>
      </div>

      <div id="register-error" class="form-error" style="display:none"></div>

      <button type="submit" class="btn-primary" id="register-submit">
        <span>CREATE ACCOUNT</span>
      </button>

      <p class="auth-form-footer">
        すでにアカウントをお持ちの方は
        <a href="#/login">ログイン</a>
      </p>
    </form>
  `;

  document.getElementById('register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name     = document.getElementById('reg-name').value.trim();
    const email    = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value;
    const errEl    = document.getElementById('register-error');
    const btn      = document.getElementById('register-submit');
    const span     = btn.querySelector('span');

    if (!name || !email || !password) {
      showError(errEl, 'すべての項目を入力してください');
      return;
    }
    if (password.length < 8) {
      showError(errEl, 'パスワードは8文字以上で入力してください');
      return;
    }

    btn.disabled = true;
    span.textContent = 'CREATING...';
    errEl.style.display = 'none';

    try {
      await api.register(name, email, password);
      // Auto-login after register
      const loginResult = await api.login(email, password);
      if (!loginResult) return;
      Auth.save(loginResult.accessToken);
      await updateNav();
      window.toast('アカウントを作成しました', 'success');
      window.location.hash = '#/';
    } catch (err) {
      btn.disabled = false;
      span.textContent = 'CREATE ACCOUNT';
      showError(errEl, err.message || '登録に失敗しました。メールアドレスが既に使用されている可能性があります。');
    }
  });
}

function showError(el, msg) {
  el.textContent = msg;
  el.style.display = 'block';
}

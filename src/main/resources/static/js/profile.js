// ============================================
// PROFILE.JS — Profile + Address form
// ============================================
import { api } from './api.js';

export async function mountProfile(container) {
  container.innerHTML = `<div class="loading-view"><div class="loading-spinner"></div></div>`;

  try {
    const me = await api.getMe();
    if (!me) {
      container.innerHTML = `
        <div class="error-view">
          <h1>ERROR</h1>
          <a href="#/login" class="btn-primary">LOG IN AGAIN</a>
        </div>`;
      return;
    }

    const a = me.address || {};

    container.innerHTML = `
      <div class="view-profile">
        <h1>MY ACCOUNT</h1>

        <div class="profile-card">
          <div class="profile-label">お名前</div>
          <div class="profile-value">${window.escapeHtml(me.name)}</div>
          <div class="profile-label">メールアドレス</div>
          <div class="profile-value">${window.escapeHtml(me.email)}</div>
          <div class="profile-label">ステータス</div>
          <div class="profile-value" style="margin:0">${{ ACTIVE: '有効', INACTIVE: '無効' }[me.status] || window.escapeHtml(me.status)}</div>
        </div>

        <hr class="divider">

        <div class="profile-section-title">配送先住所</div>

        <form id="address-form" novalidate>
          <div class="address-form">
            <div class="form-group full-width">
              <label class="form-label" for="addr-postal">郵便番号</label>
              <input class="form-input" type="text" id="addr-postal"
                     placeholder="123-4567" maxlength="8"
                     value="${window.escapeHtml(a.postalCode || '')}">
            </div>
            <div class="form-group">
              <label class="form-label" for="addr-pref">都道府県</label>
              <input class="form-input" type="text" id="addr-pref"
                     placeholder="東京都"
                     value="${window.escapeHtml(a.prefecture || '')}">
            </div>
            <div class="form-group">
              <label class="form-label" for="addr-city">市区町村</label>
              <input class="form-input" type="text" id="addr-city"
                     placeholder="渋谷区"
                     value="${window.escapeHtml(a.city || '')}">
            </div>
            <div class="form-group full-width">
              <label class="form-label" for="addr-street">番地・建物名</label>
              <input class="form-input" type="text" id="addr-street"
                     placeholder="1-2-3 〇〇ビル101"
                     value="${window.escapeHtml(a.streetAddress || '')}">
            </div>
          </div>

          <div id="addr-error" class="form-error" style="display:none; margin-bottom:12px;"></div>

          <div class="profile-form-actions">
            <button type="submit" class="btn-primary" id="addr-save">
              <span>SAVE ADDRESS</span>
            </button>
          </div>
        </form>

        <hr class="divider">

        <div class="profile-footer-actions">
          <a href="#/orders" class="btn-secondary">VIEW ORDERS →</a>
        </div>
      </div>
    `;

    document.getElementById('address-form').addEventListener('submit', async (e) => {
      e.preventDefault();
      const postalCode    = document.getElementById('addr-postal').value.trim();
      const prefecture    = document.getElementById('addr-pref').value.trim();
      const city          = document.getElementById('addr-city').value.trim();
      const streetAddress = document.getElementById('addr-street').value.trim();
      const errEl         = document.getElementById('addr-error');
      const btn           = document.getElementById('addr-save');
      const span          = btn.querySelector('span');

      if (!postalCode || !prefecture || !city || !streetAddress) {
        errEl.textContent = 'すべての住所情報を入力してください';
        errEl.style.display = 'block';
        return;
      }

      btn.disabled = true;
      span.textContent = 'SAVING...';
      errEl.style.display = 'none';

      try {
        await api.updateAddress({ postalCode, prefecture, city, streetAddress });
        window.toast('住所を保存しました', 'success');
        span.textContent = 'SAVED ✓';
        setTimeout(() => {
          span.textContent = 'SAVE ADDRESS';
          btn.disabled = false;
        }, 2000);
      } catch (err) {
        btn.disabled = false;
        span.textContent = 'SAVE ADDRESS';
        errEl.textContent = err.message || '保存に失敗しました';
        errEl.style.display = 'block';
      }
    });

  } catch (e) {
    container.innerHTML = `
      <div class="error-view">
        <h1>ERROR</h1>
        <p>${window.escapeHtml(e.message || 'プロフィールの読み込みに失敗しました')}</p>
        <a href="#/" class="btn-primary">BACK TO SHOP</a>
      </div>`;
  }
}

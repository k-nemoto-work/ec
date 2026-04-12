// ============================================
// FAVORITES.JS — Saved items view
// ============================================
import { api } from './api.js';


export async function mountFavorites(container) {
  container.innerHTML = `<div class="loading-view"><div class="loading-spinner"></div></div>`;

  try {
    const data = await api.getFavorites();
    const items = data?.items || [];

    if (items.length === 0) {
      container.innerHTML = `
        <div class="view-favorites">
          <h1>SAVED ITEMS</h1>
          <div class="empty-state">
            <div class="empty-stamp">NOTHING SAVED</div>
            <p>お気に入りに追加した商品はありません</p>
            <br>
            <a href="#/" class="btn-primary">BROWSE SHOP</a>
          </div>
        </div>`;
      return;
    }

    container.innerHTML = `
      <div class="view-favorites">
        <h1>SAVED ITEMS</h1>
        <div class="products-grid">
          ${items.map((item, i) => `
            <div class="product-card" style="--card-rotation:${i % 2 === 0 ? 1.2 : -1.2}; --index:${i}; position:relative;">
              <a href="#/product/${item.productId}" style="display:contents; text-decoration:none; color:inherit;">
                <div class="card-image">${window.productPattern(item.productId)}</div>
                <div class="card-body">
                  ${window.statusStamp(item.status)}
                  <h3 class="card-title">${window.escapeHtml(item.productName)}</h3>
                  <div class="card-price">${window.formatPrice(item.price)}</div>
                </div>
              </a>
              <button class="fav-btn active" data-pid="${item.productId}" aria-label="お気に入りから削除">♥</button>
            </div>
          `).join('')}
        </div>
      </div>
    `;

    container.querySelectorAll('.fav-btn').forEach(btn => {
      btn.addEventListener('click', async (e) => {
        e.preventDefault();
        e.stopPropagation();
        const pid = btn.dataset.pid;
        btn.disabled = true;
        try {
          await api.removeFavorite(pid);
          window.toast('お気に入りから削除しました', 'info');
          await mountFavorites(container);
        } catch (err) {
          window.toast(err.message || 'エラーが発生しました', 'error');
          btn.disabled = false;
        }
      });
    });

  } catch (e) {
    container.innerHTML = `
      <div class="error-view">
        <h1>ERROR</h1>
        <p>${window.escapeHtml(e.message || 'お気に入りの読み込みに失敗しました')}</p>
        <a href="#/" class="btn-primary">BACK TO SHOP</a>
      </div>`;
  }
}

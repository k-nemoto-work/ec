// ============================================
// FAVORITES.JS — Saved items view
// ============================================
import { api } from './api.js';

function productPattern(id) {
  const seed = (id || 'default').replace(/-/g, '');
  function hi(i) { return parseInt(seed.slice(i * 2, i * 2 + 2) || 'aa', 16); }
  const h1 = hi(0) % 360;
  const h2 = (h1 + 137) % 360;
  const cx1 = hi(1) % 80 + 10;
  const cy1 = hi(2) % 80 + 10;
  const r1  = hi(3) % 35 + 25;
  const cx2 = 200 - (hi(4) % 80 + 10);
  const cy2 = 200 - (hi(5) % 80 + 10);

  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200" preserveAspectRatio="xMidYMid slice" width="100%" height="100%">
    <rect width="200" height="200" fill="hsl(${h1},18%,86%)"/>
    <circle cx="${cx1}" cy="${cy1}" r="${r1}" fill="hsl(${h1},38%,72%)" opacity="0.55"/>
    <circle cx="${cx2}" cy="${cy2}" r="${r1 * 0.65}" fill="hsl(${h2},42%,68%)" opacity="0.45"/>
  </svg>`;
}

function statusStamp(status) {
  const labels = { ON_SALE: '販売中', RESERVED: '予約済', SOLD: '売約済' };
  const cls    = { ON_SALE: 'on-sale', RESERVED: 'reserved', SOLD: 'sold' };
  return `<span class="status-stamp ${cls[status] || ''}">${labels[status] || status}</span>`;
}

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
                <div class="card-image">${productPattern(item.productId)}</div>
                <div class="card-body">
                  ${statusStamp(item.status)}
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

// ============================================
// PRODUCTS.JS — Product listing + detail views
// ============================================
import { api, Auth } from './api.js';
import { updateNav } from './app.js';

const CATEGORIES = [
  { id: 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', name: '古着' },
  { id: 'b2c3d4e5-f6a7-8901-bcde-f12345678901', name: '家電' },
  { id: 'c3d4e5f6-a7b8-9012-cdef-123456789012', name: '雑貨' },
  { id: 'd4e5f6a7-b8c9-0123-def0-234567890123', name: '本・メディア' },
  { id: 'e5f6a7b8-c9d0-1234-ef01-345678901234', name: 'スポーツ・アウトドア' },
];

function getCategoryName(id) {
  const c = CATEGORIES.find(c => c.id === id);
  return c ? c.name : '';
}

// Deterministic geometric SVG from product ID
function productPattern(id) {
  const seed = (id || 'default').replace(/-/g, '');
  function hi(i) { return parseInt(seed.slice(i * 2, i * 2 + 2) || 'aa', 16); }
  const h1 = hi(0) % 360;
  const h2 = (h1 + 137) % 360;
  const h3 = (h1 + 270) % 360;
  const cx1 = hi(1) % 80 + 10;
  const cy1 = hi(2) % 80 + 10;
  const r1  = hi(3) % 35 + 25;
  const cx2 = 200 - (hi(4) % 80 + 10);
  const cy2 = 200 - (hi(5) % 80 + 10);
  const rot = hi(6) % 60;
  const tx  = hi(7) % 120 + 40;
  const ty  = hi(8) % 120 + 40;

  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 200" preserveAspectRatio="xMidYMid slice" width="100%" height="100%">
    <rect width="200" height="200" fill="hsl(${h1},18%,86%)"/>
    <circle cx="${cx1}" cy="${cy1}" r="${r1}" fill="hsl(${h1},38%,72%)" opacity="0.55"/>
    <circle cx="${cx2}" cy="${cy2}" r="${r1 * 0.65}" fill="hsl(${h2},42%,68%)" opacity="0.45"/>
    <polygon points="${tx},${ty - r1 * 0.8} ${tx + r1 * 0.7},${ty + r1 * 0.5} ${tx - r1 * 0.7},${ty + r1 * 0.5}"
      fill="hsl(${h3},30%,60%)" opacity="0.3" transform="rotate(${rot} 100 100)"/>
    <line x1="0" y1="${hi(9) % 160 + 20}" x2="200" y2="${hi(10) % 160 + 20}"
      stroke="hsl(${h1},25%,55%)" stroke-width="1" opacity="0.2"/>
    <line x1="${hi(11) % 160 + 20}" y1="0" x2="${hi(12) % 160 + 20}" y2="200"
      stroke="hsl(${h2},20%,60%)" stroke-width="0.8" opacity="0.15"/>
  </svg>`;
}

function statusStamp(status) {
  const labels = { ON_SALE: '販売中', RESERVED: '予約済', SOLD: '売約済', PRIVATE: '非公開' };
  const cls    = { ON_SALE: 'on-sale', RESERVED: 'reserved', SOLD: 'sold', PRIVATE: 'reserved' };
  return `<span class="status-stamp ${cls[status] || ''}">${labels[status] || status}</span>`;
}

// ==========================================
// PRODUCT LISTING
// ==========================================
export async function mountProducts(container) {
  let currentPage = 0;
  let currentCategory = '';
  const PAGE_SIZE = 12;

  async function render(page, catId) {
    container.innerHTML = `
      <div class="view-products">
        <div class="hero-band">
          <div class="hero-text">
            <div class="hero-eyebrow">TOKYO THRIFT MARKET</div>
            <h1 class="hero-headline">FIND ITS<br><em>NEXT HOME</em></h1>
            <div class="hero-sub">厳選された中古品との出会い</div>
          </div>
          <div class="hero-decoration" aria-hidden="true">
            <span class="hero-deco-text">REUSE</span>
          </div>
        </div>

        <div class="filter-bar">
          <button class="filter-chip ${!catId ? 'active' : ''}" data-cat="">ALL</button>
          ${CATEGORIES.map(c =>
            `<button class="filter-chip ${catId === c.id ? 'active' : ''}" data-cat="${c.id}">${window.escapeHtml(c.name)}</button>`
          ).join('')}
        </div>

        <div id="products-grid" class="products-grid">
          ${[...Array(8)].map(() => '<div class="product-card skeleton"></div>').join('')}
        </div>

        <div id="products-pagination" class="pagination"></div>
      </div>
    `;

    container.querySelectorAll('.filter-chip').forEach(chip => {
      chip.addEventListener('click', () => {
        currentCategory = chip.dataset.cat;
        currentPage = 0;
        render(0, currentCategory);
      });
    });

    try {
      const params = { page, size: PAGE_SIZE };
      if (catId) params.categoryId = catId;
      const data = await api.getProducts(params);
      const grid = document.getElementById('products-grid');
      if (!grid) return;

      if (!data || !data.products || data.products.length === 0) {
        grid.innerHTML = `
          <div class="empty-state">
            <div class="empty-stamp">NO ITEMS</div>
            <p>このカテゴリには商品がありません</p>
          </div>`;
        return;
      }

      grid.innerHTML = data.products.map((p, i) => `
        <a href="#/product/${p.productId}" class="product-card"
           style="--card-rotation: ${i % 2 === 0 ? 1.2 : -1.2}; --index: ${i}">
          <div class="card-image">${productPattern(p.productId)}</div>
          <div class="card-body">
            ${statusStamp(p.status)}
            <h3 class="card-title">${window.escapeHtml(p.name)}</h3>
            <div class="card-price">${window.formatPrice(p.price)}</div>
            <div class="card-category">${window.escapeHtml(getCategoryName(p.categoryId))}</div>
          </div>
        </a>
      `).join('');

      // Pagination
      const totalPages = Math.ceil((data.totalCount || 0) / PAGE_SIZE);
      const pag = document.getElementById('products-pagination');
      if (pag && totalPages > 1) {
        pag.innerHTML = `
          ${page > 0 ? `<button class="pag-btn" data-page="${page - 1}">← PREV</button>` : ''}
          <span class="pag-info">${page + 1} / ${totalPages}</span>
          ${page < totalPages - 1 ? `<button class="pag-btn" data-page="${page + 1}">NEXT →</button>` : ''}
        `;
        pag.querySelectorAll('.pag-btn').forEach(btn => {
          btn.addEventListener('click', () => {
            currentPage = parseInt(btn.dataset.page);
            render(currentPage, currentCategory);
            window.scrollTo(0, 0);
          });
        });
      }
    } catch (e) {
      const grid = document.getElementById('products-grid');
      if (grid) grid.innerHTML = `<div class="error-msg">商品の読み込みに失敗しました: ${window.escapeHtml(e.message)}</div>`;
    }
  }

  await render(currentPage, currentCategory);
}

// ==========================================
// PRODUCT DETAIL
// ==========================================
export async function mountProductDetail(container, productId) {
  container.innerHTML = `<div class="loading-view"><div class="loading-spinner"></div></div>`;

  try {
    const [product, favorites] = await Promise.all([
      api.getProduct(productId),
      Auth.isLoggedIn() ? api.getFavorites().catch(() => null) : Promise.resolve(null),
    ]);

    if (!product) {
      container.innerHTML = `
        <div class="error-view">
          <h1>ITEM<br>NOT<br>FOUND</h1>
          <p>この商品は存在しないか、削除されました。</p>
          <a href="#/" class="btn-primary">BACK TO SHOP</a>
        </div>`;
      return;
    }

    const isFav  = (favorites?.items || []).some(f => String(f.productId) === String(productId));
    const canBuy = product.status === 'ON_SALE';
    const catName = getCategoryName(product.categoryId);

    container.innerHTML = `
      <div class="view-detail">
        <nav class="breadcrumb">
          <a href="#/">SHOP</a>
          <span>→</span>
          ${catName ? `<span>${window.escapeHtml(catName)}</span><span>→</span>` : ''}
          <span>${window.escapeHtml(product.name)}</span>
        </nav>

        <div class="detail-grid">
          <div class="detail-image-wrap">
            <div class="detail-image">${productPattern(productId)}</div>
            ${Auth.isLoggedIn() ? `
            <button class="fav-btn ${isFav ? 'active' : ''}" id="fav-btn" aria-label="お気に入り">
              ${isFav ? '♥' : '♡'}
            </button>` : ''}
          </div>

          <div class="detail-info">
            ${statusStamp(product.status)}
            ${catName ? `<div class="detail-category">${window.escapeHtml(catName)}</div>` : ''}
            <h1 class="detail-title">${window.escapeHtml(product.name)}</h1>
            <div class="detail-price">${window.formatPrice(product.price)}</div>
            ${product.description ? `<p class="detail-description">${window.escapeHtml(product.description)}</p>` : ''}

            ${canBuy && Auth.isLoggedIn() ? `
            <button class="btn-primary btn-cart" id="add-to-cart">
              <span>ADD TO BAG</span>
            </button>` : ''}
            ${canBuy && !Auth.isLoggedIn() ? `
            <a href="#/login" class="btn-primary btn-cart">LOG IN TO BUY</a>` : ''}
            ${!canBuy ? `<div class="btn-unavailable">${product.status === 'RESERVED' ? '現在予約済みです' : '売約済みです'}</div>` : ''}

            <a href="#/" class="btn-secondary">← BACK TO SHOP</a>
          </div>
        </div>
      </div>
    `;

    // Add to cart
    const cartBtn = document.getElementById('add-to-cart');
    if (cartBtn) {
      cartBtn.addEventListener('click', async () => {
        cartBtn.disabled = true;
        const span = cartBtn.querySelector('span');
        span.textContent = 'ADDING...';
        try {
          await api.addToCart(productId);
          window.toast('カートに追加しました', 'success');
          span.textContent = 'ADDED ✓';
          updateNav();
        } catch (e) {
          window.toast(e.message || 'エラーが発生しました', 'error');
          cartBtn.disabled = false;
          span.textContent = 'ADD TO BAG';
        }
      });
    }

    // Favorite toggle
    const favBtn = document.getElementById('fav-btn');
    if (favBtn) {
      let favState = isFav;
      favBtn.addEventListener('click', async () => {
        favBtn.disabled = true;
        try {
          if (favState) {
            await api.removeFavorite(productId);
            favState = false;
            favBtn.textContent = '♡';
            favBtn.classList.remove('active');
            window.toast('お気に入りから削除しました', 'info');
          } else {
            await api.addFavorite(productId);
            favState = true;
            favBtn.textContent = '♥';
            favBtn.classList.add('active');
            window.toast('お気に入りに追加しました', 'success');
          }
        } catch (e) {
          window.toast(e.message || 'エラーが発生しました', 'error');
        }
        favBtn.disabled = false;
      });
    }

  } catch (e) {
    container.innerHTML = `
      <div class="error-view">
        <h1>ERROR</h1>
        <p>${window.escapeHtml(e.message || '商品の読み込みに失敗しました')}</p>
        <a href="#/" class="btn-primary">BACK TO SHOP</a>
      </div>`;
  }
}

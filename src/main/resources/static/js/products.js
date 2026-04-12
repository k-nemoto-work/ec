// ============================================
// PRODUCTS.JS — Product listing + detail views
// ============================================
import { api, Auth } from './api.js';
import { updateNav } from './app.js';

let CATEGORIES = [];

async function loadCategories() {
  try {
    const data = await api.getCategories();
    CATEGORIES = data || [];
  } catch (_) {
    CATEGORIES = [];
  }
}

function getCategoryName(id) {
  const c = CATEGORIES.find(c => String(c.id) === String(id));
  return c ? c.name : '';
}


// ==========================================
// PRODUCT LISTING
// ==========================================
export async function mountProducts(container) {
  let currentPage = 0;
  let currentCategory = '';
  const PAGE_SIZE = 12;

  await loadCategories();

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
          <div class="card-image">${window.productPattern(p.productId)}</div>
          <div class="card-body">
            ${window.statusStamp(p.status)}
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
            <div class="detail-image">${window.productPattern(productId)}</div>
            ${Auth.isLoggedIn() ? `
            <button class="fav-btn ${isFav ? 'active' : ''}" id="fav-btn" aria-label="お気に入り">
              ${isFav ? '♥' : '♡'}
            </button>` : ''}
          </div>

          <div class="detail-info">
            ${window.statusStamp(product.status)}
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

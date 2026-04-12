// ============================================
// APP.JS — Router + Nav + Global init
// ============================================
import { Auth, api }                        from './api.js';
import { mountProducts, mountProductDetail } from './products.js';
import { mountAuth }                         from './auth.js';
import { mountCart }                         from './cart.js';
import { mountOrders, mountOrderDetail }     from './orders.js';
import { mountProfile }                      from './profile.js';
import { mountFavorites }                    from './favorites.js';

const root = document.getElementById('app-root');

// --- Nav update ---
export async function updateNav() {
  const loggedIn = Auth.isLoggedIn();

  document.getElementById('nav-login').style.display   = loggedIn ? 'none' : '';
  document.getElementById('nav-profile').style.display = loggedIn ? '' : 'none';
  document.getElementById('nav-orders').style.display  = loggedIn ? '' : 'none';
  document.getElementById('nav-logout').style.display  = loggedIn ? '' : 'none';
  document.getElementById('mob-login').style.display   = loggedIn ? 'none' : '';
  document.getElementById('mob-profile').style.display = loggedIn ? '' : 'none';
  document.getElementById('mob-orders').style.display  = loggedIn ? '' : 'none';
  document.getElementById('mob-logout').style.display  = loggedIn ? '' : 'none';

  if (loggedIn) {
    try {
      const cart = await api.getCart();
      const count = cart ? (cart.items || []).length : 0;
      document.getElementById('cart-badge').textContent     = count;
      document.getElementById('mob-cart-badge').textContent = count;
    } catch (_) { /* ignore */ }
  } else {
    document.getElementById('cart-badge').textContent     = '0';
    document.getElementById('mob-cart-badge').textContent = '0';
  }
}

// --- Hash parser ---
function parseHash() {
  const hash = (window.location.hash || '#/').replace(/^#/, '') || '/';
  const parts = hash.split('/').filter(Boolean);
  return parts;
}

// --- Router ---
const PROTECTED = new Set(['cart', 'orders', 'profile', 'favorites']);

async function route() {
  const parts = parseHash();
  const view  = parts[0] || '';
  const param = parts[1] || '';

  // Close mobile nav
  const mobile    = document.getElementById('nav-mobile');
  const hamburger = document.getElementById('hamburger');
  mobile.classList.remove('open');
  hamburger.classList.remove('active');
  hamburger.setAttribute('aria-expanded', 'false');
  mobile.setAttribute('aria-hidden', 'true');

  window.scrollTo(0, 0);
  root.innerHTML = '';

  // Auth guard
  if (PROTECTED.has(view) && !Auth.isLoggedIn()) {
    window.location.hash = '#/login';
    return;
  }

  try {
    if (view === '' || view === 'home') {
      await mountProducts(root);
    } else if (view === 'product' && param) {
      await mountProductDetail(root, param);
    } else if (view === 'login' || view === 'register') {
      mountAuth(root, view);
    } else if (view === 'cart') {
      await mountCart(root);
    } else if (view === 'orders' && param) {
      await mountOrderDetail(root, param);
    } else if (view === 'orders') {
      await mountOrders(root);
    } else if (view === 'profile') {
      await mountProfile(root);
    } else if (view === 'favorites') {
      await mountFavorites(root);
    } else {
      root.innerHTML = `
        <div class="error-view">
          <h1>PAGE<br>NOT FOUND</h1>
          <p>お探しのページは存在しません。</p>
          <a href="#/" class="btn-primary">BACK TO SHOP</a>
        </div>`;
    }
  } catch (e) {
    console.error('Route error:', e);
    root.innerHTML = `
      <div class="error-view">
        <h1>ERROR</h1>
        <p>${window.escapeHtml(e.message || '予期しないエラーが発生しました')}</p>
        <a href="#/" class="btn-primary">BACK TO SHOP</a>
      </div>`;
  }

  updateNav();
}

// --- Hamburger ---
document.getElementById('hamburger').addEventListener('click', function() {
  const mobile    = document.getElementById('nav-mobile');
  const open      = mobile.classList.toggle('open');
  this.classList.toggle('active', open);
  this.setAttribute('aria-expanded', String(open));
  mobile.setAttribute('aria-hidden', String(!open));
});

// --- Logout handlers ---
['nav-logout', 'mob-logout'].forEach(id => {
  document.getElementById(id).addEventListener('click', () => {
    Auth.clear();
    window.toast('ログアウトしました', 'info');
    updateNav();
    window.location.hash = '#/';
  });
});

// --- Boot ---
window.addEventListener('hashchange', route);
window.addEventListener('load', route);

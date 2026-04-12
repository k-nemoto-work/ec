// ============================================
// API.JS — Auth + Fetch wrapper + All API calls
// ============================================

const TOKEN_KEY = 'reuse_jwt';

export const Auth = {
  save(token)   { localStorage.setItem(TOKEN_KEY, token); },
  get()         { return localStorage.getItem(TOKEN_KEY); },
  clear()       { localStorage.removeItem(TOKEN_KEY); },
  isLoggedIn()  { return !!localStorage.getItem(TOKEN_KEY); },
};

async function request(method, path, body = null) {
  const headers = { 'Content-Type': 'application/json' };
  const token = Auth.get();
  if (token) headers['Authorization'] = 'Bearer ' + token;

  let res;
  try {
    res = await fetch('/api/v1' + path, {
      method,
      headers,
      body: body != null ? JSON.stringify(body) : null,
    });
  } catch (e) {
    throw { message: 'ネットワークエラーが発生しました' };
  }

  if (res.status === 401) {
    Auth.clear();
    window.location.hash = '#/login';
    return null;
  }

  if (res.status === 204 || res.status === 201 && res.headers.get('content-length') === '0') {
    return null;
  }

  if (!res.ok) {
    let errBody = {};
    try { errBody = await res.json(); } catch (_) {}
    throw { message: errBody.message || `エラー (${res.status})`, status: res.status, body: errBody };
  }

  // 201 Created may have a body
  const ct = res.headers.get('content-type') || '';
  if (ct.includes('application/json')) {
    return res.json();
  }
  return null;
}

export const api = {
  // --- Auth ---
  login:    (email, password) => request('POST', '/auth/login',    { email, password }),
  register: (name, email, password) => request('POST', '/auth/register', { name, email, password }),

  // --- Products ---
  getProducts: (params = {}) => {
    const q = new URLSearchParams(params).toString();
    return request('GET', '/products' + (q ? '?' + q : ''));
  },
  getProduct:          (id) => request('GET',   `/products/${id}`),
  createProduct:     (data) => request('POST',  '/products', data),
  updateProduct:   (id, d)  => request('PUT',   `/products/${id}`, d),
  updateProductStatus: (id, status) => request('PATCH', `/products/${id}/status`, { status }),

  // --- Cart ---
  getCart:         ()    => request('GET',    '/cart'),
  addToCart:       (pid) => request('POST',   '/cart/items', { productId: pid }),
  removeFromCart:  (pid) => request('DELETE', `/cart/items/${pid}`),

  // --- Favorites ---
  getFavorites:    ()    => request('GET',    '/favorites'),
  addFavorite:     (pid) => request('POST',   '/favorites/items', { productId: pid }),
  removeFavorite:  (pid) => request('DELETE', `/favorites/items/${pid}`),

  // --- Orders ---
  placeOrder: (data) => request('POST', '/orders', data),
  getOrders:  (params = {}) => {
    const q = new URLSearchParams(params).toString();
    return request('GET', '/orders' + (q ? '?' + q : ''));
  },
  getOrder:        (id) => request('GET',   `/orders/${id}`),
  cancelOrder:     (id) => request('PATCH', `/orders/${id}/cancel`),
  updatePayment:   (id) => request('PATCH', `/orders/${id}/payment`),
  updateShipment:  (id, status) => request('PATCH', `/orders/${id}/shipment`, { status }),

  // --- Customer ---
  getMe:          ()     => request('GET', '/customers/me'),
  updateAddress:  (data) => request('PUT', '/customers/me/address', data),
};

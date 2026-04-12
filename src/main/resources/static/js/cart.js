// ============================================
// CART.JS — Cart view + Order placement
// ============================================
import { api } from './api.js';
import { updateNav } from './app.js';

const PAYMENT_METHODS = [
  { value: 'CREDIT_CARD',   label: 'クレジットカード' },
  { value: 'BANK_TRANSFER', label: '銀行振込' },
];

export async function mountCart(container) {
  container.innerHTML = `<div class="loading-view"><div class="loading-spinner"></div></div>`;

  try {
    const cart = await api.getCart();
    if (!cart) {
      container.innerHTML = `
        <div class="view-cart">
          <h1>MY BAG</h1>
          <div class="empty-state">
            <div class="empty-stamp">EMPTY BAG</div>
            <p>カートに商品がありません</p>
            <br>
            <a href="#/" class="btn-primary">BROWSE SHOP</a>
          </div>
        </div>`;
      return;
    }

    const items = cart.items || [];
    if (items.length === 0) {
      container.innerHTML = `
        <div class="view-cart">
          <h1>MY BAG</h1>
          <div class="empty-state">
            <div class="empty-stamp">EMPTY BAG</div>
            <p>カートに商品がありません</p>
            <br>
            <a href="#/" class="btn-primary">BROWSE SHOP</a>
          </div>
        </div>`;
      return;
    }

    const canOrderItems = items.filter(i => i.status === 'ON_SALE');

    container.innerHTML = `
      <div class="view-cart">
        <h1>MY BAG</h1>

        <table class="cart-table">
          <thead>
            <tr>
              <th>ITEM</th>
              <th>STATUS</th>
              <th style="text-align:right">PRICE</th>
              <th></th>
            </tr>
          </thead>
          <tbody id="cart-tbody">
            ${items.map((item, i) => `
              <tr style="--index:${i}">
                <td>
                  <a href="#/product/${item.productId}" class="cart-item-name">
                    ${window.escapeHtml(item.productName)}
                  </a>
                </td>
                <td>
                  <span class="status-stamp ${statusCls(item.status)}">${statusLabel(item.status)}</span>
                </td>
                <td class="cart-item-price" style="text-align:right">
                  ${window.formatPrice(item.price)}
                </td>
                <td style="text-align:right">
                  <button class="cart-item-remove" data-pid="${item.productId}" aria-label="削除">✕</button>
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>

        <div class="cart-total-row">
          <span class="cart-total-label">TOTAL</span>
          <span class="cart-total-amount">${window.formatPrice(cart.totalAmount)}</span>
        </div>

        <div class="cart-actions">
          <a href="#/" class="btn-secondary">CONTINUE SHOPPING</a>
          ${canOrderItems.length > 0
            ? `<button class="btn-primary" id="btn-proceed">PROCEED TO ORDER →</button>`
            : `<div class="btn-unavailable">購入可能な商品がありません</div>`
          }
        </div>

        <div class="order-form" id="order-form">
          <h3>配送・お支払い情報</h3>
          <div class="order-form-grid">
            <div class="form-group full-width">
              <label class="form-label">お支払い方法</label>
              <select class="form-select" id="payment-method">
                ${PAYMENT_METHODS.map(m => `<option value="${m.value}">${m.label}</option>`).join('')}
              </select>
            </div>
            <div class="form-group full-width">
              <label class="form-label" for="postal-code">郵便番号</label>
              <input class="form-input" type="text" id="postal-code" placeholder="123-4567" maxlength="8">
            </div>
            <div class="form-group">
              <label class="form-label" for="prefecture">都道府県</label>
              <input class="form-input" type="text" id="prefecture" placeholder="東京都">
            </div>
            <div class="form-group">
              <label class="form-label" for="city">市区町村</label>
              <input class="form-input" type="text" id="city" placeholder="渋谷区">
            </div>
            <div class="form-group full-width">
              <label class="form-label" for="street">番地・建物名</label>
              <input class="form-input" type="text" id="street" placeholder="1-2-3 〇〇ビル101">
            </div>
          </div>

          <div id="order-error" class="form-error" style="display:none; margin-top:12px;"></div>

          <div class="form-actions-end">
            <button class="btn-secondary" id="btn-cancel-order">キャンセル</button>
            <button class="btn-primary" id="btn-place-order"><span>ORDER NOW</span></button>
          </div>
        </div>
      </div>
    `;

    // Pre-fill address from profile if available
    prefillAddress();

    // Remove item handlers
    container.querySelectorAll('.cart-item-remove').forEach(btn => {
      btn.addEventListener('click', async () => {
        const pid = btn.dataset.pid;
        btn.disabled = true;
        btn.textContent = '…';
        try {
          await api.removeFromCart(pid);
          window.toast('カートから削除しました', 'info');
          await updateNav();
          await mountCart(container);
        } catch (e) {
          window.toast(e.message || 'エラーが発生しました', 'error');
          btn.disabled = false;
          btn.textContent = '✕';
        }
      });
    });

    // Proceed button
    const proceedBtn = document.getElementById('btn-proceed');
    if (proceedBtn) {
      proceedBtn.addEventListener('click', () => {
        const form = document.getElementById('order-form');
        form.classList.toggle('visible');
        if (form.classList.contains('visible')) {
          form.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }
      });
    }

    // Cancel order form
    const cancelBtn = document.getElementById('btn-cancel-order');
    if (cancelBtn) {
      cancelBtn.addEventListener('click', () => {
        document.getElementById('order-form').classList.remove('visible');
      });
    }

    // Place order
    const placeBtn = document.getElementById('btn-place-order');
    if (placeBtn) {
      placeBtn.addEventListener('click', async () => {
        const postalCode  = document.getElementById('postal-code').value.trim();
        const prefecture  = document.getElementById('prefecture').value.trim();
        const city        = document.getElementById('city').value.trim();
        const street      = document.getElementById('street').value.trim();
        const paymentMethod = document.getElementById('payment-method').value;
        const errEl       = document.getElementById('order-error');

        if (!postalCode || !prefecture || !city || !street) {
          errEl.textContent = 'すべての配送情報を入力してください';
          errEl.style.display = 'block';
          return;
        }

        placeBtn.disabled = true;
        placeBtn.querySelector('span').textContent = 'PLACING ORDER...';
        errEl.style.display = 'none';

        try {
          await api.placeOrder({
            paymentMethod,
            shippingPostalCode:    postalCode,
            shippingPrefecture:    prefecture,
            shippingCity:          city,
            shippingStreetAddress: street,
          });
          window.toast('注文が確定しました', 'success');
          await updateNav();
          window.location.hash = '#/orders';
        } catch (e) {
          placeBtn.disabled = false;
          placeBtn.querySelector('span').textContent = 'ORDER NOW';
          errEl.textContent = e.message || '注文処理中にエラーが発生しました';
          errEl.style.display = 'block';
        }
      });
    }

  } catch (e) {
    container.innerHTML = `
      <div class="error-view">
        <h1>ERROR</h1>
        <p>${window.escapeHtml(e.message || 'カートの読み込みに失敗しました')}</p>
        <a href="#/" class="btn-primary">BACK TO SHOP</a>
      </div>`;
  }
}

async function prefillAddress() {
  try {
    const me = await api.getMe();
    if (me && me.address) {
      const a = me.address;
      if (document.getElementById('postal-code'))  document.getElementById('postal-code').value  = a.postalCode  || '';
      if (document.getElementById('prefecture'))   document.getElementById('prefecture').value   = a.prefecture  || '';
      if (document.getElementById('city'))         document.getElementById('city').value         = a.city        || '';
      if (document.getElementById('street'))       document.getElementById('street').value       = a.streetAddress || '';
    }
  } catch (_) { /* ignore */ }
}

function statusLabel(s) {
  const m = { ON_SALE: '販売中', RESERVED: '予約済', SOLD: '売約済' };
  return m[s] || s;
}

function statusCls(s) {
  const m = { ON_SALE: 'on-sale', RESERVED: 'reserved', SOLD: 'sold' };
  return m[s] || '';
}

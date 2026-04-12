// ============================================
// ORDERS.JS — Order list + Order detail views
// ============================================
import { api } from './api.js';

const ORDER_STATUS_LABELS = {
  CONFIRMED:  '注文確定',
  SHIPPING:   '配送中',
  DELIVERED:  '配送完了',
  CANCELLED:  'キャンセル',
};

const PAYMENT_LABELS = {
  CREDIT_CARD:   'クレジットカード',
  BANK_TRANSFER: '銀行振込',
};

const SHIPMENT_LABELS = {
  NOT_SHIPPED: '未発送',
  SHIPPED:     '発送済',
  DELIVERED:   '配達完了',
};

function formatDate(str) {
  if (!str) return '-';
  try {
    return new Date(str).toLocaleDateString('ja-JP', {
      year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit'
    });
  } catch (_) { return str; }
}

// ==========================================
// ORDER LIST
// ==========================================
export async function mountOrders(container) {
  container.innerHTML = `<div class="loading-view"><div class="loading-spinner"></div></div>`;

  try {
    const data = await api.getOrders({ page: 0, size: 20 });
    const orders = data?.orders || [];

    if (orders.length === 0) {
      container.innerHTML = `
        <div class="view-orders">
          <h1>MY ORDERS</h1>
          <div class="empty-state">
            <div class="empty-stamp">NO ORDERS</div>
            <p>注文履歴がありません</p>
            <br>
            <a href="#/" class="btn-primary">BROWSE SHOP</a>
          </div>
        </div>`;
      return;
    }

    container.innerHTML = `
      <div class="view-orders">
        <h1>MY ORDERS</h1>
        <div class="order-list">
          ${orders.map((o, i) => `
            <a href="#/orders/${o.orderId}" class="order-card" style="--index:${i}">
              <div class="order-card-left">
                <div class="order-card-id">ORDER #${String(o.orderId).slice(0, 8).toUpperCase()}</div>
                <div class="order-card-info">
                  <span class="order-card-date">${formatDate(o.orderedAt)}</span>
                  <span class="order-card-items">${o.itemCount}点</span>
                </div>
              </div>
              <div class="order-card-right">
                <span class="order-status ${o.status}">${ORDER_STATUS_LABELS[o.status] || o.status}</span>
                <div class="order-card-amount">${window.formatPrice(o.totalAmount)}</div>
              </div>
            </a>
          `).join('')}
        </div>
      </div>
    `;
  } catch (e) {
    container.innerHTML = `
      <div class="error-view">
        <h1>ERROR</h1>
        <p>${window.escapeHtml(e.message || '注文履歴の読み込みに失敗しました')}</p>
        <a href="#/orders" class="btn-primary">RETRY</a>
      </div>`;
  }
}

// ==========================================
// ORDER DETAIL
// ==========================================
export async function mountOrderDetail(container, orderId) {
  container.innerHTML = `<div class="loading-view"><div class="loading-spinner"></div></div>`;

  try {
    const order = await api.getOrder(orderId);
    if (!order) {
      container.innerHTML = `
        <div class="error-view">
          <h1>ORDER<br>NOT FOUND</h1>
          <a href="#/orders" class="btn-primary">BACK TO ORDERS</a>
        </div>`;
      return;
    }

    const canCancel = order.status === 'CONFIRMED';

    container.innerHTML = `
      <div class="view-order-detail">
        <nav class="breadcrumb">
          <a href="#/orders">ORDERS</a>
          <span>→</span>
          <span>#${String(order.orderId).slice(0, 8).toUpperCase()}</span>
        </nav>

        <div class="order-detail-header">
          <div class="order-detail-id">ORDER ID: ${order.orderId}</div>
          <div style="margin-top:12px">
            <span class="order-status ${order.status}">${ORDER_STATUS_LABELS[order.status] || order.status}</span>
          </div>
          <div class="order-detail-date">${formatDate(order.orderedAt)}</div>
        </div>

        <div class="order-detail-section">
          <div class="order-detail-section-title">ORDER ITEMS</div>
          ${(order.items || []).map(item => `
            <div class="order-item-row">
              <span class="order-item-name">${window.escapeHtml(item.productNameSnapshot)}</span>
              <span class="order-item-price">${window.formatPrice(item.priceSnapshot)}</span>
            </div>
          `).join('')}
          <div class="order-total-row">
            <span class="order-total-label">TOTAL</span>
            <span class="order-total-amount">${window.formatPrice(order.totalAmount)}</span>
          </div>
        </div>

        <div class="order-detail-section">
          <div class="order-detail-section-title">SHIPPING & PAYMENT</div>
          <div class="order-detail-meta">
            <div class="order-meta-item">
              <div class="order-meta-label">お支払い方法</div>
              <div class="order-meta-value">${PAYMENT_LABELS[order.paymentMethod] || order.paymentMethod}</div>
            </div>
            <div class="order-meta-item">
              <div class="order-meta-label">お支払い状態</div>
              <div class="order-meta-value">${window.escapeHtml(order.paymentStatus || '-')}</div>
            </div>
            <div class="order-meta-item">
              <div class="order-meta-label">配送状態</div>
              <div class="order-meta-value">${SHIPMENT_LABELS[order.shipmentStatus] || order.shipmentStatus}</div>
            </div>
            <div class="order-meta-item">
              <div class="order-meta-label">配送先</div>
              <div class="order-meta-value">
                〒${window.escapeHtml(order.shippingPostalCode)}<br>
                ${window.escapeHtml(order.shippingPrefecture)}${window.escapeHtml(order.shippingCity)}<br>
                ${window.escapeHtml(order.shippingStreetAddress)}
              </div>
            </div>
          </div>
        </div>

        <div class="order-detail-actions">
          <a href="#/orders" class="btn-secondary">← BACK</a>
          ${canCancel ? `<button class="btn-danger" id="btn-cancel">CANCEL ORDER</button>` : ''}
        </div>
      </div>
    `;

    const cancelBtn = document.getElementById('btn-cancel');
    if (cancelBtn) {
      cancelBtn.addEventListener('click', async () => {
        if (!confirm('この注文をキャンセルしますか？')) return;
        cancelBtn.disabled = true;
        cancelBtn.textContent = 'CANCELLING...';
        try {
          await api.cancelOrder(orderId);
          window.toast('注文をキャンセルしました', 'info');
          await mountOrderDetail(container, orderId);
        } catch (e) {
          window.toast(e.message || 'キャンセルに失敗しました', 'error');
          cancelBtn.disabled = false;
          cancelBtn.textContent = 'CANCEL ORDER';
        }
      });
    }

  } catch (e) {
    container.innerHTML = `
      <div class="error-view">
        <h1>ERROR</h1>
        <p>${window.escapeHtml(e.message || '注文の読み込みに失敗しました')}</p>
        <a href="#/orders" class="btn-primary">BACK TO ORDERS</a>
      </div>`;
  }
}

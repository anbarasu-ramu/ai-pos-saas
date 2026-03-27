import { inject, Injectable } from '@angular/core';
import {
  AiSuggestion,
  CartLine,
  MetricCard,
  OrderSummary,
  PageResponse,
  ProductSummary,
} from './pos.models';
import { signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class PosApiService {
  private readonly cartLines = signal<CartLine[]>([]);
  private http = inject(HttpClient);

  // 🔹 Dashboard (unchanged)
  getDashboardMetrics(): MetricCard[] {
    return [
      { label: 'Sales Today', value: '$4,820', trend: '+12% vs yesterday' },
      { label: 'Orders Open', value: '18', trend: '5 awaiting payment' },
      { label: 'Low Stock Alerts', value: '6', trend: '2 need urgent restock' },
    ];
  }

  // 🔹 Cart (readonly signal)
  getCartLines() {
    return this.cartLines.asReadonly();
  }

  // 🔥 Add to cart (immutable)
  addToCart(product: ProductSummary, quantity = 1): void {
  this.cartLines.update(lines => {
    const sku = String(product.id);
    const unitPrice = Number(product.price ?? 0);

    const existing = lines.find(line => line.sku === sku);

    if (existing) {
      return lines.map(line =>
        line.sku === sku
          ? {
              ...line,
              quantity: line.quantity + quantity,
              total: Number(((line.quantity + quantity) * unitPrice).toFixed(2))
            }
          : line
      );
    }

    return [
      ...lines,
      {
        sku : String(product.id),
        name: product.name,
        quantity,
        total: Number((quantity * unitPrice).toFixed(2))
      }
    ];
  });
}

  // 🔥 Increment
  increment(sku: string): void {
    this.cartLines.update(lines =>
      lines.map(line => {
        if (line.sku !== sku) return line;

        const unitPrice = line.total / line.quantity;
        const newQty = line.quantity + 1;

        return {
          ...line,
          quantity: newQty,
          total: Number((newQty * unitPrice).toFixed(2))
        };
      })
    );
  }

  // 🔥 Decrement
  decrement(sku: string): void {
    this.cartLines.update(lines =>
      lines
        .map(line => {
          if (line.sku !== sku) return line;

          if (line.quantity <= 1) return null;

          const unitPrice = line.total / line.quantity;
          const newQty = line.quantity - 1;

          return {
            ...line,
            quantity: newQty,
            total: Number((newQty * unitPrice).toFixed(2))
          };
        })
        .filter(Boolean) as CartLine[]
    );
  }

  // 🔥 Remove item
  removeFromCart(sku: string): void {
    this.cartLines.update(lines =>
      lines.filter(line => line.sku !== sku)
    );
  }

  // 🔥 Clear cart
  clearCart(): void {
    this.cartLines.set([]);
  }

  confirmOrder() {
    throw new Error('Method not implemented.');
  }

  checkout(request: any) {
  return this.http.post('/api/checkout', request);
}

  getOrders(page = 0, size = 20) {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);

    return this.http.get<PageResponse<OrderSummary>>('/api/orders', { params });
  }

  // 🔹 AI suggestions (unchanged)
  getAiSuggestions(): AiSuggestion[] {
    return [
      { title: 'Restock signal', detail: 'Blueberry Muffin is below the preferred threshold for the lunch rush.' },
      { title: 'Upsell idea', detail: 'Bundle paper cups with coffee beans for wholesale buyers.' },
      { title: 'Checkout guardrail', detail: 'Prompt for tenant-aware stock validation before payment capture.' },
    ];
  }
}

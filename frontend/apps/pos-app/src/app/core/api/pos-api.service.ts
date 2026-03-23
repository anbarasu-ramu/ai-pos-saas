import { Injectable } from '@angular/core';
import {
  AiSuggestion,
  CartLine,
  MetricCard,
  ProductSummary,
} from './pos.models';

@Injectable({ providedIn: 'root' })
export class PosApiService {
  private cartLines: CartLine[] = [];

  getDashboardMetrics(): MetricCard[] {
    return [
      { label: 'Sales Today', value: '$4,820', trend: '+12% vs yesterday' },
      { label: 'Orders Open', value: '18', trend: '5 awaiting payment' },
      { label: 'Low Stock Alerts', value: '6', trend: '2 need urgent restock' },
    ];
  }

  // getProducts(): ProductSummary[] {
  //   return [
  //     { id: 'SKU-101', name: 'Arabica Beans 1kg', price: 24.99, stockQuantity: 14, category: 'Inventory', active: true },
  //     { id: 'SKU-102', name: 'Paper Cups', price: 5.5, stockQuantity: 200, category: 'Supplies', active: true },
  //     { id: 'SKU-103', name: 'Blueberry Muffin', price: 3.99, stockQuantity: 9, category: 'Bakery', active: true },
  //   ];
  // }

  getCartLines(): CartLine[] {
    return this.cartLines;
  }

  addToCart(product: ProductSummary, quantity = 1): void {
    const existing = this.cartLines.find((line) => line.sku === product.id);
    const unitPrice = product.price;

    if (existing) {
      existing.quantity += quantity;
      existing.total = Number((existing.quantity * unitPrice).toFixed(2));
    } else {
      this.cartLines.push({
        sku: product.id,
        name: product.name,
        quantity,
        total: Number((quantity * unitPrice).toFixed(2)),
      });
    }
  }

  removeFromCart(sku: string): void {
    this.cartLines = this.cartLines.filter((line) => line.sku !== sku);
  }

  clearCart(): void {
    this.cartLines = [];
  }

  getAiSuggestions(): AiSuggestion[] {
    return [
      { title: 'Restock signal', detail: 'Blueberry Muffin is below the preferred threshold for the lunch rush.' },
      { title: 'Upsell idea', detail: 'Bundle paper cups with coffee beans for wholesale buyers.' },
      { title: 'Checkout guardrail', detail: 'Prompt for tenant-aware stock validation before payment capture.' },
    ];
  }
}

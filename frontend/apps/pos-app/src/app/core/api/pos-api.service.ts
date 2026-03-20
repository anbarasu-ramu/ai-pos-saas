import { Injectable } from '@angular/core';
import { getAppConfig } from '../config/app-config';
import {
  AiSuggestion,
  CartLine,
  MetricCard,
  ProductSummary,
} from './pos.models';

@Injectable({ providedIn: 'root' })
export class PosApiService {
  private readonly appConfig = getAppConfig();

  getAuthStatusUrl(): string {
    return `${this.appConfig.apiBaseUrl}/api/auth/status`;
  }

  getDashboardMetrics(): MetricCard[] {
    return [
      { label: 'Sales Today', value: '$4,820', trend: '+12% vs yesterday' },
      { label: 'Orders Open', value: '18', trend: '5 awaiting payment' },
      { label: 'Low Stock Alerts', value: '6', trend: '2 need urgent restock' },
    ];
  }

  getProducts(): ProductSummary[] {
    return [
      { id: 'SKU-101', name: 'Arabica Beans 1kg', price: 24.99, stockQuantity: 14, category: 'Inventory' },
      { id: 'SKU-102', name: 'Paper Cups', price: 5.5, stockQuantity: 200, category: 'Supplies' },
      { id: 'SKU-103', name: 'Blueberry Muffin', price: 3.99, stockQuantity: 9, category: 'Bakery' },
    ];
  }

  getCartLines(): CartLine[] {
    return [
      { sku: 'SKU-103', name: 'Blueberry Muffin', quantity: 2, total: 7.98 },
      { sku: 'SKU-101', name: 'Arabica Beans 1kg', quantity: 1, total: 24.99 },
    ];
  }

  getAiSuggestions(): AiSuggestion[] {
    return [
      { title: 'Restock signal', detail: 'Blueberry Muffin is below the preferred threshold for the lunch rush.' },
      { title: 'Upsell idea', detail: 'Bundle paper cups with coffee beans for wholesale buyers.' },
      { title: 'Checkout guardrail', detail: 'Prompt for tenant-aware stock validation before payment capture.' },
    ];
  }
}

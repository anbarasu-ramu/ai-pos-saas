import { CommonModule, CurrencyPipe } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { PosApiService } from '../../core/api/pos-api.service';

@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  templateUrl: './cart-page.component.html',
  styleUrls: ['./cart-page.component.css'],
})
export class CartPageComponent {
  private readonly api = inject(PosApiService);
  protected readonly lines = this.api.getCartLines();
  protected readonly total = computed(() => this.lines.reduce((sum, line) => sum + line.total, 0));

  protected removeItem(sku: string): void {
    this.api.removeFromCart(sku);
  }

  protected clearCart(): void {
    this.api.clearCart();
  }

  protected increment(line: { sku: string; quantity: number; total: number }): void {
    const unitPrice = line.quantity ? line.total / line.quantity : 0;
    line.quantity += 1;
    line.total = Number((line.quantity * unitPrice).toFixed(2));
  }

  protected decrement(line: { sku: string; quantity: number; total: number }): void {
    if (line.quantity <= 1) {
      this.removeItem(line.sku);
      return;
    }

    const unitPrice = line.total / line.quantity;
    line.quantity -= 1;
    line.total = Number((line.quantity * unitPrice).toFixed(2));
  }
}

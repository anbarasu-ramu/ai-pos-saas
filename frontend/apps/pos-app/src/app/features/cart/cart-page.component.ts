import { CommonModule, CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { PosApiService } from '../../core/api/pos-api.service';
import { CartLine } from '../../core/api/pos.models';
import { NotificationService } from '../../core/notification.service';

@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  templateUrl: './cart-page.component.html',
  styleUrls: ['./cart-page.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CartPageComponent {
  private readonly api = inject(PosApiService);
  private notification = inject(NotificationService);
  protected readonly lines = this.api.getCartLines();
  protected readonly total = computed(() =>
    this.lines().reduce((sum, line) => sum + line.total, 0)
  );

  protected removeItem(sku: string): void {
    this.api.removeFromCart(sku);
  }

  protected clearCart(): void {
    this.api.clearCart();
  }

  protected increment(line: CartLine) {
  this.api.increment(line.sku);
}

protected decrement(line: CartLine) {
  this.api.decrement(line.sku);
}
}

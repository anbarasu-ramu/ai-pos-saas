import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { PosApiService } from '../../core/api/pos-api.service';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NotificationService } from '../../core/notification.service';

@Component({
  selector: 'app-checkout-page',
   standalone: true,
  imports: [CommonModule, CurrencyPipe, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './checkout-page.component.html',
  styleUrls: ['./checkout-page.component.css'],
})
export class CheckoutPageComponent {
   private readonly api = inject(PosApiService);
   private notification = inject(NotificationService);
  protected readonly lines = this.api.getCartLines();
  protected readonly total = computed(() =>
    this.lines().reduce((sum, line) => sum + line.total, 0)
  );
  protected amountPaid = signal(0);
  // protected changeDue = computed(() => this.amountPaid() - this.total());

  protected changeDue = computed(() => {
  const paid = this.amountPaid();
  const total = this.total();
  return paid - total;
});

  onAmountChange(event: Event) {
  const value = (event.target as HTMLInputElement).value;
  this.amountPaid.set(value ? Number(value) : 0);
}

protected isPaymentSufficient = computed(() =>
  this.amountPaid() >= this.total()
);


  protected confirmOrder(): void {
    // 🔥 Guard checks
  if (!this.lines().length) return;

  if (this.amountPaid() < this.total()) {
    this.notification.error('Insufficient payment');
    return;
  }

  const request = {
    items: this.lines().map(line => ({
      productId: line.sku,
      quantity: line.quantity
    })),
    paymentType: 'CASH',
    amountPaid: this.amountPaid()
  };

  this.api.checkout(request).subscribe({
    next: (res: any) => {
      // ✅ success
      console.log('Order success:', res);
      this.notification.success(`Order success: ${res}`);

      // clear cart
      this.api.clearCart();

      // reset UI
      this.amountPaid.set(0);

      // optional: show success message
      this.notification.success(`Order placed! Change: ${res.change}`);
    },
    error: (err) => {
      console.error('Checkout failed', err);
      this.notification.error('Checkout failed. Try again.');
    }
  });
  }

  protected cancelOrder(): void {
    this.api.clearCart();
  }
}

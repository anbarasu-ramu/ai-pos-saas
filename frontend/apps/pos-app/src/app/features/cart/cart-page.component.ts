import { CommonModule, CurrencyPipe } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { PosApiService } from '../../core/api/pos-api.service';

@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  template: `
    <section class="page">
      <p class="eyebrow">Cart</p>
      <h2>Current basket scaffold</h2>

      <article class="card" *ngFor="let line of lines">
        <div>
          <strong>{{ line.name }}</strong>
          <p>{{ line.sku }}</p>
        </div>
        <span>x{{ line.quantity }}</span>
        <span>{{ line.total | currency }}</span>
      </article>

      <footer class="summary">
        <span>Total</span>
        <strong>{{ total() | currency }}</strong>
      </footer>
    </section>
  `,
  styles: [`
    .page { display: grid; gap: 1rem; max-width: 760px; }
    .eyebrow { margin: 0; text-transform: uppercase; letter-spacing: 0.16em; font-size: 0.75rem; color: #0284c7; }
    h2 { margin: 0; }
    .card, .summary { display: grid; grid-template-columns: 1fr auto auto; gap: 1rem; align-items: center; background: white; padding: 1rem 1.25rem; border-radius: 22px; box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08); }
    p { margin: 0.25rem 0 0; color: #64748b; }
    .summary { font-size: 1.2rem; }
  `],
})
export class CartPageComponent {
  private readonly api = inject(PosApiService);
  protected readonly lines = this.api.getCartLines();
  protected readonly total = computed(() => this.lines.reduce((sum, line) => sum + line.total, 0));
}

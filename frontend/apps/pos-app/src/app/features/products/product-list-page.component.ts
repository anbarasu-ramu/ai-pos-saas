import { CommonModule, CurrencyPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { PosApiService } from '../../core/api/pos-api.service';

@Component({
  selector: 'app-product-list-page',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  template: `
    <section class="page">
      <div class="heading">
        <div>
          <p class="eyebrow">Inventory</p>
          <h2>Product and stock scaffold</h2>
        </div>
        <button type="button">Add Product</button>
      </div>

      <div class="table">
        <div class="row header">
          <span>Name</span>
          <span>Category</span>
          <span>Stock</span>
          <span>Price</span>
        </div>

        <div class="row" *ngFor="let product of products">
          <span>{{ product.name }}</span>
          <span>{{ product.category }}</span>
          <span [class.low]="product.stockQuantity < 10">{{ product.stockQuantity }}</span>
          <span>{{ product.price | currency }}</span>
        </div>
      </div>
    </section>
  `,
  styles: [`
    .page { display: grid; gap: 1rem; }
    .heading { display: flex; justify-content: space-between; gap: 1rem; align-items: center; }
    .eyebrow { margin: 0; text-transform: uppercase; letter-spacing: 0.16em; font-size: 0.75rem; color: #0284c7; }
    h2 { margin: 0.4rem 0 0; }
    button { border: 0; border-radius: 999px; background: #0f172a; color: white; padding: 0.9rem 1.2rem; cursor: pointer; }
    .table { background: white; border-radius: 22px; overflow: hidden; box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08); }
    .row { display: grid; grid-template-columns: 2fr 1fr 1fr 1fr; gap: 1rem; padding: 1rem 1.25rem; border-bottom: 1px solid #e2e8f0; }
    .row.header { background: #f8fafc; font-weight: 700; color: #334155; }
    .row:last-child { border-bottom: 0; }
    .low { color: #b91c1c; font-weight: 700; }
  `],
})
export class ProductListPageComponent {
  private readonly api = inject(PosApiService);
  protected readonly products = this.api.getProducts();
}

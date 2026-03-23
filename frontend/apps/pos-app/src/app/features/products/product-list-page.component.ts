import { CommonModule, CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { PosApiService } from '../../core/api/pos-api.service';
import { ProductService } from './product.service';
import { NotificationService } from '../../core/notification.service';
@Component({
  selector: 'app-product-list-page',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  templateUrl: './product-list-page.component.html',
  styleUrl: './product-list-page.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductListPageComponent implements OnInit {
  private readonly api = inject(PosApiService);
  private readonly productService = inject(ProductService);
  private readonly cdr = inject(ChangeDetectorRef);
  private notification = inject(NotificationService);
  
  @Output() updateProduct = new EventEmitter<any>();
  @Output() toggleActive = new EventEmitter<any>();
  @Input() showAddToCart = true;
  @Output() addToCart = new EventEmitter<any>();

  @Input() products: any[] = [];

  @Output() refresh = new EventEmitter<void>();

  ngOnInit() {
    if (this.shouldLoadFromBackend()) {
      this.loadProductsFromBackend();
    }
  }

  ngOnChanges() {
    if (this.shouldLoadFromBackend()) {
      this.loadProductsFromBackend();
    }
    console.log('Child received:', this.products);
  }

  private shouldLoadFromBackend(): boolean {
    return this.showAddToCart || this.products.length === 0;
  }

  private loadProductsFromBackend() {
    this.productService.getProducts().subscribe({
      next: (res) => {
        this.products = [...res];
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load products', err);
      }
    });
  }

  onRefresh() {
    this.refresh.emit();
  }

  onEdit(product: any) {
    console.log('Edit product:', product);
    this.updateProduct.emit(product);  
  }

onToggleActive(product: any) {
    console.log('Toggle active for product:', product);
    this.toggleActive.emit(product);
  }

  isProductActive(product: any): boolean {
    if (this.showAddToCart) {
      // Cashier flow may show all available products, allow adding to cart regardless
      // of admin-side active flag to avoid cart being blocked for default product data.
      return true;
    }

    return product?.active !== false;
  }

  onAddToCart(product: any) {
    // alert('Add to cart clicked for product: ' + product.name);
    console.log('Add to cart clicked for product:', product);
    // Ensure we still allow cashier usage when showAddToCart is true.
    if (!this.isProductActive(product)) {
      return;
    }

    this.api.addToCart(product);
    this.addToCart.emit(product);
    this.notification.success(`${product.name} added to cart`);
    console.log('Added to cart:', product);
  }
}


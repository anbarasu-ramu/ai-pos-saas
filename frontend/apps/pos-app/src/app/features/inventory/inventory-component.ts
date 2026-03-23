import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../auth/auth.service';
import { firstValueFrom } from 'rxjs';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ProductListPageComponent } from '../products/product-list-page.component';
import { ProductService } from '../products/product.service';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ProductListPageComponent],
  templateUrl: './inventory-component.html',
})
export class InventoryComponent {

  private http = inject(HttpClient);
  private fb = inject(FormBuilder);
  productService = inject(ProductService);
  private cdr = inject(ChangeDetectorRef);

  products: any[] = [];
  showForm = false;
  editingProductId: number | null = null;

  form = this.fb.group({
    name: ['', Validators.required],
    category: ['', Validators.required],
    price: [0, Validators.required],
    stockQuantity: [0, Validators.required]
  });
  

  ngOnInit() {
    this.loadProducts();
  }

  toggleForm() {
    this.showForm = !this.showForm;
  }
loadProducts() {
  this.productService.getProducts().subscribe({
    next: res => {
      console.log('Products loaded:', res);
      this.products = [...res];
      this.cdr.markForCheck();
    },
    error: err => {
      console.error('Failed to load products', err);
      // optionally:
      // this.products = [];
      // show message to user
    }
  });
}
  saveProduct() {
    if (this.form.invalid) return;

    const payload = this.form.value;

    const request$ = this.editingProductId
      ? this.productService.updateProduct(this.editingProductId, payload)
      : this.productService.createProduct(payload);

    request$.subscribe({
      next: () => {
        this.loadProducts();
        this.resetForm();
      },
      error: (err) => {
        console.error('Save product failed', err);
      }
    });
  }

  cancelEdit() {
    this.resetForm();
  }

  private resetForm() {
    this.form.reset({ name: '', category: '', price: 0, stockQuantity: 0 });
    this.showForm = false;
    this.editingProductId = null;
  }

  onEdit(product: any) {
    console.log('Edit product:', product);
    this.showForm = true;
    this.editingProductId = product.id;
    this.form.setValue({
      name: product.name,
      category: product.category,
      price: product.price,
      stockQuantity: product.stockQuantity
    });
  }

  onToggleActive(product: any) {
    const action = product.active ? 'deactivate' : 'activate';
    console.log(`${action} product with ID:`, product.id);

    const request$ = product.active
      ? this.productService.deactivateProduct(product.id)
      : this.productService.activateProduct(product.id);

    request$.subscribe({
      next: () => this.loadProducts(),
      error: (err) => console.error(`${action} failed`, err)
    });
  }
}


 
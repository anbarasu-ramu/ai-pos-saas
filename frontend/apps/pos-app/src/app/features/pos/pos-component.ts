import { Component } from '@angular/core';
import { ProductListPageComponent } from '../products/product-list-page.component';
import { CartPageComponent } from '../cart/cart-page.component';
import { CheckoutPageComponent } from '../checkout/checkout-page.component';

@Component({
  selector: 'app-pos-component',
  imports: [ProductListPageComponent, CartPageComponent, CheckoutPageComponent],
  templateUrl: './pos-component.html',
  styleUrl: './pos-component.css',
})
export class PosComponent {}

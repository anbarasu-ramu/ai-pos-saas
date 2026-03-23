import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ProductService {

  private http = inject(HttpClient);
  private readonly API = '/api/products';

  getProducts(): Observable<any[]> {
    return this.http.get<any[]>(this.API);
  }

  createProduct(product: any): Observable<any> {
    return this.http.post(this.API, product);
  }

  updateProduct(productId: number, product: any): Observable<any> {
    return this.http.put(`${this.API}/${productId}`, product);
  }

  deactivateProduct(productId: number): Observable<any> {
    return this.http.delete(`${this.API}/${productId}`);
  }

  activateProduct(productId: number): Observable<any> {
    return this.http.put(`${this.API}/${productId}/activate`, {});
  }
}
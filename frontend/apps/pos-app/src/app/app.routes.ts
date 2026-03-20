import { Route } from '@angular/router';

export const appRoutes: Route[] = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'dashboard',
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login-page.component').then((m) => m.LoginPageComponent),
  },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./features/dashboard/dashboard-page.component').then((m) => m.DashboardPageComponent),
  },
  {
    path: 'products',
    loadComponent: () =>
      import('./features/products/product-list-page.component').then((m) => m.ProductListPageComponent),
  },
  {
    path: 'cart',
    loadComponent: () =>
      import('./features/cart/cart-page.component').then((m) => m.CartPageComponent),
  },
  {
    path: 'checkout',
    loadComponent: () =>
      import('./features/checkout/checkout-page.component').then((m) => m.CheckoutPageComponent),
  },
  {
    path: 'ai-assistant',
    loadComponent: () =>
      import('./features/ai/ai-assistant-page.component').then((m) => m.AiAssistantPageComponent),
  },
  {
    path: '**',
    redirectTo: 'dashboard',
  },
];

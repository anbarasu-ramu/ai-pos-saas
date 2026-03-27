import { Route } from '@angular/router';

export const appRoutes: Route[] = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login',
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login-page.component').then((m) => m.LoginPageComponent),
  },
  {
    path: 'auth/callback',
    loadComponent: () =>
      import('./features/auth/auth-callback-page.component').then((m) => m.AuthCallbackPageComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./auth/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'users',
    loadComponent: () =>
      import('./features/users/users-component').then((m) => m.UsersComponent),
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
    path: 'inventory',
    loadComponent: () =>
      import('./features/inventory/inventory-component').then((m) => m.InventoryComponent),
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
    path: 'orders',
    loadComponent: () =>
      import('./features/orders/orders-page.component').then((m) => m.OrdersPageComponent),
  },
   {
    path: 'pos',
    loadComponent: () =>
      import('./features/pos/pos-component').then((m) => m.PosComponent),
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

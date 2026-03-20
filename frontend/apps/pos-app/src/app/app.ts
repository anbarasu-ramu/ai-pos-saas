import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './auth/auth.service';

@Component({
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly authService = inject(AuthService);

  private readonly navItems = [
    { label: 'Dashboard', path: '/dashboard' },
    { label: 'Products', path: '/products' },
    { label: 'Cart', path: '/cart' },
    { label: 'Checkout', path: '/checkout' },
    { label: 'AI Assistant', path: '/ai-assistant' },
    { label: 'Login', path: '/login' },
    { label: 'Register', path: '/register' },
  ];

  protected visibleNavItems() {
    const authenticated = !!this.session();
    return this.navItems.filter((item) => {
      if (authenticated && (item.path === '/login' || item.path === '/register')) {
        return false;
      }

      return true;
    });
  }

  protected session() {
    return this.authService.currentSession();
  }

  protected logout(): void {
    this.authService.logout();
  }
}

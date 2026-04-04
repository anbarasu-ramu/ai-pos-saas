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

  private readonly navItems  = [
    { label: 'Dashboard', path: '/dashboard' , authenticationRequired: true},
    { label: 'Products', path: '/products', authenticationRequired: true ,adminOnly: true},
    { label: 'Inventory', path: '/inventory', authenticationRequired: true, adminOnly: true },
    // { label: 'Cart', path: '/cart' , authenticationRequired: true, adminOnly: true},
    // { label: 'Checkout', path: '/checkout' , authenticationRequired: true, adminOnly: true},
    { label: 'Sale', path: '/pos' , authenticationRequired: true},
    { label: 'Orders', path: '/orders' , authenticationRequired: true},
    { label: 'AI Assistant', path: '/ai-assistant' , authenticationRequired: true},
    { label: 'Login', path: '/login' , authenticationRequired: false},
    { label: 'Register', path: '/register' , authenticationRequired: false},
    { label: 'Users', path: '/users' , authenticationRequired: true, adminOnly: true}
  ];

  protected visibleNavItems() {
    const authenticated = !!this.session();
    return this.navItems.filter((item) => {
      if (authenticated && item.authenticationRequired === false) {
        return false;
      } 
      
      if(!authenticated && item.authenticationRequired === true) {
        return false;
      }

      if (item.adminOnly && !this.isAdmin()) {
        return false;
      }

      return true;
    });
  }

  protected session() {
    return this.authService.currentSession();
  }

  protected isAdmin(): boolean {
    const session = this.session();
    const roles: string[] = (session?.roles ?? []).map((r: string) => r.toUpperCase());
    return roles.includes('TENANT_ADMIN');
  }

  protected logout(): void {
    this.authService.logout();
  }
}

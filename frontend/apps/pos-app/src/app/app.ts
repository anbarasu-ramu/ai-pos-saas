import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly navItems = [
    { label: 'Dashboard', path: '/dashboard' },
    { label: 'Products', path: '/products' },
    { label: 'Cart', path: '/cart' },
    { label: 'Checkout', path: '/checkout' },
    { label: 'AI Assistant', path: '/ai-assistant' },
    { label: 'Login', path: '/login' },
  ];
}

import { Component } from '@angular/core';
import { getAppConfig } from '../../core/config/app-config';

@Component({
  selector: 'app-login-page',
  standalone: true,
  template: `
    <section class="page card">
      <p class="eyebrow">Authentication</p>
      <h2>Connect cashiers through Keycloak</h2>
      <p>
        This screen is the placeholder for OIDC login, refresh token handling, and tenant-aware role mapping.
      </p>

      <form class="form">
        <label>
          Realm
          <input type="text" [value]="realm" readonly />
        </label>
        <label>
          Frontend client
          <input type="text" [value]="clientId" readonly />
        </label>
        <div class="creds">
          <p>Sample users</p>
          <code>admin / admin123</code>
          <code>cashier / cashier123</code>
        </div>
        <button type="button" (click)="login()">Continue to Keycloak</button>
      </form>
    </section>
  `,
  styles: [`
    .page { max-width: 640px; }
    .card { background: white; padding: 2rem; border-radius: 24px; box-shadow: 0 20px 50px rgba(15, 23, 42, 0.08); }
    .eyebrow { text-transform: uppercase; letter-spacing: 0.16em; font-size: 0.75rem; color: #0284c7; }
    h2 { margin: 0.5rem 0 1rem; font-size: 2rem; }
    p { color: #475569; line-height: 1.6; }
    .form { display: grid; gap: 1rem; margin-top: 1.5rem; }
    label { display: grid; gap: 0.5rem; color: #334155; font-weight: 600; }
    input { padding: 0.9rem 1rem; border: 1px solid #cbd5e1; border-radius: 14px; }
    .creds { display: grid; gap: 0.35rem; color: #475569; }
    .creds p { margin: 0; font-weight: 700; color: #0f172a; }
    code { width: fit-content; padding: 0.35rem 0.55rem; border-radius: 10px; background: #e2e8f0; }
    button { width: fit-content; border: 0; border-radius: 999px; background: #0f172a; color: white; padding: 0.9rem 1.3rem; cursor: pointer; }
  `],
})
export class LoginPageComponent {
  private readonly appConfig = getAppConfig();

  protected readonly realm = this.appConfig.realm;
  protected readonly clientId = this.appConfig.clientId;

  login(): void {
    const redirectUri = `${window.location.origin}/dashboard`;
    const loginUrl =
      `${this.appConfig.keycloakBaseUrl}/realms/${this.appConfig.realm}/protocol/openid-connect/auth` +
      `?client_id=${encodeURIComponent(this.appConfig.clientId)}` +
      `&redirect_uri=${encodeURIComponent(redirectUri)}` +
      `&response_type=code` +
      `&scope=${encodeURIComponent('openid profile email')}`;

    window.location.href = loginUrl;
  }
}

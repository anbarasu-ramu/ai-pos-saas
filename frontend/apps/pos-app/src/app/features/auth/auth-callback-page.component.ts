import { isPlatformBrowser } from '@angular/common';
import { Component, PLATFORM_ID, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-auth-callback-page',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './auth-callback-page.component.html',
  styleUrl: './auth-callback-page.component.css',
})
export class AuthCallbackPageComponent {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  protected title = 'Finishing sign in';
  protected message = 'Please wait while we complete your Keycloak session.';
  protected errorMessage: string | null = null;

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      void this.completeLogin();
    }
  }

  private async completeLogin(): Promise<void> {
    const queryParamMap = await firstValueFrom(this.route.queryParamMap);
    const code = queryParamMap.get('code');
    const state = queryParamMap.get('state');
    const error = queryParamMap.get('error');
    const errorDescription = queryParamMap.get('error_description');

    if (error) {
      this.title = 'Login failed';
      this.message = 'Keycloak rejected the sign-in request.';
      this.errorMessage = errorDescription ?? error;
      return;
    }

    try {
      await this.authService.handleCallback(code, state);
      this.title = 'Login successful';
      this.message = 'Redirecting to your workspace.';
      await this.router.navigateByUrl('/dashboard');
    } catch (error) {
      this.title = 'Login failed';
      this.message = 'We could not establish your session.';
      this.errorMessage = error instanceof Error ? error.message : 'Unable to complete login.';
    }
  }
}

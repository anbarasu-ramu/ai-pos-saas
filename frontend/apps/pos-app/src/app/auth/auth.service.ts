import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface RegistrationRequest {
  email: string;
  password: string;
  tenantName: string;
}

export interface RegistrationResponse {
  message: string;
  email: string;
  tenantId: string;
  role: string;
  loginUrl: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly authApiBaseUrl = `${this.resolveBaseUrl(8080)}/api/auth`;
  private readonly keycloakBaseUrl = this.resolveBaseUrl(8081);
  private readonly realm = 'ai-pos';
  private readonly clientId = 'pos-client';

  register(payload: RegistrationRequest): Observable<RegistrationResponse> {
    return this.http.post<RegistrationResponse>(`${this.authApiBaseUrl}/register`, payload);
  }

  buildLoginRedirectUrl(redirectUri: string = window.location.origin): string {
    const params = new URLSearchParams({
      client_id: this.clientId,
      response_type: 'code',
      scope: 'openid profile email',
      redirect_uri: redirectUri,
    });

    return `${this.keycloakBaseUrl}/realms/${this.realm}/protocol/openid-connect/auth?${params.toString()}`;
  }

  private resolveBaseUrl(port: number): string {
    if (typeof window === 'undefined') {
      return `http://localhost:${port}`;
    }

    const protocol = window.location.protocol;
    const hostname = window.location.hostname || 'localhost';
    return `${protocol}//${hostname}:${port}`;
  }
}

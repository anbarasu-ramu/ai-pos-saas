import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, firstValueFrom } from 'rxjs';

export interface RegistrationRequest {
  email: string;
  password: string;
  tenantName ?: string;
}

export interface RegistrationResponse {
  message: string;
  email: string;
  tenantId: string;
  role: string;
  loginUrl: string;
}

export interface UserSession {
  accessToken: string;
  refreshToken: string | null;
  idToken: string;
  expiresAt: number;
  email: string | null;
  username: string | null;
  tenantId: string | null;
  roles: string[];
}

interface TokenResponse {
  access_token: string;
  refresh_token?: string;
  id_token: string;
  expires_in: number;
}

interface IdTokenClaims {
  nonce?: string;
  email?: string;
  preferred_username?: string;
  tenant_id?: string;
}

interface CurrentSessionResponse {
  authenticated: boolean;
  subject: string;
  email: string | null;
  username: string | null;
  roles: string[];
  tenantId: string | null;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly authApiBaseUrl = `${this.resolveBaseUrl(8080)}/api/auth`;
  private readonly userApiBaseUrl = `${this.resolveBaseUrl(8080)}/api/users`;
  private readonly keycloakBaseUrl = this.resolveBaseUrl(8081);
  private readonly realm = 'ai-pos';
  private readonly clientId = 'pos-client';
  private readonly callbackPath = '/auth/callback';
  private readonly sessionStorageAvailable = typeof window !== 'undefined' && !!window.sessionStorage;
  private session: UserSession | null = null;

  constructor() {
    this.restoreSession();
  }

  register(payload: RegistrationRequest): Observable<RegistrationResponse> {
    return this.http.post<RegistrationResponse>(`${this.authApiBaseUrl}/register`, payload);
  }

    createUser(payload: RegistrationRequest): Observable<RegistrationResponse> {
    return this.http.post<RegistrationResponse>(`${this.userApiBaseUrl}`, payload);
  }

  async login(loginHint?: string): Promise<void> {
    this.clearSession();

    const verifier = this.generateRandomString(64);
    const state = this.generateRandomString(32);
    const nonce = this.generateRandomString(32);
    const challenge = await this.createCodeChallenge(verifier);

    this.writeStorage('pkce_verifier', verifier);
    this.writeStorage('oauth_state', state);
    this.writeStorage('oauth_nonce', nonce);

    if (typeof window !== 'undefined') {
      window.location.assign(this.buildLoginRedirectUrl(this.redirectUri(), challenge, state, nonce, loginHint));
    }
  }

  async handleCallback(code: string | null, state: string | null): Promise<UserSession> {
    if (!code) {
      throw new Error('Missing authorization code.');
    }

    const storedState = this.readStorage('oauth_state');
    if (!state || !storedState || state !== storedState) {
      this.clearOAuthState();
      throw new Error('Invalid login state. Please try again.');
    }

    const codeVerifier = this.readStorage('pkce_verifier');
    if (!codeVerifier) {
      this.clearOAuthState();
      throw new Error('Login verification data is missing. Please try again.');
    }

    const storedNonce = this.readStorage('oauth_nonce');
    const body = new URLSearchParams({
      grant_type: 'authorization_code',
      client_id: this.clientId,
      code,
      redirect_uri: this.redirectUri(),
      code_verifier: codeVerifier,
    });

    try {
      const response = await firstValueFrom(this.http.post<TokenResponse>(
        this.tokenEndpoint(),
        body.toString(),
        {
          headers: new HttpHeaders({
            'Content-Type': 'application/x-www-form-urlencoded',
          }),
        },
      ));

      const claims = this.decodeIdToken(response.id_token);
      if (!storedNonce || !claims.nonce || claims.nonce !== storedNonce) {
        this.clearSession();
        throw new Error('Invalid login response. Please try again.');
      }

      const session: UserSession = {
        accessToken: response.access_token,
        refreshToken: response.refresh_token ?? null,
        idToken: response.id_token,
        expiresAt: Date.now() + (response.expires_in * 1000),
        email: claims.email ?? null,
        username: claims.preferred_username ?? null,
        tenantId: claims.tenant_id ?? null,
        roles: [],
      };

      this.setSession(session);
      await this.hydrateSessionFromBackend();
      this.clearOAuthState();
      return this.session ?? session;
    } catch {
      this.clearSession();
      throw new Error('Unable to complete login. The authorization code may be invalid or expired.');
    }
  }

  getAccessToken(): string | null {
    if (!this.isAuthenticated()) {
      return null;
    }

    return this.session?.accessToken ?? null;
  }

  isAuthenticated(): boolean {
    if (!this.session) {
      return false;
    }

    if (this.session.expiresAt <= Date.now()) {
      this.clearSession();
      return false;
    }

    return true;
  }

  logout(): void {
    const idToken = this.session?.idToken ?? this.readStorage('id_token');
    const logoutUrl = this.buildLogoutUrl(idToken);
    this.clearSession();

    if (typeof window !== 'undefined') {
      window.location.assign(logoutUrl);
    }
  }

  currentSession(): UserSession | null {
    return this.isAuthenticated() ? this.session : null;
  }

  restoreSession(): void {
    if (!this.sessionStorageAvailable) {
      this.session = null;
      return;
    }

    const accessToken = this.readStorage('access_token');
    const idToken = this.readStorage('id_token');
    const expiresAt = Number(this.readStorage('expires_at'));

    if (!accessToken || !idToken || !Number.isFinite(expiresAt) || expiresAt <= Date.now()) {
      this.clearStoredSessionTokens();
      return;
    }

    this.session = {
      accessToken,
      refreshToken: this.readStorage('refresh_token'),
      idToken,
      expiresAt,
      email: this.readStorage('session_email'),
      username: this.readStorage('session_username'),
      tenantId: this.readStorage('session_tenant_id'),
      roles: this.readStorage('session_roles')?.split(',').filter((role) => role.length > 0) ?? [],
    };

    void this.hydrateSessionFromBackend();
  }

  buildLoginRedirectUrl(
    redirectUri: string = this.redirectUri(),
    codeChallenge?: string,
    state?: string,
    nonce?: string,
    loginHint?: string,
  ): string {
    const params = new URLSearchParams({
      client_id: this.clientId,
      response_type: 'code',
      scope: 'openid',
      redirect_uri: redirectUri,
      code_challenge: codeChallenge ?? '',
      code_challenge_method: 'S256',
      state: state ?? '',
      nonce: nonce ?? '',
    });

    if (loginHint) {
      params.set('login_hint', loginHint);
    }

    return `${this.keycloakBaseUrl}/realms/${this.realm}/protocol/openid-connect/auth?${params.toString()}`;
  }

  private setSession(session: UserSession): void {
    this.session = session;
    this.writeStorage('access_token', session.accessToken);
    this.writeStorage('refresh_token', session.refreshToken ?? '');
    this.writeStorage('id_token', session.idToken);
    this.writeStorage('expires_at', String(session.expiresAt));
    this.writeStorage('session_email', session.email ?? '');
    this.writeStorage('session_username', session.username ?? '');
    this.writeStorage('session_tenant_id', session.tenantId ?? '');
    this.writeStorage('session_roles', session.roles.join(','));
  }

  private clearSession(): void {
    this.session = null;
    this.clearStoredSessionTokens();
    this.clearOAuthState();
  }

  private clearStoredSessionTokens(): void {
    [
      'access_token',
      'refresh_token',
      'id_token',
      'expires_at',
      'session_email',
      'session_username',
      'session_tenant_id',
      'session_roles',
    ].forEach((key) => this.removeStorage(key));
  }

  private async hydrateSessionFromBackend(): Promise<void> {
    if (!this.session || !this.isAuthenticated()) {
      return;
    }

    try {
      const currentSession = await firstValueFrom(
        this.http.get<CurrentSessionResponse>(`${this.authApiBaseUrl}/me`),
      );

      this.setSession({
        ...this.session,
        email: currentSession.email ?? this.session.email,
        username: currentSession.username ?? this.session.username,
        tenantId: currentSession.tenantId ?? this.session.tenantId,
        roles: currentSession.roles ?? this.session.roles,
      });
    } catch {
      // Keep the locally decoded session when profile hydration is unavailable.
    }
  }

  private clearOAuthState(): void {
    ['pkce_verifier', 'oauth_state', 'oauth_nonce'].forEach((key) => this.removeStorage(key));
  }

  private redirectUri(): string {
    if (typeof window === 'undefined') {
      return 'http://localhost:4200/auth/callback';
    }

    return `${window.location.origin}${this.callbackPath}`;
  }

  private tokenEndpoint(): string {
    return `${this.keycloakBaseUrl}/realms/${this.realm}/protocol/openid-connect/token`;
  }

  private buildLogoutUrl(idTokenHint?: string | null): string {
    const params = new URLSearchParams({
      client_id: this.clientId,
      post_logout_redirect_uri: typeof window === 'undefined' ? 'http://localhost:4200' : window.location.origin,
    });

    if (idTokenHint) {
      params.set('id_token_hint', idTokenHint);
    }

    return `${this.keycloakBaseUrl}/realms/${this.realm}/protocol/openid-connect/logout?${params.toString()}`;
  }

  private decodeIdToken(token: string): IdTokenClaims {
    const parts = token.split('.');
    if (parts.length < 2) {
      throw new Error('Invalid ID token.');
    }

    const payload = parts[1]
      .replace(/-/g, '+')
      .replace(/_/g, '/')
      .padEnd(Math.ceil(parts[1].length / 4) * 4, '=');

    if (typeof window === 'undefined') {
      return {};
    }

    return JSON.parse(window.atob(payload)) as IdTokenClaims;
  }

  private async createCodeChallenge(verifier: string): Promise<string> {
    if (typeof window === 'undefined' || !window.crypto?.subtle) {
      throw new Error('PKCE is not available in this environment.');
    }

    const data = new TextEncoder().encode(verifier);
    const digest = await window.crypto.subtle.digest('SHA-256', data);
    return this.base64UrlEncode(new Uint8Array(digest));
  }

  private base64UrlEncode(bytes: Uint8Array): string {
    let binary = '';

    bytes.forEach((value) => {
      binary += String.fromCharCode(value);
    });

    return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
  }

  private generateRandomString(length: number): string {
    if (typeof window === 'undefined' || !window.crypto?.getRandomValues) {
      return Math.random().toString(36).slice(2, 2 + length);
    }

    const values = new Uint8Array(length);
    window.crypto.getRandomValues(values);
    return Array.from(values, (value) => ('0' + (value % 36).toString(36)).slice(-1)).join('');
  }

  private readStorage(key: string): string | null {
    if (!this.sessionStorageAvailable) {
      return null;
    }

    const value = window.sessionStorage.getItem(key);
    return value && value.length > 0 ? value : null;
  }

  private writeStorage(key: string, value: string): void {
    if (!this.sessionStorageAvailable) {
      return;
    }

    window.sessionStorage.setItem(key, value);
  }

  private removeStorage(key: string): void {
    if (!this.sessionStorageAvailable) {
      return;
    }

    window.sessionStorage.removeItem(key);
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

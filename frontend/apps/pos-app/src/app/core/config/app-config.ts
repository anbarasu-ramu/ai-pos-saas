import { environment } from '../../../environments/environment';

export interface AppConfig {
  apiBaseUrl: string;
  keycloakBaseUrl: string;
  realm: string;
  clientId: string;
}

declare global {
  interface Window {
    __APP_CONFIG__?: Partial<AppConfig>;
  }
}

export function getAppConfig(): AppConfig {
  const runtimeConfig =
    typeof window === 'undefined' ? {} : (window.__APP_CONFIG__ ?? {});

  return {
    apiBaseUrl: runtimeConfig.apiBaseUrl ?? environment.apiBaseUrl,
    keycloakBaseUrl: runtimeConfig.keycloakBaseUrl ?? environment.keycloakBaseUrl,
    realm: runtimeConfig.realm ?? environment.realm,
    clientId: runtimeConfig.clientId ?? environment.clientId,
  };
}

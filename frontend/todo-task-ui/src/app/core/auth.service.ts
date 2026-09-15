import {computed, inject, Injectable, signal} from '@angular/core';
import {AuthConfig, OAuthService} from 'angular-oauth2-oidc';
import {AppConfigService} from './app-config';

@Injectable({providedIn: 'root'})
export class AuthService {
	private readonly oauthService = inject(OAuthService);
	private readonly appConfig = inject(AppConfigService);
	private readonly claims = signal<Record<string, unknown> | undefined>(undefined);

	readonly userName = computed(() => (this.claims()?.['preferred_username'] as string) ?? (this.claims()?.['sub'] as string) ?? '');
	readonly roles = computed(() => (this.claims()?.['userroles'] as string[]) ?? []);
	readonly isAuthenticated = computed(() => !!this.claims());

	async initialize(): Promise<void> {
		const config = this.appConfig.current;
		const authConfig: AuthConfig = {
			issuer: config.issuer,
			clientId: config.clientId,
			redirectUri: `${window.location.origin}/`,
			postLogoutRedirectUri: `${window.location.origin}/`,
			responseType: 'code',
			scope: 'openid profile',
			requireHttps: window.location.protocol === 'https:',
			strictDiscoveryDocumentValidation: false,
			showDebugInformation: false
		};
		this.oauthService.configure(authConfig);
		await this.oauthService.loadDiscoveryDocumentAndTryLogin();
		this.claims.set(this.oauthService.getIdentityClaims() as Record<string, unknown>);
	}

	login(): void {
		this.oauthService.initCodeFlow();
	}

	logout(): void {
		this.oauthService.logOut();
		this.claims.set(undefined);
	}

	get accessToken(): string {
		return this.oauthService.getAccessToken();
	}
}

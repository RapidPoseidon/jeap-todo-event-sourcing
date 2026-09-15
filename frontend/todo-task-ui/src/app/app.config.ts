import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {ApplicationConfig, inject, provideAppInitializer, provideBrowserGlobalErrorListeners} from '@angular/core';
import {provideAnimationsAsync} from '@angular/platform-browser/animations/async';
import {provideRouter, withComponentInputBinding} from '@angular/router';
import {provideOAuthClient} from 'angular-oauth2-oidc';
import {provideObliqueConfiguration} from '@oblique/oblique';
import {routes} from './app.routes';
import {AppConfigService} from './core/app-config';
import {authInterceptor} from './core/auth.interceptor';
import {AuthService} from './core/auth.service';

export const appConfig: ApplicationConfig = {
	providers: [
		provideBrowserGlobalErrorListeners(),
		provideRouter(routes, withComponentInputBinding()),
		provideAnimationsAsync(),
		provideHttpClient(withInterceptors([authInterceptor])),
		provideOAuthClient(),
		provideObliqueConfiguration({
			accessibilityStatement: {
				applicationName: 'Todo',
				conformity: 'none',
				applicationOperator: 'Demo Operator (fictional), Example Street 1, 3000 Example',
				contact: [{email: 'todo@example.org'}],
				createdOn: new Date('2026-09-14')
			},
			translate: {
				locales: {
					locales: ['en-US', 'de-CH', 'fr-CH'],
					defaultLanguage: 'en-US',
					disabled: false,
					languages: {en: 'English', de: 'Deutsch', fr: 'Français'}
				}
			}
		}),
		provideAppInitializer(() => {
			const appConfig = inject(AppConfigService);
			const auth = inject(AuthService);
			return appConfig.load().then(() => auth.initialize());
		})
	]
};

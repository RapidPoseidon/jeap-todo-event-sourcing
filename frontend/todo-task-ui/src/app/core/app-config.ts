import {HttpClient} from '@angular/common/http';
import {inject, Injectable, signal} from '@angular/core';
import {firstValueFrom} from 'rxjs';

export interface AppConfig {
	apiUrl: string;
	issuer: string;
	clientId: string;
}

/**
 * Backend and issuer URLs are read at runtime so the same built image can serve every environment.
 */
@Injectable({providedIn: 'root'})
export class AppConfigService {
	private readonly http = inject(HttpClient);
	private readonly config = signal<AppConfig | undefined>(undefined);

	async load(): Promise<void> {
		this.config.set(await firstValueFrom(this.http.get<AppConfig>('assets/config.json')));
	}

	get current(): AppConfig {
		const config = this.config();
		if (!config) {
			throw new Error('Application configuration has not been loaded yet');
		}
		return config;
	}
}

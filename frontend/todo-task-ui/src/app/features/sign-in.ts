import {Component, inject} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {AuthService} from '../core/auth.service';

@Component({
	selector: 'app-sign-in',
	imports: [MatButtonModule, MatCardModule],
	template: `
		<div class="sign-in">
			<mat-card appearance="outlined">
				<mat-card-content>
					<h1>Sign in to continue</h1>
					<p>
						Every endpoint of the task service requires an OAuth2 access token. Sign in with the mock identity
						provider — the users <code>anna</code> (read and write) and <code>ben</code> (read only) are preconfigured.
					</p>
					<button mat-flat-button color="primary" type="button" (click)="auth.login()">Log in</button>
				</mat-card-content>
			</mat-card>
		</div>
	`,
	styles: `
		.sign-in {
			max-width: 34rem;
			margin: 4rem auto;
			padding: 0 1rem;
		}

		p {
			color: #5a6570;
		}
	`
})
export class SignIn {
	protected readonly auth = inject(AuthService);
}

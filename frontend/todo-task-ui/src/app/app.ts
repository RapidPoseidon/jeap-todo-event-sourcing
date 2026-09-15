import {Component, signal} from '@angular/core';
import {ObMasterLayoutModule} from '@oblique/oblique';

@Component({
	selector: 'app-root',
	imports: [ObMasterLayoutModule],
	templateUrl: './app.html',
	styleUrl: './app.scss'
})
export class App {
	protected readonly year = signal(new Date().getFullYear());
	protected readonly navigation = [{url: 'tasks', label: 'Tasks', icon: 'home'}];
}

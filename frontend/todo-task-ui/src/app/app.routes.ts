import {Routes} from '@angular/router';
import {authGuard} from './core/auth.guard';

export const routes: Routes = [
	{path: '', pathMatch: 'full', redirectTo: 'tasks'},
	{path: 'sign-in', loadComponent: () => import('./features/sign-in').then(m => m.SignIn)},
	{path: 'tasks', canActivate: [authGuard], loadComponent: () => import('./features/tasks/task-list').then(m => m.TaskList)},
	{path: 'tasks/:taskId', canActivate: [authGuard], loadComponent: () => import('./features/tasks/task-detail').then(m => m.TaskDetail)}
];

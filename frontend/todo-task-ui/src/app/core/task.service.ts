import {HttpClient, HttpParams} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {AppConfigService} from './app-config';
import {Task, TaskCommandPayload, TaskEvent, TaskStatistics, TaskStatus} from './task.model';

@Injectable({providedIn: 'root'})
export class TaskService {
	private readonly http = inject(HttpClient);
	private readonly appConfig = inject(AppConfigService);

	findTasks(status?: TaskStatus): Observable<Task[]> {
		const params = status ? new HttpParams().set('status', status) : undefined;
		return this.http.get<Task[]>(this.url('/api/tasks'), {params});
	}

	findTask(taskId: string): Observable<Task> {
		return this.http.get<Task>(this.url(`/api/tasks/${taskId}`));
	}

	findHistory(taskId: string): Observable<TaskEvent[]> {
		return this.http.get<TaskEvent[]>(this.url(`/api/tasks/${taskId}/events`));
	}

	findAtVersion(taskId: string, version: number): Observable<Task> {
		return this.http.get<Task>(this.url(`/api/tasks/${taskId}/versions/${version}`));
	}

	createTask(payload: TaskCommandPayload): Observable<Task> {
		return this.http.post<Task>(this.url('/api/tasks'), payload);
	}

	changeDetails(taskId: string, payload: TaskCommandPayload): Observable<Task> {
		return this.http.put<Task>(this.url(`/api/tasks/${taskId}`), payload);
	}

	changeStatus(taskId: string, status: TaskStatus): Observable<Task> {
		return this.http.put<Task>(this.url(`/api/tasks/${taskId}/status`), {status});
	}

	assign(taskId: string, assignee: string): Observable<Task> {
		return this.http.put<Task>(this.url(`/api/tasks/${taskId}/assignee`), {assignee});
	}

	deleteTask(taskId: string): Observable<void> {
		return this.http.delete<void>(this.url(`/api/tasks/${taskId}`));
	}

	findStatistics(): Observable<TaskStatistics> {
		return this.http.get<TaskStatistics>(this.url('/api/statistics'));
	}

	private url(path: string): string {
		return `${this.appConfig.current.apiUrl}${path}`;
	}
}

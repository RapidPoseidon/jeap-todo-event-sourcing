import {DatePipe, JsonPipe} from '@angular/common';
import {Component, effect, inject, input, signal} from '@angular/core';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatSliderModule} from '@angular/material/slider';
import {RouterLink} from '@angular/router';
import {TaskService} from '../../core/task.service';
import {Task, TaskEvent, TaskPriority} from '../../core/task.model';

@Component({
	selector: 'app-task-detail',
	imports: [
		DatePipe,
		JsonPipe,
		ReactiveFormsModule,
		RouterLink,
		MatButtonModule,
		MatCardModule,
		MatFormFieldModule,
		MatInputModule,
		MatSelectModule,
		MatSliderModule
	],
	templateUrl: './task-detail.html',
	styleUrl: './task-detail.scss'
})
export class TaskDetail {
	readonly taskId = input.required<string>();

	private readonly taskService = inject(TaskService);
	private readonly formBuilder = inject(FormBuilder);

	protected readonly task = signal<Task | undefined>(undefined);
	protected readonly events = signal<TaskEvent[]>([]);
	protected readonly replayed = signal<Task | undefined>(undefined);
	protected readonly selectedVersion = signal(1);
	protected readonly error = signal<string | undefined>(undefined);
	protected readonly priorities: TaskPriority[] = ['HIGH', 'MEDIUM', 'LOW'];

	protected readonly form = this.formBuilder.nonNullable.group({
		title: ['', [Validators.required, Validators.maxLength(200)]],
		description: [''],
		priority: ['MEDIUM' as TaskPriority],
		dueDate: ['']
	});

	constructor() {
		effect(() => this.reload(this.taskId()));
	}

	protected save(): void {
		if (this.form.invalid) {
			return;
		}
		const value = this.form.getRawValue();
		this.taskService
			.changeDetails(this.taskId(), {
				title: value.title,
				description: value.description || undefined,
				priority: value.priority,
				dueDate: value.dueDate || null
			})
			.subscribe({
				next: () => this.reload(this.taskId()),
				error: (error: unknown) => this.showError(error)
			});
	}

	protected replay(version: number): void {
		this.selectedVersion.set(version);
		this.taskService.findAtVersion(this.taskId(), version).subscribe({
			next: task => this.replayed.set(task),
			error: (error: unknown) => this.showError(error)
		});
	}

	protected payloadOf(event: TaskEvent): Record<string, unknown> {
		const {aggregateId, version, createdDate, initiatedBy, eventType, ...rest} = event.payload;
		return rest;
	}

	private reload(taskId: string): void {
		this.taskService.findTask(taskId).subscribe({
			next: task => {
				this.task.set(task);
				this.selectedVersion.set(task.version);
				this.replayed.set(task);
				this.form.patchValue({
					title: task.title,
					description: task.description ?? '',
					priority: task.priority,
					dueDate: task.dueDate ?? ''
				});
			},
			error: (error: unknown) => this.showError(error)
		});
		this.taskService.findHistory(taskId).subscribe({
			next: events => this.events.set(events),
			error: (error: unknown) => this.showError(error)
		});
	}

	private showError(error: unknown): void {
		const detail = error as {error?: {detail?: string}; message?: string};
		this.error.set(detail.error?.detail ?? detail.message ?? 'Unexpected error');
	}
}

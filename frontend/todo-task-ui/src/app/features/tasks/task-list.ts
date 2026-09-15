import {DatePipe} from '@angular/common';
import {Component, computed, inject, signal, viewChild} from '@angular/core';
import {FormBuilder, FormGroupDirective, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {RouterLink} from '@angular/router';
import {AuthService} from '../../core/auth.service';
import {TaskService} from '../../core/task.service';
import {Task, TaskPriority, TaskStatistics, TaskStatus} from '../../core/task.model';

@Component({
	selector: 'app-task-list',
	imports: [
		DatePipe,
		ReactiveFormsModule,
		RouterLink,
		MatButtonModule,
		MatButtonToggleModule,
		MatCardModule,
		MatFormFieldModule,
		MatIconModule,
		MatInputModule,
		MatSelectModule
	],
	templateUrl: './task-list.html',
	styleUrl: './task-list.scss'
})
export class TaskList {
	protected readonly auth = inject(AuthService);
	private readonly taskService = inject(TaskService);
	private readonly formBuilder = inject(FormBuilder);

	protected readonly tasks = signal<Task[]>([]);
	protected readonly statistics = signal<TaskStatistics | undefined>(undefined);
	protected readonly filter = signal<TaskStatus | 'ALL'>('ALL');
	protected readonly error = signal<string | undefined>(undefined);
	protected readonly openCount = computed(() => this.tasks().filter(task => task.status === 'OPEN').length);
	protected readonly priorities: TaskPriority[] = ['HIGH', 'MEDIUM', 'LOW'];

	private readonly formDirective = viewChild.required(FormGroupDirective);

	protected readonly form = this.formBuilder.nonNullable.group({
		title: ['', [Validators.required, Validators.maxLength(200)]],
		description: [''],
		priority: ['MEDIUM' as TaskPriority],
		dueDate: ['']
	});

	constructor() {
		this.reload();
	}

	protected changeFilter(filter: TaskStatus | 'ALL'): void {
		this.filter.set(filter);
		this.reload();
	}

	protected createTask(): void {
		if (this.form.invalid) {
			return;
		}
		const value = this.form.getRawValue();
		this.taskService
			.createTask({
				title: value.title,
				description: value.description || undefined,
				priority: value.priority,
				dueDate: value.dueDate || null
			})
			.subscribe({
				next: () => {
					this.formDirective().resetForm({title: '', description: '', priority: 'MEDIUM', dueDate: ''});
					this.reload();
				},
				error: (error: unknown) => this.showError(error)
			});
	}

	protected changeStatus(task: Task, status: TaskStatus): void {
		this.taskService.changeStatus(task.id, status).subscribe({
			next: () => this.reload(),
			error: (error: unknown) => this.showError(error)
		});
	}

	protected deleteTask(task: Task): void {
		this.taskService.deleteTask(task.id).subscribe({
			next: () => this.reload(),
			error: (error: unknown) => this.showError(error)
		});
	}

	private reload(): void {
		const filter = this.filter();
		this.taskService.findTasks(filter === 'ALL' ? undefined : filter).subscribe({
			next: tasks => this.tasks.set(tasks),
			error: (error: unknown) => this.showError(error)
		});
		this.taskService.findStatistics().subscribe({
			next: statistics => this.statistics.set(statistics),
			error: () => this.statistics.set(undefined)
		});
	}

	private showError(error: unknown): void {
		const detail = (error as {error?: {detail?: string}; message?: string});
		this.error.set(detail.error?.detail ?? detail.message ?? 'Unexpected error');
	}
}

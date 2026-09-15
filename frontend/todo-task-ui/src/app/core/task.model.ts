export type TaskStatus = 'OPEN' | 'COMPLETED' | 'DELETED';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface Task {
	id: string;
	version: number;
	status: TaskStatus;
	title: string;
	description?: string;
	priority: TaskPriority;
	dueDate?: string;
	owner: string;
	assignee?: string;
	createdBy?: string;
	createdDate: string;
	lastModifiedDate: string;
	completedDate?: string;
	completionCount: number;
}

export interface TaskEvent {
	id: number;
	version: number;
	eventType: string;
	createdDate: string;
	initiatedBy?: string;
	payload: Record<string, unknown>;
}

export interface TaskStatistics {
	owner: string;
	createdCount: number;
	completedCount: number;
	reopenedCount: number;
	deletedCount: number;
	lastUpdated: string;
}

export interface TaskCommandPayload {
	title: string;
	description?: string;
	priority: TaskPriority;
	dueDate?: string | null;
}

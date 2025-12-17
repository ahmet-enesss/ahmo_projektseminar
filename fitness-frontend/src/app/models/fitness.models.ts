export interface Exercise {
  id: number;
  name: string;
  category: string;
  muscleGroups: string[]; // Set<String> im Backend wird zu Array im Frontend
  description?: string;
}

export interface ExerciseRequest {
  name: string;
  category: string;
  muscleGroups: string[];
  description?: string;
}

export interface TrainingPlanOverview {
  id: number;
  name: string;
  description: string;
  sessionCount: number;
}

export interface TrainingSessionSummary {
  id: number;
  name: string;
  orderIndex: number;
  exerciseCount: number;
}

export interface TrainingPlanDetail {
  id: number;
  name: string;
  description: string;
  sessions: TrainingSessionSummary[];
  hasSessions: boolean;
  sessionsHint: string;
}

export interface TrainingPlanRequest {
  name: string;
  description: string;
}

// --- Sprint 3: Exercise-Templates und Trainingslogs ---

export interface ExerciseExecutionTemplate {
  id: number;
  sessionId: number;
  exerciseId: number;
  exerciseName: string;
  exerciseCategory: string;
  plannedSets: number;
  plannedReps: number;
  plannedWeight: number;
  orderIndex: number;
}

export type LogStatus = 'IN_PROGRESS' | 'COMPLETED';

export interface ExecutionLog {
  id: number;
  exerciseTemplateId: number;
  exerciseName: string;
  plannedSets: number;
  plannedReps: number;
  plannedWeight: number;
  actualSets: number;
  actualReps: number;
  actualWeight: number;
  completed: boolean;
  notes?: string;
}

export interface SessionLog {
  id: number;
  sessionTemplateId: number;
  sessionName: string;
  startTime: string;
  endTime?: string;
  status: LogStatus;
  notes?: string;
  executions: ExecutionLog[];
}

// --- Session-Template Übersicht (Sprint 3) ---

export interface TrainingSessionTemplateOverview {
  id: number;
  name: string;
  planId?: number;
  planName: string;
  orderIndex: number;
  exerciseCount: number;
  executionCount: number;
}

export interface TrainingSessionTemplateRequest {
  planId?: number;
  name: string;
  orderIndex: number;
}
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
  scheduledDate: string; // LocalDate wird als String übertragen
  exerciseCount: number;
  status: 'GEPLANT' | 'ABGESCHLOSSEN';
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

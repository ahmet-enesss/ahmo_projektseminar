import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  Exercise, ExerciseRequest,
  TrainingPlanOverview, TrainingPlanDetail, TrainingPlanRequest,
  ExerciseExecutionTemplate, SessionLog, ExecutionLog,
  TrainingSessionTemplateOverview, TrainingSessionTemplateRequest
} from '../models/fitness.models';

@Injectable({
  providedIn: 'root'
})
export class FitnessService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api';

  // ... (Hier bleiben alle Exercise und TrainingPlan Methoden unverändert) ...

  // --- Exercises ---
  getExercises(): Observable<Exercise[]> {
    return this.http.get<Exercise[]>(`${this.baseUrl}/exercises`);
  }

  getExerciseById(id: number): Observable<Exercise> {
    return this.http.get<Exercise>(`${this.baseUrl}/exercises/${id}`);
  }

  createExercise(exercise: ExerciseRequest): Observable<Exercise> {
    return this.http.post<Exercise>(`${this.baseUrl}/exercises`, exercise)
      .pipe(catchError(this.handleError));
  }

  updateExercise(id: number, exercise: ExerciseRequest): Observable<any> {
    return this.http.put(`${this.baseUrl}/exercises/${id}`, exercise)
      .pipe(catchError(this.handleError));
  }

  deleteExercise(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/exercises/${id}`);
  }

  // --- Training Plans ---
  getTrainingPlans(): Observable<TrainingPlanOverview[]> {
    return this.http.get<TrainingPlanOverview[]>(`${this.baseUrl}/trainingplans`);
  }

  getTrainingPlanById(id: number): Observable<TrainingPlanDetail> {
    return this.http.get<TrainingPlanDetail>(`${this.baseUrl}/trainingplans/${id}`);
  }

  createTrainingPlan(plan: TrainingPlanRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/trainingplans`, plan)
      .pipe(catchError(this.handleError));
  }

  updateTrainingPlan(id: number, plan: TrainingPlanRequest): Observable<any> {
    return this.http.put(`${this.baseUrl}/trainingplans/${id}`, plan)
      .pipe(catchError(this.handleError));
  }

  deleteTrainingPlan(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/trainingplans/${id}`);
  }

  // --- Training Sessions (ERWEITERT) ---

  // Anmerkung: Backend verwendet Session-Templates unter /api/session-templates.
  // Die alten /api/trainingsessions create/update Endpunkte sind im Backend veraltet.
  // Um Inkonsistenzen zu vermeiden, leiten wir Frontend-Aufrufe auf die Session-Template-APIs um.

  createTrainingSession(sessionRequest: any): Observable<any> {
    // Mappe Request-Felder falls nötig: erwartet planId, name, orderIndex
    const templateReq: TrainingSessionTemplateRequest = {
      planId: sessionRequest.planId ?? null,
      name: sessionRequest.name ?? 'Neue Session',
      orderIndex: sessionRequest.orderIndex ?? 1
    };
    return this.createSessionTemplate(templateReq);
  }

  // NEU: Session laden (für Update wichtig) - wird jetzt auf Session-Templates gemappt
  getTrainingSessionById(id: number): Observable<any> {
    return this.getSessionTemplateById(id);
  }

  // NEU: Session aktualisieren - leitet auf Session-Templates um
  updateTrainingSession(id: number, sessionRequest: any): Observable<any> {
    const templateReq: TrainingSessionTemplateRequest = {
      planId: sessionRequest.planId ?? null,
      name: sessionRequest.name ?? 'Session',
      orderIndex: sessionRequest.orderIndex ?? 1
    };
    return this.updateSessionTemplate(id, templateReq);
  }

  deleteTrainingSession(id: number): Observable<void> {
    return this.deleteSessionTemplate(id);
  }

  // --- Exercise-Templates pro Session (Sprint 3) ---

  getExerciseTemplatesForSession(sessionId: number): Observable<ExerciseExecutionTemplate[]> {
    return this.http
      .get<ExerciseExecutionTemplate[]>(`${this.baseUrl}/trainingsessions/${sessionId}/exercise-templates`)
      .pipe(catchError(this.handleError));
  }

  saveExerciseTemplate(sessionId: number, template: Partial<ExerciseExecutionTemplate>): Observable<ExerciseExecutionTemplate> {
    if (template.id) {
      return this.http
        .put<ExerciseExecutionTemplate>(
          `${this.baseUrl}/trainingsessions/${sessionId}/exercise-templates/${template.id}`,
          template
        )
        .pipe(catchError(this.handleError));
    }
    return this.http
      .post<ExerciseExecutionTemplate>(
        `${this.baseUrl}/trainingsessions/${sessionId}/exercise-templates`,
        template
      )
      .pipe(catchError(this.handleError));
  }

  deleteExerciseTemplate(sessionId: number, templateId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.baseUrl}/trainingsessions/${sessionId}/exercise-templates/${templateId}`)
      .pipe(catchError(this.handleError));
  }

  // --- Trainingslogs (SessionLog / ExecutionLog) ---

  startTraining(sessionTemplateId: number, notes?: string): Observable<SessionLog> {
    return this.http
      .post<SessionLog>(`${this.baseUrl}/sessionlogs/start`, { sessionTemplateId, notes })
      .pipe(catchError(this.handleError));
  }

  getSessionLog(id: number): Observable<SessionLog> {
    return this.http
      .get<SessionLog>(`${this.baseUrl}/sessionlogs/${id}`)
      .pipe(catchError(this.handleError));
  }

  updateExecutionLog(payload: {
    executionLogId: number;
    actualSets: number;
    actualReps: number;
    actualWeight: number;
    completed?: boolean;
    notes?: string;
    // optional geplante Werte (werden vom Backend akzeptiert)
    plannedSets?: number;
    plannedReps?: number;
    plannedWeight?: number;
  }): Observable<ExecutionLog> {
    return this.http
      .put<ExecutionLog>(`${this.baseUrl}/sessionlogs/execution`, payload)
      .pipe(catchError(this.handleError));
  }

  completeTraining(logId: number): Observable<SessionLog> {
    return this.http
      .post<SessionLog>(`${this.baseUrl}/sessionlogs/${logId}/complete`, {})
      .pipe(catchError(this.handleError));
  }

  abortTraining(logId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.baseUrl}/sessionlogs/${logId}`)
      .pipe(catchError(this.handleError));
  }

  // --- Session-Templates (unabhängige Verwaltung) ---

  getSessionTemplates(): Observable<TrainingSessionTemplateOverview[]> {
    return this.http
      .get<TrainingSessionTemplateOverview[]>(`${this.baseUrl}/session-templates`)
      .pipe(catchError(this.handleError));
  }

  getSessionTemplateById(id: number): Observable<TrainingSessionTemplateOverview> {
    return this.http
      .get<TrainingSessionTemplateOverview>(`${this.baseUrl}/session-templates/${id}`)
      .pipe(catchError(this.handleError));
  }

  createSessionTemplate(request: TrainingSessionTemplateRequest): Observable<TrainingSessionTemplateOverview> {
    return this.http
      .post<TrainingSessionTemplateOverview>(`${this.baseUrl}/session-templates`, request)
      .pipe(catchError(this.handleError));
  }

  updateSessionTemplate(id: number, request: TrainingSessionTemplateRequest): Observable<TrainingSessionTemplateOverview> {
    return this.http
      .put<TrainingSessionTemplateOverview>(`${this.baseUrl}/session-templates/${id}`, request)
      .pipe(catchError(this.handleError));
  }

  deleteSessionTemplate(id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.baseUrl}/session-templates/${id}`)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Ein unbekannter Fehler ist aufgetreten.';
    if (error.status === 400 || error.status === 409) {
      errorMessage = error.error.message || error.error.error || JSON.stringify(error.error);
    }
    return throwError(() => new Error(errorMessage));
  }
}

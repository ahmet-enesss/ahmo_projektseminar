import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  Exercise, ExerciseRequest,
  TrainingPlanOverview, TrainingPlanDetail, TrainingPlanRequest
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

  createTrainingSession(sessionRequest: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/trainingsessions`, sessionRequest)
      .pipe(catchError(this.handleError));
  }

  // NEU: Session laden (für Update wichtig)
  getTrainingSessionById(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/trainingsessions/${id}`);
  }

  // NEU: Session aktualisieren
  updateTrainingSession(id: number, sessionRequest: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/trainingsessions/${id}`, sessionRequest)
      .pipe(catchError(this.handleError));
  }

  deleteTrainingSession(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/trainingsessions/${id}`);
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Ein unbekannter Fehler ist aufgetreten.';
    if (error.status === 400 || error.status === 409) {
      errorMessage = error.error.message || error.error.error || JSON.stringify(error.error);
    }
    return throwError(() => new Error(errorMessage));
  }
}

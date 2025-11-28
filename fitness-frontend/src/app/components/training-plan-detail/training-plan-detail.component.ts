import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { FitnessService } from '../../services/fitness.service';
import { TrainingPlanDetail, TrainingSessionSummary, Exercise } from '../../models/fitness.models';

@Component({
  selector: 'app-training-plan-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="container mt-4" *ngIf="plan">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <button class="btn btn-secondary" (click)="goBack()">Zurück</button>
        <span class="badge bg-info text-dark">ID: {{ plan.id }}</span>
      </div>

      <h3>Plan bearbeiten: {{ plan.name }}</h3>

      <div *ngIf="successMessage" class="alert alert-success">{{ successMessage }}</div>
      <div *ngIf="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

      <form [formGroup]="editForm" (ngSubmit)="saveChanges()" class="card p-3 mb-5 shadow-sm">
        <div class="row">
          <div class="col-md-6 mb-3">
            <label class="form-label">Name des Plans</label>
            <input type="text" class="form-control" formControlName="name">
          </div>
          <div class="col-md-6 mb-3">
            <label class="form-label">Beschreibung</label>
            <input type="text" class="form-control" formControlName="description">
          </div>
        </div>
        <button type="submit" class="btn btn-success" [disabled]="editForm.invalid">Plan-Änderungen speichern</button>
      </form>

      <hr>

      <div class="row">
        <div class="col-md-4">
          <div class="card bg-light mb-3">
            <div class="card-header fw-bold">Neue Session hinzufügen</div>
            <div class="card-body">
              <form [formGroup]="sessionForm" (ngSubmit)="addSession()">
                <div class="mb-3">
                  <label class="form-label">Session Name</label>
                  <input type="text" class="form-control" formControlName="name" placeholder="z.B. Brusttraining A">
                </div>
                <div class="mb-3">
                  <label class="form-label">Geplantes Datum</label>
                  <input type="date" class="form-control" formControlName="scheduledDate">
                </div>

                <div class="mb-3">
                  <label class="form-label">Übungen hinzufügen</label>
                  <select multiple class="form-select" formControlName="exerciseIds" style="height: 150px;">
                    <option *ngFor="let ex of availableExercises" [value]="ex.id">
                      {{ ex.name }} ({{ ex.category }})
                    </option>
                  </select>
                  <small class="text-muted d-block mt-1">
                    Halte <kbd>STRG</kbd> (Win) oder <kbd>CMD</kbd> (Mac) gedrückt, um mehrere zu wählen.
                  </small>
                </div>

                <button type="submit" class="btn btn-primary w-100" [disabled]="sessionForm.invalid">
                  Session hinzufügen
                </button>
              </form>
            </div>
          </div>
        </div>

        <div class="col-md-8">
          <h4>Geplante Sessions</h4>

          <div *ngIf="!plan.hasSessions" class="alert alert-warning">
            {{ plan.sessionsHint }}
          </div>

          <div class="table-responsive" *ngIf="plan.hasSessions">
            <table class="table table-bordered table-hover bg-white align-middle">
              <thead class="table-dark">
                <tr>
                  <th>Datum</th>
                  <th>Name</th>
                  <th>Übungen</th>
                  <th>Status</th>
                  <th>Aktion</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let session of plan.sessions">
                  <td>{{ session.scheduledDate }}</td>
                  <td>{{ session.name }}</td>
                  <td>
                    <span class="badge bg-secondary">{{ session.exerciseCount }} Übungen</span>
                  </td>
                  <td>
                    <button class="btn btn-sm w-100"
                            [class.btn-outline-success]="session.status === 'ABGESCHLOSSEN'"
                            [class.btn-outline-primary]="session.status === 'GEPLANT'"
                            (click)="toggleStatus(session)"
                            title="Klicken zum Ändern">
                      <i class="bi" [class.bi-check-circle-fill]="session.status === 'ABGESCHLOSSEN'" [class.bi-clock]="session.status === 'GEPLANT'"></i>
                      {{ session.status }}
                    </button>
                  </td>
                  <td>
                    <button class="btn btn-danger btn-sm" (click)="deleteSession(session)">
                      Entfernen
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <div class="container mt-5 text-center" *ngIf="!plan">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <p>Lade Plan-Details...</p>
    </div>
  `
})
export class TrainingPlanDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private service = inject(FitnessService);
  private location = inject(Location);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  plan: TrainingPlanDetail | null = null;
  availableExercises: Exercise[] = [];

  successMessage = '';
  errorMessage = '';

  editForm = this.fb.group({
    name: ['', Validators.required],
    description: ['', Validators.required]
  });

  sessionForm = this.fb.group({
    name: ['', Validators.required],
    scheduledDate: ['', Validators.required],
    exerciseIds: [[]]
  });

  ngOnInit() {
    this.loadPlanData();
    this.loadExercises();
  }

  loadExercises() {
    this.service.getExercises().subscribe(data => {
      this.availableExercises = data;
    });
  }

  loadPlanData() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.service.getTrainingPlanById(id).subscribe({
      next: (data) => {
        this.plan = data;
        this.editForm.patchValue({
          name: data.name,
          description: data.description
        });
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = 'Fehler beim Laden: ' + err.message;
        this.cdr.detectChanges();
      }
    });
  }

  saveChanges() {
    if (!this.plan || this.editForm.invalid) return;

    this.service.updateTrainingPlan(this.plan.id, this.editForm.value as any).subscribe({
      next: () => {
        this.successMessage = 'Plan-Details erfolgreich gespeichert';
        this.errorMessage = '';
        this.cdr.detectChanges();
        setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 3000);
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.cdr.detectChanges();
      }
    });
  }

  addSession() {
    if (!this.plan || this.sessionForm.invalid) return;

    const request = {
      planId: this.plan.id,
      name: this.sessionForm.value.name,
      scheduledDate: this.sessionForm.value.scheduledDate,
      exerciseIds: this.sessionForm.value.exerciseIds || []
    };

    this.service.createTrainingSession(request).subscribe({
      next: () => {
        this.sessionForm.reset();
        this.sessionForm.get('exerciseIds')?.setValue([] as any);
        this.loadPlanData();
        this.successMessage = 'Session hinzugefügt!';
        this.errorMessage = ''; // Fehler zurücksetzen falls vorher einer da war
        setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 3000);
      },
      error: (err) => {
        // Direkt err.message anzeigen für einheitliche Fehlermeldung
        this.errorMessage = err.message;
        this.cdr.detectChanges();
      }
    });
  }

  // 1. Status umschalten
  toggleStatus(sessionSummary: TrainingSessionSummary) {
    // 1. Erst die volle Session laden, damit wir die Exercise-Liste bekommen
    this.service.getTrainingSessionById(sessionSummary.id).subscribe({
      next: (fullSession) => {

        // 2. Status umkehren
        const newStatus = fullSession.status === 'GEPLANT' ? 'ABGESCHLOSSEN' : 'GEPLANT';

        // 3. Request bauen (wir brauchen Exercise IDs, nicht die ganzen Objekte)
        const exerciseIds = fullSession.exerciseExecutions
            ? fullSession.exerciseExecutions.map((ex: any) => ex.id)
            : [];

        const updateRequest = {
          planId: this.plan!.id,
          name: fullSession.name,
          scheduledDate: fullSession.scheduledDate,
          status: newStatus,
          exerciseIds: exerciseIds
        };

        // 4. Update senden
        this.service.updateTrainingSession(sessionSummary.id, updateRequest).subscribe({
          next: () => {
            this.loadPlanData(); // Liste aktualisieren
          },
          error: (err) => {
            this.errorMessage = 'Status konnte nicht geändert werden: ' + err.message;
            this.cdr.detectChanges();
          }
        });
      },
      error: (err) => {
        this.errorMessage = 'Fehler beim Laden der Session-Details: ' + err.message;
        this.cdr.detectChanges();
      }
    });
  }

  deleteSession(session: TrainingSessionSummary) {
    if(!confirm(`Session "${session.name}" am ${session.scheduledDate} wirklich löschen?`)) return;

    this.service.deleteTrainingSession(session.id).subscribe({
      next: () => {
        this.loadPlanData();
      },
      error: (err) => {
        this.errorMessage = 'Löschen fehlgeschlagen: ' + err.message;
        this.cdr.detectChanges();
      }
    });
  }

  goBack() {
    this.location.back();
  }
}

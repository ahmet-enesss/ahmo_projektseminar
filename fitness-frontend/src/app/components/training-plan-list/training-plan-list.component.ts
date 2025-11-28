import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core'; // <--- 1. ChangeDetectorRef importieren
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { FitnessService } from '../../services/fitness.service';
import { TrainingPlanOverview } from '../../models/fitness.models';

@Component({
  selector: 'app-training-plan-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="container mt-4">
      <h2>Trainingspläne</h2>

      <div *ngIf="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

      <div class="card mb-4">
        <div class="card-header">Neuen Plan anlegen</div>
        <div class="card-body">
          <form [formGroup]="planForm" (ngSubmit)="createPlan()">
            <div class="mb-3">
              <label class="form-label">Name</label>
              <input type="text" class="form-control" formControlName="name"
                     [class.is-invalid]="planForm.get('name')?.invalid && planForm.get('name')?.touched">
            </div>
            <div class="mb-3">
              <label class="form-label">Beschreibung</label>
              <input type="text" class="form-control" formControlName="description">
            </div>
            <button type="submit" class="btn btn-primary" [disabled]="planForm.invalid">Anlegen</button>
          </form>
        </div>
      </div>

      <div class="row">
        <div class="col-md-4 mb-3" *ngFor="let plan of plans">
          <div class="card h-100">
            <div class="card-body">
              <h5 class="card-title">{{ plan.name }}</h5>
              <p class="card-text">{{ plan.description }}</p>
              <p class="text-muted">Anzahl Sessions: {{ plan.sessionCount }}</p>

              <a [routerLink]="['/plans', plan.id]" class="btn btn-info btn-sm me-2">Details / Sessions</a>

              <button class="btn btn-danger btn-sm" (click)="deletePlan(plan)">Löschen</button>
            </div>
          </div>
        </div>
        <div *ngIf="plans.length === 0" class="col-12 text-center text-muted">
          Noch keine Trainingspläne vorhanden.
        </div>
      </div>
    </div>
  `
})
export class TrainingPlanListComponent implements OnInit {
  private service = inject(FitnessService);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef); // <--- 2. Hier injizieren wir den "Wecker"

  plans: TrainingPlanOverview[] = [];
  errorMessage = '';

  planForm = this.fb.group({
    name: ['', Validators.required],
    description: ['', Validators.required]
  });

  ngOnInit() {
    this.loadPlans();
  }

  loadPlans() {
    this.service.getTrainingPlans().subscribe({
      next: (data) => {
        console.log('Trainingspläne geladen:', data);
        this.plans = data;
        this.cdr.detectChanges(); // <--- 3. Angular zwingen, die Ansicht zu aktualisieren
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Fehler beim Laden: ' + err.message;
        this.cdr.detectChanges(); // Auch bei Fehlern aktualisieren
      }
    });
  }

  createPlan() {
    if (this.planForm.invalid) return;
    this.service.createTrainingPlan(this.planForm.value as any).subscribe({
      next: () => {
        this.loadPlans();
        this.planForm.reset();
        this.errorMessage = '';
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.cdr.detectChanges();
      }
    });
  }

  deletePlan(plan: TrainingPlanOverview) {
    if (confirm(`Plan "${plan.name}" löschen?`)) {
      this.service.deleteTrainingPlan(plan.id).subscribe(() => this.loadPlans());
    }
  }
}

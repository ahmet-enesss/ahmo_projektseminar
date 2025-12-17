import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FitnessService } from '../../services/fitness.service';
import { TrainingPlanDetail, TrainingSessionSummary, Exercise } from '../../models/fitness.models';

@Component({
  selector: 'app-training-plan-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './training-plan-detail.component.html',
  styleUrl: './training-plan-detail.component.css'
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

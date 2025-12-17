import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { FitnessService } from '../../services/fitness.service';
import { TrainingSessionTemplateOverview, TrainingSessionTemplateRequest, TrainingPlanOverview } from '../../models/fitness.models';

@Component({
  selector: 'app-session-template-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './session-template-list.component.html',
  styleUrl: './session-template-list.component.css'
})
export class SessionTemplateListComponent implements OnInit {
  private service = inject(FitnessService);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  sessions: TrainingSessionTemplateOverview[] = [];
  plans: TrainingPlanOverview[] = [];
  errorMessage = '';
  successMessage = '';
  editingSessionId: number | null = null;

  sessionForm = this.fb.group({
    name: ['', [Validators.required]],
    planId: [null as number | null],
    orderIndex: [1, [Validators.required, Validators.min(1), Validators.max(30)]]
  });

  ngOnInit() {
    this.loadSessions();
    this.loadPlans();
  }

  loadSessions() {
    this.service.getSessionTemplates().subscribe({
      next: (data) => {
        this.sessions = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = 'Fehler beim Laden: ' + err.message;
        this.cdr.detectChanges();
      }
    });
  }

  loadPlans() {
    this.service.getTrainingPlans().subscribe({
      next: (data) => {
        this.plans = data;
      },
      error: (err) => {
        console.error('Fehler beim Laden der Pläne:', err);
      }
    });
  }

  createSession() {
    if (this.sessionForm.invalid) return;

    const formVal = this.sessionForm.value;
    const request: TrainingSessionTemplateRequest = {
      name: formVal.name!,
      planId: formVal.planId || undefined,
      orderIndex: formVal.orderIndex!
    };

    this.service.createSessionTemplate(request).subscribe({
      next: () => {
        this.loadSessions();
        this.sessionForm.reset();
        this.sessionForm.get('orderIndex')?.setValue(1);
        this.successMessage = 'Session-Vorlage erfolgreich erstellt';
        this.errorMessage = '';
        setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 3000);
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.cdr.detectChanges();
      }
    });
  }

  startEdit(session: TrainingSessionTemplateOverview) {
    this.editingSessionId = session.id;
    this.sessionForm.patchValue({
      name: session.name,
      planId: session.planId || null,
      orderIndex: session.orderIndex
    });
    this.errorMessage = '';
    this.successMessage = '';
  }

  cancelEdit() {
    this.editingSessionId = null;
    this.sessionForm.reset();
    this.sessionForm.get('orderIndex')?.setValue(1);
    this.errorMessage = '';
  }

  updateSession() {
    if (this.sessionForm.invalid || !this.editingSessionId) return;

    const formVal = this.sessionForm.value;
    const request: TrainingSessionTemplateRequest = {
      name: formVal.name!,
      planId: formVal.planId || undefined,
      orderIndex: formVal.orderIndex!
    };

    this.service.updateSessionTemplate(this.editingSessionId, request).subscribe({
      next: () => {
        this.loadSessions();
        this.cancelEdit();
        this.successMessage = 'Session-Vorlage erfolgreich gespeichert';
        setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 3000);
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.cdr.detectChanges();
      }
    });
  }

  deleteSession(session: TrainingSessionTemplateOverview) {
    if (!confirm(`Möchten Sie die Session-Vorlage "${session.name}" wirklich löschen?`)) {
      return;
    }

    this.service.deleteSessionTemplate(session.id).subscribe({
      next: () => {
        this.loadSessions();
        this.successMessage = 'Session-Vorlage erfolgreich gelöscht';
        setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 3000);
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = 'Fehler beim Löschen: ' + err.message;
        this.cdr.detectChanges();
      }
    });
  }
}


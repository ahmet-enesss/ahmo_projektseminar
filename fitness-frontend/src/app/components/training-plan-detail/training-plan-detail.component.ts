import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FitnessService } from '../../services/fitness.service';
import { TrainingPlanDetail, TrainingSessionSummary, Exercise } from '../../models/fitness.models';

//verbindet ts mit html und css
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

  //speichert geladene Trainingsplan und Liste verfügbaren Übungen
  plan: TrainingPlanDetail | null = null;
  availableExercises: Exercise[] = [];
  availableTemplates: { id: number; name: string; orderIndex: number }[] = [];

  successMessage = '';
  errorMessage = '';

  //bearbeitung name und beschreibung
  editForm = this.fb.group({
    name: ['', Validators.required],
    description: ['', Validators.required]
  });

  //session hinzufügen (Name,Reihenfolge)
  sessionForm = this.fb.group({
    name: ['', Validators.required],
    orderIndex: [1, [Validators.required, Validators.min(1), Validators.max(30)]]
  });

  ngOnInit() {
    this.loadPlanData();
    this.loadExercises();
    this.loadAllSessionTemplates();
  }

  loadExercises() {
    this.service.getExercises().subscribe(data => {
      this.availableExercises = data;
    });
  }

  loadAllSessionTemplates() {
    this.service.getSessionTemplates().subscribe({
      next: (data) => {
        this.availableTemplates = data.map(t => ({ id: t.id, name: t.name, orderIndex: t.orderIndex }));
        this.cdr.detectChanges();
      },
      error: (err) => {
        // Nicht kritisch
        console.warn('Fehler beim Laden der Session-Vorlagen:', err.message);
      }
    });
  }

  //liest Plan-ID  und füllt die daten des plans aus
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

  //erstellt neuen Session vorlage für Plan und aktualisiert plan
  addSession() {
    if (!this.plan || this.sessionForm.invalid) return;

    const request = {
      planId: this.plan.id,
      name: this.sessionForm.value.name!,
      orderIndex: this.sessionForm.value.orderIndex!
    };

    this.service.createSessionTemplate(request).subscribe({
      next: () => {
        this.sessionForm.reset();
        this.sessionForm.get('orderIndex')?.setValue(1);
        this.loadPlanData();
        this.successMessage = 'Session-Vorlage hinzugefügt!';
        this.errorMessage = '';
        setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 3000);
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.cdr.detectChanges();
      }
    });
  }

  // Hilfsfunktion: Templates, die noch nicht im Plan sind
  getTemplatesNotInPlan() {
    if (!this.plan) return this.availableTemplates;
    const existingIds = new Set<number>((this.plan.templates || []).map(p => p.id));
    return this.availableTemplates.filter(t => !existingIds.has(t.id));
  }

  // Hinzufügen vorhandener Vorlage in den Plan (Referenz)
  addExistingTemplateToPlan(templateIdStr: string) {
    if (!this.plan) return;
    const templateId = Number(templateIdStr);
    if (!templateId || isNaN(templateId)) return;
    // optional: position aus UI (nicht implementiert hier, verwendet default null)
    this.service.addTemplateToPlan(this.plan.id, templateId).subscribe({
      next: () => {
        this.successMessage = 'Vorlage in Plan aufgenommen';
        this.loadPlanData();
        setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 3000);
      },
      error: (err) => {
        this.errorMessage = err.message || 'Fehler beim Hinzufügen der Vorlage';
        this.cdr.detectChanges();
      }
    });
  }

  deleteSession(session: TrainingSessionSummary) {
    if(!confirm(`Session-Vorlage "${session.name}" wirklich löschen?`)) return;

    this.service.deleteSessionTemplate(session.id).subscribe({
      next: () => {
        this.loadPlanData();
        this.successMessage = 'Session-Vorlage gelöscht';
        setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 3000);
        this.cdr.detectChanges();
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

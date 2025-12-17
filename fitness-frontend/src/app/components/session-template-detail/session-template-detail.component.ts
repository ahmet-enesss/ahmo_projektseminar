import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FitnessService } from '../../services/fitness.service';
import { Exercise, ExerciseExecutionTemplate } from '../../models/fitness.models';

@Component({
  selector: 'app-session-template-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './session-template-detail.component.html',
  styleUrl: './session-template-detail.component.css'
})
export class SessionTemplateDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private service = inject(FitnessService);
  private location = inject(Location);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  planId!: number;
  sessionId!: number;

  availableExercises: Exercise[] = [];
  templates: ExerciseExecutionTemplate[] = [];

  editingTemplate: ExerciseExecutionTemplate | null = null;

  errorMessage = '';
  successMessage = '';

  form = this.fb.group({
    exerciseId: [null as number | null, Validators.required],
    plannedSets: [3, [Validators.required, Validators.min(1)]],
    plannedReps: [10, [Validators.required, Validators.min(1)]],
    plannedWeight: [0, [Validators.required, Validators.min(0)]],
    orderIndex: [1, [Validators.required, Validators.min(1)]]
  });

  ngOnInit(): void {
    this.planId = Number(this.route.snapshot.paramMap.get('planId'));
    this.sessionId = Number(this.route.snapshot.paramMap.get('sessionId'));

    this.loadExercises();
    this.loadTemplates();
  }

  loadExercises() {
    this.service.getExercises().subscribe({
      next: (data) => {
        this.availableExercises = data;
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.cdr.detectChanges();
      }
    });
  }

  loadTemplates() {
    this.service.getExerciseTemplatesForSession(this.sessionId).subscribe({
      next: (data) => {
        this.templates = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.cdr.detectChanges();
      }
    });
  }

  // Gibt die Übungen zurück, die für das Hinzufügen auswählbar sind.
  // Bereits in der Session verwendete Übungen werden ausgeblendet, außer wenn wir gerade diese Vorlage bearbeiten.
  getSelectableExercises(): Exercise[] {
    const used = new Set<number>(this.templates.map(t => t.exerciseId));
    if (this.editingTemplate) {
      used.delete(this.editingTemplate.exerciseId);
    }
    return this.availableExercises.filter(ex => !used.has(ex.id));
  }

  startCreate() {
    this.editingTemplate = null;
    this.form.reset({
      exerciseId: null,
      plannedSets: 3,
      plannedReps: 10,
      plannedWeight: 0,
      orderIndex: this.templates.length + 1
    });
  }

  edit(template: ExerciseExecutionTemplate) {
    this.editingTemplate = template;
    this.form.reset({
      exerciseId: template.exerciseId,
      plannedSets: template.plannedSets,
      plannedReps: template.plannedReps,
      plannedWeight: template.plannedWeight,
      orderIndex: template.orderIndex
    });
  }

  save() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload: Partial<ExerciseExecutionTemplate> = {
      id: this.editingTemplate?.id,
      sessionId: this.sessionId,
      exerciseId: this.form.value.exerciseId!,
      plannedSets: this.form.value.plannedSets!,
      plannedReps: this.form.value.plannedReps!,
      plannedWeight: this.form.value.plannedWeight!,
      orderIndex: this.form.value.orderIndex!
    };

    this.service.saveExerciseTemplate(this.sessionId, payload).subscribe({
      next: () => {
        this.successMessage = this.editingTemplate ? 'Vorlage aktualisiert' : 'Vorlage hinzugefügt';
        this.errorMessage = '';
        this.editingTemplate = null;
        this.loadTemplates();
        this.startCreate();
        setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 3000);
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.cdr.detectChanges();
      }
    });
  }

  delete(template: ExerciseExecutionTemplate) {
    if (!confirm(`Übung "${template.exerciseName}" wirklich aus der Session entfernen?`)) {
      return;
    }

    this.service.deleteExerciseTemplate(this.sessionId, template.id).subscribe({
      next: () => {
        this.loadTemplates();
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.cdr.detectChanges();
      }
    });
  }

  goBack() {
    this.location.back();
  }
}



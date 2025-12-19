import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FitnessService } from '../../services/fitness.service';
import { ExecutionLog, SessionLog } from '../../models/fitness.models';
import { firstValueFrom } from 'rxjs';

//verbindet ts mit html und css
@Component({
  selector: 'app-training-execution',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './training-execution.component.html',
  styleUrl: './training-execution.component.css'
})
export class TrainingExecutionComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private service = inject(FitnessService);
  private location = inject(Location);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  //speicher log-ID und trainings-log
  logId!: number;
  log: SessionLog | null = null;

  errorMessage = '';
  successMessage = '';

  //pro Übung eigenes Formular abgespeichert
  executionForms = new Map<number, ReturnType<FormBuilder['group']>>();

  ngOnInit(): void {
    // Es gibt zwei Einstiege: vorhandenen Log laden oder neuen starten
    const existingLogId = this.route.snapshot.paramMap.get('logId');
    const sessionTemplateId = this.route.snapshot.paramMap.get('sessionTemplateId');

    if (existingLogId) {
      this.logId = Number(existingLogId);
      this.loadLog();
    } else if (sessionTemplateId) {
      this.startTraining(Number(sessionTemplateId));
    } else {
      this.errorMessage = 'Keine Trainingssession ausgewählt.';
    }
  }

  private buildFormForExecution(exec: ExecutionLog) {
    const form = this.fb.group({
      // geplante Werte (editable während IN_PROGRESS)
      plannedSets: [exec.plannedSets, [Validators.required, Validators.min(1)]],
      plannedReps: [exec.plannedReps, [Validators.required, Validators.min(1)]],
      plannedWeight: [exec.plannedWeight, [Validators.required, Validators.min(0)]],

      // ist-Werte
      actualSets: [exec.actualSets, [Validators.required, Validators.min(1)]],
      actualReps: [exec.actualReps, [Validators.required, Validators.min(1)]],
      actualWeight: [exec.actualWeight, [Validators.required, Validators.min(0)]],
      completed: [exec.completed],
      notes: [exec.notes || '']
    });
    this.executionForms.set(exec.id, form);
  }

  private startTraining(sessionTemplateId: number) {
    this.service.startTraining(sessionTemplateId).subscribe({
      next: (log) => {
        this.log = log;
        this.logId = log.id;
        this.initForms();
        this.cdr.detectChanges();

        // Retry: falls executions leer sind (Race-Condition), nochmals laden
        if (!this.log.executions || this.log.executions.length === 0) {
          setTimeout(() => {
            this.loadLog();
          }, 300);
        }
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.cdr.detectChanges();
      }
    });
  }

  private loadLog() {
    this.service.getSessionLog(this.logId).subscribe({
      next: (log) => {
        this.log = log;
        // Fallback: falls executions leer sind, lade session templates und baue temporäre executions
        if (!this.log.executions || this.log.executions.length === 0) {
          this.tryBuildFallbackExecutions(this.log.sessionTemplateId).then(() => {
            this.initForms();
            this.cdr.detectChanges();
          });
          return;
        }
        this.initForms();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.cdr.detectChanges();
      }
    });
  }

  // Versucht, temporäre Execution-Objekte aus Session-Templates zu erstellen (nur Anzeige)
  private async tryBuildFallbackExecutions(sessionTemplateId: number) {
    try {
      const templates = await firstValueFrom(this.service.getExerciseTemplatesForSession(sessionTemplateId));
      if (!this.log) return;
      // templates für ExecutionLog-like objekte werden für UI umgewandelt (note: negative ids bedeuten nur temporär )
      this.log.executions = templates.map((t, idx) => ({
        id: -(idx + 1),
        exerciseTemplateId: t.id,
        exerciseName: t.exerciseName,
        plannedSets: t.plannedSets,
        plannedReps: t.plannedReps,
        plannedWeight: t.plannedWeight,
        actualSets: t.plannedSets,
        actualReps: t.plannedReps,
        actualWeight: t.plannedWeight,
        completed: false,
        notes: ''
      }));
      this.cdr.detectChanges();
    } catch (e: any) {
      // ignoriere fallback errors
    }
  }

  private initForms() {
    if (!this.log) return;
    this.executionForms.clear();
    this.log.executions.forEach(exec => this.buildFormForExecution(exec));
  }

  saveExecution(exec: ExecutionLog) {
    if (!this.log) return;
    const form = this.executionForms.get(exec.id);
    if (!form || form.invalid) {
      form?.markAllAsTouched();
      return;
    }

    const value = form.value;
    this.service.updateExecutionLog({
      executionLogId: exec.id,
      // aktuelle values
      actualSets: value.actualSets,
      actualReps: value.actualReps,
      actualWeight: value.actualWeight,
      // optional: mark completed and notes
      completed: value.completed,
      notes: value.notes,
      // neu: geplante Werte mitschicken, wenn verändert
      plannedSets: value.plannedSets,
      plannedReps: value.plannedReps,
      plannedWeight: value.plannedWeight
    }).subscribe({
      next: () => {
        this.successMessage = 'Übung gespeichert';
        this.errorMessage = '';
        setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 2000);
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.cdr.detectChanges();
      }
    });
  }

  completeTraining() {
    if (!this.log) return;

    if (!confirm('Training wirklich abschließen? Danach sind keine Änderungen mehr möglich.')) {
      return;
    }

    this.service.completeTraining(this.log.id).subscribe({
      next: (updated) => {
        this.log = { ...this.log!, ...updated };
        this.successMessage = 'Training abgeschlossen';
        this.errorMessage = '';
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.cdr.detectChanges();
      }
    });
  }

  abortTraining() {
    if (!this.log) {
      this.goBack();
      return;
    }
    if (!confirm('Laufendes Training wirklich abbrechen und löschen?')) {
      return;
    }

    this.service.abortTraining(this.log.id).subscribe({
      next: () => {
        this.goBack();
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.cdr.detectChanges();
      }
    });
  }

  isCompleted(): boolean {
    return this.log?.status === 'COMPLETED';
  }

  goBack() {
    this.location.back();
  }
}

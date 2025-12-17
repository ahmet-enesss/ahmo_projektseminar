import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FitnessService } from '../../services/fitness.service';
import { ExecutionLog, SessionLog } from '../../models/fitness.models';

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

  logId!: number;
  log: SessionLog | null = null;

  errorMessage = '';
  successMessage = '';

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
        this.initForms();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.cdr.detectChanges();
      }
    });
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
      actualSets: value.actualSets,
      actualReps: value.actualReps,
      actualWeight: value.actualWeight,
      completed: value.completed,
      notes: value.notes
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



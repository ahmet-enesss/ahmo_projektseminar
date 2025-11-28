import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { FitnessService } from '../../services/fitness.service';
import { Exercise } from '../../models/fitness.models';

@Component({
  selector: 'app-exercise-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="container mt-4">
      <h2>Übungen Übersicht</h2>

      <div *ngIf="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

      <div class="card mb-4">
        <div class="card-header">Neue Übung erstellen</div>
        <div class="card-body">
          <form [formGroup]="exerciseForm" (ngSubmit)="createExercise()">
            <div class="mb-3">
              <label class="form-label">Name</label>
              <input type="text" class="form-control" formControlName="name"
                     [class.is-invalid]="exerciseForm.get('name')?.invalid && exerciseForm.get('name')?.touched">
            </div>

            <div class="mb-3">
              <label class="form-label">Kategorie</label>
              <select class="form-select" formControlName="category">
                <option value="" disabled>Bitte wählen...</option>
                <option *ngFor="let cat of categories" [value]="cat">{{ cat }}</option>
              </select>
            </div>

            <div class="mb-3">
              <label class="form-label">Muskelgruppen (Kommagetrennt)</label>
              <input type="text" class="form-control" formControlName="muscleGroupsInput" placeholder="z.B. Brust, Trizeps">
            </div>
            <div class="mb-3">
               <label class="form-label">Beschreibung</label>
               <textarea class="form-control" formControlName="description"></textarea>
            </div>
            <button type="submit" class="btn btn-primary" [disabled]="exerciseForm.invalid">Erstellen</button>
          </form>
        </div>
      </div>

      <table class="table table-striped table-hover border">
        <thead class="table-dark">
          <tr>
            <th>Name</th>
            <th>Kategorie</th>
            <th>Muskelgruppen</th>
            <th>Aktionen</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let ex of exercises">
            <td>
                <a [routerLink]="['/exercises', ex.id]" class="fw-bold text-decoration-none">{{ ex.name }}</a>
            </td>
            <td>
              <span class="badge bg-info text-dark">{{ ex.category }}</span>
            </td>
            <td>
              <span *ngFor="let mg of ex.muscleGroups" class="badge bg-secondary me-1">{{ mg }}</span>
            </td>
            <td>
              <button class="btn btn-danger btn-sm" (click)="deleteExercise(ex)">Löschen</button>
            </td>
          </tr>
          <tr *ngIf="exercises.length === 0">
            <td colspan="4" class="text-center text-muted">Keine Übungen gefunden.</td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class ExerciseListComponent implements OnInit {
  private service = inject(FitnessService);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  exercises: Exercise[] = [];
  errorMessage = '';

  // Die festen Kategorien
  categories = ['Gerät', 'Freihantel', 'Körpergewicht'];

  exerciseForm = this.fb.group({
    name: ['', Validators.required],
    category: ['', Validators.required], // Dropdown Bindung
    muscleGroupsInput: ['', Validators.required],
    description: ['']
  });

  ngOnInit() {
    this.loadExercises();
    // Standardwert für Dropdown setzen
    this.exerciseForm.get('category')?.setValue(this.categories[0]);
  }

  loadExercises() {
    this.service.getExercises().subscribe({
      next: (data) => {
        this.exercises = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = 'Fehler beim Laden: ' + err.message;
        this.cdr.detectChanges();
      }
    });
  }

  createExercise() {
    if (this.exerciseForm.invalid) return;

    const formVal = this.exerciseForm.value;
    const muscleGroupsArray = formVal.muscleGroupsInput!.split(',').map(s => s.trim());

    this.service.createExercise({
      name: formVal.name!,
      category: formVal.category!,
      muscleGroups: muscleGroupsArray,
      description: formVal.description || ''
    }).subscribe({
      next: () => {
        this.loadExercises();
        this.exerciseForm.reset();
        // Reset setzt Kategorie auf null, wir wollen wieder den Standard
        this.exerciseForm.get('category')?.setValue(this.categories[0]);
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.cdr.detectChanges();
      }
    });
  }

  deleteExercise(ex: Exercise) {
    if (confirm(`Möchten Sie die Übung "${ex.name}" wirklich löschen?`)) {
      this.service.deleteExercise(ex.id).subscribe(() => this.loadExercises());
    }
  }
}

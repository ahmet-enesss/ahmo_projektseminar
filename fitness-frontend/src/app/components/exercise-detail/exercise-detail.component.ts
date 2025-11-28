import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { FitnessService } from '../../services/fitness.service';

@Component({
  selector: 'app-exercise-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="container mt-4">
      <button class="btn btn-secondary mb-3" (click)="goBack()">Zurück</button>

      <h2>Übungs Details</h2>

      <div *ngIf="successMessage" class="alert alert-success">{{ successMessage }}</div>
      <div *ngIf="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

      <form [formGroup]="editForm" (ngSubmit)="saveChanges()">
        <div class="mb-3">
          <label class="form-label">Name</label>
          <input type="text" class="form-control" formControlName="name">
        </div>

        <div class="mb-3">
          <label class="form-label">Kategorie</label>
          <select class="form-select" formControlName="category">
            <option *ngFor="let cat of categories" [value]="cat">{{ cat }}</option>
          </select>
        </div>

        <div class="mb-3">
          <label class="form-label">Muskelgruppen (Kommagetrennt)</label>
          <input type="text" class="form-control" formControlName="muscleGroupsInput">
        </div>

        <div class="mb-3">
          <label class="form-label">Beschreibung</label>
          <textarea class="form-control" formControlName="description" rows="3"></textarea>
        </div>

        <button type="submit" class="btn btn-success">Speichern</button>
      </form>
    </div>
  `
})
export class ExerciseDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private service = inject(FitnessService);
  private location = inject(Location);
  private fb = inject(FormBuilder);

  exerciseId!: number;
  successMessage = '';
  errorMessage = '';

  categories = ['Gerät', 'Freihantel', 'Körpergewicht'];

  editForm = this.fb.group({
    name: ['', Validators.required],
    category: ['', Validators.required],
    muscleGroupsInput: ['', Validators.required],
    description: ['']
  });

  ngOnInit() {
    this.exerciseId = Number(this.route.snapshot.paramMap.get('id'));
    this.service.getExerciseById(this.exerciseId).subscribe(ex => {
      this.editForm.patchValue({
        name: ex.name,
        category: ex.category,
        muscleGroupsInput: ex.muscleGroups.join(', '),
        description: ex.description
      });
    });
  }

  saveChanges() {
    if (this.editForm.invalid) return;

    const formVal = this.editForm.value;
    const muscleGroupsArray = formVal.muscleGroupsInput!.split(',').map(s => s.trim());

    this.service.updateExercise(this.exerciseId, {
      name: formVal.name!,
      category: formVal.category!,
      muscleGroups: muscleGroupsArray,
      description: formVal.description || ''
    }).subscribe({
      next: () => {
        this.successMessage = 'Speichern erfolgreich';
        this.errorMessage = '';
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.successMessage = '';
      }
    });
  }

  goBack() {
    this.location.back();
  }
}

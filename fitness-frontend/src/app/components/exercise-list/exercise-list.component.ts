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
  templateUrl: './exercise-list.component.html',
  styleUrl: './exercise-list.component.css'
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

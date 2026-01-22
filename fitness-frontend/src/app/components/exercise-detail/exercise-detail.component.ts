import { Component, OnInit, inject } from '@angular/core'; //Grundfunktion Angular
import { CommonModule, Location } from '@angular/common'; //Standard-Module
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms'; //Tools für Reactive Forms
import { ActivatedRoute } from '@angular/router'; //Zugriff auf URL-Parameter
import { FitnessService } from '../../services/fitness.service'; //eigener service der Daten lädt
import { AuthService } from '../../services/auth.service';

//Metadaten Komponente:name,nutzung welcher Datei und welche Module benötigt
@Component({
  selector: 'app-exercise-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './exercise-detail.component.html',
  styleUrl: './exercise-detail.component.css'
})
export class ExerciseDetailComponent implements OnInit {
  private route = inject(ActivatedRoute); //zugriff aktuelle Route
  private service = inject(FitnessService); //service, der Übungen aud Backend holt und speicher
  private authService = inject(AuthService);
  private location = inject(Location); //Hilft beim navigieren im browser
  private fb = inject(FormBuilder); //hilfe beim erstellen von Formularen

  exerciseId!: number; //speichert ID der Übung
  successMessage = '';
  errorMessage = '';
  isLoggedIn = false;

  categories = ['Gerät', 'Freihantel', 'Körpergewicht']; //Dropdown für Kategorien

  editForm = this.fb.group({
    name: ['', Validators.required],
    category: ['', Validators.required],
    muscleGroupsInput: ['', Validators.required],
    description: ['']
  });

//ID wird geholt, Übung geladen, Formular mit Werten ausgefüllt
  ngOnInit() {
    this.isLoggedIn = this.authService.isLoggedIn();
    this.authService.isAuthenticated$.subscribe(isAuth => {
      this.isLoggedIn = isAuth;
    });
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
    if (this.editForm.invalid) return; //nicht speicherung bei ungültiger daten

    const formVal = this.editForm.value; //holt aktuelle Werte
    const muscleGroupsArray = formVal.muscleGroupsInput!.split(',').map(s => s.trim()); //textfeld wird zum array

    //schickt aktualisierte daten an Backend
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

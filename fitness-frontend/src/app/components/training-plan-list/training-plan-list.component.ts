import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core'; // <--- 1. ChangeDetectorRef importieren
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { FitnessService } from '../../services/fitness.service';
import { AuthService } from '../../services/auth.service';
import { TrainingPlanOverview } from '../../models/fitness.models';

//verbindet ts mit html und css
@Component({
  selector: 'app-training-plan-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './training-plan-list.component.html',
  styleUrl: './training-plan-list.component.css'
})
export class TrainingPlanListComponent implements OnInit {
  private service = inject(FitnessService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef); // <--- 2. Hier injizieren wir den "Wecker"

  //enthält geladene Pläne
  plans: TrainingPlanOverview[] = [];
  errorMessage = '';
  isLoggedIn = false;

  //eingabe für neue Trainingsplan
  planForm = this.fb.group({
    name: ['', Validators.required],
    description: ['', Validators.required]
  });

  ngOnInit() {
    this.isLoggedIn = this.authService.isLoggedIn();
    this.authService.isAuthenticated$.subscribe(isAuth => {
      this.isLoggedIn = isAuth;
      this.cdr.detectChanges();
    });
    this.loadPlans();
  }

  //holt alle trainingspläne aus backend und aktualisier anzeige
  loadPlans() {
    this.service.getTrainingPlans().subscribe({
      next: (data) => {
        console.log('Trainingspläne geladen:', data);
        this.plans = data;
        this.cdr.detectChanges(); // <--- 3. Angular zwingen, die Ansicht zu aktualisieren
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Fehler beim Laden: ' + err.message;
        this.cdr.detectChanges(); // Auch bei Fehlern aktualisieren
      }
    });
  }

  createPlan() {
    if (this.planForm.invalid) return;
    this.service.createTrainingPlan(this.planForm.value as any).subscribe({
      next: () => {
        this.loadPlans();
        this.planForm.reset();
        this.errorMessage = '';
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.cdr.detectChanges();
      }
    });
  }

  deletePlan(plan: TrainingPlanOverview) {
    if (confirm(`Plan "${plan.name}" löschen?`)) {
      this.service.deleteTrainingPlan(plan.id).subscribe(() => this.loadPlans());
    }
  }
}

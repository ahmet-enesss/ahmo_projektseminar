import { Routes } from '@angular/router';
import { ExerciseListComponent } from './components/exercise-list/exercise-list.component';
import { ExerciseDetailComponent } from './components/exercise-detail/exercise-detail.component';
import { TrainingPlanListComponent } from './components/training-plan-list/training-plan-list.component';
import { TrainingPlanDetailComponent } from './components/training-plan-detail/training-plan-detail.component';

export const routes: Routes = [
  { path: '', redirectTo: '/exercises', pathMatch: 'full' },
  { path: 'exercises', component: ExerciseListComponent },
  { path: 'exercises/:id', component: ExerciseDetailComponent },
  { path: 'plans', component: TrainingPlanListComponent },
  { path: 'plans/:id', component: TrainingPlanDetailComponent }
];

import { Routes } from '@angular/router';
import { ExerciseListComponent } from './components/exercise-list/exercise-list.component';
import { ExerciseDetailComponent } from './components/exercise-detail/exercise-detail.component';
import { TrainingPlanListComponent } from './components/training-plan-list/training-plan-list.component';
import { TrainingPlanDetailComponent } from './components/training-plan-detail/training-plan-detail.component';
import { SessionTemplateDetailComponent } from './components/session-template-detail/session-template-detail.component';
import { SessionTemplateListComponent } from './components/session-template-list/session-template-list.component';
import { TrainingExecutionComponent } from './components/training-execution/training-execution.component';
import { LoginComponent } from './components/login/login.component';
import { TrainingHistoryComponent } from './components/training-history/training-history.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/exercises', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'exercises', component: ExerciseListComponent },
  { path: 'exercises/:id', component: ExerciseDetailComponent },
  { path: 'plans', component: TrainingPlanListComponent },
  { path: 'plans/:id', component: TrainingPlanDetailComponent },
  { path: 'plans/:planId/sessions/:sessionId', component: SessionTemplateDetailComponent },
  { path: 'sessions', component: SessionTemplateListComponent },
  { path: 'history', component: TrainingHistoryComponent, canActivate: [authGuard] },
  { path: 'training/start/:sessionTemplateId', component: TrainingExecutionComponent },
  { path: 'training/log/:logId', component: TrainingExecutionComponent }
];

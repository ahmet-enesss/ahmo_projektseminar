import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FitnessService } from '../../services/fitness.service';
import { SessionLogSummary } from '../../models/fitness.models';

@Component({
  selector: 'app-training-history',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './training-history.component.html',
  styleUrl: './training-history.component.css'
})
export class TrainingHistoryComponent implements OnInit {
  private service = inject(FitnessService);
  private cdr = inject(ChangeDetectorRef);

  history: SessionLogSummary[] = [];
  errorMessage = '';
  loading = true;

  ngOnInit() {
    this.loadHistory();
  }

  loadHistory() {
    this.loading = true;
    this.service.getTrainingHistory().subscribe({
      next: (data) => {
        // Nur abgeschlossene Trainings anzeigen
        this.history = data.filter(log => log.status === 'COMPLETED');
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = 'Fehler beim Laden der Trainingshistorie: ' + err.message;
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  formatDate(dateString: string): string {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleString('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getDuration(startTime: string, endTime?: string): string {
    if (!endTime) return '-';
    const start = new Date(startTime);
    const end = new Date(endTime);
    const diffMs = end.getTime() - start.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const hours = Math.floor(diffMins / 60);
    const mins = diffMins % 60;
    if (hours > 0) {
      return `${hours}h ${mins}min`;
    }
    return `${mins}min`;
  }
}

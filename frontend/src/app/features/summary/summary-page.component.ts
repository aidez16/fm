import { ChangeDetectionStrategy, Component, computed, effect, inject, viewChild } from '@angular/core';
import { DecimalPipe, DatePipe, NgClass } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';

import { FutureMovementApiService } from '../../core/services/future-movement-api.service';
import { SummaryStore } from '../../core/state/summary.store';
import { StatCardComponent } from '../../shared/components/stat-card/stat-card.component';
import { ToolbarComponent } from '../../shared/components/toolbar/toolbar.component';
import { DailySummaryRecord } from '../../core/models/daily-summary-record.model';

@Component({
  selector: 'app-summary-page',
  standalone: true,
  imports: [
    DecimalPipe,
    DatePipe,
    NgClass,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSortModule,
    MatTableModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule,
    StatCardComponent,
    ToolbarComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './summary-page.component.html',
  styleUrl: './summary-page.component.scss',
})
export class SummaryPageComponent {
  protected readonly store = inject(SummaryStore);
  private readonly api = inject(FutureMovementApiService);

  // A signal query, not @ViewChild: the table sits inside an @if that only
  // renders once the load resolves, so there is no MatSort to capture at
  // ngAfterViewInit time.
  private readonly sort = viewChild(MatSort);

  protected readonly displayedColumns = ['clientInformation', 'productInformation', 'totalTransactionAmount'];

  protected readonly dataSource = new MatTableDataSource<DailySummaryRecord>([]);

  protected readonly netTotalTone = computed(() => {
    const total = this.store.netTotal();
    if (total > 0) return 'positive' as const;
    if (total < 0) return 'negative' as const;
    return 'neutral' as const;
  });

  constructor() {
    // MatTableDataSource isn't reactive, so bridge it to the store's signal.
    effect(() => {
      this.dataSource.data = this.store.rows();
    });

    // Re-attaches when the table is rebuilt, e.g. after an error and retry.
    effect(() => {
      const sort = this.sort();
      if (sort) {
        this.dataSource.sort = sort;
      }
    });

    this.store.load();
  }

  protected applyFilter(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.dataSource.filter = value.trim().toLowerCase();
  }

  protected refresh(): void {
    this.store.load();
  }

  protected downloadCsv(): void {
    window.open(this.api.getCsvDownloadUrl(), '_blank');
  }

  protected amountClass(value: number): string {
    if (value > 0) return 'amount-positive';
    if (value < 0) return 'amount-negative';
    return '';
  }
}

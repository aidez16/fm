import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { catchError, of, tap } from 'rxjs';

import { DailySummaryRecord } from '../models/daily-summary-record.model';
import { FutureMovementApiService } from '../services/future-movement-api.service';

export type LoadState = 'idle' | 'loading' | 'loaded' | 'error';

@Injectable({ providedIn: 'root' })
export class SummaryStore {
  private readonly api = inject(FutureMovementApiService);

  private readonly _rows = signal<DailySummaryRecord[]>([]);
  private readonly _state = signal<LoadState>('idle');
  private readonly _errorMessage = signal<string | null>(null);
  private readonly _lastLoadedAt = signal<Date | null>(null);

  readonly rows = this._rows.asReadonly();
  readonly state = this._state.asReadonly();
  readonly errorMessage = this._errorMessage.asReadonly();
  readonly lastLoadedAt = this._lastLoadedAt.asReadonly();

  readonly isLoading = computed(() => this._state() === 'loading');
  readonly hasError = computed(() => this._state() === 'error');
  readonly isEmpty = computed(() => this._state() === 'loaded' && this._rows().length === 0);

  readonly distinctClientCount = computed(
    () => new Set(this._rows().map((r) => r.clientInformation)).size,
  );
  readonly distinctProductCount = computed(
    () => new Set(this._rows().map((r) => r.productInformation)).size,
  );
  readonly netTotal = computed(() =>
    this._rows().reduce((sum, r) => sum + r.totalTransactionAmount, 0),
  );

  load(): void {
    this._state.set('loading');
    this._errorMessage.set(null);

    this.api
      .getDailySummary()
      .pipe(
        tap((rows) => {
          this._rows.set(rows);
          this._state.set('loaded');
          this._lastLoadedAt.set(new Date());
        }),
        catchError((error: HttpErrorResponse) => {
          this._state.set('error');
          this._errorMessage.set(this.describeError(error));
          return of(null);
        }),
      )
      .subscribe();
  }

  private describeError(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'Could not reach the server. Check that the backend is running and reachable.';
    }
    if (typeof error.error?.message === 'string') {
      return error.error.message;
    }
    return `Request failed (HTTP ${error.status}).`;
  }
}

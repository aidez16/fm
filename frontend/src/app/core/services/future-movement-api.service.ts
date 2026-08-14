import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { DailySummaryRecord } from '../models/daily-summary-record.model';


@Injectable({ providedIn: 'root' })
export class FutureMovementApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  getDailySummary(): Observable<DailySummaryRecord[]> {
    return this.http.get<DailySummaryRecord[]>(`${this.baseUrl}/summary`);
  }

  /** Used directly as a window.open target. */
  getCsvDownloadUrl(): string {
    return `${this.baseUrl}/summary/csv`;
  }

  /** Triggers the demo replay endpoint. */
  replaySampleData(): Observable<{ publishedRecords: number }> {
    return this.http.post<{ publishedRecords: number }>(`${this.baseUrl}/replay-sample-data`, {});
  }
}

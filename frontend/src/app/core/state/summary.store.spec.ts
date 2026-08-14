import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { SummaryStore } from './summary.store';
import { FutureMovementApiService } from '../services/future-movement-api.service';
import { DailySummaryRecord } from '../models/daily-summary-record.model';

describe('SummaryStore', () => {
  let store: SummaryStore;
  let apiSpy: jasmine.SpyObj<FutureMovementApiService>;

  function setup(): void {
    apiSpy = jasmine.createSpyObj<FutureMovementApiService>('FutureMovementApiService', ['getDailySummary']);
    TestBed.configureTestingModule({
      providers: [SummaryStore, { provide: FutureMovementApiService, useValue: apiSpy }],
    });
    store = TestBed.inject(SummaryStore);
  }

  it('starts in the idle state with no rows', () => {
    setup();
    expect(store.state()).toBe('idle');
    expect(store.rows()).toEqual([]);
  });

  it('transitions to loaded and stores the rows on success', () => {
    setup();
    const rows: DailySummaryRecord[] = [
      { clientInformation: 'CL-4321-0002-0001', productInformation: 'SGX-FU-NK-20100910', totalTransactionAmount: 46 },
      { clientInformation: 'CL-1234-0002-0001', productInformation: 'SGX-FU-NK-20100910', totalTransactionAmount: -52 },
    ];
    apiSpy.getDailySummary.and.returnValue(of(rows));

    store.load();

    expect(store.state()).toBe('loaded');
    expect(store.rows()).toEqual(rows);
    expect(store.hasError()).toBeFalse();
    expect(store.lastLoadedAt()).not.toBeNull();
  });

  it('transitions to error and exposes a friendly message on network failure (status 0)', () => {
    setup();
    apiSpy.getDailySummary.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 0, statusText: 'Unknown Error' })),
    );

    store.load();

    expect(store.state()).toBe('error');
    expect(store.errorMessage()).toContain('Could not reach the server');
    expect(store.rows()).toEqual([]);
  });

  it('surfaces a backend-provided error message when present', () => {
    setup();
    apiSpy.getDailySummary.and.returnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 422,
            statusText: 'Unprocessable Entity',
            error: { message: 'No record schema registered for record code \'999\'' },
          }),
      ),
    );

    store.load();

    expect(store.errorMessage()).toBe("No record schema registered for record code '999'");
  });

  it('falls back to a generic message when the backend gives no message', () => {
    setup();
    apiSpy.getDailySummary.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 503, statusText: 'Service Unavailable' })),
    );

    store.load();

    expect(store.errorMessage()).toBe('Request failed (HTTP 503).');
  });

  it('marks the summary as empty only after a successful load with zero rows', () => {
    setup();
    apiSpy.getDailySummary.and.returnValue(of([]));

    store.load();

    expect(store.isEmpty()).toBeTrue();
  });

  it('does not consider the idle state as empty', () => {
    setup();
    expect(store.isEmpty()).toBeFalse();
  });

  it('computes distinct client and product counts, ignoring duplicates', () => {
    setup();
    const rows: DailySummaryRecord[] = [
      { clientInformation: 'CL-4321-0002-0001', productInformation: 'SGX-FU-NK-20100910', totalTransactionAmount: 46 },
      { clientInformation: 'CL-4321-0002-0001', productInformation: 'CME-FU-N1-20100910', totalTransactionAmount: 10 },
      { clientInformation: 'CL-1234-0002-0001', productInformation: 'SGX-FU-NK-20100910', totalTransactionAmount: -52 },
    ];
    apiSpy.getDailySummary.and.returnValue(of(rows));

    store.load();

    expect(store.distinctClientCount()).toBe(2);
    expect(store.distinctProductCount()).toBe(2);
  });

  it('computes the net total across all rows, including negatives', () => {
    setup();
    const rows: DailySummaryRecord[] = [
      { clientInformation: 'a', productInformation: 'x', totalTransactionAmount: 46 },
      { clientInformation: 'b', productInformation: 'y', totalTransactionAmount: -52 },
      { clientInformation: 'c', productInformation: 'z', totalTransactionAmount: 6 },
    ];
    apiSpy.getDailySummary.and.returnValue(of(rows));

    store.load();

    expect(store.netTotal()).toBe(0);
  });

  it('clears the previous error when a subsequent load succeeds', () => {
    setup();
    apiSpy.getDailySummary.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    store.load();
    expect(store.hasError()).toBeTrue();

    apiSpy.getDailySummary.and.returnValue(of([]));
    store.load();

    expect(store.hasError()).toBeFalse();
    expect(store.errorMessage()).toBeNull();
  });
});

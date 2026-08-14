import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { FutureMovementApiService } from './future-movement-api.service';
import { DailySummaryRecord } from '../models/daily-summary-record.model';

describe('FutureMovementApiService', () => {
  let service: FutureMovementApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FutureMovementApiService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(FutureMovementApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('fetches the daily summary from the /summary endpoint', () => {
    const expected: DailySummaryRecord[] = [
      { clientInformation: 'CL-4321-0002-0001', productInformation: 'SGX-FU-NK-20100910', totalTransactionAmount: 46 },
    ];

    service.getDailySummary().subscribe((rows) => {
      expect(rows).toEqual(expected);
    });

    const req = httpMock.expectOne('/api/v1/future-movements/summary');
    expect(req.request.method).toBe('GET');
    req.flush(expected);
  });

  it('propagates HTTP errors to the caller', () => {
    let capturedError: unknown;

    service.getDailySummary().subscribe({
      error: (err) => (capturedError = err),
    });

    const req = httpMock.expectOne('/api/v1/future-movements/summary');
    req.flush({ message: 'boom' }, { status: 500, statusText: 'Server Error' });

    expect(capturedError).toBeTruthy();
  });

  it('builds the CSV download URL without making a request', () => {
    expect(service.getCsvDownloadUrl()).toBe('/api/v1/future-movements/summary/csv');
    httpMock.verify();
  });

  it('posts to the replay endpoint and returns the published count', () => {
    service.replaySampleData().subscribe((result) => {
      expect(result.publishedRecords).toBe(717);
    });

    const req = httpMock.expectOne('/api/v1/future-movements/replay-sample-data');
    expect(req.request.method).toBe('POST');
    req.flush({ publishedRecords: 717 });
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

import { SummaryPageComponent } from './summary-page.component';

describe('SummaryPageComponent', () => {
  let fixture: ComponentFixture<SummaryPageComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SummaryPageComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideNoopAnimations()],
    }).compileComponents();

    fixture = TestBed.createComponent(SummaryPageComponent);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('shows a loading indicator while the initial request is in flight', () => {
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain("Loading today's summary");

    httpMock.expectOne('/api/v1/future-movements/summary').flush([]);
  });

  it('renders dashboard cards and table rows once data loads', () => {
    fixture.detectChanges();
    const req = httpMock.expectOne('/api/v1/future-movements/summary');
    req.flush([
      { clientInformation: 'CL-4321-0002-0001', productInformation: 'SGX-FU-NK-20100910', totalTransactionAmount: 46 },
      { clientInformation: 'CL-1234-0002-0001', productInformation: 'SGX-FU-NK-20100910', totalTransactionAmount: -52 },
    ]);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('CL-4321-0002-0001');
    expect(text).toContain('CL-1234-0002-0001');
    expect(text).toContain('Client Accounts');
  });

  it('shows the empty state when the backend returns no rows', () => {
    fixture.detectChanges();
    httpMock.expectOne('/api/v1/future-movements/summary').flush([]);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('No transactions found for today');
  });

  it('shows an error panel with a retry button when the request fails', () => {
    fixture.detectChanges();
    httpMock
      .expectOne('/api/v1/future-movements/summary')
      .flush({ message: 'boom' }, { status: 500, statusText: 'Server Error' });
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Try again');
  });

  it('reorders rows when a column header is clicked', () => {
    fixture.detectChanges();
    httpMock.expectOne('/api/v1/future-movements/summary').flush([
      { clientInformation: 'CL-4321-0002-0001', productInformation: 'SGX-FU-NK-20100910', totalTransactionAmount: 46 },
      { clientInformation: 'CL-1234-0002-0001', productInformation: 'SGX-FU-NK-20100910', totalTransactionAmount: -52 },
    ]);
    fixture.detectChanges();

    const clientColumn = () =>
      Array.from(
        fixture.nativeElement.querySelectorAll('td.mat-column-clientInformation') as NodeListOf<HTMLElement>,
      ).map((cell) => cell.textContent!.trim());

    // Backend order.
    expect(clientColumn()).toEqual(['CL-4321-0002-0001', 'CL-1234-0002-0001']);

    const header = fixture.nativeElement.querySelector('th.mat-column-clientInformation') as HTMLElement;
    header.click();
    fixture.detectChanges();

    // MatSort doesn't exist at ngAfterViewInit time: wire it up there and the
    // arrow toggles but the rows never move.
    expect(clientColumn()).toEqual(['CL-1234-0002-0001', 'CL-4321-0002-0001']);
  });

  it('re-requests the summary when refresh is triggered', () => {
    fixture.detectChanges();
    httpMock.expectOne('/api/v1/future-movements/summary').flush([]);
    fixture.detectChanges();

    (fixture.componentInstance as unknown as { refresh(): void }).refresh();

    httpMock.expectOne('/api/v1/future-movements/summary').flush([]);
  });
});

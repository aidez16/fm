import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

import { ToolbarComponent } from './toolbar.component';
import { ThemeService } from '../../../core/services/theme.service';

describe('ToolbarComponent', () => {
  let fixture: ComponentFixture<ToolbarComponent>;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [ToolbarComponent],
      providers: [provideNoopAnimations()],
    }).compileComponents();

    fixture = TestBed.createComponent(ToolbarComponent);
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.clear();
    document.documentElement.classList.remove('dark-theme', 'light-theme');
  });

  it('emits refresh when the refresh button is clicked', () => {
    // Counter, not array.push(): push() with no argument appends nothing.
    let emitted = 0;
    fixture.componentInstance.refresh.subscribe(() => (emitted += 1));

    const button = fixture.debugElement.query(By.css('button[aria-label="Refresh summary"]'));
    button.nativeElement.click();

    expect(emitted).toBe(1);
  });

  it('emits downloadCsv when the download button is clicked', () => {
    let emitted = 0;
    fixture.componentInstance.downloadCsv.subscribe(() => (emitted += 1));

    const button = fixture.debugElement.query(By.css('.toolbar__download'));
    button.nativeElement.click();

    expect(emitted).toBe(1);
  });

  it('cycles the theme when the theme button is clicked', () => {
    const themeService = TestBed.inject(ThemeService);
    expect(themeService.mode()).toBe('system');

    const button = fixture.debugElement.query(By.css('button[aria-label="Toggle color theme"]'));
    button.nativeElement.click();

    expect(themeService.mode()).toBe('light');
  });
});

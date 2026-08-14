import { TestBed } from '@angular/core/testing';

import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.classList.remove('dark-theme', 'light-theme');
    TestBed.configureTestingModule({ providers: [ThemeService] });
  });

  afterEach(() => {
    localStorage.clear();
    document.documentElement.classList.remove('dark-theme', 'light-theme');
  });

  it('defaults to system mode when nothing is stored', () => {
    const service = TestBed.inject(ThemeService);
    expect(service.mode()).toBe('system');
  });

  it('applies dark-theme class when set to dark', () => {
    const service = TestBed.inject(ThemeService);
    service.setMode('dark');
    expect(document.documentElement.classList.contains('dark-theme')).toBeTrue();
  });

  it('applies light-theme class when set to light', () => {
    const service = TestBed.inject(ThemeService);
    service.setMode('light');
    expect(document.documentElement.classList.contains('light-theme')).toBeTrue();
  });

  it('removes the dark-theme class when switching back to system', () => {
    const service = TestBed.inject(ThemeService);
    service.setMode('dark');
    expect(document.documentElement.classList.contains('dark-theme')).toBeTrue();

    service.setMode('system');
    expect(document.documentElement.classList.contains('dark-theme')).toBeFalse();
    expect(document.documentElement.classList.contains('light-theme')).toBeFalse();
  });

  it('cycles system -> light -> dark -> system', () => {
    const service = TestBed.inject(ThemeService);
    expect(service.mode()).toBe('system');
    service.cycle();
    expect(service.mode()).toBe('light');
    service.cycle();
    expect(service.mode()).toBe('dark');
    service.cycle();
    expect(service.mode()).toBe('system');
  });

  it('persists the chosen mode to localStorage', () => {
    const service = TestBed.inject(ThemeService);
    service.setMode('dark');
    expect(localStorage.getItem('future-movement-summary.theme-mode')).toBe('dark');
  });

  it('restores a previously persisted mode on construction', () => {
    localStorage.setItem('future-movement-summary.theme-mode', 'dark');
    const service = TestBed.inject(ThemeService);
    expect(service.mode()).toBe('dark');
  });

  it('ignores a corrupted/unexpected stored value and falls back to system', () => {
    localStorage.setItem('future-movement-summary.theme-mode', 'not-a-real-mode');
    const service = TestBed.inject(ThemeService);
    expect(service.mode()).toBe('system');
  });
});

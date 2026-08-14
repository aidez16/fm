import { Injectable, signal } from '@angular/core';

export type ThemeMode = 'light' | 'dark' | 'system';

const STORAGE_KEY = 'future-movement-summary.theme-mode';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly _mode = signal<ThemeMode>(this.readStoredMode());
  readonly mode = this._mode.asReadonly();

  constructor() {
    this.applyToDom(this._mode());
  }

  setMode(mode: ThemeMode): void {
    this._mode.set(mode);
    this.applyToDom(mode);
    try {
      localStorage.setItem(STORAGE_KEY, mode);
    } catch {
      // Storage disabled: the toggle still works, it just doesn't persist.
    }
  }

  cycle(): void {
    const next: Record<ThemeMode, ThemeMode> = { system: 'light', light: 'dark', dark: 'system' };
    this.setMode(next[this._mode()]);
  }

  private applyToDom(mode: ThemeMode): void {
    const root = document.documentElement;
    root.classList.remove('dark-theme', 'light-theme');
    if (mode === 'dark') {
      root.classList.add('dark-theme');
    } else if (mode === 'light') {
      root.classList.add('light-theme');
    }
    // 'system' -> no class; styles.scss already sets color-scheme: light dark.
  }

  private readStoredMode(): ThemeMode {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored === 'light' || stored === 'dark' || stored === 'system') {
        return stored;
      }
    } catch {
      // ignore
    }
    return 'system';
  }
}

import { ChangeDetectionStrategy, Component, inject, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';

import { ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'app-toolbar',
  standalone: true,
  imports: [MatToolbarModule, MatButtonModule, MatIconModule, MatTooltipModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <mat-toolbar class="toolbar">
      <mat-icon class="toolbar__brand-icon" aria-hidden="true">insights</mat-icon>
      <span class="toolbar__title">Future Movement Daily Summary</span>

      <span class="toolbar__spacer"></span>

      <button
        mat-icon-button
        type="button"
        matTooltip="Refresh"
        aria-label="Refresh summary"
        (click)="refresh.emit()"
      >
        <mat-icon>refresh</mat-icon>
      </button>

      <button
        mat-icon-button
        type="button"
        [matTooltip]="themeTooltip()"
        aria-label="Toggle color theme"
        (click)="theme.cycle()"
      >
        <mat-icon>{{ themeIcon() }}</mat-icon>
      </button>

      <button mat-flat-button type="button" class="toolbar__download" (click)="downloadCsv.emit()">
        <mat-icon>download</mat-icon>
        Download CSV
      </button>
    </mat-toolbar>
  `,
  styles: `
    .toolbar {
      position: sticky;
      top: 0;
      z-index: 10;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
    }

    .toolbar__brand-icon {
      margin-right: 12px;
    }

    .toolbar__title {
      font-size: 1.1rem;
      font-weight: 600;
      white-space: nowrap;
    }

    .toolbar__spacer {
      flex: 1 1 auto;
    }

    .toolbar__download {
      margin-left: 8px;
    }

    @media (max-width: 640px) {
      .toolbar__title {
        display: none;
      }
    }
  `,
})
export class ToolbarComponent {
  protected readonly theme = inject(ThemeService);

  readonly refresh = output<void>();
  readonly downloadCsv = output<void>();

  protected themeIcon(): string {
    switch (this.theme.mode()) {
      case 'light':
        return 'light_mode';
      case 'dark':
        return 'dark_mode';
      default:
        return 'brightness_auto';
    }
  }

  protected themeTooltip(): string {
    switch (this.theme.mode()) {
      case 'light':
        return 'Light theme (click for dark)';
      case 'dark':
        return 'Dark theme (click for auto)';
      default:
        return 'Auto theme (click for light)';
    }
  }
}

import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  imports: [MatCardModule, MatIconModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <mat-card class="stat-card" [class.stat-card--positive]="tone() === 'positive'" [class.stat-card--negative]="tone() === 'negative'">
      <mat-card-content>
        <div class="stat-card__icon">
          <mat-icon>{{ icon() }}</mat-icon>
        </div>
        <div class="stat-card__body">
          <span class="stat-card__label">{{ label() }}</span>
          <span class="stat-card__value">{{ value() }}</span>
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: `
    .stat-card {
      display: flex;
      height: 100%;
    }

    mat-card-content {
      display: flex;
      align-items: center;
      gap: 16px;
      width: 100%;
    }

    .stat-card__icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: var(--mat-sys-secondary-container);
      color: var(--mat-sys-on-secondary-container);
      flex-shrink: 0;
    }

    .stat-card--positive .stat-card__icon {
      background: color-mix(in srgb, var(--app-positive) 20%, transparent);
      color: var(--app-positive);
    }

    .stat-card--negative .stat-card__icon {
      background: color-mix(in srgb, var(--app-negative) 20%, transparent);
      color: var(--app-negative);
    }

    .stat-card__body {
      display: flex;
      flex-direction: column;
      min-width: 0;
    }

    .stat-card__label {
      font-size: 0.8rem;
      color: var(--mat-sys-on-surface-variant);
      letter-spacing: 0.02em;
    }

    .stat-card__value {
      font-size: 1.5rem;
      font-weight: 600;
      font-family: 'Roboto Mono', ui-monospace, monospace;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  `,
})
export class StatCardComponent {
  readonly icon = input.required<string>();
  readonly label = input.required<string>();
  readonly value = input.required<string>();
  readonly tone = input<'neutral' | 'positive' | 'negative'>('neutral');
}

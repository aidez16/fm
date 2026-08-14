import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/summary/summary-page.component').then((m) => m.SummaryPageComponent),
    title: 'Daily Summary — Future Movements',
  },
  { path: '**', redirectTo: '' },
];

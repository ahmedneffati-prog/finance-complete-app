import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'pivot',
    loadComponent: () => import('./features/pivot-table/pivot-table.component').then(m => m.PivotTableComponent)
  },
  {
    path: 'charts',
    loadComponent: () => import('./features/charts/chart-container.component').then(m => m.ChartContainerComponent)
  },
  {
    path: 'live-market',
    loadComponent: () => import('./features/live-market/live-market.component').then(m => m.LiveMarketComponent)
  },
  {
    path: 'exports',
    loadComponent: () => import('./features/exports/export-options.component').then(m => m.ExportOptionsComponent)
  },
  { path: '**', redirectTo: 'dashboard' }
];

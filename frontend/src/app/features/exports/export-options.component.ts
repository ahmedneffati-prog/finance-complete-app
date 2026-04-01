import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ExportService } from '../../core/services/export.service';
import { ApiService } from '../../core/services/api.service';

interface ExportOption {
  id: string;
  title: string;
  description: string;
  icon: string;
  color: string;
  formats: string[];
}

@Component({
  selector: 'app-export-options',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatButtonModule, MatIconModule, MatCardModule,
    MatSelectModule, MatFormFieldModule, MatInputModule, MatCheckboxModule,
    MatProgressBarModule, MatSnackBarModule
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>📥 Export Data</h1>
        <p>Download your financial data in multiple formats</p>
      </div>

      <!-- Quick Export Cards -->
      <h3 style="margin-bottom:16px;font-size:.9rem;color:var(--text-muted);text-transform:uppercase;letter-spacing:.05em">
        Quick Export
      </h3>
      <div class="grid-3" style="margin-bottom:32px">
        <div *ngFor="let opt of exportOptions" class="export-card card">
          <div class="export-card-icon" [style.background]="opt.color">
            <span class="material-icons">{{ opt.icon }}</span>
          </div>
          <h3>{{ opt.title }}</h3>
          <p>{{ opt.description }}</p>
          <div class="export-formats">
            <button *ngFor="let fmt of opt.formats" mat-stroked-button
              (click)="quickExport(opt.id, fmt)"
              [disabled]="exporting === opt.id + fmt"
              style="font-size:.75rem">
              <mat-icon style="font-size:16px">{{ fmt === 'EXCEL' ? 'table_view' : 'picture_as_pdf' }}</mat-icon>
              {{ fmt }}
            </button>
          </div>
          <mat-progress-bar *ngIf="exporting?.startsWith(opt.id)" mode="indeterminate"></mat-progress-bar>
        </div>
      </div>

      <!-- Custom Export -->
      <h3 style="margin-bottom:16px;font-size:.9rem;color:var(--text-muted);text-transform:uppercase;letter-spacing:.05em">
        Custom Export
      </h3>
      <div class="card">
        <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:16px;margin-bottom:20px">
          <mat-form-field appearance="outline">
            <mat-label>Data Entity</mat-label>
            <mat-select [(ngModel)]="customEntity">
              <mat-option value="trades">Trades</mat-option>
              <mat-option value="market_data">Market Data</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Format</mat-label>
            <mat-select [(ngModel)]="customFormat">
              <mat-option value="EXCEL">Excel (.xlsx)</mat-option>
              <mat-option value="PDF">PDF (.pdf)</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Symbol (optional)</mat-label>
            <input matInput [(ngModel)]="customSymbol" placeholder="e.g. AAPL">
          </mat-form-field>
        </div>

        <button mat-raised-button color="primary" (click)="customExport()" [disabled]="exporting === 'custom'">
          <mat-icon>download</mat-icon>
          {{ exporting === 'custom' ? 'Exporting...' : 'Export Custom Report' }}
        </button>
      </div>

      <!-- Recent exports log -->
      <div class="card" style="margin-top:20px" *ngIf="exportLog.length > 0">
        <div class="card-header">
          <h3><span class="material-icons">history</span> Export History</h3>
          <button mat-icon-button (click)="exportLog = []"><mat-icon>clear_all</mat-icon></button>
        </div>
        <div style="display:flex;flex-direction:column;gap:8px">
          <div *ngFor="let log of exportLog.slice().reverse()" class="log-entry">
            <span class="material-icons" [style.color]="log.success ? '#10b981' : '#ef4444'">
              {{ log.success ? 'check_circle' : 'error' }}
            </span>
            <div>
              <div style="font-size:.875rem;font-weight:500">{{ log.filename }}</div>
              <div style="font-size:.75rem;color:var(--text-muted)">{{ log.timestamp | date:'HH:mm:ss' }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .export-card {
      display: flex;
      flex-direction: column;
      gap: 12px;
      position: relative;
      overflow: hidden;

      h3 { font-weight: 600; font-size: .95rem; }
      p { font-size: .8rem; color: var(--text-muted); flex: 1; }
    }

    .export-card-icon {
      width: 48px; height: 48px;
      border-radius: 12px;
      display: flex; align-items: center; justify-content: center;
      .material-icons { font-size: 24px; color: white; }
    }

    .export-formats {
      display: flex; gap: 8px; flex-wrap: wrap;
    }

    .log-entry {
      display: flex; align-items: center; gap: 12px;
      padding: 8px 12px; border-radius: 8px;
      background: var(--surface-2); border: 1px solid var(--border);
    }
  `]
})
export class ExportOptionsComponent {
  customEntity = 'trades';
  customFormat: 'EXCEL' | 'PDF' = 'EXCEL';
  customSymbol = '';
  exporting: string | null = null;
  exportLog: { filename: string; timestamp: Date; success: boolean }[] = [];

  exportOptions: ExportOption[] = [
    {
      id: 'trades',
      title: 'Trade History',
      description: 'All trade records with symbols, prices, quantities and dates',
      icon: 'receipt_long',
      color: 'linear-gradient(135deg,#6366f1,#4338ca)',
      formats: ['EXCEL', 'PDF']
    },
    {
      id: 'market_data',
      title: 'Market Data',
      description: 'Historical OHLCV data for all tracked symbols',
      icon: 'candlestick_chart',
      color: 'linear-gradient(135deg,#06b6d4,#0891b2)',
      formats: ['EXCEL', 'PDF']
    },
    {
      id: 'aggregation',
      title: 'Summary Report',
      description: 'Aggregated analysis with totals per symbol and broker',
      icon: 'summarize',
      color: 'linear-gradient(135deg,#10b981,#059669)',
      formats: ['EXCEL', 'PDF']
    }
  ];

  constructor(
    private exportService: ExportService,
    private api: ApiService,
    private snackBar: MatSnackBar
  ) {}

  quickExport(entityId: string, format: string): void {
    this.exporting = entityId + format;
    const filename = `${entityId}_${new Date().toISOString().slice(0,10)}.${format === 'PDF' ? 'pdf' : 'xlsx'}`;

    const done = (success: boolean) => {
      this.exporting = null;
      this.exportLog.push({ filename, timestamp: new Date(), success });
      this.snackBar.open(success ? `✅ ${filename} downloaded` : '❌ Export failed', 'Close', {
        duration: 3000,
        panelClass: success ? 'success-snack' : 'error-snack'
      });
    };

    if (entityId === 'trades') {
      this.api.exportCustom({ entity: 'trades', format }).subscribe({
        next: blob => { this.saveBlob(blob, filename); done(true); },
        error: () => done(false)
      });
    } else if (entityId === 'market_data') {
      this.api.exportCustom({ entity: 'market_data', format }).subscribe({
        next: blob => { this.saveBlob(blob, filename); done(true); },
        error: () => done(false)
      });
    } else {
      this.api.exportCustom({ entity: entityId, format }).subscribe({
        next: blob => { this.saveBlob(blob, filename); done(true); },
        error: () => done(false)
      });
    }
  }

  customExport(): void {
    this.exporting = 'custom';
    const filename = `${this.customEntity}_custom.${this.customFormat === 'PDF' ? 'pdf' : 'xlsx'}`;
    const body: any = { entity: this.customEntity, format: this.customFormat };
    if (this.customSymbol) body.filters = { symbol: this.customSymbol };

    this.api.exportCustom(body).subscribe({
      next: blob => {
        this.saveBlob(blob, filename);
        this.exporting = null;
        this.exportLog.push({ filename, timestamp: new Date(), success: true });
        this.snackBar.open(`✅ ${filename} downloaded`, 'Close', { duration: 3000 });
      },
      error: () => {
        this.exporting = null;
        this.snackBar.open('❌ Export failed', 'Close', { duration: 3000 });
      }
    });
  }

  private saveBlob(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  }
}

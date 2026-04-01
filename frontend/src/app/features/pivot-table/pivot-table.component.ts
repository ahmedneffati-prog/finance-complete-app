import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartConfiguration } from 'chart.js';
import { ApiService } from '../../core/services/api.service';
import { AggregationRequest, AggregationResult, MeasureConfig } from '../../core/models';
import { ExportService } from '../../core/services/export.service';

type AggregateFn = 'SUM' | 'AVG' | 'MAX' | 'MIN' | 'COUNT';

interface FieldDef {
  key: string;
  label: string;
  type: 'dimension' | 'measure';
}

@Component({
  selector: 'app-pivot-table',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatButtonModule, MatSelectModule, MatIconModule,
    MatChipsModule, MatProgressSpinnerModule, MatTabsModule, MatInputModule,
    MatFormFieldModule, MatTooltipModule, MatSnackBarModule, BaseChartDirective
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>📋 Pivot Table</h1>
        <p>Dynamically aggregate and analyze your financial data</p>
      </div>

      <div class="pivot-layout">
        <!-- Control Panel -->
        <aside class="control-panel card">
          <h3 style="font-weight:700;margin-bottom:16px;font-size:.95rem">⚙️ Configuration</h3>

          <!-- Data source -->
          <div class="control-section">
            <label class="section-label">Data Source</label>
            <mat-form-field appearance="outline">
              <mat-label>Entity</mat-label>
              <mat-select [(ngModel)]="entity">
                <mat-option value="trades">Trades</mat-option>
                <mat-option value="market_data">Market Data</mat-option>
              </mat-select>
            </mat-form-field>
          </div>

          <!-- Dimensions (GROUP BY) -->
          <div class="control-section">
            <label class="section-label">
              <span class="material-icons" style="font-size:14px;vertical-align:middle">view_column</span>
              Dimensions (Group By)
            </label>
            <div class="field-list">
              <div *ngFor="let f of getDimensionFields()" class="field-chip"
                   [class.selected]="isDimSelected(f.key)"
                   (click)="toggleDimension(f.key)">
                {{ f.label }}
                <span class="material-icons" *ngIf="isDimSelected(f.key)" style="font-size:14px">check</span>
              </div>
            </div>
          </div>

          <!-- Measures -->
          <div class="control-section">
            <label class="section-label">
              <span class="material-icons" style="font-size:14px;vertical-align:middle">functions</span>
              Measures
            </label>
            <div *ngFor="let m of measures; let i = index" class="measure-row">
              <mat-form-field appearance="outline" style="flex:1">
                <mat-label>Field</mat-label>
                <mat-select [(ngModel)]="m.field">
                  <mat-option *ngFor="let f of getMeasureFields()" [value]="f.key">{{ f.label }}</mat-option>
                </mat-select>
              </mat-form-field>
              <mat-form-field appearance="outline" style="width:90px">
                <mat-label>Fn</mat-label>
                <mat-select [(ngModel)]="m.function">
                  <mat-option *ngFor="let fn of aggregateFns" [value]="fn">{{ fn }}</mat-option>
                </mat-select>
              </mat-form-field>
              <button mat-icon-button color="warn" (click)="removeMeasure(i)" [matTooltip]="'Remove'">
                <mat-icon>remove_circle_outline</mat-icon>
              </button>
            </div>
            <button mat-stroked-button (click)="addMeasure()" style="width:100%;margin-top:8px">
              <mat-icon>add</mat-icon> Add Measure
            </button>
          </div>

          <!-- Date filter -->
          <div class="control-section">
            <label class="section-label">Date Range</label>
            <mat-form-field appearance="outline">
              <mat-label>Start Date</mat-label>
              <input matInput type="date" [(ngModel)]="startDate">
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>End Date</mat-label>
              <input matInput type="date" [(ngModel)]="endDate">
            </mat-form-field>
          </div>

          <!-- Sort -->
          <div class="control-section">
            <label class="section-label">Sort</label>
            <mat-form-field appearance="outline">
              <mat-label>Direction</mat-label>
              <mat-select [(ngModel)]="sortDirection">
                <mat-option value="ASC">Ascending</mat-option>
                <mat-option value="DESC">Descending</mat-option>
              </mat-select>
            </mat-form-field>
          </div>

          <!-- Actions -->
          <button mat-raised-button color="primary" (click)="runAggregation()" [disabled]="loading" style="width:100%;margin-bottom:8px">
            <mat-icon>play_arrow</mat-icon>
            {{ loading ? 'Running...' : 'Run Analysis' }}
          </button>
          <button mat-stroked-button (click)="reset()" style="width:100%">
            <mat-icon>refresh</mat-icon> Reset
          </button>
        </aside>

        <!-- Results -->
        <div class="results-panel">
          <!-- Totals bar -->
          <div *ngIf="result" class="totals-bar">
            <div class="total-item" *ngFor="let key of getTotalKeys()">
              <span class="total-label">{{ key }}</span>
              <span class="total-value">{{ result.totals[key] | number:'1.2-4' }}</span>
            </div>
            <div class="total-item">
              <span class="total-label">Total Rows</span>
              <span class="total-value">{{ result.totalRows | number }}</span>
            </div>
          </div>

          <mat-tab-group *ngIf="result">
            <!-- Table tab -->
            <mat-tab label="📊 Table">
              <div class="table-actions">
                <button mat-stroked-button (click)="exportService.exportCustom(entity, 'EXCEL')">
                  <mat-icon>download</mat-icon> Excel
                </button>
                <button mat-stroked-button (click)="exportService.exportCustom(entity, 'PDF')">
                  <mat-icon>picture_as_pdf</mat-icon> PDF
                </button>
              </div>

              <div style="overflow:auto">
                <table class="data-table" *ngIf="result.rows.length > 0">
                  <thead>
                    <tr>
                      <th *ngFor="let col of getColumns()">{{ col }}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr *ngFor="let row of result.rows">
                      <td *ngFor="let col of getColumns()">
                        <span *ngIf="isNumber(row[col])">
                          {{ row[col] | number:'1.2-4' }}
                        </span>
                        <span *ngIf="!isNumber(row[col])">{{ row[col] }}</span>
                      </td>
                    </tr>
                  </tbody>
                  <tfoot>
                    <tr style="background:var(--surface-2);font-weight:700">
                      <td *ngFor="let col of getColumns(); let i = index">
                        {{ i === 0 ? 'TOTAL' : (result.totals[col] != null ? (result.totals[col] | number:'1.2-4') : '') }}
                      </td>
                    </tr>
                  </tfoot>
                </table>
                <div *ngIf="result.rows.length === 0" style="text-align:center;padding:60px;color:var(--text-muted)">
                  <span class="material-icons" style="font-size:48px;opacity:.3">table_rows</span>
                  <p>No data matches your criteria</p>
                </div>
              </div>
            </mat-tab>

            <!-- Bar chart tab -->
            <mat-tab label="📊 Bar Chart">
              <div style="padding:20px;height:400px;position:relative">
                <canvas baseChart [data]="barData" [options]="chartOptions" type="bar"></canvas>
              </div>
            </mat-tab>

            <!-- Line chart tab -->
            <mat-tab label="📈 Line Chart">
              <div style="padding:20px;height:400px;position:relative">
                <canvas baseChart [data]="lineData" [options]="chartOptions" type="line"></canvas>
              </div>
            </mat-tab>

            <!-- Pie chart tab -->
            <mat-tab label="🥧 Pie Chart">
              <div style="padding:20px;height:400px;position:relative;display:flex;justify-content:center">
                <canvas baseChart [data]="pieData" [options]="pieOptions" type="pie"></canvas>
              </div>
            </mat-tab>
          </mat-tab-group>

          <!-- Placeholder -->
          <div *ngIf="!result && !loading" class="placeholder">
            <span class="material-icons">table_chart</span>
            <h3>Configure & Run Analysis</h3>
            <p>Select dimensions and measures on the left, then click "Run Analysis"</p>
          </div>

          <div class="spinner-overlay" *ngIf="loading">
            <mat-spinner diameter="60"></mat-spinner>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .pivot-layout {
      display: grid;
      grid-template-columns: 300px 1fr;
      gap: 20px;
      align-items: start;
    }

    @media (max-width: 1024px) {
      .pivot-layout { grid-template-columns: 1fr; }
    }

    .control-panel { padding: 20px; }

    .control-section { margin-bottom: 20px; }

    .section-label {
      display: block;
      font-size: .75rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: .05em;
      color: var(--text-muted);
      margin-bottom: 8px;
    }

    .field-list {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
    }

    .field-chip {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 5px 10px;
      border-radius: 100px;
      font-size: .75rem;
      font-weight: 500;
      background: var(--surface-2);
      border: 1px solid var(--border);
      cursor: pointer;
      transition: all .15s;
      color: var(--text-muted);

      &:hover { border-color: var(--primary-light); color: var(--primary); }
      &.selected { background: #eef2ff; border-color: #6366f1; color: #4338ca; font-weight: 600; }
    }

    .measure-row {
      display: flex;
      gap: 8px;
      align-items: flex-start;
      margin-bottom: 8px;
    }

    .results-panel {
      background: var(--surface);
      border-radius: var(--radius);
      box-shadow: var(--shadow);
      border: 1px solid var(--border);
      overflow: hidden;
    }

    .totals-bar {
      display: flex;
      gap: 0;
      background: #1e1b4b;
      overflow-x: auto;
      padding: 12px 20px;

      .total-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 0 20px;
        border-right: 1px solid rgba(255,255,255,.15);
        min-width: 100px;

        &:last-child { border-right: none; }
      }

      .total-label {
        font-size: .7rem;
        color: rgba(255,255,255,.6);
        text-transform: uppercase;
        letter-spacing: .05em;
        margin-bottom: 2px;
      }

      .total-value {
        font-size: 1rem;
        font-weight: 700;
        color: #a5b4fc;
      }
    }

    .table-actions {
      display: flex;
      gap: 8px;
      padding: 12px 20px;
      border-bottom: 1px solid var(--border);
    }

    .placeholder {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 80px 20px;
      color: var(--text-muted);
      text-align: center;

      .material-icons { font-size: 64px; opacity: .3; margin-bottom: 16px; }
      h3 { font-size: 1.1rem; margin-bottom: 8px; color: var(--text); }
      p { font-size: .875rem; }
    }

    ::ng-deep .mat-mdc-tab-body-content { padding: 0 !important; }
  `]
})
export class PivotTableComponent implements OnInit {
  entity = 'trades';
  selectedDimensions: string[] = ['symbol'];
  measures: MeasureConfig[] = [{ field: 'totalValue', function: 'SUM' }];
  startDate = '';
  endDate = '';
  sortDirection: 'ASC' | 'DESC' = 'DESC';
  loading = false;
  result: AggregationResult | null = null;

  aggregateFns: AggregateFn[] = ['SUM', 'AVG', 'MAX', 'MIN', 'COUNT'];

  tradeFields: FieldDef[] = [
    { key: 'symbol', label: 'Symbol', type: 'dimension' },
    { key: 'tradeType', label: 'Trade Type', type: 'dimension' },
    { key: 'brokerName', label: 'Broker', type: 'dimension' },
    { key: 'sector', label: 'Sector', type: 'dimension' },
    { key: 'totalValue', label: 'Total Value', type: 'measure' },
    { key: 'quantity', label: 'Quantity', type: 'measure' },
    { key: 'price', label: 'Price', type: 'measure' },
  ];

  marketFields: FieldDef[] = [
    { key: 'symbol', label: 'Symbol', type: 'dimension' },
    { key: 'interval', label: 'Interval', type: 'dimension' },
    { key: 'close', label: 'Close', type: 'measure' },
    { key: 'volume', label: 'Volume', type: 'measure' },
    { key: 'high', label: 'High', type: 'measure' },
    { key: 'low', label: 'Low', type: 'measure' },
    { key: 'changePercent', label: 'Change %', type: 'measure' },
  ];

  // Charts
  barData: ChartData<'bar'> = { labels: [], datasets: [] };
  lineData: ChartData<'line'> = { labels: [], datasets: [] };
  pieData: ChartData<'pie'> = { labels: [], datasets: [] };
  chartOptions: ChartConfiguration['options'] = {
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { position: 'top' } },
    scales: { y: { beginAtZero: true } }
  };
  pieOptions: ChartConfiguration['options'] = {
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { position: 'right' } }
  };

  constructor(private api: ApiService, public exportService: ExportService, private snackBar: MatSnackBar) {}

  ngOnInit(): void {}

  getDimensionFields(): FieldDef[] {
    return (this.entity === 'market_data' ? this.marketFields : this.tradeFields)
      .filter(f => f.type === 'dimension');
  }

  getMeasureFields(): FieldDef[] {
    return (this.entity === 'market_data' ? this.marketFields : this.tradeFields)
      .filter(f => f.type === 'measure');
  }

  isDimSelected(key: string): boolean { return this.selectedDimensions.includes(key); }

  toggleDimension(key: string): void {
    if (this.isDimSelected(key)) {
      this.selectedDimensions = this.selectedDimensions.filter(d => d !== key);
    } else {
      this.selectedDimensions.push(key);
    }
  }

  addMeasure(): void {
    const fields = this.getMeasureFields();
    if (fields.length > 0) {
      this.measures.push({ field: fields[0].key, function: 'SUM' });
    }
  }

  removeMeasure(i: number): void { this.measures.splice(i, 1); }

  runAggregation(): void {
    if (this.selectedDimensions.length === 0) {
      this.snackBar.open('Please select at least one dimension', 'Close', { duration: 3000 });
      return;
    }
    if (this.measures.length === 0) {
      this.snackBar.open('Please add at least one measure', 'Close', { duration: 3000 });
      return;
    }

    this.loading = true;
    const measures = this.measures.map((m, i) => ({ ...m, alias: `${m.function}_${m.field}` }));
    const req: AggregationRequest = {
      entity: this.entity,
      dimensions: this.selectedDimensions,
      measures,
      sortDirection: this.sortDirection,
      limit: 500,
      startDate: this.startDate || undefined,
      endDate: this.endDate || undefined
    };

    this.api.aggregate(req).subscribe({
      next: result => {
        this.result = result;
        this.buildCharts(result);
        this.loading = false;
      },
      error: err => {
        this.snackBar.open('Error running aggregation', 'Close', { duration: 4000, panelClass: 'error-snack' });
        this.loading = false;
      }
    });
  }

  buildCharts(result: AggregationResult): void {
    const labels = result.rows.map(r => String(r[this.selectedDimensions[0]] || ''));
    const measureKey = result.measures[0] || '';
    const values = result.rows.map(r => Number(r[measureKey] || 0));
    const colors = this.genColors(labels.length);

    this.barData = {
      labels,
      datasets: [{ label: measureKey, data: values, backgroundColor: '#6366f1', borderRadius: 4 }]
    };
    this.lineData = {
      labels,
      datasets: [{ label: measureKey, data: values, borderColor: '#06b6d4', backgroundColor: 'rgba(6,182,212,.1)', fill: true, tension: .4 }]
    };
    this.pieData = { labels, datasets: [{ data: values, backgroundColor: colors, borderWidth: 0 }] };
  }

  genColors(n: number): string[] {
    const palette = ['#6366f1','#06b6d4','#10b981','#f59e0b','#ef4444','#8b5cf6','#ec4899','#14b8a6'];
    return Array.from({ length: n }, (_, i) => palette[i % palette.length]);
  }

  getColumns(): string[] {
    if (!this.result || this.result.rows.length === 0) return [];
    return Object.keys(this.result.rows[0]);
  }

  getTotalKeys(): string[] {
    return Object.keys(this.result?.totals || {});
  }

  isNumber(val: any): boolean { return typeof val === 'number'; }

  reset(): void {
    this.entity = 'trades';
    this.selectedDimensions = ['symbol'];
    this.measures = [{ field: 'totalValue', function: 'SUM' }];
    this.startDate = '';
    this.endDate = '';
    this.result = null;
  }
}

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartConfiguration } from 'chart.js';
import { ApiService } from '../../core/services/api.service';
import { Trade, Stock, MarketData } from '../../core/models';

@Component({
  selector: 'app-chart-container',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatButtonModule, MatSelectModule, MatFormFieldModule,
    MatInputModule, MatIconModule, MatProgressSpinnerModule, MatTabsModule, BaseChartDirective
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>📈 Charts & Visualizations</h1>
        <p>Interactive charts generated from your financial data</p>
      </div>

      <!-- Controls -->
      <div class="card" style="margin-bottom:20px">
        <div style="display:flex;gap:16px;flex-wrap:wrap;align-items:center">
          <mat-form-field appearance="outline" style="min-width:160px;margin:0">
            <mat-label>Chart Type</mat-label>
            <mat-select [(ngModel)]="chartType" (ngModelChange)="updateCharts()">
              <mat-option value="trades">Trade Analysis</mat-option>
              <mat-option value="market">Market Data</mat-option>
              <mat-option value="comparison">Symbol Comparison</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field *ngIf="chartType === 'market'" appearance="outline" style="min-width:140px;margin:0">
            <mat-label>Symbol</mat-label>
            <mat-select [(ngModel)]="selectedSymbol" (ngModelChange)="loadMarketData()">
              <mat-option *ngFor="let s of stocks" [value]="s.symbol">{{ s.symbol }}</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field *ngIf="chartType === 'market'" appearance="outline" style="min-width:120px;margin:0">
            <mat-label>Interval</mat-label>
            <mat-select [(ngModel)]="interval" (ngModelChange)="loadMarketData()">
              <mat-option value="1min">1 min</mat-option>
              <mat-option value="5min">5 min</mat-option>
              <mat-option value="1h">1 hour</mat-option>
              <mat-option value="1day">1 day</mat-option>
              <mat-option value="1week">1 week</mat-option>
            </mat-select>
          </mat-form-field>

          <button mat-raised-button color="primary" (click)="refresh()">
            <mat-icon>refresh</mat-icon> Refresh
          </button>
        </div>
      </div>

      <div *ngIf="loading" class="spinner-overlay" style="height:300px">
        <mat-spinner diameter="60"></mat-spinner>
      </div>

      <div *ngIf="!loading">
        <!-- Trade Charts -->
        <div *ngIf="chartType === 'trades'">
          <div class="grid-2" style="margin-bottom:20px">
            <div class="card">
              <div class="card-header">
                <h3><span class="material-icons">bar_chart</span> Volume by Symbol</h3>
              </div>
              <div style="height:300px;position:relative">
                <canvas baseChart [data]="symbolVolumeData" [options]="barOptions" type="bar"></canvas>
              </div>
            </div>
            <div class="card">
              <div class="card-header">
                <h3><span class="material-icons">pie_chart</span> Trade Type Distribution</h3>
              </div>
              <div style="height:300px;position:relative">
                <canvas baseChart [data]="tradeTypeData" [options]="doughnutOptions" type="doughnut"></canvas>
              </div>
            </div>
          </div>

          <div class="grid-2">
            <div class="card">
              <div class="card-header">
                <h3><span class="material-icons">show_chart</span> Total Value Over Time</h3>
              </div>
              <div style="height:300px;position:relative">
                <canvas baseChart [data]="valueOverTimeData" [options]="lineOptions" type="line"></canvas>
              </div>
            </div>
            <div class="card">
              <div class="card-header">
                <h3><span class="material-icons">pie_chart</span> Value by Sector</h3>
              </div>
              <div style="height:300px;position:relative">
                <canvas baseChart [data]="sectorData" [options]="pieOptions" type="pie"></canvas>
              </div>
            </div>
          </div>
        </div>

        <!-- Market Charts -->
        <div *ngIf="chartType === 'market'">
          <div class="card" style="margin-bottom:20px">
            <div class="card-header">
              <h3><span class="material-icons">candlestick_chart</span> Price History - {{ selectedSymbol }}</h3>
            </div>
            <div style="height:350px;position:relative">
              <canvas baseChart [data]="priceHistoryData" [options]="lineOptions" type="line"></canvas>
            </div>
          </div>
          <div class="grid-2">
            <div class="card">
              <div class="card-header">
                <h3><span class="material-icons">bar_chart</span> Volume History</h3>
              </div>
              <div style="height:280px;position:relative">
                <canvas baseChart [data]="volumeHistoryData" [options]="barOptions" type="bar"></canvas>
              </div>
            </div>
            <div class="card">
              <div class="card-header">
                <h3><span class="material-icons">show_chart</span> Price Change %</h3>
              </div>
              <div style="height:280px;position:relative">
                <canvas baseChart [data]="changeData" [options]="lineOptions" type="line"></canvas>
              </div>
            </div>
          </div>
        </div>

        <!-- Comparison Charts -->
        <div *ngIf="chartType === 'comparison'">
          <div class="card">
            <div class="card-header">
              <h3><span class="material-icons">compare</span> Symbol Performance Comparison</h3>
            </div>
            <div style="height:400px;position:relative">
              <canvas baseChart [data]="comparisonData" [options]="barOptions" type="bar"></canvas>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class ChartContainerComponent implements OnInit {
  chartType = 'trades';
  selectedSymbol = 'AAPL';
  interval = '1day';
  loading = false;
  trades: Trade[] = [];
  stocks: Stock[] = [];
  marketData: MarketData[] = [];

  // Chart data
  symbolVolumeData: ChartData<'bar'> = { labels: [], datasets: [] };
  tradeTypeData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  valueOverTimeData: ChartData<'line'> = { labels: [], datasets: [] };
  sectorData: ChartData<'pie'> = { labels: [], datasets: [] };
  priceHistoryData: ChartData<'line'> = { labels: [], datasets: [] };
  volumeHistoryData: ChartData<'bar'> = { labels: [], datasets: [] };
  changeData: ChartData<'line'> = { labels: [], datasets: [] };
  comparisonData: ChartData<'bar'> = { labels: [], datasets: [] };

  barOptions: ChartConfiguration['options'] = {
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: { y: { beginAtZero: true, grid: { color: '#f1f5f9' } }, x: { grid: { display: false } } }
  };
  lineOptions: ChartConfiguration['options'] = {
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { position: 'top' } },
    scales: { y: { grid: { color: '#f1f5f9' } }, x: { grid: { display: false } } }
  };
  doughnutOptions: ChartConfiguration['options'] = {
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { position: 'bottom' } },
    cutout: '60%'
  };
  pieOptions: ChartConfiguration['options'] = {
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { position: 'right' } }
  };

  palette = ['#6366f1','#06b6d4','#10b981','#f59e0b','#ef4444','#8b5cf6','#ec4899','#14b8a6','#f97316'];

  constructor(private api: ApiService) {}

  ngOnInit(): void { this.loadAll(); }

  loadAll(): void {
    this.loading = true;
    this.api.getTrades().subscribe(trades => {
      this.trades = trades;
      this.api.getStocks().subscribe(stocks => {
        this.stocks = stocks;
        if (stocks.length > 0) this.selectedSymbol = stocks[0].symbol;
        this.buildTradeCharts();
        this.buildComparisonChart();
        if (this.chartType === 'market') this.loadMarketData();
        else this.loading = false;
      });
    });
  }

  refresh(): void {
    if (this.chartType === 'market') this.loadMarketData();
    else this.loadAll();
  }

  loadMarketData(): void {
    if (!this.selectedSymbol) return;
    this.loading = true;
    this.api.getTimeSeries(this.selectedSymbol, this.interval, 60).subscribe({
      next: data => {
        this.marketData = data.reverse();
        this.buildMarketCharts();
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  updateCharts(): void {
    if (this.chartType === 'market') this.loadMarketData();
    else { this.buildTradeCharts(); this.buildComparisonChart(); }
  }

  buildTradeCharts(): void {
    // Volume by symbol
    const symbolMap = new Map<string, number>();
    this.trades.forEach(t => symbolMap.set(t.symbol, (symbolMap.get(t.symbol) || 0) + (t.totalValue || 0)));
    const top = Array.from(symbolMap.entries()).sort((a,b) => b[1]-a[1]).slice(0,10);

    this.symbolVolumeData = {
      labels: top.map(e => e[0]),
      datasets: [{ label: 'Total Value ($)', data: top.map(e => e[1]),
        backgroundColor: top.map((_, i) => this.palette[i % this.palette.length]), borderRadius: 4 }]
    };

    // Trade types
    const buys = this.trades.filter(t => t.tradeType === 'BUY').length;
    const sells = this.trades.filter(t => t.tradeType === 'SELL').length;
    this.tradeTypeData = {
      labels: ['Buy', 'Sell'],
      datasets: [{ data: [buys, sells], backgroundColor: ['#10b981','#ef4444'], borderWidth: 0 }]
    };

    // Value over time (by month)
    const monthMap = new Map<string, number>();
    this.trades.forEach(t => {
      const m = t.tradeDate?.substring(0, 7) || 'Unknown';
      monthMap.set(m, (monthMap.get(m) || 0) + (t.totalValue || 0));
    });
    const months = Array.from(monthMap.keys()).sort();
    this.valueOverTimeData = {
      labels: months,
      datasets: [{ label: 'Trade Value', data: months.map(m => monthMap.get(m) || 0),
        borderColor: '#6366f1', backgroundColor: 'rgba(99,102,241,.15)', fill: true, tension: .4 }]
    };

    // Sector breakdown
    const sectorMap = new Map<string, number>();
    this.trades.forEach(t => {
      const stock = this.stocks.find(s => s.symbol === t.symbol);
      const sector = stock?.sector || 'Unknown';
      sectorMap.set(sector, (sectorMap.get(sector) || 0) + (t.totalValue || 0));
    });
    const sectors = Array.from(sectorMap.entries());
    this.sectorData = {
      labels: sectors.map(e => e[0]),
      datasets: [{ data: sectors.map(e => e[1]),
        backgroundColor: sectors.map((_, i) => this.palette[i % this.palette.length]), borderWidth: 0 }]
    };
  }

  buildMarketCharts(): void {
    const labels = this.marketData.map(d => d.timestamp?.substring(0, 10) || '');
    this.priceHistoryData = {
      labels,
      datasets: [{
        label: `${this.selectedSymbol} Close`,
        data: this.marketData.map(d => d.close || 0),
        borderColor: '#6366f1', backgroundColor: 'rgba(99,102,241,.1)', fill: true, tension: .3, pointRadius: 2
      }]
    };
    this.volumeHistoryData = {
      labels,
      datasets: [{ label: 'Volume', data: this.marketData.map(d => d.volume || 0),
        backgroundColor: '#06b6d4', borderRadius: 2 }]
    };
    this.changeData = {
      labels,
      datasets: [{
        label: 'Change %',
        data: this.marketData.map(d => d.changePercent || 0),
        borderColor: '#10b981', backgroundColor: 'rgba(16,185,129,.1)', fill: true, tension: .3, pointRadius: 2
      }]
    };
  }

  buildComparisonChart(): void {
    const symbolMap = new Map<string, number>();
    this.trades.forEach(t => symbolMap.set(t.symbol, (symbolMap.get(t.symbol) || 0) + (t.totalValue || 0)));
    const entries = Array.from(symbolMap.entries()).sort((a,b) => b[1]-a[1]);
    this.comparisonData = {
      labels: entries.map(e => e[0]),
      datasets: [{ label: 'Total Trade Value', data: entries.map(e => e[1]),
        backgroundColor: entries.map((_, i) => this.palette[i % this.palette.length]), borderRadius: 6 }]
    };
  }
}

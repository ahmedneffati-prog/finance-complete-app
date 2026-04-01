import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { Chart, registerables } from 'chart.js';
import { ApiService } from '../../core/services/api.service';
import { WebSocketService } from '../../core/services/websocket.service';
import { Trade, Stock, MarketData, LiveMarketData } from '../../core/models';
import { Subscription } from 'rxjs';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule, MatButtonModule, MatIconModule, MatSelectModule,
    MatProgressSpinnerModule, RouterLink, BaseChartDirective
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>📊 Dashboard</h1>
        <p>Real-time financial overview and analytics</p>
      </div>

      <!-- KPI Cards -->
      <div class="grid-4" style="margin-bottom:24px">
        <div class="kpi-card">
          <div style="display:flex;justify-content:space-between;align-items:flex-start">
            <div>
              <div class="kpi-label">Total Trades</div>
              <div class="kpi-value">{{ trades.length | number }}</div>
              <div class="kpi-change positive">
                <span class="material-icons" style="font-size:14px">trending_up</span>
                All time
              </div>
            </div>
            <div class="kpi-icon" style="background:linear-gradient(135deg,#6366f1,#4338ca)">
              <span class="material-icons">receipt_long</span>
            </div>
          </div>
        </div>

        <div class="kpi-card">
          <div style="display:flex;justify-content:space-between;align-items:flex-start">
            <div>
              <div class="kpi-label">Total Volume ($)</div>
              <div class="kpi-value">{{ totalVolume | number:'1.0-0' }}</div>
              <div class="kpi-change positive">
                <span class="material-icons" style="font-size:14px">trending_up</span>
                Portfolio value
              </div>
            </div>
            <div class="kpi-icon" style="background:linear-gradient(135deg,#06b6d4,#0891b2)">
              <span class="material-icons">attach_money</span>
            </div>
          </div>
        </div>

        <div class="kpi-card">
          <div style="display:flex;justify-content:space-between;align-items:flex-start">
            <div>
              <div class="kpi-label">Active Stocks</div>
              <div class="kpi-value">{{ stocks.length | number }}</div>
              <div class="kpi-change positive">
                <span class="material-icons" style="font-size:14px">trending_up</span>
                Tracked symbols
              </div>
            </div>
            <div class="kpi-icon" style="background:linear-gradient(135deg,#10b981,#059669)">
              <span class="material-icons">show_chart</span>
            </div>
          </div>
        </div>

        <div class="kpi-card">
          <div style="display:flex;justify-content:space-between;align-items:flex-start">
            <div>
              <div class="kpi-label">Buy/Sell Ratio</div>
              <div class="kpi-value">{{ buySellRatio | number:'1.2-2' }}</div>
              <div class="kpi-change" [class.positive]="buySellRatio >= 1" [class.negative]="buySellRatio < 1">
                <span class="material-icons" style="font-size:14px">{{ buySellRatio >= 1 ? 'trending_up' : 'trending_down' }}</span>
                {{ buyCount }} buys / {{ sellCount }} sells
              </div>
            </div>
            <div class="kpi-icon" style="background:linear-gradient(135deg,#f59e0b,#d97706)">
              <span class="material-icons">balance</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Charts Row -->
      <div class="grid-2" style="margin-bottom:24px">
        <!-- Trade Volume by Symbol -->
        <div class="card">
          <div class="card-header">
            <h3><span class="material-icons">bar_chart</span> Trade Volume by Symbol</h3>
          </div>
          <div style="height:280px;position:relative" *ngIf="!loading">
            <canvas baseChart
              [data]="barChartData"
              [options]="barChartOptions"
              type="bar">
            </canvas>
          </div>
          <div class="spinner-overlay" *ngIf="loading">
            <mat-spinner diameter="40"></mat-spinner>
          </div>
        </div>

        <!-- Buy vs Sell Donut -->
        <div class="card">
          <div class="card-header">
            <h3><span class="material-icons">pie_chart</span> Buy vs Sell Distribution</h3>
          </div>
          <div style="height:280px;position:relative;display:flex;align-items:center;justify-content:center" *ngIf="!loading">
            <canvas baseChart
              [data]="pieChartData"
              [options]="pieChartOptions"
              type="doughnut"
              style="max-height:260px">
            </canvas>
          </div>
        </div>
      </div>

      <!-- Recent Trades + Live Prices -->
      <div class="grid-2">
        <!-- Recent Trades -->
        <div class="card">
          <div class="card-header">
            <h3><span class="material-icons">history</span> Recent Trades</h3>
            <a routerLink="/pivot" mat-button color="primary" style="font-size:.8rem">View All →</a>
          </div>
          <div style="overflow:auto;max-height:320px">
            <table class="data-table" *ngIf="recentTrades.length > 0">
              <thead>
                <tr>
                  <th>Symbol</th>
                  <th>Type</th>
                  <th>Qty</th>
                  <th>Price</th>
                  <th>Value</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let t of recentTrades">
                  <td><strong>{{ t.symbol }}</strong></td>
                  <td>
                    <span class="badge" [class.badge-success]="t.tradeType==='BUY'" [class.badge-danger]="t.tradeType==='SELL'">
                      {{ t.tradeType }}
                    </span>
                  </td>
                  <td>{{ t.quantity | number:'1.0-2' }}</td>
                  <td>{{ t.price | currency }}</td>
                  <td>{{ t.totalValue | currency }}</td>
                </tr>
              </tbody>
            </table>
            <div *ngIf="recentTrades.length === 0 && !loading" style="text-align:center;padding:40px;color:var(--text-muted)">
              No trades found. Add data to get started.
            </div>
          </div>
        </div>

        <!-- Live Prices -->
        <div class="card">
          <div class="card-header">
            <h3><span class="material-icons">bolt</span> Live Prices</h3>
            <a routerLink="/live-market" mat-button color="primary" style="font-size:.8rem">Live View →</a>
          </div>
          <div style="display:flex;flex-direction:column;gap:12px">
            <div *ngFor="let stock of stocks.slice(0,6)" class="live-price-row">
              <div>
                <div style="font-weight:600;font-size:.9rem">{{ stock.symbol }}</div>
                <div style="font-size:.75rem;color:var(--text-muted)">{{ stock.name }}</div>
              </div>
              <div style="text-align:right">
                <div *ngIf="getLivePrice(stock.symbol) as price; else noPrice" style="font-weight:700">
                  {{ price.price | currency }}
                </div>
                <ng-template #noPrice>
                  <div style="color:var(--text-muted);font-size:.85rem">—</div>
                </ng-template>
                <span class="badge badge-info" style="font-size:.7rem">{{ stock.sector }}</span>
              </div>
            </div>
            <div *ngIf="stocks.length === 0" style="text-align:center;padding:20px;color:var(--text-muted)">
              No stocks tracked yet.
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .live-price-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 10px 12px;
      border-radius: 8px;
      background: var(--surface-2);
      border: 1px solid var(--border);
      transition: background .15s;

      &:hover { background: #eef2ff; }
    }
  `]
})
export class DashboardComponent implements OnInit, OnDestroy {
  trades: Trade[] = [];
  stocks: Stock[] = [];
  loading = true;
  livePrices = new Map<string, LiveMarketData>();
  private sub?: Subscription;

  // Chart data
  barChartData: ChartData<'bar'> = { labels: [], datasets: [] };
  barChartOptions: ChartConfiguration['options'] = {
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      y: { beginAtZero: true, grid: { color: '#f1f5f9' } },
      x: { grid: { display: false } }
    }
  };

  pieChartData: ChartData<'doughnut'> = {
    labels: ['Buy', 'Sell'],
    datasets: [{ data: [0, 0], backgroundColor: ['#10b981','#ef4444'], borderWidth: 0 }]
  };
  pieChartOptions: ChartConfiguration['options'] = {
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { position: 'bottom' } },
    cutout: '65%'
  };

  constructor(private api: ApiService, private wsService: WebSocketService) {}

  ngOnInit(): void {
    this.loadData();
    this.sub = this.wsService.allPrices$.subscribe(d => {
      this.livePrices.set(d.symbol, d);
    });
  }

  loadData(): void {
    this.loading = true;
    this.api.getTrades().subscribe({
      next: trades => {
        this.trades = trades;
        this.buildCharts();
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
    this.api.getStocks().subscribe(stocks => this.stocks = stocks);
  }

  buildCharts(): void {
    // Bar chart: total value per symbol
    const symbolMap = new Map<string, number>();
    this.trades.forEach(t => {
      const cur = symbolMap.get(t.symbol) || 0;
      symbolMap.set(t.symbol, cur + (t.totalValue || 0));
    });
    const symbols = Array.from(symbolMap.keys()).slice(0, 8);
    this.barChartData = {
      labels: symbols,
      datasets: [{
        data: symbols.map(s => symbolMap.get(s) || 0),
        backgroundColor: '#6366f1',
        borderRadius: 6,
        label: 'Total Value ($)'
      }]
    };

    // Pie: buy vs sell
    const buys = this.trades.filter(t => t.tradeType === 'BUY').length;
    const sells = this.trades.filter(t => t.tradeType === 'SELL').length;
    this.pieChartData = {
      labels: ['Buy', 'Sell'],
      datasets: [{ data: [buys, sells], backgroundColor: ['#10b981', '#ef4444'], borderWidth: 0 }]
    };
  }

  get totalVolume(): number {
    return this.trades.reduce((s, t) => s + (t.totalValue || 0), 0);
  }

  get buyCount(): number { return this.trades.filter(t => t.tradeType === 'BUY').length; }
  get sellCount(): number { return this.trades.filter(t => t.tradeType === 'SELL').length; }
  get buySellRatio(): number {
    return this.sellCount > 0 ? this.buyCount / this.sellCount : this.buyCount;
  }

  get recentTrades(): Trade[] {
    return [...this.trades].sort((a, b) =>
      new Date(b.tradeDate).getTime() - new Date(a.tradeDate).getTime()
    ).slice(0, 8);
  }

  getLivePrice(symbol: string): LiveMarketData | undefined {
    return this.livePrices.get(symbol);
  }

  ngOnDestroy(): void { this.sub?.unsubscribe(); }
}

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartConfiguration } from 'chart.js';
import { Subscription } from 'rxjs';
import { WebSocketService, ConnectionStatus } from '../../core/services/websocket.service';
import { ApiService } from '../../core/services/api.service';
import { LiveMarketData, Stock } from '../../core/models';

interface WatchItem {
  symbol: string;
  stockName?: string;
  price?: number;
  prevPrice?: number;
  changePercent?: number;
  change?: number;
  volume?: number;
  high?: number;
  low?: number;
  lastUpdate?: Date;
  priceHistory: number[];
  direction: 'up' | 'down' | 'neutral';
}

@Component({
  selector: 'app-live-market',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatButtonModule, MatIconModule, MatFormFieldModule,
    MatInputModule, MatChipsModule, MatBadgeModule, MatTooltipModule, MatProgressSpinnerModule,
    BaseChartDirective
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <div style="display:flex;align-items:center;gap:16px">
          <div>
            <h1>⚡ Live Market</h1>
            <p>Real-time price streaming via WebSocket</p>
          </div>
          <div class="connection-badge" [class]="'conn-' + (wsStatus$ | async)?.toLowerCase()">
            <span class="dot"></span>
            {{ wsStatus$ | async }}
          </div>
        </div>
      </div>

      <!-- Add Symbol -->
      <div class="card" style="margin-bottom:20px">
        <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap">
          <input
            #symbolInput
            type="text"
            class="symbol-input"
            placeholder="Add symbol (e.g. AAPL, TSLA, MSFT)"
            [(ngModel)]="newSymbol"
            (keydown.enter)="addSymbol()">
          <button mat-raised-button color="primary" (click)="addSymbol()">
            <mat-icon>add</mat-icon> Watch
          </button>
          <div style="display:flex;gap:8px;flex-wrap:wrap">
            <button *ngFor="let s of quickSymbols" mat-stroked-button (click)="addSymbolDirect(s)" style="font-size:.75rem;padding:4px 12px">
              {{ s }}
            </button>
          </div>
          <button mat-icon-button (click)="clearAll()" matTooltip="Clear all">
            <mat-icon>delete_sweep</mat-icon>
          </button>
        </div>
      </div>

      <!-- Live Price Grid -->
      <div class="price-grid" *ngIf="watchList.length > 0">
        <div *ngFor="let item of watchList" class="price-card" [class]="'direction-' + item.direction">
          <div class="price-card-header">
            <div>
              <div class="symbol-name">{{ item.symbol }}</div>
              <div class="stock-name">{{ item.stockName || '—' }}</div>
            </div>
            <button mat-icon-button (click)="removeSymbol(item.symbol)" style="opacity:.5;width:28px;height:28px">
              <mat-icon style="font-size:16px">close</mat-icon>
            </button>
          </div>

          <div class="price-main">
            <div class="price-value" [class.price-up]="item.direction==='up'" [class.price-down]="item.direction==='down'">
              {{ item.price != null ? (item.price | currency) : '—' }}
            </div>
            <div class="price-change" *ngIf="item.change != null"
                 [class.positive]="item.change >= 0" [class.negative]="item.change < 0">
              <span class="material-icons" style="font-size:14px">
                {{ item.change >= 0 ? 'arrow_upward' : 'arrow_downward' }}
              </span>
              {{ item.change | number:'1.2-2' }} ({{ item.changePercent | number:'1.2-2' }}%)
            </div>
          </div>

          <div class="price-stats" *ngIf="item.high != null">
            <span>H: {{ item.high | currency }}</span>
            <span>L: {{ item.low | currency }}</span>
            <span>Vol: {{ item.volume | number:'1.0-0' }}</span>
          </div>

          <!-- Mini sparkline -->
          <div style="height:50px;margin-top:8px" *ngIf="item.priceHistory.length > 1">
            <canvas baseChart
              [data]="getSparklineData(item)"
              [options]="sparklineOptions"
              type="line">
            </canvas>
          </div>

          <div class="last-update" *ngIf="item.lastUpdate">
            {{ item.lastUpdate | date:'HH:mm:ss' }}
          </div>
        </div>
      </div>

      <!-- Empty state -->
      <div *ngIf="watchList.length === 0" class="card empty-state">
        <span class="material-icons">show_chart</span>
        <h3>No symbols tracked</h3>
        <p>Add a symbol above to start watching live prices</p>
      </div>

      <!-- Ticker Bar at bottom -->
      <div class="ticker-bar" *ngIf="watchList.length > 0">
        <div class="ticker-content">
          <span *ngFor="let item of watchList" class="ticker-item">
            <strong>{{ item.symbol }}</strong>
            <span [class.positive]="(item.change||0)>=0" [class.negative]="(item.change||0)<0">
              {{ item.price | currency }}
              <span *ngIf="item.change != null"> ({{ item.changePercent | number:'1.2-2' }}%)</span>
            </span>
          </span>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .connection-badge {
      display: flex; align-items: center; gap: 6px;
      padding: 6px 14px; border-radius: 100px;
      font-size: .75rem; font-weight: 600;
      .dot { width: 8px; height: 8px; border-radius: 50%; }

      &.conn-connected { background: #d1fae5; color: #065f46;
        .dot { background: #10b981; box-shadow: 0 0 6px #10b981; } }
      &.conn-disconnected { background: #fee2e2; color: #991b1b;
        .dot { background: #ef4444; } }
      &.conn-connecting { background: #fef3c7; color: #92400e;
        .dot { background: #f59e0b; animation: pulse .8s infinite; } }
    }

    @keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.3} }

    .symbol-input {
      border: 1.5px solid var(--border); border-radius: 8px;
      padding: 10px 14px; font-size: .9rem; outline: none;
      width: 260px; font-family: inherit;
      &:focus { border-color: var(--primary-light); box-shadow: 0 0 0 3px rgba(99,102,241,.12); }
    }

    .price-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
      gap: 16px;
      margin-bottom: 80px;
    }

    .price-card {
      background: var(--surface);
      border-radius: var(--radius);
      padding: 16px;
      border: 1.5px solid var(--border);
      box-shadow: var(--shadow);
      transition: all .2s;
      position: relative;

      &:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }
      &.direction-up { border-color: rgba(16,185,129,.4); }
      &.direction-down { border-color: rgba(239,68,68,.4); }
    }

    .price-card-header {
      display: flex; justify-content: space-between; align-items: flex-start;
    }

    .symbol-name { font-weight: 700; font-size: 1rem; }
    .stock-name { font-size: .75rem; color: var(--text-muted); }

    .price-main { margin: 10px 0; }

    .price-value {
      font-size: 1.5rem; font-weight: 700;
      transition: color .3s;
      &.price-up { color: #10b981; animation: flash-green .5s; }
      &.price-down { color: #ef4444; animation: flash-red .5s; }
    }

    @keyframes flash-green { from{background:#d1fae5} to{background:transparent} }
    @keyframes flash-red { from{background:#fee2e2} to{background:transparent} }

    .price-change { display:flex;align-items:center;gap:3px;font-size:.8rem;font-weight:600; }

    .price-stats {
      display: flex; gap: 10px;
      font-size: .72rem; color: var(--text-muted);
      margin-top: 6px;
    }

    .last-update {
      font-size: .7rem; color: var(--text-muted);
      text-align: right; margin-top: 4px;
    }

    .empty-state {
      display: flex; flex-direction: column; align-items: center;
      justify-content: center; padding: 80px; text-align: center; color: var(--text-muted);
      .material-icons { font-size: 64px; opacity: .2; margin-bottom: 16px; }
      h3 { color: var(--text); margin-bottom: 8px; }
    }

    .ticker-bar {
      position: fixed; bottom: 0; left: 0; right: 0;
      background: #1e1b4b; color: white;
      height: 36px; overflow: hidden; z-index: 200;
    }

    .ticker-content {
      display: flex; align-items: center; gap: 32px;
      height: 100%; padding: 0 20px;
      animation: ticker-scroll 30s linear infinite;
      white-space: nowrap;
    }

    @keyframes ticker-scroll {
      from { transform: translateX(0); }
      to { transform: translateX(-50%); }
    }

    .ticker-item {
      display: flex; gap: 8px; align-items: center;
      font-size: .8rem;
    }
  `]
})
export class LiveMarketComponent implements OnInit, OnDestroy {
  watchList: WatchItem[] = [];
  newSymbol = '';
  stocks: Stock[] = [];
  wsStatus$ = this.wsService.connectionStatus$;
  quickSymbols = ['AAPL', 'MSFT', 'GOOGL', 'AMZN', 'TSLA', 'META'];
  private subs: Subscription[] = [];
  private symbolSubs = new Map<string, Subscription>();

  sparklineOptions: ChartConfiguration['options'] = {
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { display: false }, tooltip: { enabled: false } },
    scales: {
      x: { display: false },
      y: { display: false }
    },
    elements: { point: { radius: 0 } }
  };

  constructor(private wsService: WebSocketService, private api: ApiService) {}

  ngOnInit(): void {
    this.api.getStocks().subscribe(stocks => this.stocks = stocks);
    // Add default symbols
    this.quickSymbols.slice(0, 3).forEach(s => this.addSymbolDirect(s));
  }

  addSymbol(): void {
    if (this.newSymbol.trim()) {
      this.addSymbolDirect(this.newSymbol.trim().toUpperCase());
      this.newSymbol = '';
    }
  }

  addSymbolDirect(symbol: string): void {
    symbol = symbol.toUpperCase();
    if (this.watchList.find(w => w.symbol === symbol)) return;

    const stock = this.stocks.find(s => s.symbol === symbol);
    const item: WatchItem = {
      symbol,
      stockName: stock?.name,
      priceHistory: [],
      direction: 'neutral'
    };
    this.watchList.push(item);

    const sub = this.wsService.subscribeSymbol(symbol).subscribe(data => {
      const w = this.watchList.find(x => x.symbol === symbol);
      if (!w) return;

      const prevPrice = w.price;
      w.price = data.price;
      w.change = data.change;
      w.changePercent = data.changePercent;
      w.volume = data.volume;
      w.high = data.high;
      w.low = data.low;
      w.lastUpdate = new Date();

      if (prevPrice != null) {
        w.direction = data.price > prevPrice ? 'up' : data.price < prevPrice ? 'down' : 'neutral';
        setTimeout(() => { if (w) w.direction = 'neutral'; }, 1000);
      }

      w.priceHistory.push(data.price);
      if (w.priceHistory.length > 20) w.priceHistory.shift();
    });

    this.symbolSubs.set(symbol, sub);
  }

  removeSymbol(symbol: string): void {
    this.symbolSubs.get(symbol)?.unsubscribe();
    this.symbolSubs.delete(symbol);
    this.wsService.unsubscribeSymbol(symbol);
    this.watchList = this.watchList.filter(w => w.symbol !== symbol);
  }

  clearAll(): void {
    [...this.watchList].forEach(w => this.removeSymbol(w.symbol));
  }

  getSparklineData(item: WatchItem): ChartData<'line'> {
    const isUp = (item.priceHistory[item.priceHistory.length - 1] || 0) >= (item.priceHistory[0] || 0);
    return {
      labels: item.priceHistory.map((_, i) => String(i)),
      datasets: [{
        data: item.priceHistory,
        borderColor: isUp ? '#10b981' : '#ef4444',
        backgroundColor: isUp ? 'rgba(16,185,129,.1)' : 'rgba(239,68,68,.1)',
        fill: true,
        tension: .4
      }]
    };
  }

  ngOnDestroy(): void {
    this.symbolSubs.forEach(s => s.unsubscribe());
    this.subs.forEach(s => s.unsubscribe());
  }
}

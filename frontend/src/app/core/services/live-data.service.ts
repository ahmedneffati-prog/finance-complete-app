import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { LiveMarketData } from '../models';
import { WebSocketService } from './websocket.service';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class LiveDataService {
  private priceMap = new Map<string, BehaviorSubject<LiveMarketData | null>>();
  private watchList: string[] = [];

  constructor(
    private wsService: WebSocketService,
    private apiService: ApiService
  ) {
    // Forward all price updates into individual subjects
    this.wsService.allPrices$.subscribe(data => {
      const sub = this.priceMap.get(data.symbol);
      if (sub) sub.next(data);
    });
  }

  watchSymbol(symbol: string): Observable<LiveMarketData | null> {
    if (!this.priceMap.has(symbol)) {
      this.priceMap.set(symbol, new BehaviorSubject<LiveMarketData | null>(null));
      this.wsService.subscribeSymbol(symbol);
      this.watchList.push(symbol);
    }
    return this.priceMap.get(symbol)!.asObservable();
  }

  unwatchSymbol(symbol: string): void {
    this.wsService.unsubscribeSymbol(symbol);
    this.priceMap.get(symbol)?.complete();
    this.priceMap.delete(symbol);
    this.watchList = this.watchList.filter(s => s !== symbol);
  }

  getWatchList(): string[] {
    return [...this.watchList];
  }

  getAllPrices$(): Observable<LiveMarketData> {
    return this.wsService.allPrices$.asObservable();
  }
}

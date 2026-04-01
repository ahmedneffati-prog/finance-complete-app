import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, Trade, Stock, Broker, MarketData, AggregationRequest, AggregationResult, PivotRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // ── Brokers ──────────────────────────────────────────────────────────────
  getBrokers(): Observable<Broker[]> {
    return this.http.get<ApiResponse<Broker[]>>(`${this.base}/brokers`).pipe(map(r => r.data));
  }
  createBroker(b: Broker): Observable<Broker> {
    return this.http.post<ApiResponse<Broker>>(`${this.base}/brokers`, b).pipe(map(r => r.data));
  }
  updateBroker(id: number, b: Broker): Observable<Broker> {
    return this.http.put<ApiResponse<Broker>>(`${this.base}/brokers/${id}`, b).pipe(map(r => r.data));
  }
  deleteBroker(id: number): Observable<void> {
    return this.http.delete<any>(`${this.base}/brokers/${id}`);
  }

  // ── Stocks ───────────────────────────────────────────────────────────────
  getStocks(): Observable<Stock[]> {
    return this.http.get<ApiResponse<Stock[]>>(`${this.base}/stocks`).pipe(map(r => r.data));
  }
  getStockBySymbol(symbol: string): Observable<Stock> {
    return this.http.get<ApiResponse<Stock>>(`${this.base}/stocks/symbol/${symbol}`).pipe(map(r => r.data));
  }
  getSectors(): Observable<string[]> {
    return this.http.get<ApiResponse<string[]>>(`${this.base}/stocks/sectors`).pipe(map(r => r.data));
  }
  createStock(s: Stock): Observable<Stock> {
    return this.http.post<ApiResponse<Stock>>(`${this.base}/stocks`, s).pipe(map(r => r.data));
  }
  updateStock(id: number, s: Stock): Observable<Stock> {
    return this.http.put<ApiResponse<Stock>>(`${this.base}/stocks/${id}`, s).pipe(map(r => r.data));
  }
  deleteStock(id: number): Observable<void> {
    return this.http.delete<any>(`${this.base}/stocks/${id}`);
  }

  // ── Trades ───────────────────────────────────────────────────────────────
  getTrades(): Observable<Trade[]> {
    return this.http.get<ApiResponse<Trade[]>>(`${this.base}/trades`).pipe(map(r => r.data));
  }
  getTradesBySymbol(symbol: string): Observable<Trade[]> {
    return this.http.get<ApiResponse<Trade[]>>(`${this.base}/trades/symbol/${symbol}`).pipe(map(r => r.data));
  }
  createTrade(t: Trade): Observable<Trade> {
    return this.http.post<ApiResponse<Trade>>(`${this.base}/trades`, t).pipe(map(r => r.data));
  }
  updateTrade(id: number, t: Trade): Observable<Trade> {
    return this.http.put<ApiResponse<Trade>>(`${this.base}/trades/${id}`, t).pipe(map(r => r.data));
  }
  deleteTrade(id: number): Observable<void> {
    return this.http.delete<any>(`${this.base}/trades/${id}`);
  }

  // ── Market Data ──────────────────────────────────────────────────────────
  getTimeSeries(symbol: string, interval = '1day', outputSize = 100): Observable<MarketData[]> {
    const params = new HttpParams()
      .set('symbol', symbol).set('interval', interval).set('outputSize', outputSize);
    return this.http.get<ApiResponse<MarketData[]>>(`${this.base}/market-data/time-series`, { params })
      .pipe(map(r => r.data));
  }
  getQuote(symbol: string): Observable<MarketData> {
    return this.http.get<ApiResponse<MarketData>>(`${this.base}/market-data/quote/${symbol}`)
      .pipe(map(r => r.data));
  }

  // ── Aggregation ──────────────────────────────────────────────────────────
  aggregate(req: AggregationRequest): Observable<AggregationResult> {
    return this.http.post<ApiResponse<AggregationResult>>(`${this.base}/aggregation`, req)
      .pipe(map(r => r.data));
  }
  getPivotData(req: PivotRequest): Observable<any> {
    return this.http.post<ApiResponse<any>>(`${this.base}/aggregation/pivot`, req)
      .pipe(map(r => r.data));
  }
  getTradesSummary(): Observable<any> {
    return this.http.get<ApiResponse<any>>(`${this.base}/aggregation/trades/summary`).pipe(map(r => r.data));
  }
  getMarketSummary(): Observable<any> {
    return this.http.get<ApiResponse<any>>(`${this.base}/aggregation/market/summary`).pipe(map(r => r.data));
  }

  // ── Export ───────────────────────────────────────────────────────────────
  exportTradesToExcel(): Observable<Blob> {
    return this.http.get(`${this.base}/export/trades/excel`, { responseType: 'blob' });
  }
  exportMarketDataToExcel(symbol?: string): Observable<Blob> {
    const params = symbol ? new HttpParams().set('symbol', symbol) : new HttpParams();
    return this.http.get(`${this.base}/export/market-data/excel`, { params, responseType: 'blob' });
  }
  exportCustom(body: any): Observable<Blob> {
    return this.http.post(`${this.base}/export`, body, { responseType: 'blob' });
  }
}

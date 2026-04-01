import { Injectable, OnDestroy } from '@angular/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LiveMarketData } from '../models';

export type ConnectionStatus = 'CONNECTED' | 'DISCONNECTED' | 'CONNECTING';

@Injectable({ providedIn: 'root' })
export class WebSocketService implements OnDestroy {
  private client!: Client;
  private subscriptions = new Map<string, StompSubscription>();
  private messageSubjects = new Map<string, Subject<LiveMarketData>>();

  connectionStatus$ = new BehaviorSubject<ConnectionStatus>('DISCONNECTED');
  allPrices$ = new Subject<LiveMarketData>();

  constructor() {
    this.initClient();
  }

  private initClient(): void {
    this.client = new Client({
      webSocketFactory: () => new SockJS(environment.wsUrl),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        this.connectionStatus$.next('CONNECTED');
        // Subscribe to live data topic
        this.client.subscribe('/topic/live-data', (msg) => {
          try {
            const data: LiveMarketData = JSON.parse(msg.body);
            this.allPrices$.next(data);
            // Forward to symbol-specific subjects
            const sub = this.messageSubjects.get(data.symbol);
            if (sub) sub.next(data);
          } catch (e) { console.error('WS parse error', e); }
        });
      },
      onDisconnect: () => this.connectionStatus$.next('DISCONNECTED'),
      onStompError: (frame) => {
        console.error('STOMP error', frame);
        this.connectionStatus$.next('DISCONNECTED');
      }
    });
  }

  connect(): void {
    if (!this.client.active) {
      this.connectionStatus$.next('CONNECTING');
      this.client.activate();
    }
  }

  disconnect(): void {
    this.client.deactivate();
  }

  subscribeSymbol(symbol: string): Observable<LiveMarketData> {
    if (!this.messageSubjects.has(symbol)) {
      this.messageSubjects.set(symbol, new Subject<LiveMarketData>());
    }
    // Send subscribe request to backend
    if (this.client.connected) {
      this.client.publish({
        destination: '/app/subscribe',
        body: JSON.stringify({ symbols: [symbol], action: 'subscribe' })
      });
    }
    return this.messageSubjects.get(symbol)!.asObservable();
  }

  unsubscribeSymbol(symbol: string): void {
    if (this.client.connected) {
      this.client.publish({
        destination: '/app/subscribe',
        body: JSON.stringify({ symbols: [symbol], action: 'unsubscribe' })
      });
    }
    this.messageSubjects.get(symbol)?.complete();
    this.messageSubjects.delete(symbol);
  }

  subscribeMany(symbols: string[]): void {
    if (this.client.connected) {
      this.client.publish({
        destination: '/app/subscribe',
        body: JSON.stringify({ symbols, action: 'subscribe' })
      });
    }
  }

  get isConnected(): boolean {
    return this.client.connected;
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}

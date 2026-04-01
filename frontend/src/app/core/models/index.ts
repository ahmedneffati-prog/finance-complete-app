// trade.model.ts
export interface Trade {
  id?: number;
  symbol: string;
  tradeType: 'BUY' | 'SELL';
  quantity: number;
  price: number;
  totalValue?: number;
  tradeDate: string;
  createdAt?: string;
  brokerId?: number;
  brokerName?: string;
  stockId?: number;
  stockName?: string;
}

// stock.model.ts
export interface Stock {
  id?: number;
  symbol: string;
  name: string;
  sector?: string;
  exchange?: string;
  currency?: string;
  marketCap?: number;
  isActive?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// broker.model.ts
export interface Broker {
  id?: number;
  name: string;
  country?: string;
  website?: string;
  isActive?: boolean;
  createdAt?: string;
}

// market-data.model.ts
export interface MarketData {
  id?: number;
  symbol: string;
  open?: number;
  high?: number;
  low?: number;
  close?: number;
  adjustedClose?: number;
  volume?: number;
  changeValue?: number;
  changePercent?: number;
  previousClose?: number;
  interval?: string;
  timestamp: string;
}

// live-market-data.model.ts
export interface LiveMarketData {
  symbol: string;
  price: number;
  change?: number;
  changePercent?: number;
  volume?: number;
  open?: number;
  high?: number;
  low?: number;
  previousClose?: number;
  timestamp: string;
  exchange?: string;
  currency?: string;
}

// aggregation.model.ts
export interface MeasureConfig {
  field: string;
  function: 'SUM' | 'AVG' | 'MAX' | 'MIN' | 'COUNT';
  alias?: string;
}

export interface AggregationRequest {
  entity: string;
  dimensions: string[];
  measures: MeasureConfig[];
  filters?: Record<string, any>;
  sortBy?: string[];
  sortDirection?: 'ASC' | 'DESC';
  limit?: number;
  startDate?: string;
  endDate?: string;
}

export interface AggregationResult {
  dimensions: string[];
  measures: string[];
  rows: Record<string, any>[];
  totals: Record<string, any>;
  totalRows: number;
}

export interface PivotRequest {
  entity: string;
  rows: string[];
  columns: string[];
  values: MeasureConfig[];
  filters?: Record<string, any>;
  startDate?: string;
  endDate?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp?: string;
  totalCount?: number;
}

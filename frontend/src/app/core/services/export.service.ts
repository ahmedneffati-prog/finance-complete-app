import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { saveAs } from 'file-saver';

@Injectable({ providedIn: 'root' })
export class ExportService {
  constructor(private api: ApiService) {}

  exportTrades(): void {
    this.api.exportTradesToExcel().subscribe(blob => {
      saveAs(blob, `trades_${this.timestamp()}.xlsx`);
    });
  }

  exportMarketData(symbol?: string): void {
    this.api.exportMarketDataToExcel(symbol).subscribe(blob => {
      saveAs(blob, `market_data_${symbol || 'all'}_${this.timestamp()}.xlsx`);
    });
  }

  exportCustom(entity: string, format: 'EXCEL' | 'PDF'): void {
    this.api.exportCustom({ entity, format }).subscribe(blob => {
      const ext = format === 'PDF' ? 'pdf' : 'xlsx';
      saveAs(blob, `${entity}_${this.timestamp()}.${ext}`);
    });
  }

  private timestamp(): string {
    return new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
  }
}

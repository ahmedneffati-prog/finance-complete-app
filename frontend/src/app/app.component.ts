import { Component, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { WebSocketService, ConnectionStatus } from './core/services/websocket.service';

interface NavItem {
  path: string;
  icon: string;
  label: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule, RouterOutlet, RouterLink, RouterLinkActive,
    MatSidenavModule, MatToolbarModule, MatIconModule, MatButtonModule, MatTooltipModule
  ],
  template: `
    <div class="app-shell">
      <!-- Sidebar -->
      <aside class="sidebar" [class.collapsed]="sidebarCollapsed">
        <div class="sidebar-header">
          <div class="logo">
            <span class="logo-icon">📈</span>
            <span class="logo-text" *ngIf="!sidebarCollapsed">FinanceBI</span>
          </div>
          <button class="collapse-btn" (click)="sidebarCollapsed = !sidebarCollapsed">
            <span class="material-icons">{{ sidebarCollapsed ? 'chevron_right' : 'chevron_left' }}</span>
          </button>
        </div>

        <nav class="sidebar-nav">
          <a *ngFor="let item of navItems"
             [routerLink]="item.path"
             routerLinkActive="active"
             class="nav-item"
             [matTooltip]="sidebarCollapsed ? item.label : ''"
             matTooltipPosition="right">
            <span class="material-icons">{{ item.icon }}</span>
            <span class="nav-label" *ngIf="!sidebarCollapsed">{{ item.label }}</span>
          </a>
        </nav>

        <div class="sidebar-footer" *ngIf="!sidebarCollapsed">
          <div class="ws-status" [class]="'status-' + (wsStatus$ | async)?.toLowerCase()">
            <span class="status-dot"></span>
            <span class="status-text">
              {{ (wsStatus$ | async) === 'CONNECTED' ? 'Live Connected' : 
                 (wsStatus$ | async) === 'CONNECTING' ? 'Connecting...' : 'Offline' }}
            </span>
          </div>
        </div>
      </aside>

      <!-- Main content -->
      <main class="main-content">
        <header class="top-bar">
          <div class="top-bar-left">
            <h2 class="page-title">Finance Analytics Platform</h2>
          </div>
          <div class="top-bar-right">
            <button mat-icon-button (click)="reconnectWs()" [matTooltip]="'Reconnect WebSocket'">
              <span class="material-icons">wifi</span>
            </button>
          </div>
        </header>

        <div class="content-area">
          <router-outlet></router-outlet>
        </div>
      </main>
    </div>
  `,
  styles: [`
    .app-shell {
      display: flex;
      height: 100vh;
      overflow: hidden;
    }

    /* ── Sidebar ── */
    .sidebar {
      width: 240px;
      background: #1e1b4b;
      color: white;
      display: flex;
      flex-direction: column;
      transition: width .25s ease;
      flex-shrink: 0;
      z-index: 100;

      &.collapsed { width: 64px; }
    }

    .sidebar-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20px 16px;
      border-bottom: 1px solid rgba(255,255,255,.1);
    }

    .logo {
      display: flex;
      align-items: center;
      gap: 10px;
      overflow: hidden;
    }

    .logo-icon { font-size: 1.5rem; }
    .logo-text { font-size: 1.1rem; font-weight: 700; color: #a5b4fc; white-space: nowrap; }

    .collapse-btn {
      background: none;
      border: none;
      color: rgba(255,255,255,.6);
      cursor: pointer;
      padding: 4px;
      border-radius: 6px;
      display: flex;
      align-items: center;
      &:hover { background: rgba(255,255,255,.1); color: white; }
    }

    /* ── Nav ── */
    .sidebar-nav {
      flex: 1;
      padding: 16px 8px;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .nav-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 12px;
      border-radius: 8px;
      color: rgba(255,255,255,.65);
      text-decoration: none;
      transition: all .15s;
      white-space: nowrap;
      overflow: hidden;

      .material-icons { font-size: 20px; flex-shrink: 0; }
      .nav-label { font-size: 0.875rem; font-weight: 500; }

      &:hover { background: rgba(255,255,255,.08); color: white; }
      &.active { background: #4338ca; color: white; }
    }

    /* ── Footer ── */
    .sidebar-footer {
      padding: 16px;
      border-top: 1px solid rgba(255,255,255,.1);
    }

    .ws-status {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 0.75rem;
      color: rgba(255,255,255,.6);

      .status-dot {
        width: 8px; height: 8px;
        border-radius: 50%;
        background: #94a3b8;
      }

      &.status-connected {
        color: #6ee7b7;
        .status-dot { background: #10b981; box-shadow: 0 0 6px #10b981; }
      }
      &.status-connecting .status-dot { background: #fbbf24; animation: pulse 1s infinite; }
      &.status-disconnected .status-dot { background: #f87171; }
    }

    @keyframes pulse {
      0%, 100% { opacity: 1; }
      50% { opacity: .4; }
    }

    /* ── Main ── */
    .main-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }

    .top-bar {
      background: white;
      border-bottom: 1px solid #e2e8f0;
      padding: 0 24px;
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-shrink: 0;
      box-shadow: 0 1px 3px rgba(0,0,0,.06);
    }

    .page-title {
      font-size: 1rem;
      font-weight: 600;
      color: #1e1b4b;
    }

    .content-area {
      flex: 1;
      overflow-y: auto;
      background: #f0f2f5;
    }
  `]
})
export class AppComponent implements OnInit {
  title = 'finance-frontend';
  sidebarCollapsed = false;

  navItems: NavItem[] = [
    { path: '/dashboard',   icon: 'dashboard',   label: 'Dashboard'     },
    { path: '/pivot',       icon: 'table_chart',  label: 'Pivot Table'   },
    { path: '/charts',      icon: 'bar_chart',    label: 'Charts'        },
    { path: '/live-market', icon: 'show_chart',   label: 'Live Market'   },
    { path: '/exports',     icon: 'download',     label: 'Export'        },
  ];

  wsStatus$ = this.wsService.connectionStatus$;

  constructor(private wsService: WebSocketService) {}

  ngOnInit(): void {
    this.wsService.connect();
  }

  reconnectWs(): void {
    this.wsService.disconnect();
    setTimeout(() => this.wsService.connect(), 500);
  }
}

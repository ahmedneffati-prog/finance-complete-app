# Finance Backend - Spring Boot

## Description
Backend REST API for the Finance Data Visualization application.
Provides real-time market data, trade management, aggregation, and export features.

## Prerequisites
- Java 17+
- PostgreSQL 15 (database: `finance_db`)
- Redis (for caching)
- Twelve Data API Key (for real-time market data)

## Configuration
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/finance_db
spring.datasource.username=postgres
spring.datasource.password=your_password
twelve.data.api-key=YOUR_KEY_HERE
```

## Build & Run
```bash
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```

## API Endpoints
| Method | URL | Description |
|--------|-----|-------------|
| GET | /api/brokers | List all brokers |
| POST | /api/brokers | Create broker |
| GET | /api/stocks | List all stocks |
| GET | /api/stocks/symbol/{symbol} | Get stock by symbol |
| GET | /api/trades | List all trades |
| POST | /api/trades | Create trade |
| GET | /api/market-data/time-series?symbol=AAPL&interval=1day | Historical data |
| GET | /api/market-data/quote/{symbol} | Latest quote |
| POST | /api/aggregation | Custom aggregation |
| POST | /api/aggregation/pivot | Pivot table data |
| GET | /api/aggregation/trades/summary | Trade summary |
| POST | /api/export | Export data (Excel/PDF) |
| GET | /api/export/trades/excel | Export trades to Excel |
| GET | /api/ws/status | WebSocket connection status |

## WebSocket
Connect via SockJS to `/ws`, use STOMP protocol.
- Subscribe: `/topic/live-data` for all price updates
- Subscribe: `/topic/market/{SYMBOL}` for specific symbol
- Send to: `/app/subscribe` with `{ "symbols": ["AAPL"], "action": "subscribe" }`

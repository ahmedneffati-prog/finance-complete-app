-- ============================================================
-- Sample Data for Finance Analytics Platform
-- Run this AFTER the application has started once
-- (Hibernate will create the tables automatically)
-- ============================================================

-- Brokers
INSERT INTO brokers (name, country, website, is_active, created_at) VALUES
('Goldman Sachs', 'USA', 'https://www.goldmansachs.com', true, NOW()),
('Morgan Stanley', 'USA', 'https://www.morganstanley.com', true, NOW()),
('JP Morgan', 'USA', 'https://www.jpmorgan.com', true, NOW()),
('Deutsche Bank', 'Germany', 'https://www.db.com', true, NOW()),
('HSBC', 'UK', 'https://www.hsbc.com', true, NOW())
ON CONFLICT DO NOTHING;

-- Stocks
INSERT INTO stocks (symbol, name, sector, exchange, currency, market_cap, is_active, created_at, updated_at) VALUES
('AAPL',  'Apple Inc.',                'Technology',    'NASDAQ', 'USD', 3000000000000.00, true, NOW(), NOW()),
('MSFT',  'Microsoft Corporation',     'Technology',    'NASDAQ', 'USD', 2800000000000.00, true, NOW(), NOW()),
('GOOGL', 'Alphabet Inc.',             'Technology',    'NASDAQ', 'USD', 1700000000000.00, true, NOW(), NOW()),
('AMZN',  'Amazon.com Inc.',           'Consumer',      'NASDAQ', 'USD', 1600000000000.00, true, NOW(), NOW()),
('TSLA',  'Tesla Inc.',                'Automotive',    'NASDAQ', 'USD',  700000000000.00, true, NOW(), NOW()),
('META',  'Meta Platforms Inc.',       'Technology',    'NASDAQ', 'USD',  900000000000.00, true, NOW(), NOW()),
('NVDA',  'NVIDIA Corporation',        'Technology',    'NASDAQ', 'USD', 2200000000000.00, true, NOW(), NOW()),
('JPM',   'JPMorgan Chase & Co.',      'Finance',       'NYSE',   'USD',  470000000000.00, true, NOW(), NOW()),
('JNJ',   'Johnson & Johnson',         'Healthcare',    'NYSE',   'USD',  380000000000.00, true, NOW(), NOW()),
('XOM',   'Exxon Mobil Corporation',   'Energy',        'NYSE',   'USD',  420000000000.00, true, NOW(), NOW())
ON CONFLICT (symbol) DO NOTHING;

-- Trades (sample)
DO $$
DECLARE
  broker_id1 BIGINT := (SELECT id FROM brokers WHERE name='Goldman Sachs' LIMIT 1);
  broker_id2 BIGINT := (SELECT id FROM brokers WHERE name='Morgan Stanley' LIMIT 1);
  broker_id3 BIGINT := (SELECT id FROM brokers WHERE name='JP Morgan' LIMIT 1);
  stock_aapl BIGINT := (SELECT id FROM stocks WHERE symbol='AAPL' LIMIT 1);
  stock_msft BIGINT := (SELECT id FROM stocks WHERE symbol='MSFT' LIMIT 1);
  stock_googl BIGINT := (SELECT id FROM stocks WHERE symbol='GOOGL' LIMIT 1);
  stock_tsla BIGINT := (SELECT id FROM stocks WHERE symbol='TSLA' LIMIT 1);
  stock_nvda BIGINT := (SELECT id FROM stocks WHERE symbol='NVDA' LIMIT 1);
BEGIN
  -- Recent trades
  INSERT INTO trades (symbol, trade_type, quantity, price, total_value, trade_date, broker_id, stock_id, created_at) VALUES
  ('AAPL',  'BUY',  100, 178.50,  17850.00,  NOW() - INTERVAL '1 day',  broker_id1, stock_aapl,  NOW()),
  ('MSFT',  'BUY',   50, 415.20,  20760.00,  NOW() - INTERVAL '2 days', broker_id1, stock_msft,  NOW()),
  ('GOOGL', 'BUY',   30, 165.80,   4974.00,  NOW() - INTERVAL '3 days', broker_id2, stock_googl, NOW()),
  ('TSLA',  'SELL',  75, 189.30,  14197.50,  NOW() - INTERVAL '4 days', broker_id2, stock_tsla,  NOW()),
  ('NVDA',  'BUY',   40, 875.40,  35016.00,  NOW() - INTERVAL '5 days', broker_id3, stock_nvda,  NOW()),
  ('AAPL',  'SELL',  50, 182.00,   9100.00,  NOW() - INTERVAL '6 days', broker_id1, stock_aapl,  NOW()),
  ('MSFT',  'BUY',   80, 408.50,  32680.00,  NOW() - INTERVAL '7 days', broker_id2, stock_msft,  NOW()),
  ('TSLA',  'BUY',  120, 195.40,  23448.00,  NOW() - INTERVAL '8 days', broker_id3, stock_tsla,  NOW()),
  ('NVDA',  'SELL',  25, 900.00,  22500.00,  NOW() - INTERVAL '9 days', broker_id1, stock_nvda,  NOW()),
  ('GOOGL', 'BUY',   60, 170.20,  10212.00,  NOW() - INTERVAL '10 days',broker_id3, stock_googl, NOW()),
  ('AAPL',  'BUY',  200, 175.00,  35000.00,  NOW() - INTERVAL '15 days',broker_id2, stock_aapl,  NOW()),
  ('MSFT',  'SELL', 100, 420.00,  42000.00,  NOW() - INTERVAL '20 days',broker_id1, stock_msft,  NOW()),
  ('TSLA',  'BUY',   90, 210.00,  18900.00,  NOW() - INTERVAL '25 days',broker_id3, stock_tsla,  NOW()),
  ('NVDA',  'BUY',   60, 800.00,  48000.00,  NOW() - INTERVAL '30 days',broker_id2, stock_nvda,  NOW()),
  ('AAPL',  'SELL', 150, 180.00,  27000.00,  NOW() - INTERVAL '35 days',broker_id1, stock_aapl,  NOW());
END $$;

# Exchange Simulator Client

## Overview

A client-only Next.js 16 demo crypto trading platform. Connects to a Spring Boot backend at `localhost:8080` via HTTP-only cookie session auth.

## Tech Stack

- **Next.js 16** — App Router, TypeScript
- **pnpm** — package manager
- **Tailwind CSS 4** + **shadcn/ui** — styling & components
- **React Query** (`@tanstack/react-query`) — server state management
- **orval** — generates typed React Query hooks from `openapi.yml`
- **lightweight-charts** — candlestick charts (TradingView open-source lib)
- **Binance WebSocket** — live crypto prices app-wide

## Pages

| Route | Description |
| --- | --- |
| `/login` | Login form (username + password) |
| `/register` | Registration form (username, email, password). Role is always `USER`. |
| `/dashboard` | User info (funds), portfolio positions table with live prices |
| `/trade/[token]` | Trading page — chart, order form, order book, order history |

## Architecture

### Authentication

- Backend sets an HTTP-only cookie on login. The frontend simply calls the auth endpoints and the browser attaches the cookie automatically.
- `credentials: "include"` on all fetch requests.
- No token storage, no `Authorization` header.
- `userId` is not sent in request bodies — the backend extracts it from the session.

### API Client (orval — generated, no raw fetch)

- `openapi.yml` defines the full API contract.
- `pnpm generate-api` runs orval to produce typed React Query hooks in `src/api/generated.ts`.
- **All API calls use the generated hooks** — e.g. `useGetPortfolio()`, `useSell()`, `useAuthUser()`. No manual `fetch` calls anywhere.
- orval is configured with a custom axios (or fetch) instance that sets `baseURL: "http://localhost:8080"` and `credentials: "include"` / `withCredentials: true` so the session cookie is sent automatically.
- Components simply call the generated hooks and get typed data, loading states, and mutations out of the box.

### Live Prices (Binance WebSocket)

- A React context (`PriceProvider`) maintains WebSocket connections to Binance for all supported tokens.
- Provides a `prices` map (`Record<string, number>`) accessible anywhere in the app.
- Dashboard uses live prices to show real-time portfolio value / P&L.
- Trading page uses the live price for the selected token.

### Supported Tokens (hardcoded)

| Token | Binance Symbol |
| --- | --- |
| BTC | btcusdt |
| ETH | ethusdt |
| SOL | solusdt |
| XRP | xrpusdt |
| DOGE | dogeusdt |
| ADA | adausdt |
| BNB | bnbusdt |
| TRX | trxusdt |
| LINK | linkusdt |
| LTC | ltcusdt |

This list lives in a single config file and is easy to extend.

### Charts

- **lightweight-charts** (by TradingView) renders candlestick charts on the trading page.
- Historical candle data is fetched from the Binance public REST API (`/api/v3/klines`).
- The chart updates in real-time via the Binance WebSocket price feed.

## Key Directories

```
src/
├── api/              # orval-generated React Query hooks & types
├── app/
│   ├── login/        # login page
│   ├── register/     # registration page
│   ├── dashboard/    # dashboard page
│   └── trade/[token] # trading page
├── components/       # shared UI components (shadcn/ui based)
├── providers/        # QueryProvider, PriceProvider (Binance WS)
├── lib/              # utils, constants (token list, API base URL)
└── hooks/            # custom hooks (usePrice, useAuth, etc.)
```

## API Endpoints (from openapi.yml)

### Auth (`auth-controller`)

- `POST /api/auth/registration` — register (username, email, password)
- `POST /api/auth/login` — login (username, password) → sets session cookie
- `POST /api/auth/logout` — logout

### Users (`user-controller`)

- `GET /api/users` — list all users
- `GET /api/users/{id}` — get user by ID (includes `funds` balance)

### Orders — General (`order-controller`)

- `GET /api/users-orders` — all orders for current user
- `GET /api/users-orders/sell` — all sell orders
- `GET /api/users-orders/buy` — all buy orders

### Orders — Market (`market-order-controller`)

- `POST /api/users-orders/market/buy` — place market buy
- `POST /api/users-orders/market/sell` — place market sell
- `GET /api/users-orders/market` — all market orders
- `GET /api/users-orders/market/buy` — market buy orders
- `GET /api/users-orders/market/sell` — market sell orders

### Orders — Limit (`limit-order-controller`)

- `POST /api/users-orders/limit/buy` — place limit buy
- `POST /api/users-orders/limit/sell` — place limit sell
- `DELETE /api/users-orders/limit/{orderId}` — cancel limit order
- `GET /api/users-orders/limit` — all limit orders
- `GET /api/users-orders/limit/buy` — limit buy orders
- `GET /api/users-orders/limit/sell` — limit sell orders
- `GET /api/users-orders/book/{token}/sell` — sell side order book
- `GET /api/users-orders/book/{token}/buy` — buy side order book

### Positions (`spot-position-controller`)

- `GET /api/users-positions` — current user's portfolio (token, qty, avg price, value)

## Data Models

### OrderRequestDto (input)

```
{ token: string, limit?: number, quantity: number }
```

`limit` is only used for limit orders. `userId` is omitted (backend extracts from session).

### OrderResponseDto (output)

```
{ userId, orderId, createdAt, token, quantity, entry, orderValue,
  transactionType: "BUY" | "SELL", orderType: "LIMIT" | "MARKET", closedAt }
```

### UserResponseDto

```
{ id, updatedAt, createdAt, username, email, funds }
```

### SpotPositionResponseDto

```
{ positionId, token, quantity, avgBuyPrice, positionValue, timestamp }
```

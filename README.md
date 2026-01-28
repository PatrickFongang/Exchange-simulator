# Exchange Simulator

**Exchange Simulator** is a backend application built with **Java** and **Spring Boot** that simulates a cryptocurrency exchange. It allows users to register, manage their funds, place market and limit orders, and track their spot positions in real-time.

The application integrates with **Binance** via WebSocket to fetch real-time cryptocurrency price data, ensuring that the simulation reflects live market conditions.

## 🚀 Key Features

* **User Management & Security:**
* Secure user registration and authentication.
* Role-based access control.


* **Real-Time Market Data:**
* Fetches live cryptocurrency prices directly from the **Binance API** using WebSockets (`CryptoWebSocketService`).


* **Trading Engine:**
* **Market Orders:** Buy or sell assets immediately at the current market price.
* **Limit Orders:** Place orders to buy or sell at a specific price. These are added to the order book and executed when the market price matches the limit.
* **Order Book Management:** View active Buy and Sell order books for specific tokens.


* **Portfolio Management:**
* Track current spot positions, average buy price, and current position value.
* View detailed order history (Open, Closed, Cancelled).



## 🛠️ Tech Stack

* **Language:** Java
* **Framework:** Spring Boot 3
* **Build Tool:** Maven
* **Database:** H2 (Embedded) / SQL compatible
* **Real-time Data:** WebSocket (Binance Stream)
* **Documentation:** OpenAPI 3.1.0 / Swagger UI

## 📂 Project Structure

The project follows a standard layered architecture:

* `controller`: REST Controllers defining the API endpoints (e.g., `MarketOrderController`, `LimitOrderController`).
* `service`: Business logic for order matching, user management, and crypto data fetching.
* `repository`: Data access layer.
* `entity`: Database entities (`User`, `Order`, `SpotPosition`).
* `dto`: Data Transfer Objects for API requests and responses.
* `config`: Configuration for Security, OpenAPI, and WebSockets.

## ⚙️ Installation & Setup

### Prerequisites

* Java 17 or higher
* Maven

### Steps to Run

1. **Clone the repository:**
```bash
git clone https://github.com/your-username/exchange-simulator.git
cd exchange-simulator

```


2. **Build the project:**
```bash
./mvnw clean install

```


3. **Run the application:**
```bash
./mvnw spring-boot:run

```


4. The server will start on `http://localhost:8080`.

## 📖 API Documentation

The project includes a fully documented OpenAPI specification. Once the application is running, you can access the Swagger UI to test endpoints interactively.

**Swagger UI:** `http://localhost:8080/swagger-ui.html` (Default SpringDoc path)

### Core Endpoints

#### Authentication

* `POST /auth/registration`: Create a new user account.
* `POST /auth/login`: Authenticate and receive a session/token.
* `POST /auth/logout`: Log out the current user.

#### Market Orders

* `POST /api/users-orders/market/buy`: Execute a market buy order.
* `POST /api/users-orders/market/sell`: Execute a market sell order.

#### Limit Orders

* `POST /api/users-orders/limit/buy`: Place a limit buy order.
* `POST /api/users-orders/limit/sell`: Place a limit sell order.
* `DELETE /api/users-orders/limit/{orderId}`: Cancel a pending limit order.

#### Order Book & History

* `GET /api/users-orders/book/{token}/buy`: View the buy side of the order book.
* `GET /api/users-orders/book/{token}/sell`: View the sell side of the order book.
* `GET /api/users-orders`: Get all user orders.

#### User & Portfolio

* `GET /api/users-positions`: View current portfolio and spot positions.
* `GET /api/users/{id}`: Get user details and available funds.

## 🔄 Binance Integration

The application uses the `CryptoWebSocketService` to connect to Binance's public WebSocket streams. It listens for `MarkPriceStreamEvent` to update internal asset prices in real-time, which triggers the matching engine to check if Limit Orders should be filled.

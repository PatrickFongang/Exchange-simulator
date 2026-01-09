## **1. User Management**

Control user creation and profile retrieval.

### **Create New User**

* **Method:** `POST`
* **URL:** `http://localhost:8080/api/users`
* **Body (JSON):**

```json
{
    "username": "crypto_trader_99",
    "email": "trader@example.com"
}

```

### **List All Users**

* **Method:** `GET`
* **URL:** `http://localhost:8080/api/users`

### **Get Specific User**

* **Method:** `GET`
* **URL:** `http://localhost:8080/api/users/1`

---

## **2. Market Orders (Trading)**

Execute buy and sell orders.

### **Buy Crypto (Market Order)**

* **Method:** `POST`
* **URL:** `http://localhost:8080/api/users-orders/1/buy`
* **Body (JSON):**

```json
{
    "token": "BTC",
    "quantity": 0.05
}

```

### **Sell Crypto (Market Order)**

* **Method:** `POST`
* **URL:** `http://localhost:8080/api/users-orders/1/sell`
* **Body (JSON):**

```json
{
    "token": "BTC",
    "quantity": 0.02
}

```

### **View Order History**

* **Method:** `GET`
* **URL:** `http://localhost:8080/api/users-orders/1`

### **View Buy History Only**

* **Method:** `GET`
* **URL:** `http://localhost:8080/api/users-orders/1/buy`

### **View Sell History Only**

* **Method:** `GET`
* **URL:** `http://localhost:8080/api/users-orders/1/sell`

---

## **3. Portfolio & Positions**

Check asset holdings and average entry prices.

### **Get User Portfolio**

* **Method:** `GET`
* **URL:** `http://localhost:8080/api/users-positions/1`

---

## **API Endpoint Overview**

| Controller | Resource | Full URL | Method |
| --- | --- | --- | --- |
| **User** | Users | `http://localhost:8080/api/users` | `GET`/`POST` |
| **Orders** | All Orders | `http://localhost:8080/api/users-orders/{userId}` | `GET` |
| **Orders** | Buying | `http://localhost:8080/api/users-orders/{userId}/buy` | `POST` |
| **Orders** | Selling | `http://localhost:8080/api/users-orders/{userId}/sell` | `POST` |
| **Portfolio** | Positions | `http://localhost:8080/api/users-positions/{userId}` | `GET` |

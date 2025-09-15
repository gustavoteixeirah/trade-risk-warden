# User Configuration

Objective:

Register User Account Configuration

Input payload:

```json
{
    "clientId": "user123",
    "apiKey": "k",
    "apiSecret": "s",
    "maxRisk": { "type": "PERCENTAGE", "value": 30 },
    "dailyRisk": { "type": "ABSOLUTE", "value": 5000 },
    "currency": "USD"
}
```

After the user registration, we need to fetch the initial balance via the Architect API. However, their API does not provide a REST or Websocket interface. Only SKDs for Python and Rust.

Therefore, I've implemented a simple proxy application in python using their SDK, based on the provided python code, that exposes and endpoint to fetch account information:

```bash
curl --location 'http://localhost:8000/initial-balance' \
--header 'api_key: <api_key>' \
--header 'api_secret: <api_secret>'
```

Response example from their SDK, serialized to JSON:
```json
{
    "account": "9751e779-6637-486b-bfbc-fb2b64fb4702",
    "balances": {
        "USD": 100000.0
    },
    "cash_excess": 76551.0,
    "equity": 100000.0,
    "position_margin": 23449.0,
    "positions": {
        "ES 20250919 CME Future/USD": [
            {
                "break_even_price": null,
                "cost_basis": 6588.0,
                "liquidation_price": null,
                "quantity": 1.0,
                "trade_time": "2025-09-12T20:23:41.964442+00:00",
                "unrealized_pnl": null
            }
        ]
    },
    "purchasing_power": 76551.0,
    "realized_pnl": 0.0,
    "timestamp": "2025-09-12T20:23:41.964447+00:00",
    "total_margin": 23449.0,
    "unrealized_pnl": null,
    "yesterday_equity": null
}
```


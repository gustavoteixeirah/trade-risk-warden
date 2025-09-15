# Actions on Breach

## Objective

Apply protective actions when a risk breach is detected, ensuring positions are flattened and the user state is updated consistently.

## What it does

- Listens to breach decision events produced by the risk evaluation flow.
- Performs a single protective action: `flattenAll` (cancel all + close all positions).
- Updates the user state to reflect the block (daily or permanent) and persists it.
- Emits an audit/notification event indicating the action applied.

## How it works, step by step

1) A breach decision is produced by the Risk Checks flow (see `3_risk_checks.md`) and an event is published:
   - `EventType.DAILY_RISK_TRIGGERED` or `EventType.MAX_RISK_TRIGGERED`.
2) The primary listener `BreachEnforcementListener` receives the event and invokes the use case:
   - `EnforceBreachInputPort.handle(clientId, decisionType, loss, limit, at)`.
3) The use case `EnforceBreachUseCase` loads the user by `clientId`.
4) Idempotency checks (short-circuit when appropriate):
   - If `DecisionType.NONE` → return.
   - If `DecisionType.MAX_BREACH` and user is already permanently blocked → return.
   - If `DecisionType.DAILY_BREACH` and user is already daily-blocked for the current local trading day (using `user.tz()`) → return.
5) Protective action (single call):
   - `TradingPort.flattenAll(user.apiKey(), user.apiSecret())`.
   - Implemented by the Architect Proxy client adapter, which calls the external Architect Proxy API.
6) Update user state and persist:
   - For daily breach: set `dailyBlocked = true` and `dailyBlockedAt = at`.
   - For max breach: set `permanentBlocked = true` and `permanentBlockedAt = at`.
   - `users.save(updated)`.
7) Emit audit/notification event (fan-out friendly):
   - `EventType.DAILY_RISK_BREACH_ACTION_APPLIED` or `EventType.MAX_RISK_BREACH_ACTION_APPLIED`.
   - `details` include: `loss`, `limit`, `currency`, `snapshotTs` (if available), and `action = "flattenAll"`.

## Actions by breach type

Daily breach (`DecisionType.DAILY_BREACH`)

When the daily loss limit is breached, the system immediately issues a single protective action: `flattenAll`.
This cancels all open orders and closes every position to stop further exposure. 
Right after that, the account is marked as daily blocked and the time of the block is recorded (`dailyBlockedAt = at`).
Finally, an audit event is published — `DAILY_RISK_BREACH_ACTION_APPLIED` — containing helpful context such as the loss, the limit,
the currency, the snapshot timestamp, and the fact that the action taken was `flattenAll`.

Max breach (`DecisionType.MAX_BREACH`)

When the overall (maximum) loss limit is breached, the same protective action is applied: `flattenAll`, ensuring 
everything is closed quickly and consistently. The account is then marked as permanently blocked and the block time 
is recorded (`permanentBlockedAt = at`). To make the action traceable, the system publishes `MAX_RISK_BREACH_ACTION_APPLIED`
with the same set of details (loss, limit, currency, snapshot timestamp, and `action = flattenAll`).

## Events summary

- Consumed (to start enforcement):
  - `EventType.DAILY_RISK_TRIGGERED`
  - `EventType.MAX_RISK_TRIGGERED`

- Emitted (after applying actions):
  - `EventType.DAILY_RISK_BREACH_ACTION_APPLIED`
  - `EventType.MAX_RISK_BREACH_ACTION_APPLIED`


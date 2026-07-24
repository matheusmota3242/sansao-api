# 3D Printing Purchases (Compras de insumos 3D)

CRUD for tracking supply purchases for 3D printing, driven entirely from a
dedicated WhatsApp **group**. Unlike the other bot features (tasks, notes,
automations) — which run in the owner's direct chat — the purchases feature is
scoped to a single group chat and replies there.

## Data model

Table `purchase` (Flyway migration `V5__create_purchase_table.sql`):

| Column         | Type            | Notes                                    |
|----------------|-----------------|------------------------------------------|
| `id`           | BIGSERIAL PK    | Auto-generated                           |
| `description`  | TEXT NOT NULL   | What was bought                          |
| `amount`       | INTEGER NOT NULL| Quantity (whole units)                   |
| `unit_price`   | NUMERIC(12,2)   | Price per unit                           |
| `source`       | TEXT            | Store / supplier                         |
| `observations` | TEXT            | Optional free-text notes                 |
| `created_at`   | TIMESTAMP       | Serves as the "creation date"            |
| `updated_at`   | TIMESTAMP       | Set on creation                          |
| `active`       | BOOLEAN         | Defaults to TRUE                         |

`created_at` is used as the requested `creation_date` (it is set when the record
is first saved and shown in listings).

## Commands

The bot only reacts to messages **sent by the owner account** (WAHA
`fromMe = true`) that start with `@` or `#`. In the purchases group the
following commands are available:

| Command        | Action                                           |
|----------------|--------------------------------------------------|
| `@menu`        | Show the purchases menu                           |
| `@cbuy`        | Register a new purchase (guided interaction)       |
| `@lbuy`        | List all purchases                                 |
| `@ubuy <id>`   | Update a purchase (pick a field, enter new value)  |
| `@dbuy <id>`   | Remove a purchase                                  |
| `@cancel`      | Cancel the interaction in progress                 |

### `@cbuy` — create

A step-by-step interaction collects: description → quantity → unit price →
source → observations. Send `-` to skip observations. Unit price accepts both
Brazilian (`89,90`) and dot (`89.90`) decimal formats, with an optional `R$`.

### `@ubuy <id>` — update

Starts a guided interaction that loads the current record, asks which field to
change (1–5), then asks for the new value. Only the selected field is changed;
all other fields are preserved.

## Group scoping & message routing

The target group is configured, **not hardcoded**, via a Spring property:

```yaml
application:
  purchase-group-id: ${PURCHASE_GROUP_ID}
```

`PURCHASE_GROUP_ID` is provided through the (git-ignored) `.app_env` file and
holds the group's WAHA chat id (the `...@g.us` value). It is intentionally kept
out of source control.

Routing works as follows (`WhatsappBotService`):

1. The reply is sent back to the chat the message came from
   (`payload.to`), falling back to the owner's direct chat when absent.
2. If the originating chat id equals `purchase-group-id`, **only** the purchase
   commands (and the purchases menu) are evaluated.
3. Otherwise the existing owner commands (tasks, notes, automations, main menu)
   are evaluated as before.

This keeps the purchases group focused on purchases and leaves the owner's
direct-chat features untouched.

## Architecture (follows the existing interaction pattern)

- `model/Purchase` — JPA entity extending `BaseModel`.
- `dto/PurchaseDTO` — carries collected data through an interaction.
- `dto/chat/PurchaseChatResponse` — `ChatResponse` implementation; a non-null
  `updateId` distinguishes an update from a create at completion time.
- `repository/PurchaseRepository` — `findAllByActiveTrueOrderByCreatedAtDesc`.
- `model/chat/purchase/CreatePurchaseInteraction` and
  `UpdatePurchaseInteraction` — the state machines, registered as Jackson
  subtypes in `Interaction` so they serialize to `chat_record.interaction`.
- `service/PurchaseService` — persistence + command handlers
  (`createInteractionIf`, `listIf`, `updateInteractionIf`, `deleteIf`).
- `util/PurchaseInputUtil` — shared quantity/price parsing.

## Known limitation

`ChatRecord` state is global (not scoped per chat). For this single-owner bot a
purchase interaction started in the group and an interaction started in the
direct chat are not expected to overlap; there is no per-chat isolation of an
in-progress interaction.

## Tests

Plain JUnit 5 unit tests (no Spring context), mirroring the package layout:

- `CreatePurchaseInteractionTest`
- `UpdatePurchaseInteractionTest`

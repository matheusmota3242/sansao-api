# Print Orders & Queue (Pedidos e fila de impressão)

CRUD for customer orders, doubling as the informational printing queue: what is
running, what is next, what is already done. Like the purchases feature, it is
scoped to the purchases **group** chat and replies there.

## Data model

Flyway migration `V8__create_customer_and_print_order.sql`.

Table `customer`:

| Column       | Type          | Notes                       |
|--------------|---------------|-----------------------------|
| `id`         | BIGSERIAL PK  | Auto-generated              |
| `name`       | TEXT NOT NULL | Matched ignoring case       |
| `created_at` | TIMESTAMP     | From `BaseModel`            |
| `updated_at` | TIMESTAMP     | From `BaseModel`            |
| `active`     | BOOLEAN       | Soft-delete flag            |

Table `print_order` — named that way because `ORDER` is a reserved SQL keyword,
so a table literally called `order` would need quoting in every statement
Hibernate generates:

| Column               | Type             | Notes                                              |
|----------------------|------------------|----------------------------------------------------|
| `id`                 | BIGSERIAL PK     | Auto-generated                                     |
| `description`        | TEXT NOT NULL    | What is being printed                              |
| `customer_id`        | BIGINT NOT NULL  | FK to `customer(id)`                               |
| `print_time_minutes` | INTEGER          | Optional; stored as whole minutes                  |
| `priority`           | INTEGER          | Queue position, 1..n contiguous; NULL once closed  |
| `status`             | VARCHAR(20)      | `WAITING` / `RUNNING` / `COMPLETED` / `CANCELLED`  |
| `production_cost`    | NUMERIC(12,2)    | Optional                                           |
| `sale_price`         | NUMERIC(12,2)    | Optional                                           |
| `started_at`         | TIMESTAMP        | Stamped when status becomes `RUNNING`              |
| `observations`       | TEXT             | Optional free-text notes                           |
| `created_at`         | TIMESTAMP        | From `BaseModel`                                   |
| `updated_at`         | TIMESTAMP        | From `BaseModel`                                   |
| `active`             | BOOLEAN          | Defaults to TRUE                                   |

`status` is persisted with `@Enumerated(EnumType.STRING)` rather than the
ordinal, so inserting a value into the middle of `OrderStatus` later cannot
reinterpret rows already stored. This is the first enum column in the project.

## Queue semantics

`priority` is a **sequential position**, not a weight: the orders still in the
queue (`WAITING` or `RUNNING`) always occupy positions `1..n` with no gaps. The
queue is informational — it shows what is being printed and what is next; it is
not a work-dispatching mechanism.

- A new order joins the **back** of the queue.
- Moving an order shifts the others and renumbers the whole queue.
- Leaving the queue (`COMPLETED` / `CANCELLED`) sets `priority` to NULL and
  renumbers what is left, so finished work does not hold a position.
- Coming back to `WAITING` rejoins at the back.
- `RUNNING` stamps `started_at` if it is not set; going back to `WAITING`
  clears it.

The listing shows the queue in order, then the closed orders, and sums the
estimated print time still in the queue.

## Commands

| Command                | Action                                                    |
|------------------------|-----------------------------------------------------------|
| `@cord`                | Register a new order (guided interaction)                  |
| `@lord`                | List the queue and the closed orders                       |
| `@uord <id>`           | Update an order (pick a field, enter new value)            |
| `@mord <id> <pos>`     | Move an order to another position in the queue             |
| `@sord <id> <status>`  | Change the execution status                                |
| `@dord <id>`           | Remove an order                                            |
| `@ccli <nome>`         | Register a customer                                        |
| `@lcli`                | List customers                                             |
| `@ucli <id> <nome>`    | Rename a customer                                          |
| `@dcli <id>`           | Remove a customer (soft delete)                            |

`@sord` accepts either the enum name (`RUNNING`) or the Portuguese label
(`Imprimindo`), ignoring case.

## Customers

Order intake asks for the customer in one question, and accepts **either an id
or a name**:

- **All digits** → read as an id and looked up directly. An id that matches no
  active customer fails loudly: the order is not registered and the reply says
  so. It deliberately does not fall back to creating a customer named `12`.
- **Anything else** → matched against active customers ignoring case and
  surrounding blanks, creating a customer on the spot when nothing matches. So
  `@ccli` is only needed to register someone up front — it is not a
  prerequisite for `@cord`.

Name matching is exact apart from case: `joão silva` finds `João Silva`, but
`Joao` without the accent does not, and would create a second customer. The
trade-off of the id shortcut is that a customer whose name is only digits cannot
be created by typing it.

Because the interaction has no database access, it stores the raw input and
`CustomerService.resolveByNameOrId` decides. The interaction has already replied
"Pedido registrado com sucesso!" by the time persistence runs, so
`OrderService.createFromChat` returns a message that overrides that reply when
the customer cannot be resolved.

`@dcli` is a **soft delete** (`active = false`) because `print_order` holds a
foreign key to `customer`; a hard delete would either fail or orphan the order
history. It refuses while the customer still has orders in the queue.

## Input formats

- **Print time** — `4h30`, `4:30`, `4h`, or plain minutes (`90`). Stored as
  minutes; rendered back as `1h30` / `45min` / `2h`.
- **Money** — same rules as purchases: `49,90`, `49.90`, or `R$ 49,90`.
- **Optional fields** — print time, cost, sale price and observations accept
  `-` to skip.

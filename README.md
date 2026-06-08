# Simão API

A personal WhatsApp bot that manages tasks and automations via chat commands, built with Spring Boot and [WAHA](https://waha.devlike.pro/).

## Stack

- Java 25 + Spring Boot 3.x
- PostgreSQL + Flyway
- WAHA (WhatsApp HTTP API)
- Docker Compose

## Features

### Tasks
Create, list, complete, and delete tasks with optional scheduling and periodicity.

### Automations
Schedule automated WhatsApp messages on a recurring or one-time basis.

**Action types:**
- Send a custom text message
- Send a task reminder

**Schedule types:**
- Daily — `12:00`
- Specific date — `01/01/2030 12:00`
- Weekly custom — `SEG 09:00, QUA 14:00`

## Chat Commands

| Command | Description |
|---|---|
| `@menu` | Show all available commands |
| `@ctask` | Create a new task (interactive) |
| `@ltask` | List all active tasks |
| `@dtask <id>` | Delete a task |
| `@etask <id>` | Mark a task as completed |
| `@cauto` | Create a new automation (interactive) |
| `@lauto` | List all active automations |
| `@dauto <id>` | Delete an automation |
| `@cnote` | Create a new note (interactive) |
| `@lnote` | List all notes |
| `@dnote <id>` | Delete a note |
| `@cancel` | Cancel the current in-progress interaction |

All commands must be prefixed with `#` when sent via WhatsApp (e.g. `#@ctask`).

## Getting Started

### Prerequisites

- Docker + Docker Compose
- A running WAHA instance (configured in `.waha_env`)

### Environment files

Create the following files (never commit them):

**`.app_env`**
```env
DATABASE_URL=jdbc:postgresql://postgres-app:5432/simao_db
DATABASE_USERNAME=matheus
DATABASE_PASSWORD=your_password
WAHA_USERNAME=your_waha_username
WAHA_PASSWORD=your_waha_password
WAHA_API_KEY=your_waha_api_key
WAHA_WEBHOOK_SECRET=your_webhook_secret
APPLICATION_HOST=http://simao-app:8080
OWNER_PHONE=your_phone_number
```

**`.waha_env`**
```env
WAHA_USERNAME=your_waha_username
WAHA_PASSWORD=your_waha_password
WHATSAPP_API_KEY=your_waha_api_key
WAHA_WEBHOOK_SECRET=your_webhook_secret
WHATSAPP_SESSIONS_POSTGRESQL_URL=postgres://simao:your_password@postgres-waha:5432/waha_db?sslmode=disable
```

**`.postgres_app.env`**
```env
POSTGRES_DB=simao_db
POSTGRES_USER=matheus
POSTGRES_PASSWORD=your_password
```

**`.postgres_waha.env`**
```env
POSTGRES_DB=waha_db
POSTGRES_USER=simao
POSTGRES_PASSWORD=your_password
```

### Run

```bash
docker compose up -d
```

On startup the app will:
1. Run Flyway migrations
2. Configure the WAHA webhook automatically
3. Send a "Simão Bot is now online!" message to the owner

## Architecture

```
WhatsApp message
      │
      ▼
 WAHA webhook ──► POST /whatsapp/message (validated by X-Webhook-Secret)
      │
      ▼
 WhatsappBotService
      │
      ├── Active chat record? ──► continue interaction state machine
      │
      └── New command? ──► route to TaskService / AutomationService
```

**Interaction state machine:** multi-step chat flows (e.g. `@ctask`, `@cauto`) are persisted as `ChatRecord` in the database so they survive restarts.

**Automation scheduler:** runs every 5 minutes, executes due automations, then either schedules the next run (recurrent) or inactivates them (one-shot).

## Security

- Webhook endpoint protected by a shared secret (`X-Webhook-Secret` header)
- All ports bound to `127.0.0.1` — nothing is exposed to the public network
- Credentials managed via env files (gitignored)

## Planned: Alexa integration

Goal: control Simão by voice (pt-BR) through an Alexa Skill, reusing the existing
services (`TaskService`, `AutomationService`, notes) instead of duplicating logic.

How Alexa works: you don't call an Amazon API — you publish an **Alexa Skill** and
Amazon calls *your* backend whenever the user speaks. The backend can be your own
HTTPS endpoint (preferred here, so we reuse this Spring Boot app) or an AWS Lambda
proxy.

```
Alexa (voice, pt-BR) ──► HTTPS ──► AlexaController (new) ──► TaskService / AutomationService / NotesService
```

### Step-by-step

1. **Add the ASK SDK for Java** — add `com.amazon.alexa:ask-sdk` to `pom.xml`
   (confirm the latest version exists on Maven Central before adding).
2. **Create the Skill** in the [Alexa Developer Console](https://developer.amazon.com/alexa/console/ask)
   with locale **pt-BR**.
3. **Define the interaction model** (intents + utterances + slots). Start small:
   - `CreateTaskIntent` — "crie a tarefa {description}"
   - `ListTasksIntent` — "liste minhas tarefas"
   - Map each intent to the service method the equivalent `@`-command already uses.
4. **Add an `AlexaController`** exposing an HTTPS endpoint (e.g. `POST /alexa`) that:
   - parses the Alexa request JSON via the SDK,
   - routes the intent to the matching service,
   - returns a spoken response (`SpeechletResponse` / SDK response builder).
5. **Validate request signatures** — Alexa signs every request
   (`SignatureCertChainUrl` + `Signature` headers). The ASK SDK validates this
   automatically; keep it enabled — it is mandatory.
6. **Expose the endpoint over HTTPS** — Alexa must reach the endpoint with a valid
   TLS cert. The stack currently binds to `127.0.0.1` and runs behind WSL2 NAT, so
   it is **not** publicly reachable. For development use a tunnel (e.g. ngrok); for
   real use, a proper deployment with a valid certificate.
7. **Restrict access** — since this is a single-owner bot, keep the skill private
   (beta/development) and verify the request belongs to your skill (application id).

### Open considerations

- **Voice is single-shot; chat flows are multi-step.** The `Interaction` state
  machine is built for step-by-step text. Simple commands ("crie a tarefa X") map
  directly; longer flows need to be redesigned using Alexa **Dialog directives**
  (slot filling / multi-turn) rather than the text state machine.
- **Account linking** can be skipped for a private single-owner skill, but the
  request's application id should still be checked.

### Suggested MVP

A skill with 2–3 intents (create task, list tasks) pointing to an `AlexaController`
exposed via ngrok, reusing `TaskService`. Proves the concept without touching the
deployment.

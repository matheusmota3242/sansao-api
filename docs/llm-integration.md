# Planned: LLM integration (Claude API)

Goal: let Simão answer free-form questions and generate insight over your own
data — tasks, notes, and automations — straight from WhatsApp. You ask
"o que devo priorizar essa semana?" or "resuma minhas notas sobre o projeto X"
and the bot replies, having actually read the database to answer.

Chosen approach: **an LLM-powered command inside the bot** (not an external MCP
server). Simão calls the Claude API with tool use; the tools query Postgres
through the services that already exist; the answer comes back over WhatsApp.
This fits the current chatbot architecture and keeps everything self-contained.

```
WhatsApp: "#@ask o que priorizo hoje?"
      │
      ▼
 WhatsappBotService ──► KnowledgeService
                            │
                            ▼
                     Claude API (claude-opus-4-8, tool use)
                            │  ← loops: model asks for data, we run the tool, feed it back
                            ▼
            read-only tools: list_tasks / list_notes / list_automations / stats
                            │
                            ▼
                     Postgres (via existing repositories)
      ◄── final answer (Portuguese) ──┘
```

## Stack

- SDK: `com.anthropic:anthropic-java` (confirm the latest version on Maven
  Central before adding — the docs referenced `2.34.0`).
- Model: `claude-opus-4-8` with adaptive thinking; set `effort` explicitly
  (start at `high`).
- Auth: `ANTHROPIC_API_KEY` env var (see Configuration below).

## Step-by-step

1. **Add the SDK** to `pom.xml`:
   ```xml
   <dependency>
       <groupId>com.anthropic</groupId>
       <artifactId>anthropic-java</artifactId>
       <version>2.34.0</version> <!-- confirm latest on Maven Central -->
   </dependency>
   ```
2. **Add `ANTHROPIC_API_KEY`** to `.app_env` and pass it through in
   `docker-compose.yml` (same pattern as the existing WAHA secrets). The SDK
   reads it automatically via `AnthropicOkHttpClient.fromEnv()`.
3. **Create a `KnowledgeService`** that builds the request and drives the
   tool-use loop. Use a Portuguese system prompt (UI is in Portuguese) that
   describes Simão as a personal-productivity assistant and tells it to answer
   only from the tools' data.
4. **Define read-only, domain-specific tools** — one per query the model may
   need, each backed by an existing repository/service:
   - `list_tasks` (optionally filter by completed / scheduled window)
   - `list_notes` (optionally filter/search by title)
   - `list_automations` (active, with next execution)
   - `task_stats` / `aggregate` (counts, overdue, completion rate) if useful
   Do **not** expose raw SQL or a generic `query` tool — keep the surface typed
   and read-only so a prompt-injected note can't trigger a destructive query.
5. **Drive the loop.** The Java SDK offers `BetaToolRunner` (automatic loop) and
   a manual loop. Prefer the **manual agentic loop** here: the tools need
   Spring-managed repositories, and the manual loop lets you execute tool calls
   inside `KnowledgeService` with normal bean access (the annotated-POJO tool
   classes used by `BetaToolRunner` don't get Spring injection for free). Loop
   until `stop_reason == "end_turn"`, appending the full response content and the
   matching `tool_result` for each `tool_use` each round.
6. **Add the `@ask` command** (e.g. `#@ask <pergunta>`) routed in
   `WhatsappBotService`, and document it in `@menu` and the README command table.
   It can be single-shot (whole question in one message) — no `Interaction`
   state machine needed.
7. **Return the answer over WhatsApp.** The reply is plain text from the final
   model message.

## Considerations

- **Latency / synchronous handler.** The webhook handler replies synchronously,
  and a tool-use round-trip can take several seconds (model + DB + model again).
  For a single user this is acceptable, but it blocks WAHA's sequential webhook
  delivery while it runs. If it ever feels slow, ack the webhook `200`
  immediately and process the LLM reply on a separate thread, then send the
  answer via WAHA `/api/sendText` when ready.
- **Cost.** Each `@ask` call spends tokens (input includes the tool results).
  Single-user load makes this negligible, but keep `max_tokens` and `effort`
  sane. Use `claude-opus-4-8`; only drop to a cheaper model if you choose to.
- **Owner-only.** The bot already restricts to `OWNER_PHONE`, so the LLM command
  inherits that gate — no extra auth needed.
- **Secrets.** `ANTHROPIC_API_KEY` lives in the gitignored env file, never in
  code, the system prompt, or messages.

## Suggested MVP

`@ask` with two tools — `list_tasks` and `list_notes` — and a manual tool loop in
`KnowledgeService`. Proves end-to-end value (ask in WhatsApp → bot reads DB →
useful answer) before adding automations, stats, or async delivery.

## Alternative (not chosen): MCP server

Instead of an in-app command, you could expose an MCP server over the database
(typed tools like `list_tasks`/`list_notes`) and connect an external client
(Claude Desktop, etc.) to explore the data ad-hoc. That serves *you analyzing
from outside*, not answers in WhatsApp. It's a reasonable future addition
alongside this command, but the in-bot command is the primary goal here.

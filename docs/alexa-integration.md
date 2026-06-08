# Planned: Alexa integration

Goal: control Simão by voice (pt-BR) through an Alexa Skill, reusing the existing
services (`TaskService`, `AutomationService`, notes) instead of duplicating logic.

How Alexa works: you don't call an Amazon API — you publish an **Alexa Skill** and
Amazon calls *your* backend whenever the user speaks. The backend can be your own
HTTPS endpoint (preferred here, so we reuse this Spring Boot app) or an AWS Lambda
proxy.

```
Alexa (voice, pt-BR) ──► HTTPS ──► AlexaController (new) ──► TaskService / AutomationService / NotesService
```

## Step-by-step

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

## Open considerations

- **Voice is single-shot; chat flows are multi-step.** The `Interaction` state
  machine is built for step-by-step text. Simple commands ("crie a tarefa X") map
  directly; longer flows need to be redesigned using Alexa **Dialog directives**
  (slot filling / multi-turn) rather than the text state machine.
- **Account linking** can be skipped for a private single-owner skill, but the
  request's application id should still be checked.

## Suggested MVP

A skill with 2–3 intents (create task, list tasks) pointing to an `AlexaController`
exposed via ngrok, reusing `TaskService`. Proves the concept without touching the
deployment.

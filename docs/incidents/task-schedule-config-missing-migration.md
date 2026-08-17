# Incident case study — `task.schedule_config` missing migration

A real production incident in Simão API, preserved as a training / debugging
case study. The **broken state** is frozen at the git tag
`snapshot/task-schedule-config-bug` (on branch `feat-order-management`); the
**fix** is on branch `feat-water-tracker`.

## Symptom

Right after a deploy (Coolify pulled a fresh image from GHCR, container came up
clean, `ExitCode 0`), **every incoming WhatsApp message started returning HTTP
500**. The app itself did not crash and the automation cron kept running — only
the message flow was broken.

Error on each message:

```
ERROR: column "schedule_config" of relation "task" does not exist
```

Stack (abridged):

```
WhatsappController.receiveMessage
  → WhatsappBotService.retrieveReplyFromChatRecord
  → TaskService.create
  → JPA save → INSERT INTO task (... schedule_config ...) → SQLException
```

## Root cause

The `Task` entity mapped a `scheduleConfig` field as JSON:

```java
@JdbcTypeCode(SqlTypes.JSON)
private ScheduleConfig scheduleConfig;   // → column "schedule_config"
```

This field was added to the entity back in commit `c03bcd5` (2026-04-25), but
**no Flyway migration ever created the `schedule_config` column on the `task`
table**. Confirmed with `\d task`, which showed only:
`id, description, scheduled_at, completed, active, created_at, updated_at`.

The bug stayed latent for months: earlier environments had the column created
implicitly (e.g. an old Hibernate `ddl-auto=update` run, or a leftover dev
volume). It only surfaced when a database built **purely from migrations** was
deployed — Hibernate always includes every mapped column in its INSERT, so the
first task insert failed, and the webhook handler returned 500 for every
message.

`schedule_config JSONB` *does* exist in `V1__create_initial_schema.sql` — but on
the `automation` table, not `task`. Easy to misread the migration and think the
column was covered.

## Two valid fixes

1. **Add the missing migration** — `ALTER TABLE task ADD COLUMN IF NOT EXISTS
   schedule_config JSONB;`. Keeps the field. Minimal, but the field stays
   functionally dead — nothing in `TaskService` ever sets `scheduleConfig`.

2. **Remove the field from the entity** (chosen here). A `Task` only needs its
   own optional due date (`scheduledAt`); recurrence is already fully owned by
   `Automation` (`scheduleConfig` + `nextExecutionAt` + `recurrent`). Carrying a
   recurring `scheduleConfig` on `Task` too was duplication — and the dead,
   never-migrated field is exactly what broke production. Removing it makes the
   entity match the real schema, so inserts work with no migration at all.

## Resolution applied

On `feat-water-tracker`: `scheduleConfig` was removed from `Task` (keeping
`scheduledAt` as an optional due date, with null-safe rendering in
`TaskService.listIf`). The fix ships bundled with the tracker feature, so a
single deploy both unbreaks task inserts and adds the new feature.

## Lessons

- An `@Entity` field and its column are two separate facts; adding one without
  the other is invisible until a from-scratch migration run.
- Never rely on `ddl-auto=update` to paper over a missing migration — it hides
  drift that a Flyway-only environment will expose.
- When a JSON/JSONB field appears "already there", check *which table* the
  column belongs to before assuming it's covered.

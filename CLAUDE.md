# Simao API — Claude Code Guide

## Stack
- Java 25, Spring Boot 3.x
- Maven (NOT Gradle)
- PostgreSQL + Flyway migrations
- MapStruct for mapping
- Jackson for serialization

## Test conventions
- Test framework: JUnit 5 via `spring-boot-starter-test` (scope: test)
- No @SpringBootTest unless Testcontainers is set up — unit tests only
- Test files go in src/test/java mirroring the main package
- Interaction tests are plain Java, no Spring context needed

## Maven — do not invent artifact IDs
The only test dependency needed for unit tests:
- groupId: org.springframework.boot
- artifactId: spring-boot-starter-test
- scope: test

Artifacts like spring-boot-starter-data-jpa-test do not exist. Never add a test dependency that isn't already in pom.xml without confirming it exists on Maven Central.

## Architecture — Interaction state machine
- Interaction subclasses extend Interaction<T> and implement processInput(String)
- The steps list is mutable — some steps are inserted dynamically at runtime
  (e.g. action type step inserts "Mensagem:" or "Id da tarefa:" at index 2)
- getCurrentStep() returns the first non-completed step
- Error responses do NOT mark the step completed — state stays on same step
- Every subclass must implement cancelMessage()

## DateTimeUtil
- getDayOfWeekIndex(String) maps Portuguese abbreviations to day numbers (1–7)
- Valid abbreviations: SEG, TER, QUA, QUI, SEX, SAB, DOM
- English abbreviations (MON, TUE, etc.) are not supported and will throw

## Naming conventions
- Interaction classes: Create[Entity]Interaction
- Test classes: Create[Entity]InteractionTest
- User-facing messages in Portuguese, code in English
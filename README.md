# gskart-commons

Shared cross-cutting library for the GS Kart services. This is a multi-module Maven library, not a
runnable application — every module builds a plain jar and none of them is repackaged as a Spring Boot
executable.

Scope is deliberately narrow: technical concerns only. Business DTOs and JPA entities stay in the
services that own them, even when they look duplicated, so the services keep independent schemas.

## Modules

| Module | Holds |
|---|---|
| `gskart-commons-bom` | Dependency management for consumers — import this once, then depend on modules without versions |
| `commons-security` | Resource-server JWT filter, per-request user context, JWT claim names, the shared `HttpSecurity` baseline |
| `commons-web` | RFC 7807 problem-details exception handling and the OpenAPI bearer scheme |
| `commons-domain` | Overridable base classes (`BaseAuditEntity`, `BaseDto`) — plain classes, nothing forces their use |
| `commons-messaging` | The domain-event publish/subscribe ports and the Kafka adapter |
| `commons-logging` | Structured JSON logging defaults for deployed environments |
| `commons-observability` | Micrometer wiring (common tags) |
| `commons-caching` | Spring Cache configuration conventions — TTLs, serialization, key prefixing, fail-open behaviour |

Each module except `commons-domain` ships its own auto-configuration, so a service gets the beans by
adding the dependency. Everything is conditional: a module contributes nothing if its trigger classes
or properties are absent.

## Build

```bash
./mvnw clean install     # builds all modules and publishes 1.0.0-SNAPSHOT to ~/.m2
```

The build enforces a coverage gate on every module, so `install` fails if tests fall behind.

## Using it from a service

Import the BOM once, then depend on the modules the service actually needs:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.gskart</groupId>
      <artifactId>gskart-commons-bom</artifactId>
      <version>1.0.0-SNAPSHOT</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>com.gskart</groupId>
    <artifactId>commons-security</artifactId>
  </dependency>
</dependencies>
```

While the library surface is still settling, the version stays `1.0.0-SNAPSHOT`; `1.0.0` is cut once the
three services have been refactored onto it. A change here needs a rebuild and a re-run of the
dependent services' tests.

Artifacts resolve from the local `~/.m2` repository for now. Publishing to a shared repository comes
later and will not change how services declare the dependency.

## A note on the name

`commons-logging` shares an artifact name with Apache Commons Logging. The group ids differ
(`com.gskart` vs `commons-logging`), so Maven never confuses them, but a dependency exclusion written
for one will not affect the other.

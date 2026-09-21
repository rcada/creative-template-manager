# Creative Template Manager

A Kotlin and Spring Boot learning project: create, list, retrieve, edit, and publish
ad templates with in-memory storage. No database is required.

## Run

Requires Java 21. The Gradle wrapper downloads Gradle and dependencies on first use.

```sh
./gradlew bootRun
```

In IntelliJ, open `build.gradle.kts` as a Gradle project (or use **Link Gradle
Project** in the Gradle tool window), select JDK 21, and let the import finish.
Then run `main` in `CreativeTemplateManagerApplication.kt`.

The server listens on `http://localhost:8080`. There is no homepage or frontend yet.
Stop the server with Ctrl+C, or IntelliJ's Stop button.

## Try the API

```sh
curl -i http://localhost:8080/templates \
  -H 'Content-Type: application/json' \
  -d '{"name":"Summer sale","headline":"Buy {{productName}}","width":1080,"height":1080}'
```

The response is `201 Created`, with a backend-generated UUID, `DRAFT` status, and version `1`.
The `Location` header contains the path to retrieve the template. Copy its ID:

```sh
curl -i http://localhost:8080/templates/REPLACE_WITH_RETURNED_ID
```

Expected responses:

- Existing ID: `200 OK` with the stored template.
- Unknown UUID: `404 Not Found`.
- Malformed UUID, malformed JSON, blank name/headline, or nonpositive dimensions:
  `400 Bad Request`.

## List templates

```sh
curl -i http://localhost:8080/templates
curl -i 'http://localhost:8080/templates?status=DRAFT'
curl -i 'http://localhost:8080/templates?status=PUBLISHED'
```

Listing returns `200 OK` with a JSON array, including `[]` when nothing matches.
Omitting the status (or supplying an empty value) returns all templates. Status
names are case-sensitive: `DRAFT` and `PUBLISHED` are supported; an unsupported
value such as `ARCHIVED` or `draft` returns `400 Bad Request`.

Spring converts the query parameter into a nullable `TemplateStatus`. The repository
uses Kotlin's `filter` to create a result list without modifying stored templates.
Ordering is unspecified and there is no pagination yet. Concurrent changes may be
visible during iteration; the list is not a transactionally consistent snapshot.

## Edit a draft

Send all four editable fields and the `version` from your most recent GET:

```sh
curl -i -X PUT http://localhost:8080/templates/REPLACE_WITH_RETURNED_ID \
  -H 'Content-Type: application/json' \
  -d '{"name":"Summer sale","headline":"Save on {{productName}}","width":1080,"height":1080,"version":1}'
```

A successful edit returns `200 OK` with the updated template and an incremented
version. The ID and draft status are preserved. This replaces all editable fields;
it is not a partial update. Each accepted edit increments the version, even when
the content is unchanged.

- Stale version: `409 Conflict`, with no changes saved.
- Published template: `409 Conflict`, even with its current version.
- Unknown ID: `404 Not Found`.
- Invalid/missing fields or a nonpositive version: `400 Bad Request`.

For a deliberate overwrite after a conflict, GET the current template, review its
changes, then send your chosen content with that current version. Do not automatically
retry with a newer version without reviewing the changes. Another intervening write
still causes a conflict. There is no `force` bypass.

The service checks the client version and uses the repository's atomic conditional
replacement to catch changes that happen after its read. The stored version prevents
an old client request from succeeding even if intervening edits restore the original
content. The service returns `EditResult`; the controller maps it to HTTP.

## Publish a template

Use the ID returned when you created the draft:

```sh
curl -i -X POST http://localhost:8080/templates/REPLACE_WITH_RETURNED_ID/publish
```

The first publish returns `200 OK` with `PUBLISHED` status. Repeating it returns
`409 Conflict`; an unknown ID returns `404 Not Found`. A subsequent GET returns
the published template. Publishing also increments the version. No request body is needed;
publishing acts on the current stored draft, rather than a client-supplied version.

The service returns a sealed `PublishResult`: `Published`, `NotFound`, or `Conflict`.
The controller translates these outcomes into HTTP responses. Publishing creates
an immutable copy, then atomically replaces the original only if it is unchanged.
For competing publish requests on one draft, only one can succeed.

## Follow a request

1. `TemplateController`: maps HTTP requests to Kotlin methods and returns HTTP responses.
2. `CreateTemplateRequest`: describes the incoming JSON and its validation rules.
3. `TemplateService`: generates a UUID, trims text, and creates a draft.
4. `TemplateRepository`: defines the storage operations the service needs.
5. `InMemoryTemplateRepository`: stores immutable templates in a concurrent map.
6. `CreativeTemplate`: describes the stored template and the JSON response.

Spring creates the controller, service, and repository, and supplies their constructor
dependencies. The service depends on a repository interface so we can introduce
PostgreSQL later.

## Current boundaries

Data is lost on restart and is not shared between application processes. The map
supports concurrent reads, atomic inserts, and conditional replacements. Replacement
compares the whole stored value, including its version. Editing and publishing both
increment that version.
Pagination, persistence, authentication,
and image rendering are future steps. The headline is stored as text; placeholders
are not rendered yet. This is a local learning application.

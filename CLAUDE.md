This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
./gradlew build              # Build the project (includes jOOQ codegen + tests)
./gradlew test               # Run all tests
./gradlew test --tests "TestTableServiceTest"  # Run specific test class
./gradlew clean test         # Clean build and run tests
```

## Architecture

**Stack:** Spring Boot 4 + WebFlux + Kotlin Coroutines + jOOQ + R2DBC + PostgreSQL

**Layered architecture:**
- **Router** (`*Router.kt`) - Defines HTTP routes using `coRouter { }` DSL with error handlers
- **Handler** (`*Handler.kt`) - Suspended request handlers, delegates to service
- **Service** (`*Service.kt`) - Business logic, `@Transactional` annotations, throws `NotFoundException`
- **Repository** (`*Repository.kt`) - jOOQ DSL queries, returns `Flow<T>` or `suspend` functions

**Key patterns:**
- All HTTP handlers are `suspend` functions using coroutines
- Repositories use jOOQ's `asFlow()` and `awaitFirst/awaitFirstOrNull` extensions
- Service-layer `@Transactional` works via R2DBC connection factory proxy
- Model mapping via extension functions (e.g., `TestTable.toResponse()`)
- Never use optional Kotlin types for mandatory fields
- Always perform ./gradlew clean test after making changes and fix all warnings
- All versions in `build.gradle.kts` should be variables

**Database Patterns:**
1. Create migration file in `src/main/resources/db/migration/`
2. Run `./gradlew build` - jOOQ classes regenerate automatically
3. Use generated classes in repository (e.g., `TEST_TABLE`, `TestTable` POJO)
4. Never use optional Kotlin types for non-nullable fields

## Testing

Tests use Zonky embedded PostgreSQL with auto-migration.

### Mandatory
- If the test requires a spring context, extend AbstractContextTests to avoid launching additional spring contexts
- In spring context tests never use class-wide annotations on test classes
- In spring context tests never use mocked beans in test classes to keep spring context clean
- Instead of mocks in a spring context test, add spies to AbstractContextTests to keep spring context clean
- Never use sleep in tests

Tests are organized by layer:
- `*HandlerTest` - WebTestClient-based HTTP endpoint tests
- `*ServiceTest` - Unit tests with MockK
- `*RepositoryTest` - Integration tests with an embedded database

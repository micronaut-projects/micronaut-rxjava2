# Python Docs Disabled Test Inventory

This file tracks Python docs examples of Micronaut RxJava 2 that are present but disabled, or that deviate from the
Java example because the direct port currently fails compilation or at runtime (Python compiler gaps). Use it as the
bug-fixing task list for the final migration wave.

## Reconciliation

- Last generated active `@Disabled` count: 0.
- Last generated command: `rg -n "@Disabled\(" test-suite-python/src/test/python`.
- Last full-suite command: `./gradlew :test-suite-python:test -Ppython-ci`.
- Last full-suite result: build successful, 3 tests executed (1 test class), 0 skipped.

## Migration Rules

- Test methods and injected attributes are snake_case (`the_regular_client_returns_rxjava_types`, `http_client`); the
  controller methods keep the Java names because they are route handlers.
- The `Message` model is an `@Introspected @dataclass` (with `@ReflectiveAccess` for Jackson databind, as in core).
- RxJava lives under `io.reactivex`, which cannot be imported at runtime because `io` is the Python standard-library
  module: `HelloController` imports `Flowable` from `io.reactivex` inside a `try:` block with a fallback to the
  generated `reactivex` package (`# TODO(python)`), and the reactive controller methods are declared as
  `Publisher[...]` (the Java files declare `Flowable<...>`).
- Imported classes are passed as runtime type arguments (`jsonStream(request, Message)`, `eventStream(request, String)`
  with `from java.lang import String`); no `java.type(...)` aliases are needed.
- This branch builds on Micronaut 4.10 (Java 17 baseline) while the Python compiler and runtime ship with Micronaut
  core 5.2+ (Java 25): only `test-suite-python` resolves the Micronaut 5 versions of core and test
  (`micronautBuild.python.compilerVersion` and the `*-python` entries of `gradle/libs.versions.toml`) and targets Java 25.

## Active `@Disabled` Tests

None.

## Commented Unsupported Snippet Ports

None.

## Intentionally Unsupported Snippet Targets

None.

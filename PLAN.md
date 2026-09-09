# lol-pro-data-download-cron Implementation Plan

Goal: deliver a Spring Boot cron app (matching `lol-ddragon-snapshot-cron` stack)
that downloads Oracle's Elixir CSVs and publishes per-year atomic handoff
artifacts plus optional manifests.

## Assumptions
- Tech stack mirrors `lol-ddragon-snapshot-cron`: Java + Spring Boot + Gradle.
- The repository-local handoff contract and current tests/docs are authoritative for CSV shape, required columns, atomic replacement, and optional manifests.
- Download cron does not transform the CSV contents; it publishes the raw Oracle's Elixir files (column order and extra columns preserved).
- Keep KISS/YAGNI, but make the download method swappable with a single minimal abstraction (polymorphic fetcher) so we can switch sources later without refactoring core publish/validation logic.
- Oracle's Elixir Google Drive files are updated daily even though filenames are year-based (the year is not a freshness signal).

## Plan

1. Confirm contract + configuration surface
   - Define config keys for source URL, output file path, temp directory, manifest enable flag, optional user agent/timeouts, and years to fetch.
   - Publish year-based filenames into the same output directory.
   - Document configuration and defaults in the repository.
   - Processor requirement: ingest multiple year files and handle empty CSVs; rolling windows are computed downstream.

2. Maintain the Spring Boot/Gradle application boundary
   - Keep the standalone executable entrypoint stable.
   - Validate with the repository test suite.

3. CSV download + header validation
   - Keep a minimal source abstraction around Google Drive acquisition.
   - Always download selected year files each run unless a future repository issue changes freshness behavior.
   - Validate required columns before publish and allow empty CSVs with a valid header.

4. Atomic handoff publication
   - Publish one CSV per year via same-filesystem temp file + atomic replacement where supported.

5. Optional manifest writer
   - When enabled, write generated_at, row_count, sha256, and source_url for each year artifact.

6. Observability + exit codes
   - Emit useful per-file logging and non-zero exit codes on acquisition/validation/publication failure.

7. Documentation and tests
   - Keep README usage/output documentation current.
   - Maintain unit/integration coverage for validation, checksums, year selection, empty files, and atomic publication.

## Notes
- Known Google Drive filenames are year-based (`<year>_LoL_esports_match_data_from_OraclesElixir.csv`).
- Repository-local docs, tests, code, and GitHub issues are the source of truth for future changes.

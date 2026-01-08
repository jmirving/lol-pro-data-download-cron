# lol-pro-data-download-cron Implementation Plan

Goal: deliver a Spring Boot cron app (matching `lol-ddragon-snapshot-cron` stack)
that downloads Oracle's Elixir CSVs and publishes per-year atomic handoff
artifacts plus optional manifests, per `project-brain/DECISIONS.md`.

## Assumptions
- Tech stack mirrors `lol-ddragon-snapshot-cron`: Java + Spring Boot + Gradle.
- Handoff contract is the authoritative spec (CSV, required columns, atomic
  replacement, optional manifest).
- Keep KISS/YAGNI, but make the download method swappable with a single minimal
  abstraction (polymorphic fetcher) so we can switch sources later without
  refactoring core publish/validation logic.
- Oracle's Elixir Google Drive files are updated daily even though filenames are
  year-based (the year is not a freshness signal).

## Plan (no implementation until approved)

1. Confirm contract + configuration surface
   - Define config keys for: source URL, output file path, temp directory,
     manifest enable flag, optional user agent/timeouts, and years to fetch
     (default: current year + previous year).
   - Output layout decision: publish year-based filenames into the same output
     directory (no per-year subfolders).
   - Document config in README (or new `CONFIG.md`) with defaults.
   - Validation: config list is documented and matches contract language.
   - External input needed: confirm the Oracle's Elixir CSV source URL(s), the
     target output directory path, and desired cron cadence (if not already set).
   - Decision: use Google Drive folder listing to locate file IDs; default to
     current + previous year CSVs (year-based filename), with a config override
     to fetch specific years or multiple years if needed. Do not use the year as
     a freshness signal.
   - Processor requirement: ingest multiple year files and handle empty CSVs;
     rolling windows are computed downstream (not in the download cron).

2. Set up project skeleton (Gradle + Spring Boot)
   - Create build files and main app entrypoint matching the DDragon cron style.
   - Add deps: Spring Boot starter, Jackson, and any small IO helpers (if needed).
   - Validation: `gradle_safe test` runs (even if only a smoke test exists).
   - Port/migrate: mirror Gradle + Spring Boot setup from
     `lol-ddragon-snapshot-cron` (build files and base app structure).

3. Implement CSV download + header validation (TDD)
   - Write failing unit tests for required column validation first.
   - Introduce a minimal `DownloadProvider` (or similar) interface with one
     implementation for Google Drive, keeping the rest of the flow source-agnostic.
   - List available files from Google Drive, map year-based filenames to IDs,
     and select configured years (default: current + previous year).
   - Always download the selected year files each run (no cache or skip
     based on prior runs).
   - Download each selected year CSV to a temp file.
   - Validate required columns (per contract) before publish; allow empty CSVs
     as long as the header is present.
   - Validation: unit tests for header validation; failure blocks publish.
   - Validation: can swap in a stub provider in tests without changing publish
     logic (demonstrates polymorphism without extra abstraction).
   - Port/migrate: locate Oracle's Elixir CSV fetch logic in `draft-sage`
     (likely `scripts/ingest_oracle_elixir.py`) for behavior parity, but recreate
     in Java to fit the cron service; Google Drive parsing is new.

4. Publish atomic handoff artifact (TDD)
   - Write a failing test for atomic replace semantics (temp -> final).
   - Publish one CSV per year to the configured output path via atomic rename
     in the same filesystem.
   - Validation: integration test or local run shows output file replaced
     atomically (no partial writes observed).

5. Optional manifest writer (TDD)
   - Write failing unit tests for manifest content and checksum accuracy first.
   - If enabled, write one manifest per year with generated_at, row_count,
     sha256, source_url.
   - Validation: manifest is written and checksum matches the published CSV
     (row_count may be zero for empty files).

6. Observability + exit codes (TDD where practical)
   - Add tests for exit code behavior on download/validation failure.
   - Log source URL, output path, row count, and checksum per year.
   - Non-zero exit on download or validation failure.
   - Validation: error paths produce non-zero exit and log message.

7. Update docs + usage examples
   - Update README with run instructions and expected output layout.
   - Document the Google Drive source, file naming scheme, and the default
     "current + previous year" selection (explicitly note daily updates and
     that empty CSVs may be published).
   - Validation: README describes how to run and where outputs appear.

8. Add lightweight tests (complete TDD coverage)
   - Unit tests: header validation, manifest output, checksum, year selection,
     empty CSV acceptance.
   - Integration test: temp dir download and atomic publish for multiple years.
   - Validation: `gradle_safe test` passes locally.

## Notes
- Implementation will not begin until this plan is approved.
- Known Google Drive filenames (as of today):
  - 2014_LoL_esports_match_data_from_OraclesElixir.csv
  - 2015_LoL_esports_match_data_from_OraclesElixir.csv
  - 2016_LoL_esports_match_data_from_OraclesElixir.csv
  - 2017_LoL_esports_match_data_from_OraclesElixir.csv
  - 2018_LoL_esports_match_data_from_OraclesElixir.csv
  - 2019_LoL_esports_match_data_from_OraclesElixir.csv
  - 2020_LoL_esports_match_data_from_OraclesElixir.csv
  - 2021_LoL_esports_match_data_from_OraclesElixir.csv
  - 2022_LoL_esports_match_data_from_OraclesElixir.csv
  - 2023_LoL_esports_match_data_from_OraclesElixir.csv
  - 2024_LoL_esports_match_data_from_OraclesElixir.csv
  - 2025_LoL_esports_match_data_from_OraclesElixir.csv
  - 2026_LoL_esports_match_data_from_OraclesElixir.csv

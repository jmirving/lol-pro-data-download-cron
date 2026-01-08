# lol-pro-data-download-cron

Cron job to fetch Oracle's Elixir pro-game exports (year-based CSVs) and publish
raw CSV handoff artifacts for shared consumers.

## Scope
- Fetch raw Oracle's Elixir exports (CSV, one file per year)
- Publish per-year CSVs to a shared path (same directory, year-based filenames)
- Optionally write per-file manifests with checksum/metadata

## Out of scope
- Normalization or schema transformation
- Model training or inference
- UI or API serving

## Handoff contract
- The canonical contract lives in Project Brain `DECISIONS.md`.
- The download outputs are the sole inputs for downstream processors.

## Setup
- Java 17+
- Gradle wrapper is included; use `gradle_safe` per shell policy.
- Network access is required to reach Google Drive.

## Run
Example (current + previous year, per defaults):
```bash
gradle_safe bootRun --args="--prodata.download.outputDir=/path/to/output --prodata.download.tempDir=/path/to/output"
```

Example (explicit years):
```bash
gradle_safe bootRun --args="--prodata.download.outputDir=/path/to/output --prodata.download.tempDir=/path/to/output --prodata.download.years=2025,2026"
```

## Test
```bash
gradle_safe test
```

## Configuration (Spring Boot properties)
- `prodata.download.googleDriveFolderUrl`
  - Default: `https://drive.google.com/drive/folders/1gLSw0RLjBbtaNy0dgnGQDAZOHIgCe-HH`
- `prodata.download.outputDir` (required)
  - Directory to publish year CSVs and optional manifests.
- `prodata.download.tempDir`
  - Default: system temp directory.
- `prodata.download.years`
  - Comma-delimited list of years to fetch.
  - Default: current year + previous year.
- `prodata.download.manifestEnabled`
  - Default: false.
- `prodata.download.userAgent`
  - Default: `lol-pro-data-download-cron`.
- `prodata.download.connectTimeout`
  - Default: `30s`.
- `prodata.download.readTimeout`
  - Default: `120s`.

Notes:
- Filenames are year-based but updated daily; do not use the year as a freshness signal.
- The cron fetches the configured year files every run (no cache/skip).
- Empty CSVs are still published if present.

## Next steps
- Implement download source configuration and atomic publish
- Add basic integrity checks and optional manifest writer

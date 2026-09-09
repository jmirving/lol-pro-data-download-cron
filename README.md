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
gradle_safe bootRun
```

Example (explicit years):
```bash
gradle_safe bootRun --args="--prodata.download.years=2025,2026"
```

Example (all available years):
```bash
gradle_safe bootRun --args="--prodata.download.includeAllYears=true"
```

The bootable JAR is also a stable, non-interactive command entry point:

```bash
gradle_safe bootJar
java -jar build/libs/lol-pro-data-download-cron-1.0-SNAPSHOT.jar \
  --prodata.download.outputDir=/work/raw \
  --prodata.download.years=2025,2026
```

The command exits `0` after all selected files are published and non-zero if
listing, download, validation, or publication fails. Repeated runs atomically
replace the selected year files and any enabled manifests.

## Structured command output

For command adapters that require machine-readable results, opt into the
generic JSON stdout contract:

```bash
java -jar build/libs/lol-pro-data-download-cron-1.0-SNAPSHOT.jar \
  --prodata.download.outputDir=/work/raw \
  --prodata.download.manifestEnabled=true \
  --prodata.download.structuredOutput=json
```

In this mode, the entire stdout stream is one JSON object. The `metadata`
object reports the absolute output directory, selected years, and each
published artifact's filename, absolute path, row count, SHA-256, source URL,
generation time, and manifest path when manifests are enabled. Success uses
status `SUCCESS`; failures use status `FAILED`, reason code `DOWNLOAD_FAILED`,
and a non-zero process exit. This mode does not require or contain
orchestrator-specific behavior.

All Spring properties can also be supplied through standard environment
variables, which is convenient for ephemeral worker directories. For example:

```bash
PRODATA_DOWNLOAD_OUTPUT_DIR=/work/raw \
PRODATA_DOWNLOAD_MANIFEST_ENABLED=true \
PRODATA_DOWNLOAD_STRUCTURED_OUTPUT=json \
java -jar build/libs/lol-pro-data-download-cron-1.0-SNAPSHOT.jar
```

## Test
```bash
gradle_safe test
```

## Configuration (Spring Boot properties)
- `prodata.download.googleDriveFolderUrl`
  - Default: `https://drive.google.com/drive/folders/1gLSw0RLjBbtaNy0dgnGQDAZOHIgCe-HH`
- `prodata.download.outputDir`
  - Default: `build/prodata` (removed by `./gradlew clean`).
  - Directory to publish year CSVs and optional manifests.
- `prodata.download.tempDir`
  - Default: `<outputDir>/tmp` (`build/prodata/tmp` with the default output).
  - Keeping the default places temporary downloads on the same filesystem as
    an orchestrator-provided ephemeral output directory.
- `prodata.download.years`
  - Comma-delimited list of years to fetch.
  - Default: current year + previous year.
- `prodata.download.includeAllYears`
  - Default: false.
  - When true, ignores `prodata.download.years` and fetches every available year file.
- `prodata.download.manifestEnabled`
  - Default: false.
- `prodata.download.structuredOutput`
  - Default: `none`.
  - Set to `json` to emit the generic structured command envelope on stdout.
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

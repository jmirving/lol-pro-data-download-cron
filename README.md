# lol-pro-data-download-cron

Cron job to fetch Oracle's Elixir pro-game export and publish the raw CSV handoff
artifact for shared consumers.

## Scope
- Fetch raw Oracle's Elixir export (CSV)
- Publish a single canonical CSV to a shared path
- Optionally write a manifest with checksum/metadata

## Out of scope
- Normalization or schema transformation
- Model training or inference
- UI or API serving

## Handoff contract
- The canonical contract lives in Project Brain `DECISIONS.md`.
- The download output is the sole input for downstream processors.

## Next steps
- Implement download source configuration and atomic publish
- Add basic integrity checks and optional manifest writer

# MomentCraft Worker

The Python side of MomentCraft. Polls the Paper plugin's `jobs/` folder,
picks up detected moments, and (starting Phase 9) turns them into rendered
clips.

## Setup

```bash
cd worker
python3 -m venv venv
source venv/bin/activate   # Windows: venv\Scripts\activate
pip install -r requirements.txt
```

Edit `config.yaml` — set `jobs_dir` to point at your actual
`plugins/MomentCraft/jobs` folder.

## Running

```bash
python -m momentcraft_worker.main
```

Ctrl+C stops it cleanly — it finishes whatever job it's currently on
before exiting.

## How it works

- Polls `jobs_dir` for `.json` files.
- The instant a file is found, it's moved into `jobs_dir/processing/` —
  this "claims" it, so a crash or restart never double-processes or loses
  a job.
- The file is parsed into a typed `Job` object and handed to
  `pipeline.process_job()`.
- On success, the file moves to `jobs_dir/done/`. On failure (bad JSON, or
  an error during processing), it moves to `jobs_dir/failed/` instead —
  nothing is silently deleted.

## Status

- [x] Phase 8 — Worker setup, job polling, safe claiming
- [ ] Phase 9 — Environment analysis
- [ ] Phase 10 — Camera director
- [ ] Phase 11 — Render client
- [ ] Phase 12 — FFmpeg encoding
- [ ] Phase 13 — Discord delivery

 # MomentCraft

Self-hosted, open-source cinematic moment detection for Paper servers.

MomentCraft watches for notable gameplay moments — clutch kills, boss fights,
close calls — and turns them into short, deliberately-directed vertical clips.
No paid AI API required, no cloud dependency, and it's built to have as close
to zero impact on server TPS as possible.

## Status

Early development. Building in phases:

- [x] Phase 1 — Project foundation
- [ ] Phase 2 — Plugin lifecycle & commands
- [ ] Phase 3 — Manual capture zones
- [ ] Phase 4 — Rolling capture buffer
- [ ] Phase 5 — Performance guard
- [ ] Phase 6 — Automatic moment detection
- [ ] Phase 7 — Job export
- [ ] Phase 8 — Python worker
- [ ] Phase 9+ — Environment analysis, camera director, rendering, Discord

## Requirements

- Java 21
- PaperMC 1.21+

## Building

```bash
./gradlew build
```

The compiled jar lands in `build/libs/`.

## License

MIT — see `LICENSE`.

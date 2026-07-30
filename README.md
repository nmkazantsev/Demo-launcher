This is a demo launcher for this engine testing: https://github.com/nmkazantsev/Seal-Engine-3M

## Runtime frame capture smoke test

The capture renderer lives beside `MainRenderer` in `game/src/main/java/com/nikitos/` and is compiled separately, so the regular Demo renderer keeps its current engine API.

```bash
JAVA_HOME=/home/nikita/.jdks/ms-21.0.9 ./gradlew :frameCaptureSmoke --offline --no-daemon
```

It opens a short-lived desktop window, writes one PNG/JSON pair under `build/frame-capture-smoke-*`, prints `FRAME_CAPTURE_SMOKE_OK=...`, and exits.

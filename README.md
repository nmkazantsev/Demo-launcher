This is a demo launcher for this engine testing: https://github.com/nmkazantsev/Seal-Engine-3M

## Platform shaders

Demo shaders live under `game/src/main/resources/shaders/android` and
`game/src/main/resources/shaders/desktop`. Both directories contain the same
logical paths, including the `shape/` and `skybox/` subdirectories. Renderer
code passes those logical paths to `Shader` without a platform prefix; Seal
Engine v3.4.0 selects the physical directory at runtime.

## Runtime frame capture smoke test

The capture renderer lives beside `MainRenderer` in `game/src/main/java/com/nikitos/` and is compiled separately, so the regular Demo renderer keeps its current engine API.

```bash
JAVA_HOME=/home/nikita/.jdks/ms-21.0.9 ./gradlew :frameCaptureSmoke --offline --no-daemon
```

It opens a short-lived desktop window, writes one PNG/JSON pair under `build/frame-capture-smoke-*`, prints `FRAME_CAPTURE_SMOKE_OK=...`, and exits.

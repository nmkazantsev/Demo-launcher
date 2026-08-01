# Demo Renderers Design

## Goal

`Demo` must not define a separate `frameCapture` source set/module. `MainRenderer`, `FrameCaptureTestRenderer`, and `FrameCaptureTestMain` remain in the `game` module, while both launch paths continue to work.

## Design

Keep the existing Java sources in `game/src/main/java/com/nikitos/`. Remove the root project's custom `frameCapture` source set and its detached dependencies. Define `frameCaptureSmoke` as a `JavaExec` task using the root `main` runtime classpath; that classpath already contains the `game`, `core`, and `desktop` outputs through normal project dependencies.

Restore `Demo/settings.gradle` project paths to the checked-in `../sealEngine_3M` modules so Gradle can resolve the runtime dependencies from this workspace.

## Success criteria

- Gradle reports no `frameCapture` source set or module.
- `:compileJava` succeeds and resolves `MainRenderer` from `game`.
- `:frameCaptureSmoke` starts `FrameCaptureTestMain`, writes the capture pair, and exits with `FRAME_CAPTURE_SMOKE_OK`.
- `:run` starts the regular `Main` entry point and reaches `MainRenderer` without classpath errors.

# Demo Renderers Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Run both Demo renderers from the `game` module without a separate `frameCapture` source set.

**Architecture:** Keep all renderer entry points in `game`. Let the root Demo project expose only a `JavaExec` smoke task whose classpath is the normal root `main` runtime classpath, provided transitively by the existing `game`, `core`, and `desktop` dependencies.

**Tech Stack:** Gradle 8.14, Java, existing `core`/`desktop`/`game` modules.

## Global Constraints

- Do not add a new module, source set, dependency, or renderer abstraction.
- Preserve the existing `MainRenderer`, `FrameCaptureTestRenderer`, and `FrameCaptureTestMain` source locations.
- Keep unrelated `.idea` changes untouched.

---

### Task 1: Simplify Demo Gradle wiring

**Files:**
- Modify: `settings.gradle` — point `core` and `desktop` at the existing checked-in `../sealEngine_3M` modules.
- Modify: `build.gradle` — remove the custom `frameCapture` source set and configurations; run `frameCaptureSmoke` from `sourceSets.main.runtimeClasspath`.

**Interfaces:**
- Consumes: existing `game` dependency and `com.nikitos.FrameCaptureTestMain`.
- Produces: `:frameCaptureSmoke` with no `frameCapture` source set, plus the existing `:run` task.

- [ ] **Step 1: Update the project paths**

  In `settings.gradle`, change both project directories from `../seal-runtime-worktree/...` to `../sealEngine_3M/...`.

- [ ] **Step 2: Remove the detached source set**

  Delete the `sourceSets { frameCapture { ... } }` block and the two `frameCaptureImplementation` dependency declarations from `build.gradle`.

- [ ] **Step 3: Rewire the smoke task**

  Keep `tasks.register('frameCaptureSmoke', JavaExec)`, set its classpath to `sourceSets.main.runtimeClasspath`, and keep `mainClass = 'com.nikitos.FrameCaptureTestMain'`.

- [ ] **Step 4: Verify project wiring**

  Run:

  ```bash
  GRADLE_USER_HOME=/tmp/demo-gradle JAVA_HOME=/home/nikita/.jdks/ms-21.0.9 ./gradlew :compileJava --offline --no-daemon
  ```

  Expected: exit code 0 and no missing `seal-runtime-worktree` or `frameCapture` source-set errors.

- [ ] **Step 5: Verify the capture renderer**

  Run:

  ```bash
  GRADLE_USER_HOME=/tmp/demo-gradle JAVA_HOME=/home/nikita/.jdks/ms-21.0.9 ./gradlew :frameCaptureSmoke --offline --no-daemon
  ```

  Expected: exit code 0, `FRAME_CAPTURE_SMOKE_OK=...`, and exactly one PNG/JSON capture pair in the reported directory.

- [ ] **Step 6: Verify the regular renderer**

  Run `:run` with the same Java and Gradle settings under a short timeout. Expected: the Java process reaches the regular desktop launcher and `MainRenderer` without a classpath or compile error; timeout is acceptable because the regular renderer is interactive.

- [ ] **Step 7: Inspect the diff**

  Run `git diff --check` and `git status --short` from `Demo`. Confirm only the intended Gradle/docs files changed and existing `.idea` edits remain untouched.

package com.nikitos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

/** Minimal page for manually verifying one runtime frame capture. */
public final class FrameCaptureTestRenderer extends GamePageClass {
    private final Path outputDirectory;
    private boolean captureRequested;

    public FrameCaptureTestRenderer(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override public void onSurfaceChanged(int x, int y) { }

    @Override public void update(float dtMillis) {
        if (!captureRequested) {
            CoreRenderer.engine.setFrameCaptureDataProvider(() -> Map.of("smoke", true, "dtMillis", dtMillis));
            CoreRenderer.engine.requestFrameCapture(outputDirectory);
            captureRequested = true;
        } else if (capturePairExists()) {
            System.out.println("FRAME_CAPTURE_SMOKE_OK=" + outputDirectory.toAbsolutePath());
            CoreRenderer.engine.requestShutdown();
        }
    }

    @Override public void render() { CoreRenderer.engine.glClear(); }
    @Override public void onResume() { }
    @Override public void onPause() { }

    private boolean capturePairExists() {
        try (Stream<Path> files = Files.list(outputDirectory)) {
            return files.filter(Files::isRegularFile).count() == 2;
        } catch (IOException ignored) {
            return false;
        }
    }
}

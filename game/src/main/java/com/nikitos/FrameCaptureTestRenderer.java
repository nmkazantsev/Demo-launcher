package com.nikitos;

import com.nikitos.main.camera.Camera;
import com.nikitos.main.images.PImage;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.shaders.default_adaptors.MainShaderAdaptor;
import com.nikitos.main.vertices.SimplePolygon;
import com.nikitos.maths.Matrix;
import com.nikitos.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** Minimal page for manually verifying one runtime frame capture. */
public final class FrameCaptureTestRenderer extends GamePageClass {
    private final Path outputDirectory;
    private final Shader shader;
    private final SimplePolygon marker;
    private Camera camera;
    private int frameCount;
    private boolean captureRequested;

    public FrameCaptureTestRenderer(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
        shader = new Shader("vertex_shader.glsl", "fragment_shader.glsl", this, new MainShaderAdaptor());
        marker = new SimplePolygon(this::drawMarker, false, 0, this);
    }

    @Override public void onSurfaceChanged(int x, int y) {
        camera = new Camera(x, y);
        camera.resetFor2d();
        marker.redrawNow();
    }

    @Override public void update(float dtMillis) {
        frameCount++;
        if (!captureRequested && frameCount == 100) {
            CoreRenderer.engine.setFrameCaptureDataProvider(() -> Map.of(
                    "smoke", true, "rendererFrame", frameCount, "dtMillis", dtMillis));
            CoreRenderer.engine.requestFrameCapture(outputDirectory);
            captureRequested = true;
        } else if (capturePairExists()) {
            System.out.println("FRAME_CAPTURE_SMOKE_OK=" + outputDirectory.toAbsolutePath());
            CoreRenderer.engine.requestShutdown();
        }
    }

    @Override public void render() {
        Utils.background(20, 28, 48);
        CoreRenderer.engine.glClear();
        shader.apply();
        camera.resetFor2d();
        camera.apply();
        Matrix.applyMatrix(Matrix.resetTranslateMatrix(new float[16]));
        float size = Math.max(100f, Math.min(Utils.getX(), Utils.getY()) * 0.22f);
        float x = Utils.getX() * 0.5f - size * 0.5f;
        float y = Utils.getY() * 0.5f - size * 0.5f;
        marker.prepareAndDraw(x, y, size, size, 1f);
    }
    @Override public void onResume() { }
    @Override public void onPause() { }

    private boolean capturePairExists() {
        try (Stream<Path> files = Files.list(outputDirectory)) {
            return files.filter(Files::isRegularFile).count() == 2;
        } catch (IOException ignored) {
            return false;
        }
    }

    private PImage drawMarker(List<Object> ignored) {
        PImage image = new PImage(256, 256);
        image.background(36, 180, 108, 255);
        image.stroke(255, 246, 214, 255);
        image.strokeWeight(10);
        image.ellipse(128, 128, 90, 90);
        image.line(36, 36, 220, 220);
        return image;
    }
}

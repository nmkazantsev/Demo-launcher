package com.nikitos;

import com.nikitos.platform.DesktopLauncher;
import com.nikitos.platformBridge.LauncherParams;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class FrameCaptureTestMain {
    private FrameCaptureTestMain() { }

    public static void main(String[] args) {
        Path outputDirectory = Paths.get("build", "frame-capture-smoke-" + System.nanoTime());
        System.out.println("FRAME_CAPTURE_DIR=" + outputDirectory.toAbsolutePath());
        new DesktopLauncher(new LauncherParams()
                .setFullScreen(false)
                .setDebug(false)
                .setStartPage(unused -> new FrameCaptureTestRenderer(outputDirectory)))
                .run();
    }
}

package com.nikitos;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShaderResourceLayoutTest {
    @Test
    void platformShaderTreesAreMirrored() throws IOException {
        Path shaderRoot = Path.of("game", "src", "main", "resources", "shaders");
        Path androidRoot = shaderRoot.resolve("android");
        Path desktopRoot = shaderRoot.resolve("desktop");

        assertTrue(Files.isDirectory(androidRoot), "missing Android shader tree");
        assertTrue(Files.isDirectory(desktopRoot), "missing desktop shader tree");
        assertEquals(shaderPaths(androidRoot), shaderPaths(desktopRoot));
    }

    private static Set<Path> shaderPaths(Path root) throws IOException {
        try (var paths = Files.walk(root)) {
            return paths.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".glsl"))
                    .map(root::relativize)
                    .collect(Collectors.toSet());
        }
    }
}

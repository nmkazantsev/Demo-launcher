package com.nikitos;

import com.nikitos.main.shaders.Shader;
import com.nikitos.main.shaders.default_adaptors.MainShaderAdaptor;
import com.nikitos.utils.FileUtils;
import com.nikitos.utils.Utils;

public class MainRenderer extends GamePageClass {
    private Shader shader;

    public MainRenderer() {
        FileUtils fileUtils = new FileUtils();
        shader = new Shader(
                fileUtils.readFileFromAssets(CoreRenderer.class, "vertex_shader.glsl"),
                fileUtils.readFileFromAssets(CoreRenderer.class, "fragment_shader.glsl"),
                this, new MainShaderAdaptor());
    }

    @Override
    public void draw() {
        shader.apply();
        Utils.background(255, 100, 0);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }
}

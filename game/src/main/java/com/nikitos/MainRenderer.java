package com.nikitos;

import com.nikitos.main.camera.Camera;
import com.nikitos.main.images.PImage;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.shaders.default_adaptors.MainShaderAdaptor;
import com.nikitos.main.vertices.SimplePolygon;
import com.nikitos.maths.Matrix;
import com.nikitos.platformBridge.PlatformBridge;
import com.nikitos.utils.FileUtils;
import com.nikitos.utils.Utils;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;


public class MainRenderer extends GamePageClass {
    private final Shader shader;
    private final PlatformBridge pb;

    private float[] matrix = new float[16];

    private Camera camera;

    private SimplePolygon simplePolygon;

    public MainRenderer() {
        pb = CoreRenderer.engine.getPlatformBridge();
        String path = System.getProperty("user.dir") + "/../Demo/game/src/main/shaders/";
        pb.print(path);
        ///vertex_shader.glsl
        InputStream vertex;
        try {
            vertex = new FileInputStream(path + "vertex_shader.glsl");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        InputStream fragment;
        try {
            fragment = new FileInputStream(path + "fragment_shader.glsl");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        FileUtils fileUtils = new FileUtils();
        shader = new Shader(
                fileUtils.readFile(vertex),
                fileUtils.readFile(fragment),
                this, new MainShaderAdaptor());

        matrix = Matrix.resetTranslateMatrix(matrix);

        camera = new Camera(Utils.x, Utils.y);
        camera.resetFor2d();

        simplePolygon = new SimplePolygon(this::redraw, true, 0,this);
    }

    @Override
    public void draw() {
        Utils.background(255, 100, 0);
        shader.apply();
        camera.apply();
        Matrix.applyMatrix(matrix);

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    private PImage redraw(List<Object> params){
        PImage image = new PImage();
    }
}

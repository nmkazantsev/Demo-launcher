package com.nikitos;

import com.nikitos.main.camera.Camera;
import com.nikitos.main.frameBuffers.FrameBuffer;
import com.nikitos.main.images.PImage;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.shaders.default_adaptors.MainShaderAdaptor;
import com.nikitos.main.vertices.Shape;
import com.nikitos.main.vertices.SimplePolygon;
import com.nikitos.main.vertices.SkyBox;
import com.nikitos.maths.Matrix;
import com.nikitos.maths.PVector;
import com.nikitos.platformBridge.PlatformBridge;
import com.nikitos.utils.FileUtils;
import com.nikitos.utils.Utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.nikitos.utils.Utils.*;


public class MainRenderer extends GamePageClass {
    private final Engine engine;
    private final Shader shader;
    private final PlatformBridge pb;

    private float[] matrix = new float[16];

    private final Camera camera;

    private final SimplePolygon simplePolygon;
    private final Shape shape;

    private final SkyBox skyBox;
    private final Shader skyBoxShader;

    private final FrameBuffer fb;

    public MainRenderer() {
        engine = CoreRenderer.engine;
        pb = engine.getPlatformBridge();

        FileUtils fileUtils = new FileUtils();
        shader = new Shader(
                fileUtils.readFileFromAssets(this.getClass(), "/vertex_shader.glsl"),
                fileUtils.readFileFromAssets(this.getClass(), "/fragment_shader.glsl"),
                this, new MainShaderAdaptor());

        matrix = Matrix.resetTranslateMatrix(matrix);

        camera = new Camera(x, Utils.y);
        camera.resetFor2d();

        simplePolygon = new SimplePolygon(this::redraw, true, 0, this);
        shape = new Shape("/shape/ponchik.obj", "/shape/texture.png", this, this.getClass());

        skyBox = new SkyBox("/skybox/", "jpg", this);

        skyBoxShader = new Shader(
                fileUtils.readFileFromAssets(this.getClass(), "/skybox/skybox_vertex.glsl"),
                fileUtils.readFileFromAssets(this.getClass(), "/skybox/skybox_fragment.glsl"),
                this, new MainShaderAdaptor());

        fb = new FrameBuffer((int) x, (int) Utils.y, this);
    }

    @Override
    public void draw() {
        Utils.background(255, 255, 255);
        fb.apply();
        skyBoxShader.apply();

        camera.resetFor3d();
        camera.apply();

        skyBox.prepareAndDraw();


        shader.apply();
        camera.apply();
        Matrix.rotateM(matrix, 0, engine.pageMillis() / 10.0f, 0, 1, 1);
        Matrix.applyMatrix(matrix);
        shape.prepareAndDraw();
        fb.connectDefaultFrameBuffer();

        engine.glClear();
        camera.resetFor2d();
        camera.apply();
        matrix = Matrix.resetTranslateMatrix(matrix);
        Matrix.applyMatrix(matrix);
        fb.drawTexture(new PVector(0, 0, 1), new PVector(x, 0, 1), new PVector(0, y, 1));
        simplePolygon.prepareAndDraw((engine.pageMillis() / 100.0f + 100.0f) * kx, (engine.pageMillis() / 100.0f + 100.0f) * ky, 300 * kx, 1.1f);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    private PImage redraw(List<Object> params) {
        PImage image = new PImage(200, 200);
        image.background(255, 0, 255, 255);
        return image;
    }
}

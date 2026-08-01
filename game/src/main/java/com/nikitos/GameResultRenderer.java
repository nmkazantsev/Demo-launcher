package com.nikitos;

import static com.nikitos.maths.Matrix.applyMatrix;
import static com.nikitos.maths.Matrix.resetTranslateMatrix;

import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.main.camera.Camera;
import com.nikitos.main.images.PImage;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.shaders.default_adaptors.MainShaderAdaptor;
import com.nikitos.main.touch.TouchProcessor;
import com.nikitos.main.vertices.SimplePolygon;
import com.nikitos.platformBridge.ErrorPrinter;
import com.nikitos.utils.Utils;

import java.util.List;
import java.util.Locale;



public class GameResultRenderer extends GamePageClass {
    private final Shader shader;
    private Camera camera;
    private final SimplePolygon resultPolygon;

    private final float[] matrix = new float[16];

    private String resultText = "NO RESULT";


    public GameResultRenderer(String resultText) {
        this.resultText = resultText;

        shader = new Shader("vertex_shader.glsl", "fragment_shader.glsl", this, new MainShaderAdaptor());
        TouchProcessor restartTouch = new TouchProcessor(
                touchPoint -> true,
                touchPoint -> {
                    CoreRenderer.engine.startNewPage(new MainRenderer());
                    return null;
                },
                null,
                null,
                this
        );
        resultPolygon = new SimplePolygon(this::redrawResultPage, true, 0, this);
        ErrorPrinter errorPrinter = CoreRenderer.engine.getPlatformBridge().getErrorPrinter();
        errorPrinter.printOpenGLState();
       errorPrinter.checkGLErrors("setup");
    }

    @Override
    public void onSurfaceChanged(int i, int i1) {
        camera = new Camera(i,i1);
        camera.resetFor2d();
    }

    @Override
    public void update(float dtMillis) {
    }

    @Override
    public void render() {
        Utils.background(240, 240, 240);
        shader.apply();
        applyMatrix(resetTranslateMatrix(matrix));
        camera.resetFor2d();
        camera.apply();

       // CoreRenderer.engine.getPlatformBridge().print(String.valueOf(Utils.getX()) + " " + Utils.getY());
        resultPolygon.prepareAndDraw(0, 0, 0, Utils.getX(), Utils.getY(), 1);

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    private PImage redrawResultPage(List<Object> params) {
        int width = (int) Utils.getX();
        int height = (int) Utils.getY();
        PImage image = new PImage(width, height);
        image.background(240, 240, 240);
        image.fill(0);
        image.textSize(80 * Utils.getKx());
        float resultWidth = image.getTextWidth(resultText);
        image.text(resultText, width / 2.0f - resultWidth / 2.0f, height / 2.0f - 60 * Utils.getKy());
        String restartText = "click to restart";
        image.textSize(40 * Utils.getKx());
        float restartWidth = image.getTextWidth(restartText);
        image.text(restartText, width / 2.0f - restartWidth / 2.0f, height / 2.0f + 40 * Utils.getKy());
        return image;
    }
}

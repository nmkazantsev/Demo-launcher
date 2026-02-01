package com.nikitos;

import com.nikitos.main.camera.Camera;
import com.nikitos.main.debugger.Axes;
import com.nikitos.main.frameBuffers.FrameBuffer;
import com.nikitos.main.images.PImage;
import com.nikitos.main.light.AmbientLight;
import com.nikitos.main.light.DirectedLight;
import com.nikitos.main.light.Material;
import com.nikitos.main.light.SourceLight;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.shaders.default_adaptors.LightShaderAdaptor;
import com.nikitos.main.shaders.default_adaptors.MainShaderAdaptor;
import com.nikitos.main.vertices.Shape;
import com.nikitos.main.vertices.SimplePolygon;
import com.nikitos.main.vertices.SkyBox;
import com.nikitos.maths.Matrix;
import com.nikitos.maths.PVector;
import com.nikitos.platformBridge.PlatformBridge;
import com.nikitos.utils.FileUtils;
import com.nikitos.utils.Utils;

import java.util.List;

import static com.nikitos.utils.Utils.*;


public class MainRenderer extends GamePageClass {
    private final Engine engine;
    private final Shader shader, lightShader;
    private final PlatformBridge pb;

    private float[] matrix = new float[16];

    private final Camera camera;

    private final SimplePolygon simplePolygon;
    private final Shape shape;

    private final SkyBox skyBox;
    private final Shader skyBoxShader;

    private final FrameBuffer fb;

    private final SourceLight sourceLight;
    private final AmbientLight ambientLight;
    private final DirectedLight directedLight1;
    private final Material material;

    private final Axes axes;
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
        shape.addNormalMap("/shape/normal_tex_smooth.png");

        skyBox = new SkyBox("/skybox/", "jpg", this);

        skyBoxShader = new Shader(
                fileUtils.readFileFromAssets(this.getClass(), "/skybox/skybox_vertex.glsl"),
                fileUtils.readFileFromAssets(this.getClass(), "/skybox/skybox_fragment.glsl"),
                this, new MainShaderAdaptor());

        lightShader = new Shader(
                fileUtils.readFileFromAssets(this.getClass(), "/shape/vertex_shader_light.glsl"),
                fileUtils.readFileFromAssets(this.getClass(), "/shape/fragment_shader_light.glsl"),
                this, new LightShaderAdaptor());


        fb = new FrameBuffer((int) x, (int) Utils.y, this);

        ambientLight = new AmbientLight(this);
        // ambientLight.color = new Vec3(0.3f, 0.3f, 0.3f);

        directedLight1 = new DirectedLight(this);
        directedLight1.direction = new PVector(-1, 0, 0);
        directedLight1.color = new PVector(0.9f);
        directedLight1.diffuse = 0.2f;
        directedLight1.specular = 0.8f;
       /* directedLight2 = new DirectedLight(this);
        directedLight2.direction = new Vec3(0, 1, 0);
        directedLight2.color = new Vec3(0.6f);
        directedLight2.diffuse = 0.9f;
        directedLight2.specular = 0.8f;

        */
        sourceLight = new SourceLight(this);
        sourceLight.diffuse = 0.8f;
        sourceLight.specular = 0.9f;
        sourceLight.constant = 1f;
        sourceLight.linear = 0.01f;
        sourceLight.quadratic = 0.01f;
        sourceLight.color = new PVector(0.5f);
        sourceLight.position = new PVector(5f, 0, 0);
        sourceLight.direction = new PVector(-0.3f, 0, 0);
        sourceLight.outerCutOff = cos(radians(40));
        sourceLight.cutOff = cos(radians(30f));

        material = new Material(this);
        material.ambient = new PVector(1);
        material.specular = new PVector(1);
        material.diffuse = new PVector(1);
        material.shininess = 1.1f;

        axes = new Axes(this);
    }

    @Override
    public void draw() {
        Utils.background(255, 255, 255);
        fb.apply();
        skyBoxShader.apply();

        camera.resetFor3d();
        camera.cameraSettings.eyeZ = 5;
        camera.apply();

        skyBox.prepareAndDraw();

        lightShader.apply();
        material.apply();
        camera.apply();
        Matrix.applyMatrix(matrix);
        axes.drawAxes(6,0.5f, 0.2f,null, camera);
        Matrix.rotateM(matrix, 0, engine.pageMillis() / 50.0f, 0, 1, 1);
        Matrix.applyMatrix(matrix);
        shape.prepareAndDraw();
        fb.connectDefaultFrameBuffer();
        shader.apply();
        engine.glClear();
        camera.resetFor2d();
        camera.apply();
        matrix = Matrix.resetTranslateMatrix(matrix);
        Matrix.applyMatrix(matrix);
        fb.drawTexture(new PVector(0, 0, 1), new PVector(x, 0, 1), new PVector(0, y, 1));
        simplePolygon.prepareAndDraw((engine.pageMillis() / 100.0f + 100.0f) * kx, (engine.pageMillis() / 100.0f + 100.0f) * ky, 30 * kx, 1.1f);
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

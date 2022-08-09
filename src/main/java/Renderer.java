import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import Utils.Maths;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    private Framebuffer framebuffer;

    private ShaderProgram shaderProgram;
    private ShaderProgram lightCubeShader;
    private ShaderProgram skyboxShader;
    private ShaderProgram hdrShader;

    private float FOV = (float) Math.toRadians(60.0);
    private float aspectRatio;
    private boolean wireframe;
    private boolean isNormalMapping = false;
    private float exposure = 1.0f;

    private final float Z_NEAR = 0.1f;
    private final float Z_FAR = 100f;
    private static final int MAX_POINT_LIGHTS = 2;
    private static final int MAX_SPOT_LIGHTS = 1;
    private Matrix4f projection;

    public void init(Window window) throws Exception {
        framebuffer = new Framebuffer(new Texture(window.getWidth(), window.getHeight(), Texture.Format.RGBA16F, Texture.Format.RGBA));

        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Files.readString(new File("src/main/resources/shaders/vertex.vs").toPath(), StandardCharsets.US_ASCII));
        shaderProgram.createFragmentShader(Files.readString(new File("src/main/resources/shaders/fragment_alt.glsl").toPath(), StandardCharsets.US_ASCII));
        shaderProgram.link();

        shaderProgram.createUniform("model");
        shaderProgram.createUniform("view");

        aspectRatio = (float) window.getWidth() / window.getHeight();
        projection = new Matrix4f().setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
        shaderProgram.createUniform("projection");

        shaderProgram.createUniform("viewPos");
        shaderProgram.createUniform("isNormalMapping");

        // Light uniforms
        shaderProgram.createMaterialUniform("material");
//        shaderProgram.createDirLightUniform("dirLight");
        shaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
//        shaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);

        // Light cube shader
        lightCubeShader = new ShaderProgram();
        lightCubeShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/light_cube.vs").toPath(), StandardCharsets.US_ASCII));
        lightCubeShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/light_cube.fs").toPath(), StandardCharsets.US_ASCII));
        lightCubeShader.link();

        lightCubeShader.createUniform("model");
        lightCubeShader.createUniform("view");
        lightCubeShader.createUniform("projection");

        // Skybox shader
        skyboxShader = new ShaderProgram();
        skyboxShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/skybox.vs").toPath(), StandardCharsets.US_ASCII));
        skyboxShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/skybox.fs").toPath(), StandardCharsets.US_ASCII));
        skyboxShader.link();

        skyboxShader.createUniform("view");
        skyboxShader.createUniform("projection");

        // HDR shader
        hdrShader = new ShaderProgram();
        hdrShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/hdr.vert").toPath(), StandardCharsets.US_ASCII));
        hdrShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/hdr.frag").toPath(), StandardCharsets.US_ASCII));
        hdrShader.link();

        hdrShader.createUniform("hdrBuffer");
        hdrShader.createUniform("exposure");
    }

    public void render(Camera camera, Scene scene) {
        Entity[] entities = scene.getEntities();
        DirLight dirLight = scene.getDirLight();
        PointLight[] pointLights = scene.getPointLights();
        SpotLight[] spotLights = scene.getSpotLights();
        Skybox skybox = scene.getSkybox();

        // First pass: render scene to floating point framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.getID());
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (wireframe) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        } else {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }

        shaderProgram.bind();

        Matrix4f view = camera.calculateViewMatrix();
        projection = new Matrix4f().setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
        shaderProgram.setUniform("view", view);
        shaderProgram.setUniform("projection", projection);

        shaderProgram.setUniform("viewPos", camera.getPosition());
        shaderProgram.setUniform("isNormalMapping", isNormalMapping);

//        // Update directional light uniforms
//        shaderProgram.setUniform("dirLight.direction", dirLight.getDirection());
//        shaderProgram.setUniform("dirLight.ambient", dirLight.getAmbient());
//        shaderProgram.setUniform("dirLight.diffuse", dirLight.getDiffuse());
//        shaderProgram.setUniform("dirLight.specular", dirLight.getSpecular());

        // Update point light uniforms
        for (int i = 0; i < pointLights.length; i++) {
            shaderProgram.setUniform("pointLights[" + i + "].position", pointLights[i].getPosition());
            shaderProgram.setUniform("pointLights[" + i + "].ambient", pointLights[i].getAmbient());
            shaderProgram.setUniform("pointLights[" + i + "].diffuse", pointLights[i].getDiffuse());
            shaderProgram.setUniform("pointLights[" + i + "].specular", pointLights[i].getSpecular());
        }

//        // Update spotlight uniforms
//        for (int i = 0; i < spotLights.length; i++) {
//            shaderProgram.setUniform("spotLights[" + i + "].position",    spotLights[i].getPosition());
//            shaderProgram.setUniform("spotLights[" + i + "].direction",   spotLights[i].getDirection());
//            shaderProgram.setUniform("spotLights[" + i + "].cutoff",      spotLights[i].getCutoff());
//            shaderProgram.setUniform("spotLights[" + i + "].outerCutoff", spotLights[i].getOuterCutoff());
//            shaderProgram.setUniform("spotLights[" + i + "].ambient",     spotLights[i].getAmbient());
//            shaderProgram.setUniform("spotLights[" + i + "].diffuse",     spotLights[i].getDiffuse());
//            shaderProgram.setUniform("spotLights[" + i + "].specular",    spotLights[i].getSpecular());
//            shaderProgram.setUniform("spotLights[" + i + "].enabled",     spotLights[i].isEnabled());
//        }

        // Render containers
        for (Entity entity : entities) {
            // Material uniforms
            shaderProgram.setUniform("material.diffuse", 0);
            shaderProgram.setUniform("material.specular", 1);
            shaderProgram.setUniform("material.shininess", entity.getMesh().getMaterial().getShininess());
            shaderProgram.setUniform("material.normalMap", 2);

            Matrix4f model = Maths.calculateModelMatrix(entity.getPosition(), entity.getRotation(), entity.getScale());
            shaderProgram.setUniform("model", model);
            entity.getMesh().render();
        }

        shaderProgram.unbind();

        // Render lights
        lightCubeShader.bind();
        lightCubeShader.setUniform("view", view);
        lightCubeShader.setUniform("projection", projection);
        lightCubeShader.setUniform("view", view);

        for (PointLight light : pointLights) {
            Matrix4f model = new Matrix4f();
            model.translate(light.getPosition());
            model.scale(0.5f);
            lightCubeShader.setUniform("model", model);

            light.getMesh().render();
        }

        lightCubeShader.unbind();

//        // Render skybox
//        view = new Matrix4f(new Matrix3f(camera.getView()));  // Remove translation from view matrix
//        skyboxShader.bind();
//        skyboxShader.setUniform("view", view);
//        skyboxShader.setUniform("projection", projection);
//
//        skybox.render();
//
//        skyboxShader.unbind();


        // Second pass: render floating point framebuffer to default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glDisable(GL_DEPTH_TEST);

        glClearColor(0.5f, 1.0f, 0.5f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        // Render framebuffer to screen
        hdrShader.bind();

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, framebuffer.getTexture().getID());
        hdrShader.setUniform("hdrBuffer", 0);
        hdrShader.setUniform("exposure", exposure);

        // Render quad
        float[] quadVertices = {
            // positions        // texture coords
           -1.0f,  1.0f, 0.0f,  0.0f, 1.0f,
           -1.0f, -1.0f, 0.0f,  0.0f, 0.0f,
            1.0f,  1.0f, 0.0f,  1.0f, 1.0f,
            1.0f, -1.0f, 0.0f,  1.0f, 0.0f,
        };

        int quadVAO = glGenVertexArrays();
        glBindVertexArray(quadVAO);

        int quadVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, quadVBO);
        glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindVertexArray(quadVAO);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        glBindVertexArray(0);
    }

    public void cleanup() {
        shaderProgram.cleanup();
    }

    public void setWireframe(boolean wireframe) {
        this.wireframe = wireframe;
    }

    public boolean isWireframe() {
        return wireframe;
    }

    public void setFOV(float FOV) {
        this.FOV = FOV;
    }

    public boolean isNormalMapping() {
        return isNormalMapping;
    }

    public void setNormalMapping(boolean normalMapping) {
        isNormalMapping = normalMapping;
    }

    public float getExposure() {
        return exposure;
    }

    public void setExposure(float exposure) {
        this.exposure = exposure;
    }
}

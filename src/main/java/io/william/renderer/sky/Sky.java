package io.william.renderer.sky;

import io.william.renderer.*;
import io.william.util.renderer.Cube;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL30.*;

public class Sky {

    private ShaderProgram transmittanceShader;
    private ShaderProgram multipleScatteringShader;
    private ShaderProgram skyViewShader;
    private ShaderProgram backgroundShader;
    private ShaderProgram cubemapShader;

    private Texture transmittance;
    private Texture scattering;
    private Texture skyView;

    private int backgroundCubemap;
    private int cubemapResolution = 512;

    private final Framebuffer framebuffer;

    private Vector2i transmittanceLUTRes = new Vector2i(256, 64);
    private Vector2i scatteringLUTRes = new Vector2i(32, 32);
    private Vector2i skyViewRes = new Vector2i(200, 200);

    private float time = 10.0f;
    private boolean automaticTime = false;
    private float timeScale = 5.0f;

    private boolean updated = true;
    private float lastTime;

    public Sky() throws Exception {
        transmittanceShader = new ShaderProgram("transmittance");
        transmittanceShader.createVertexShader("src/main/resources/shaders/sky/quad.vert");
        transmittanceShader.createFragmentShader("src/main/resources/shaders/sky/transmittance.frag");
        transmittanceShader.link();

        multipleScatteringShader = new ShaderProgram("multiple_scattering");
        multipleScatteringShader.createVertexShader("src/main/resources/shaders/sky/quad.vert");
        multipleScatteringShader.createFragmentShader("src/main/resources/shaders/sky/scattering.frag");
        multipleScatteringShader.link();

        multipleScatteringShader.createUniform("transmittanceLUT");
        multipleScatteringShader.createUniform("transmittanceLUTRes");

        skyViewShader = new ShaderProgram("sky_view");
        skyViewShader.createVertexShader("src/main/resources/shaders/sky/quad.vert");
        skyViewShader.createFragmentShader("src/main/resources/shaders/sky/sky_view.frag");
        skyViewShader.link();

        skyViewShader.createUniform("transmittanceLUT");
        skyViewShader.createUniform("transmittanceLUTRes");
        skyViewShader.createUniform("multiScattLUT");
        skyViewShader.createUniform("multiScattLUTRes");
        skyViewShader.createUniform("iTime");

        // All textures have linear filtering and clamp wrapping
        transmittance = new Texture(transmittanceLUTRes.x, transmittanceLUTRes.y, GL_RGBA32F, GL_RGBA, GL_FLOAT);
        transmittance.bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        scattering = new Texture(scatteringLUTRes.x, scatteringLUTRes.y, GL_RGBA32F, GL_RGBA, GL_FLOAT);
        scattering.bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        skyView = new Texture(skyViewRes.x, skyViewRes.y, GL_RGBA32F, GL_RGBA, GL_FLOAT);
        skyView.bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        framebuffer = new Framebuffer(transmittance, GL_COLOR_ATTACHMENT0, true);

        // Set up background shader
        backgroundShader = new ShaderProgram("Atmosphere Background");
        backgroundShader.createVertexShader("src/main/resources/shaders/sky/background.vert");
        backgroundShader.createFragmentShader("src/main/resources/shaders/sky/background.frag");
        backgroundShader.link();

        backgroundShader.createUniform("transmittanceLUT");
        backgroundShader.createUniform("transmittanceLUTRes");
        backgroundShader.createUniform("skyViewLUT");
        backgroundShader.createUniform("skyViewLUTRes");

        backgroundShader.createUniform("cameraPos");
        backgroundShader.createUniform("cameraDir");
        backgroundShader.createUniform("projection");
        backgroundShader.createUniform("view");
        backgroundShader.createUniform("iTime");

        // Set up cubemap shader
        cubemapShader = new ShaderProgram("Atmosphere Cubemap");
        cubemapShader.createVertexShader("src/main/resources/shaders/cubemap.vert");
        cubemapShader.createFragmentShader("src/main/resources/shaders/sky/cubemap.frag");
        cubemapShader.link();

        cubemapShader.createUniform("transmittanceLUT");
        cubemapShader.createUniform("transmittanceLUTRes");
        cubemapShader.createUniform("skyViewLUT");
        cubemapShader.createUniform("skyViewLUTRes");

        cubemapShader.createUniform("projection");
        cubemapShader.createUniform("view");
        cubemapShader.createUniform("direction");
        cubemapShader.createUniform("iTime");

        cubemapShader.createUniform("camFOV");

        backgroundCubemap = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, backgroundCubemap);

        for (int i = 0; i < 6; i++) {
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA32F, cubemapResolution, cubemapResolution, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
        }

        glGenerateMipmap(GL_TEXTURE_CUBE_MAP);

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    public void renderLUTs() {
        // Render transmittance LUT
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.getID());
        framebuffer.attachTexture2D(GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, transmittance.getID());

        // Render quad
        glViewport(0, 0, transmittanceLUTRes.x, transmittanceLUTRes.y);
        transmittanceShader.bind();
        glClear(GL_COLOR_BUFFER_BIT);
        io.william.util.renderer.Quad.render();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Render multiple scattering LUT
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.getID());
        framebuffer.attachTexture2D(GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, scattering.getID());

        // Render quad
        glViewport(0, 0, scatteringLUTRes.x, scatteringLUTRes.y);

        multipleScatteringShader.bind();
        multipleScatteringShader.setUniform("transmittanceLUT", 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, transmittance.getID());
        multipleScatteringShader.setUniform("transmittanceLUTRes", new Vector2f(transmittanceLUTRes.x, transmittanceLUTRes.y));

        glClear(GL_COLOR_BUFFER_BIT);
        io.william.util.renderer.Quad.render();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Render sky-view LUT
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.getID());
        framebuffer.attachTexture2D(GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, skyView.getID());

        // Render quad
        glViewport(0, 0, skyViewRes.x, skyViewRes.y);

        skyViewShader.bind();
        skyViewShader.setUniform("transmittanceLUT", 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, transmittance.getID());
        skyViewShader.setUniform("transmittanceLUTRes", new Vector2f(transmittanceLUTRes.x, transmittanceLUTRes.y));

        skyViewShader.setUniform("multiScattLUT", 1);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, scattering.getID());
        skyViewShader.setUniform("multiScattLUTRes", new Vector2f(scatteringLUTRes.x, scatteringLUTRes.y));

        skyViewShader.setUniform("iTime", time);

        glClear(GL_COLOR_BUFFER_BIT);
        io.william.util.renderer.Quad.render();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void render(Camera camera, Matrix4f projection, ShaderSettings settings) {
        if (automaticTime) {
            time = (float) glfwGetTime() * timeScale;
        }

        // Check if time has been updated
        if (lastTime != time) {
            updated = true;
            lastTime = time;
        }

        backgroundShader.bind();

        backgroundShader.setUniform("transmittanceLUT", 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, transmittance.getID());
        backgroundShader.setUniform("transmittanceLUTRes", new Vector2f(transmittanceLUTRes.x, transmittanceLUTRes.y));

        backgroundShader.setUniform("skyViewLUT", 1);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, skyView.getID());
        backgroundShader.setUniform("skyViewLUTRes", new Vector2f(skyViewRes.x, skyViewRes.y));

        backgroundShader.setUniform("cameraPos", camera.getPosition());
        backgroundShader.setUniform("cameraDir", camera.getFront());
        backgroundShader.setUniform("projection", projection);
        backgroundShader.setUniform("view", camera.calculateViewMatrix());
        backgroundShader.setUniform("iTime", time);

        Cube.render();

        // Render again to a cubemap so that it can be used with IBL
        cubemapShader.bind();

        cubemapShader.setUniform("transmittanceLUT", 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, transmittance.getID());
        cubemapShader.setUniform("transmittanceLUTRes", new Vector2f(transmittanceLUTRes.x, transmittanceLUTRes.y));

        cubemapShader.setUniform("skyViewLUT", 1);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, skyView.getID());
        cubemapShader.setUniform("skyViewLUTRes", new Vector2f(skyViewRes.x, skyViewRes.y));

        cubemapShader.setUniform("projection", projection);
        cubemapShader.setUniform("view", camera.calculateViewMatrix());
        cubemapShader.setUniform("iTime", time);

        cubemapShader.setUniform("camFOV", (float) Math.toRadians(settings.getCubemapCamFOV()));

        Matrix4f captureProjection = new Matrix4f().setPerspective((float) Math.toRadians(90.0f), 1.0f, 0.1f, 10.0f);

        Matrix4f[] views = new Matrix4f[]{
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  1.0f,  0.0f,  0.0f, 0.0f, -1.0f,  0.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f, -1.0f,  0.0f,  0.0f, 0.0f, -1.0f,  0.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  0.0f,  1.0f,  0.0f, 0.0f,  0.0f,  1.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  0.0f, -1.0f,  0.0f, 0.0f,  0.0f, -1.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  0.0f,  0.0f,  1.0f, 0.0f, -1.0f,  0.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  0.0f,  0.0f, -1.0f, 0.0f, -1.0f,  0.0f)
        };

        Vector3f[] directions = new Vector3f[]{
            new Vector3f(1.0f,  0.0f,  0.0f),
            new Vector3f(-1.0f, 0.0f,  0.0f),
            new Vector3f(0.001f,  1.0f,  0.0f),
            new Vector3f(0.001f, -1.0f,  0.0f),
            new Vector3f(0.0f,  0.0f,  1.0f),
            new Vector3f(0.0f,  0.0f, -1.0f)
        };

        cubemapShader.setUniform("projection", captureProjection);

        glViewport(0, 0, cubemapResolution, cubemapResolution);
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.getID());
        for (int i = 0; i < 6; i++) {
            cubemapShader.setUniform("view", views[i]);
            cubemapShader.setUniform("direction", directions[i]);
            framebuffer.attachTexture2D(GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, backgroundCubemap);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            Cube.render();
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public int getTransmittanceID() {
        return transmittance.getID();
    }

    public int getScatteringID() {
        return scattering.getID();
    }

    public int getSkyViewID() {
        return skyView.getID();
    }

    public int getBackgroundCubemapID() {
        return backgroundCubemap;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public boolean isAutomaticTime() {
        return automaticTime;
    }

    public void setAutomaticTime(boolean automaticTime) {
        this.automaticTime = automaticTime;
    }

    public float getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(float timeScale) {
        this.timeScale = timeScale;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }
}

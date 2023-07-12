package io.william.renderer.sky;

import io.william.renderer.*;
import io.william.util.renderer.Cube;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

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

    private Texture transmittance;
    private Texture scattering;
    private Texture skyView;

    private Framebuffer framebuffer;

    private Vector2i transmittanceLUTRes = new Vector2i(256, 64);
    private Vector2i scatteringLUTRes = new Vector2i(32, 32);
    private Vector2i skyViewRes = new Vector2i(200, 200);

    private float time = 0.0f;
    private boolean automaticTime = true;
    private float timeScale = 5.0f;

    public Sky() throws Exception {
        transmittanceShader = new ShaderProgram("transmittance");
        transmittanceShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/sky/quad.vert").toPath(), StandardCharsets.US_ASCII));
        transmittanceShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/sky/transmittance.frag").toPath(), StandardCharsets.US_ASCII));
        transmittanceShader.link();

        multipleScatteringShader = new ShaderProgram("multiple_scattering");
        multipleScatteringShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/sky/quad.vert").toPath(), StandardCharsets.US_ASCII));
        multipleScatteringShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/sky/scattering.frag").toPath(), StandardCharsets.US_ASCII));
        multipleScatteringShader.link();

        multipleScatteringShader.createUniform("transmittanceLUT");
        multipleScatteringShader.createUniform("transmittanceLUTRes");

        skyViewShader = new ShaderProgram("sky_view");
        skyViewShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/sky/quad.vert").toPath(), StandardCharsets.US_ASCII));
        skyViewShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/sky/sky_view.frag").toPath(), StandardCharsets.US_ASCII));
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
        backgroundShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/sky/background.vert").toPath(), StandardCharsets.US_ASCII));
        backgroundShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/sky/background.frag").toPath(), StandardCharsets.US_ASCII));
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

        skyViewShader.setUniform("iTime", automaticTime ? (float) glfwGetTime() * timeScale : time);

        glClear(GL_COLOR_BUFFER_BIT);
        io.william.util.renderer.Quad.render();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void render(Camera camera, Matrix4f projection) {
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
        backgroundShader.setUniform("iTime", automaticTime ? (float) glfwGetTime() * timeScale : time);

        Cube.render();
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
}

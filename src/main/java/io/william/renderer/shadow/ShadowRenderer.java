package io.william.renderer.shadow;

import io.william.renderer.*;
import io.william.util.Maths;
import org.joml.Matrix4f;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class ShadowRenderer {

    private final ShaderProgram shaderProgram;
    private final Framebuffer framebuffer;

    private final int resolution = 4096;

    private final Texture texture;

    private Matrix4f lightSpaceMatrix;

    private float nearPlane = -40.0f;
    private float farPlane = 40.0f;
    private float size = 40.0f;

    public ShadowRenderer() throws Exception {
        shaderProgram = new ShaderProgram("DirectionalShadow");
        shaderProgram.createVertexShader(Files.readString(new File("src/main/resources/shaders/shadow/shadow.vert").toPath(), StandardCharsets.US_ASCII));
        shaderProgram.createFragmentShader(Files.readString(new File("src/main/resources/shaders/shadow/shadow.frag").toPath(), StandardCharsets.US_ASCII));
        shaderProgram.link();

        shaderProgram.createUniform("model");
        shaderProgram.createUniform("lightSpaceMatrix");

        texture = new Texture(resolution, resolution, GL_DEPTH_COMPONENT, GL_DEPTH_COMPONENT, GL_FLOAT);
        texture.bind();

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, new float[] {1.0f, 1.0f, 1.0f, 1.0f});

        framebuffer = new Framebuffer(texture, GL_DEPTH_ATTACHMENT, false);
    }

    public void render(Scene scene) {
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);

        glViewport(0, 0, resolution, resolution);
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.getID());
        glClear(GL_DEPTH_BUFFER_BIT);
        glCullFace(GL_FRONT);

        Matrix4f lightProjection = new Matrix4f().ortho(-size, size, -size, size, nearPlane, farPlane);
        Matrix4f lightView = new Matrix4f().lookAt(scene.getDirLight().getDirection().normalize(), new org.joml.Vector3f(0, 0, 0), new org.joml.Vector3f(0, 1, 0));
        lightSpaceMatrix = new Matrix4f().mul(lightProjection).mul(lightView);

        shaderProgram.bind();

        shaderProgram.setUniform("lightSpaceMatrix", lightSpaceMatrix);

        for (Entity entity : scene.getEntities()) {
            if (entity.getMaterialMeshes() != null && entity.getMaterialMeshes().length > 0) {
                shaderProgram.setUniform("model", Maths.calculateModelMatrix(entity.getPosition(), entity.getRotation(), entity.getScale()));
                entity.render();
            }
        }

        glCullFace(GL_BACK);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        shaderProgram.unbind();

        glDepthFunc(GL_LEQUAL);
    }

    public int getResolution() {
        return resolution;
    }

    public Matrix4f getLightSpaceMatrix() {
        return lightSpaceMatrix;
    }

    public int getTextureID() {
        return texture.getID();
    }

    public float getNearPlane() {
        return nearPlane;
    }

    public void setNearPlane(float nearPlane) {
        this.nearPlane = nearPlane;
    }

    public float getFarPlane() {
        return farPlane;
    }

    public void setFarPlane(float farPlane) {
        this.farPlane = farPlane;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }
}

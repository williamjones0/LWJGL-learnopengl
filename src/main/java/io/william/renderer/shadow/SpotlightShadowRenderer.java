package io.william.renderer.shadow;

import io.william.renderer.*;
import io.william.util.Maths;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL43.glMultiDrawElementsIndirect;

public class SpotlightShadowRenderer {

    private final ShaderProgram shaderProgram;
    private final Framebuffer framebuffer;

    private final int resolution = 4096;

    private final TextureArray textureArray;

    private Matrix4f lightSpaceMatrix;

    private float nearPlane = 0.0f;
    private float farPlane = 200.0f;

    public SpotlightShadowRenderer() throws Exception {
        shaderProgram = new ShaderProgram("SpotlightShadow");
        shaderProgram.createVertexShader(Files.readString(new File("src/main/resources/shaders/shadow/spotlight/shadow.vert").toPath(), StandardCharsets.US_ASCII));
        shaderProgram.createFragmentShader(Files.readString(new File("src/main/resources/shaders/shadow/spotlight/shadow.frag").toPath(), StandardCharsets.US_ASCII));
        shaderProgram.link();

//        shaderProgram.createUniform("lightPos");
//        shaderProgram.createUniform("farPlane");
        shaderProgram.createUniform("textureLayer");
        shaderProgram.createUniform("lightSpaceMatrix");

        final int NUM_SPOT_LIGHTS = 4;

        textureArray = new TextureArray(resolution, resolution, NUM_SPOT_LIGHTS, GL_TEXTURE_2D_ARRAY);
        textureArray.bind();

        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        glTexParameterfv(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BORDER_COLOR, new float[]{1.0f, 1.0f, 1.0f, 1.0f});

        framebuffer = new Framebuffer(textureArray, GL_DEPTH_ATTACHMENT);
    }

    public void render(Scene scene, SceneMesh sceneMesh, int indirectBuffer, int drawCount) {
        System.out.println("Rendering spotlight shadows");
        // Early exit
        if (scene.getSpotLights().isEmpty() || scene.getSpotLights().size() == 0 || scene.getEntities().isEmpty() || scene.getEntities().size() == 0) {
            System.out.println("Spot early exit 1");
            return;
        }

        boolean exit = true;
        for (SpotLight light : scene.getSpotLights()) {
            if (light.isEnabled()) {
                exit = false;
                break;
            }
        }

        if (exit) {
            System.out.println("Spot early exit 2");
            return;
        }

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);

        glViewport(0, 0, resolution, resolution);
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.getID());
        glClear(GL_DEPTH_BUFFER_BIT);
        glCullFace(GL_FRONT);

//        Matrix4f shadowProj = new Matrix4f().perspective((float) Math.toRadians(90.0f), 1.0f, nearPlane, farPlane);
        Matrix4f shadowProj = new Matrix4f().ortho(-100, 100, -100, 100, nearPlane, farPlane);

        shaderProgram.bind();
        final int numSpotLights = scene.getSpotLights().size();
        for (int i = 0; i < numSpotLights; i++) {
            if (!scene.getSpotLights().get(i).isEnabled()) {
                System.out.println("Spotlight " + i + " is disabled");
                continue;
            }

            Vector3f lightPos = scene.getSpotLights().get(i).getPosition();
            Vector3f lightDir = scene.getSpotLights().get(i).getDirection().normalize();

//            shaderProgram.setUniform("lightPos", lightPos);
//            shaderProgram.setUniform("farPlane", farPlane);
            shaderProgram.setUniform("textureLayer", i);

            Matrix4f lightView = new Matrix4f().lookAt(lightPos, new Vector3f(lightPos).add(lightDir), new Vector3f(0, 1, 0));
            lightSpaceMatrix = new Matrix4f().mul(shadowProj).mul(lightView);

            shaderProgram.setUniform("lightSpaceMatrix", lightSpaceMatrix);

            // Render entities (indirect drawing)
            glBindBuffer(GL_DRAW_INDIRECT_BUFFER, indirectBuffer);
            glBindVertexArray(sceneMesh.getVAO());
            glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, 0, drawCount, 20);
            glBindVertexArray(0);
        }

        glCullFace(GL_BACK);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        shaderProgram.unbind();

        glDepthFunc(GL_LEQUAL);
    }

    public Matrix4f getLightSpaceMatrix() {
        return lightSpaceMatrix;
    }

    public int getTextureArrayID() {
        return textureArray.getID();
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
}

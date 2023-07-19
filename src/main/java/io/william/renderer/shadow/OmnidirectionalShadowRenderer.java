package io.william.renderer.shadow;

import io.william.renderer.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL40.GL_TEXTURE_CUBE_MAP_ARRAY;
import static org.lwjgl.opengl.GL43.*;

public class OmnidirectionalShadowRenderer {

    private final ShaderProgram shaderProgram;
    private final Framebuffer framebuffer;

    private final int resolution = 512;

    private final TextureArray textureArray;

    private final int matricesUBO;

    private float nearPlane = 0.0f;
    private float farPlane = 200.0f;

    public OmnidirectionalShadowRenderer() throws Exception {
        shaderProgram = new ShaderProgram("OmnidirectionalShadow");
        shaderProgram.createVertexShader("src/main/resources/shaders/shadow/omnidirectional/shadow.vert");
        shaderProgram.createGeometryShader("src/main/resources/shaders/shadow/omnidirectional/shadow.geom");
        shaderProgram.createFragmentShader("src/main/resources/shaders/shadow/omnidirectional/shadow.frag");
        shaderProgram.link();

        shaderProgram.createUniform("lightPos");
        shaderProgram.createUniform("farPlane");
        shaderProgram.createUniform("cubemapLayer");

        final int NUM_POINT_LIGHTS = 8;

        textureArray = new TextureArray(resolution, resolution, 6 * NUM_POINT_LIGHTS, GL_TEXTURE_CUBE_MAP_ARRAY);  // 6 layer-faces per light
        textureArray.bind();

        glTexParameteri(GL_TEXTURE_CUBE_MAP_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_CUBE_MAP_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_CUBE_MAP_ARRAY, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_CUBE_MAP_ARRAY, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        glTexParameterfv(GL_TEXTURE_CUBE_MAP_ARRAY, GL_TEXTURE_BORDER_COLOR, new float[]{1.0f, 1.0f, 1.0f, 1.0f});

        framebuffer = new Framebuffer(textureArray, GL_DEPTH_ATTACHMENT);

        matricesUBO = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, matricesUBO);
        glBufferData(GL_UNIFORM_BUFFER, 4 * 6 * 16 * Float.BYTES, GL_STATIC_DRAW);  // 4 shadow maps max, 6 views, 16 floats per matrix
        glBindBufferBase(GL_UNIFORM_BUFFER, 0, matricesUBO);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

    public void render(Scene scene, SceneMesh sceneMesh, int indirectBuffer, int drawCount) {
        // Early exit
        if (scene.getPointLights().isEmpty() || scene.getPointLights().size() == 0 || scene.getEntities().isEmpty() || scene.getEntities().size() == 0) {
            System.out.println("Early exit 1");
            return;
        }

        boolean exit = true;
        for (PointLight light : scene.getPointLights()) {
            if (light.isEnabled()) {
                exit = false;
                break;
            }
        }

        if (exit) {
            System.out.println("Early exit 2");
            return;
        }

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);

        glViewport(0, 0, resolution, resolution);
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.getID());
        glClear(GL_DEPTH_BUFFER_BIT);
        glCullFace(GL_FRONT);

        Matrix4f shadowProj = new Matrix4f().perspective((float) Math.toRadians(90.0f), 1, nearPlane, farPlane);

        shaderProgram.bind();
        final int numPointLights = scene.getPointLights().size();
        for (int i = 0; i < numPointLights; i++) {
            Vector3f lightPos = scene.getPointLights().get(i).getPosition();
            shaderProgram.setUniform("lightPos", lightPos);
            shaderProgram.setUniform("farPlane", farPlane);
            shaderProgram.setUniform("cubemapLayer", i);

            Matrix4f[] transforms = {
                new Matrix4f(shadowProj).mul(new Matrix4f().lookAt(lightPos, new Vector3f(lightPos).add( 1,  0,  0), new Vector3f(0, -1,  0))),
                new Matrix4f(shadowProj).mul(new Matrix4f().lookAt(lightPos, new Vector3f(lightPos).add(-1,  0,  0), new Vector3f(0, -1,  0))),
                new Matrix4f(shadowProj).mul(new Matrix4f().lookAt(lightPos, new Vector3f(lightPos).add( 0,  1,  0), new Vector3f(0,  0,  1))),
                new Matrix4f(shadowProj).mul(new Matrix4f().lookAt(lightPos, new Vector3f(lightPos).add( 0, -1,  0), new Vector3f(0,  0, -1))),
                new Matrix4f(shadowProj).mul(new Matrix4f().lookAt(lightPos, new Vector3f(lightPos).add( 0,  0,  1), new Vector3f(0, -1,  0))),
                new Matrix4f(shadowProj).mul(new Matrix4f().lookAt(lightPos, new Vector3f(lightPos).add( 0,  0, -1), new Vector3f(0, -1,  0)))
            };

            glBindBuffer(GL_UNIFORM_BUFFER, matricesUBO);
            for (int j = 0; j < transforms.length; j++) {
                glBufferSubData(GL_UNIFORM_BUFFER, (long) j * 16 * Float.BYTES, transforms[j].get(new float[16]));
            }
            glBindBuffer(GL_UNIFORM_BUFFER, 0);

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

    public int getResolution() {
        return resolution;
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

package io.william.renderer.probe;

import io.william.renderer.*;
import io.william.util.renderer.Cube;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.ARBBindlessTexture.glGetTextureHandleARB;
import static org.lwjgl.opengl.ARBBindlessTexture.glMakeTextureHandleResidentARB;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL43.glMultiDrawElementsIndirect;

public class Probe {

    public Vector3f position;

    private ShaderProgram backgroundShader;
    private ShaderProgram irradianceShader;
    private ShaderProgram prefilterShader;

    private int matricesUBO;

    private int cubemap;
    private int irradianceMap;
    private long irradianceMapHandle;
    private int prefilterMap;
    private long prefilterMapHandle;

    private int irradianceResolution = 64;
    private int prefilterResolution = 512;

    private Framebuffer framebuffer;
    private Framebuffer textureFramebuffer;

    private Matrix4f projection;
    private Matrix4f[] views;

    private final int resolution = 1024;

    public Probe(Vector3f position) throws Exception {
        this.position = position;
        init();
    }

    public void init() throws Exception {
        // Create framebuffers
        framebuffer = new Framebuffer(
            new Texture(resolution, resolution, GL_RGBA16F, GL_RGBA),
            GL_DEPTH24_STENCIL8,
            GL_COLOR_ATTACHMENT0,
            GL_DEPTH_STENCIL_ATTACHMENT
        );

        textureFramebuffer = new Framebuffer(
            new Texture(resolution, resolution, GL_RGBA16F, GL_RGBA),
            GL_DEPTH_COMPONENT24,
            GL_COLOR_ATTACHMENT0,
            GL_DEPTH_ATTACHMENT,
            512,
            512
        );

        backgroundShader = new ShaderProgram("Background");
        backgroundShader.createVertexShader("src/main/resources/shaders/background.vert");
        backgroundShader.createFragmentShader("src/main/resources/shaders/background.frag");
        backgroundShader.link();

        backgroundShader.createUniform("projection");
        backgroundShader.createUniform("view");
        backgroundShader.createUniform("environmentMap");

        // Irradiance
        irradianceShader = new ShaderProgram("Probe Irradiance");
        irradianceShader.createVertexShader("src/main/resources/shaders/cubemap.vert");
        irradianceShader.createFragmentShader("src/main/resources/shaders/irradiance_convolution.frag");
        irradianceShader.link();

        irradianceShader.createUniform("projection");
        irradianceShader.createUniform("view");
        irradianceShader.createUniform("environmentMap");
        irradianceShader.createUniform("fastIrradiance");

        // Prefilter
        prefilterShader = new ShaderProgram("Probe Prefilter");
        prefilterShader.createVertexShader("src/main/resources/shaders/cubemap.vert");
        prefilterShader.createFragmentShader("src/main/resources/shaders/prefilter.frag");
        prefilterShader.link();

        prefilterShader.createUniform("environmentMap");
        prefilterShader.createUniform("projection");
        prefilterShader.createUniform("view");
        prefilterShader.createUniform("roughness");
        prefilterShader.createUniform("sampleCount");

        // Set up cubemap to render to
        cubemap = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubemap);

        for (int i = 0; i < 6; i++) {
             glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB16F, 512, 512, 0, GL_RGB, GL_FLOAT, (ByteBuffer) null);
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // Create irradiance cubemap
        irradianceMap = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, irradianceMap);
        for (int i = 0; i < 6; i++) {
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB16F, irradianceResolution, irradianceResolution, 0, GL_RGB, GL_FLOAT, (ByteBuffer) null);
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        textureFramebuffer.setRenderbufferStorage(GL_DEPTH_COMPONENT24, irradianceResolution, irradianceResolution);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Generate texture handle
        irradianceMapHandle = glGetTextureHandleARB(irradianceMap);
        glMakeTextureHandleResidentARB(irradianceMapHandle);

        // Create prefilter map
        prefilterMap = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, prefilterMap);
        for (int i = 0; i < 6; i++) {
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB16F, prefilterResolution, prefilterResolution, 0, GL_RGB, GL_FLOAT, (ByteBuffer) null);
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glGenerateMipmap(GL_TEXTURE_CUBE_MAP);

        // Generate texture handle
        prefilterMapHandle = glGetTextureHandleARB(prefilterMap);
        glMakeTextureHandleResidentARB(prefilterMapHandle);

        matricesUBO = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, matricesUBO);
        glBufferData(GL_UNIFORM_BUFFER, 4 * 6 * 16 * Float.BYTES, GL_STATIC_DRAW);  // 4 shadow maps max, 6 views, 16 floats per matrix
        glBindBufferBase(GL_UNIFORM_BUFFER, 0, matricesUBO);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        projection = new Matrix4f().setPerspective((float) Math.toRadians(90.0f), 1, 0.01f, 100.0f);

        views = new Matrix4f[]{
            new Matrix4f().setLookAt(position, new Vector3f(position).add(new Vector3f(1, 0, 0)), new Vector3f(0, -1, 0)),
            new Matrix4f().setLookAt(position, new Vector3f(position).add(new Vector3f(-1, 0, 0)), new Vector3f(0, -1, 0)),
            new Matrix4f().setLookAt(position, new Vector3f(position).add(new Vector3f(0, 1, 0)), new Vector3f(0, 0, 1)),
            new Matrix4f().setLookAt(position, new Vector3f(position).add(new Vector3f(0, -1, 0)), new Vector3f(0, 0, -1)),
            new Matrix4f().setLookAt(position, new Vector3f(position).add(new Vector3f(0, 0, 1)), new Vector3f(0, -1, 0)),
            new Matrix4f().setLookAt(position, new Vector3f(position).add(new Vector3f(0, 0, -1)), new Vector3f(0, -1, 0))
        };
    }

    public void render(ShaderProgram sceneShader, SceneMesh sceneMesh, int indirectBuffer, int drawCount, int backgroundCubemap) {
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.getID());
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glViewport(0, 0, resolution, resolution);
        glClearColor(0, 0, 0, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Render scene
        sceneShader.bind();
        sceneShader.setUniform("projection", projection);
        sceneShader.setUniform("camPos", position);

        for (int i = 0; i < 6; i++) {
            framebuffer.attachTexture2D(GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, cubemap);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            sceneShader.setUniform("view", views[i]);

            // Render entities (indirect drawing)
            glBindBuffer(GL_DRAW_INDIRECT_BUFFER, indirectBuffer);
            glBindVertexArray(sceneMesh.getVAO());
            glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, 0, drawCount, 20);
            glBindVertexArray(0);
        }

        sceneShader.unbind();

        // Render sky
        backgroundShader.bind();
        backgroundShader.setUniform("projection", projection);
        backgroundShader.setUniform("environmentMap", 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, backgroundCubemap);
        for (int i = 0; i < 6; i++) {
            backgroundShader.setUniform("view", views[i]);
            Cube.render();
        }
        backgroundShader.unbind();
    }

    public void renderTextures() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
        glDepthFunc(GL_LEQUAL);

        Matrix4f[] renderViews = {
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  1.0f,  0.0f,  0.0f, 0.0f, -1.0f,  0.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f, -1.0f,  0.0f,  0.0f, 0.0f, -1.0f,  0.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  0.0f,  1.0f,  0.0f, 0.0f,  0.0f,  1.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  0.0f, -1.0f,  0.0f, 0.0f,  0.0f, -1.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  0.0f,  0.0f,  1.0f, 0.0f, -1.0f,  0.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  0.0f,  0.0f, -1.0f, 0.0f, -1.0f,  0.0f)
        };

        // IRRADIANCE
        textureFramebuffer.setRenderbufferStorage(GL_DEPTH_COMPONENT24, irradianceResolution, irradianceResolution);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Solve diffuse integral
        irradianceShader.bind();
        irradianceShader.setUniform("environmentMap", 0);
        irradianceShader.setUniform("projection", projection);
        irradianceShader.setUniform("fastIrradiance", false);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubemap);

        glViewport(0, 0, irradianceResolution, irradianceResolution);
        glBindFramebuffer(GL_FRAMEBUFFER, textureFramebuffer.getID());
        for (int i = 0; i < 6; i++) {
            irradianceShader.setUniform("view", renderViews[i]);
            textureFramebuffer.attachTexture2D(GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, irradianceMap);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            Cube.render();
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        irradianceShader.unbind();

        // PREFILTER

        // Prefilter environment map with different roughness values over multiple mipmap levels
        prefilterShader.bind();
        prefilterShader.setUniform("environmentMap", 0);
        prefilterShader.setUniform("projection", projection);
        prefilterShader.setUniform("sampleCount", 1024);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubemap);

        glBindFramebuffer(GL_FRAMEBUFFER, textureFramebuffer.getID());
        int maxMipLevels = 5;
        for (int mip = 0; mip < maxMipLevels; mip++) {
            // Resize framebuffer to mipmap size
            int mipWidth = (int) (prefilterResolution * Math.pow(0.5, mip));
            int mipHeight = mipWidth;
            textureFramebuffer.setRenderbufferStorage(GL_DEPTH_COMPONENT24, mipWidth, mipHeight);
            glViewport(0, 0, mipWidth, mipHeight);

            float roughness = (float) mip / (float) (maxMipLevels - 1);
            prefilterShader.setUniform("roughness", roughness);

            for (int i = 0; i < 6; i++) {
                prefilterShader.setUniform("view", renderViews[i]);
                textureFramebuffer.attachTexture2D(GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, prefilterMap, mip);

                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                Cube.render();
            }
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public int getCubemap() {
        return cubemap;
    }

    public int getIrradianceMap() {
        return irradianceMap;
    }

    public long getIrradianceMapHandle() {
        return irradianceMapHandle;
    }

    public int getPrefilterMap() {
        return prefilterMap;
    }

    public long getPrefilterMapHandle() {
        return prefilterMapHandle;
    }

}

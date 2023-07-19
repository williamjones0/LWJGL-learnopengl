package io.william.renderer;

import io.william.util.renderer.Cube;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;

public class EquirectangularMap {

    private ShaderProgram equirectangularToCubemapShader;
    private ShaderProgram irradianceShader;
    private ShaderProgram prefilterShader;
    private ShaderProgram brdfShader;
    private ShaderProgram backgroundShader;

    private int environmentCubemap;
    private int irradianceMap;
    private int prefilterMap;
    private int brdfLUT;

    private int irradianceResolution = 8;
    private int prefilterResolution = 32;
    private int brdfResolution = 512;

    private Framebuffer framebuffer;
    private Matrix4f captureProjection;
    private Matrix4f[] views;

    private String path;

    public EquirectangularMap(String path) throws Exception {
        init(new Texture(
            path,
            org.lwjgl.opengl.GL30.GL_RGB16F,
            GL_RGBA,
            org.lwjgl.opengl.GL30.GL_FLOAT,
            true
        ));
    }

    public EquirectangularMap(Texture texture) throws Exception {
        init(texture);
    }

    private void init(Texture texture) throws Exception {
        this.path = texture.getPath();

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
        glDepthFunc(GL_LEQUAL);

        texture.bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        framebuffer = new Framebuffer(texture, GL_DEPTH_COMPONENT24, GL_COLOR_ATTACHMENT0, GL_DEPTH_ATTACHMENT, 512, 512);

        // Set up cubemap to render to and attach to framebuffer
        environmentCubemap = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, environmentCubemap);

        for (int i = 0; i < 6; i++) {
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB16F, 512, 512, 0, GL_RGB, GL_FLOAT, (ByteBuffer) null);
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // Set up projection and view matrices
        captureProjection = new Matrix4f().setPerspective((float) Math.toRadians(90.0f), 1.0f, 0.1f, 10.0f);

        views = new Matrix4f[]{
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  1.0f,  0.0f,  0.0f, 0.0f, -1.0f,  0.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f, -1.0f,  0.0f,  0.0f, 0.0f, -1.0f,  0.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  0.0f,  1.0f,  0.0f, 0.0f,  0.0f,  1.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  0.0f, -1.0f,  0.0f, 0.0f,  0.0f, -1.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  0.0f,  0.0f,  1.0f, 0.0f, -1.0f,  0.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  0.0f,  0.0f, -1.0f, 0.0f, -1.0f,  0.0f)
        };

        // Convert equirectangular to cubemap
        equirectangularToCubemapShader = new ShaderProgram("EquirectangularToCubemap");
        equirectangularToCubemapShader.createVertexShader("src/main/resources/shaders/cubemap.vert");
        equirectangularToCubemapShader.createFragmentShader("src/main/resources/shaders/equirectangular_to_cubemap.frag");
        equirectangularToCubemapShader.link();

        equirectangularToCubemapShader.createUniform("equirectangularMap");
        equirectangularToCubemapShader.createUniform("projection");
        equirectangularToCubemapShader.createUniform("view");

        equirectangularToCubemapShader.bind();
        equirectangularToCubemapShader.setUniform("equirectangularMap", 0);
        equirectangularToCubemapShader.setUniform("projection", captureProjection);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getID());

        glViewport(0, 0, 512, 512);
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.getID());
        for (int i = 0; i < 6; i++) {
            equirectangularToCubemapShader.setUniform("view", views[i]);
            framebuffer.attachTexture2D(GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, environmentCubemap);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Render cube
            Cube.render();
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        equirectangularToCubemapShader.unbind();

        // Generate mipmaps from first mip face
        glBindTexture(GL_TEXTURE_CUBE_MAP, environmentCubemap);
        glGenerateMipmap(GL_TEXTURE_CUBE_MAP);

        // Create an irradiance cubemap
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

        framebuffer.setRenderbufferStorage(GL_DEPTH_COMPONENT24, irradianceResolution, irradianceResolution);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Solve diffuse integral
        irradianceShader = new ShaderProgram("Irradiance");
        irradianceShader.createVertexShader("src/main/resources/shaders/cubemap.vert");
        irradianceShader.createFragmentShader("src/main/resources/shaders/irradiance_convolution.frag");
        irradianceShader.link();

        irradianceShader.createUniform("projection");
        irradianceShader.createUniform("view");
        irradianceShader.createUniform("environmentMap");
        irradianceShader.createUniform("fastIrradiance");

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

        // Prefilter environment map with different roughness values over multiple mipmap levels
        prefilterShader = new ShaderProgram("Prefilter");
        prefilterShader.createVertexShader("src/main/resources/shaders/cubemap.vert");
        prefilterShader.createFragmentShader("src/main/resources/shaders/prefilter.frag");
        prefilterShader.link();

        prefilterShader.createUniform("environmentMap");
        prefilterShader.createUniform("projection");
        prefilterShader.createUniform("view");
        prefilterShader.createUniform("roughness");

        // Generate 2D BRDF LUT (512 x 512, 16-bit float)
        brdfLUT = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, brdfLUT);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RG16F, brdfResolution, brdfResolution, 0, GL_RG, GL_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // Render it
        brdfShader = new ShaderProgram("BRDF");
        brdfShader.createVertexShader("src/main/resources/shaders/brdf.vert");
        brdfShader.createFragmentShader("src/main/resources/shaders/brdf.frag");
        brdfShader.link();

        // Configure framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.getID());
        framebuffer.setRenderbufferStorage(GL_DEPTH_COMPONENT24, brdfResolution, brdfResolution);
        framebuffer.attachTexture2D(GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, brdfLUT);

        // Render quad
        glViewport(0, 0, brdfResolution, brdfResolution);
        brdfShader.bind();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        io.william.util.renderer.Quad.render();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Set up background shader
        backgroundShader = new ShaderProgram("Background");
        backgroundShader.createVertexShader("src/main/resources/shaders/background.vert");
        backgroundShader.createFragmentShader("src/main/resources/shaders/background.frag");
        backgroundShader.link();

        backgroundShader.createUniform("projection");
        backgroundShader.createUniform("view");
        backgroundShader.createUniform("environmentMap");

        renderTextures(environmentCubemap, null);
    }

    public void renderTextures(int environmentMap, ShaderSettings settings) {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
        glDepthFunc(GL_LEQUAL);

        // IRRADIANCE
        framebuffer.setRenderbufferStorage(GL_DEPTH_COMPONENT24, irradianceResolution, irradianceResolution);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Solve diffuse integral
        irradianceShader.bind();
        irradianceShader.setUniform("environmentMap", 0);
        irradianceShader.setUniform("projection", captureProjection);
        if (settings != null) {
            irradianceShader.setUniform("fastIrradiance", settings.isFastIrradiance());
        }

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, environmentMap);

        glViewport(0, 0, irradianceResolution, irradianceResolution);
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.getID());
        for (int i = 0; i < 6; i++) {
            irradianceShader.setUniform("view", views[i]);
            framebuffer.attachTexture2D(GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, irradianceMap);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            Cube.render();
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        irradianceShader.unbind();

        // PREFILTER

        // Prefilter environment map with different roughness values over multiple mipmap levels
        prefilterShader.bind();
        prefilterShader.setUniform("environmentMap", 0);
        prefilterShader.setUniform("projection", captureProjection);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, environmentMap);

        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.getID());
        int maxMipLevels = 5;
        for (int mip = 0; mip < maxMipLevels; mip++) {
            // Resize framebuffer to mipmap size
            int mipWidth = (int) (prefilterResolution * Math.pow(0.5, mip));
            int mipHeight = mipWidth;
            framebuffer.setRenderbufferStorage(GL_DEPTH_COMPONENT24, mipWidth, mipHeight);
            glViewport(0, 0, mipWidth, mipHeight);

            float roughness = (float) mip / (float) (maxMipLevels - 1);
            prefilterShader.setUniform("roughness", roughness);

            for (int i = 0; i < 6; i++) {
                prefilterShader.setUniform("view", views[i]);
                framebuffer.attachTexture2D(GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, prefilterMap, mip);

                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                Cube.render();
            }
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void render(Camera camera, Matrix4f projection) {
        // Set up uniforms
        backgroundShader.bind();
        backgroundShader.setUniform("projection", projection);
        backgroundShader.setUniform("view", camera.calculateViewMatrix());
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, environmentCubemap);
        Cube.render();
    }

    public void updateTexture(Texture texture) throws Exception {
        init(texture);
    }

    public int getEnvironmentCubemap() {
        return environmentCubemap;
    }

    public int getIrradianceMap() {
        return irradianceMap;
    }

    public int getPrefilterMap() {
        return prefilterMap;
    }

    public int getBRDFLUT() {
        return brdfLUT;
    }

    public String getPath() {
        return path;
    }
}

import org.joml.Matrix4f;
import primitives.Cube;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS;

public class EquirectangularMap {

    private final Matrix4f projection;

    private final ShaderProgram equirectangularToCubemapShader;
    private final ShaderProgram irradianceShader;
    private final ShaderProgram prefilterShader;
    private final ShaderProgram brdfShader;
    private final ShaderProgram backgroundShader;

    private final int environmentCubemap;
    private final int irradianceMap;
    private final int prefilterMap;
    private final int brdfLUT;

    private Mesh cubeMesh;

    public EquirectangularMap(Texture texture) throws Exception {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
        glDepthFunc(GL_LEQUAL);

        texture.bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        Framebuffer framebuffer = new Framebuffer(texture, GL_DEPTH_COMPONENT24, GL_DEPTH_ATTACHMENT, 512, 512);

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
        projection = new Matrix4f().setPerspective((float) Math.toRadians(90.0f), 1.0f, 0.1f, 10.0f);

        Matrix4f[] views = new Matrix4f[]{
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  1.0f,  0.0f,  0.0f, 0.0f, -1.0f,  0.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f, -1.0f,  0.0f,  0.0f, 0.0f, -1.0f,  0.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  0.0f,  1.0f,  0.0f, 0.0f,  0.0f,  1.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  0.0f, -1.0f,  0.0f, 0.0f,  0.0f, -1.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  0.0f,  0.0f,  1.0f, 0.0f, -1.0f,  0.0f),
            new Matrix4f().setLookAt(0.0f, 0.0f, 0.0f,  0.0f,  0.0f, -1.0f, 0.0f, -1.0f,  0.0f)
        };

        // Convert equirectangular to cubemap
        equirectangularToCubemapShader = new ShaderProgram();
        equirectangularToCubemapShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/cubemap.vert").toPath(), StandardCharsets.US_ASCII));
        equirectangularToCubemapShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/equirectangular_to_cubemap.frag").toPath(), StandardCharsets.US_ASCII));
        equirectangularToCubemapShader.link();

        equirectangularToCubemapShader.createUniform("equirectangularMap");
        equirectangularToCubemapShader.createUniform("projection");
        equirectangularToCubemapShader.createUniform("view");

        equirectangularToCubemapShader.bind();
        equirectangularToCubemapShader.setUniform("equirectangularMap", 0);
        equirectangularToCubemapShader.setUniform("projection", projection);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getID());

        glViewport(0, 0, 512, 512);
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.getID());
        for (int i = 0; i < 6; i++) {
            equirectangularToCubemapShader.setUniform("view", views[i]);
            framebuffer.attachTexture(GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, environmentCubemap);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Render cube
            Cube cube = new Cube();
            cubeMesh = new Mesh(
                cube.getPositions(),
                cube.getNormals(),
                cube.getTexCoords(),
                cube.getIndices(),
                (Material) null
            );
            cubeMesh.render();
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
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB16F, 32, 32, 0, GL_RGB, GL_FLOAT, (ByteBuffer) null);
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        framebuffer.setRenderbufferStorage(GL_DEPTH_COMPONENT24, 32, 32);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Solve diffuse integral
        irradianceShader = new ShaderProgram();
        irradianceShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/cubemap.vert").toPath(), StandardCharsets.US_ASCII));
        irradianceShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/irradiance_convolution.frag").toPath(), StandardCharsets.US_ASCII));
        irradianceShader.link();

        irradianceShader.createUniform("projection");
        irradianceShader.createUniform("view");
        irradianceShader.createUniform("environmentMap");

        irradianceShader.bind();
        irradianceShader.setUniform("environmentMap", 0);
        irradianceShader.setUniform("projection", projection);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, environmentCubemap);

        glViewport(0, 0, 32, 32);
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.getID());
        for (int i = 0; i < 6; i++) {
            irradianceShader.setUniform("view", views[i]);
            framebuffer.attachTexture(GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, irradianceMap);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            cubeMesh.render();
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        irradianceShader.unbind();

        // Create prefilter map
        prefilterMap = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, prefilterMap);
        for (int i = 0; i < 6; i++) {
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB16F, 128, 128, 0, GL_RGB, GL_FLOAT, (ByteBuffer) null);
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glGenerateMipmap(GL_TEXTURE_CUBE_MAP);

        // Prefilter environment map with different roughness values over multiple mipmap levels
        prefilterShader = new ShaderProgram();
        prefilterShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/cubemap.vert").toPath(), StandardCharsets.US_ASCII));
        prefilterShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/prefilter.frag").toPath(), StandardCharsets.US_ASCII));
        prefilterShader.link();

        prefilterShader.createUniform("environmentMap");
        prefilterShader.createUniform("projection");
        prefilterShader.createUniform("view");
        prefilterShader.createUniform("roughness");

        prefilterShader.bind();
        prefilterShader.setUniform("environmentMap", 0);
        prefilterShader.setUniform("projection", projection);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, environmentCubemap);

        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.getID());
        int maxMipLevels = 5;
        for (int mip = 0; mip < maxMipLevels; mip++) {
            // Resize framebuffer to mipmap size
            int mipWidth = (int) (128 * Math.pow(0.5, mip));
            int mipHeight = mipWidth;
            framebuffer.setRenderbufferStorage(GL_DEPTH_COMPONENT24, mipWidth, mipHeight);
            glViewport(0, 0, mipWidth, mipHeight);

            float roughness = (float) mip / (float) (maxMipLevels - 1);
            prefilterShader.setUniform("roughness", roughness);

            for (int i = 0; i < 6; i++) {
                prefilterShader.setUniform("view", views[i]);
                framebuffer.attachTexture(GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, prefilterMap, mip);

                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                cubeMesh.render();
            }
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Generate 2D BRDF LUT (512 x 512, 16-bit float)
        brdfLUT = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, brdfLUT);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RG16F, 512, 512, 0, GL_RG, GL_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // Render it
        brdfShader = new ShaderProgram();
        brdfShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/brdf.vert").toPath(), StandardCharsets.US_ASCII));
        brdfShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/brdf.frag").toPath(), StandardCharsets.US_ASCII));
        brdfShader.link();

        // Configure framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.getID());
        framebuffer.setRenderbufferStorage(GL_DEPTH_COMPONENT24, 512, 512);
        framebuffer.attachTexture(GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, brdfLUT);

        // Render quad
        glViewport(0, 0, 512, 512);
        brdfShader.bind();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        Utils.rendering.Quad.render();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Set up background shader
        backgroundShader = new ShaderProgram();
        backgroundShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/background.vert").toPath(), StandardCharsets.US_ASCII));
        backgroundShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/background.frag").toPath(), StandardCharsets.US_ASCII));
        backgroundShader.link();

        backgroundShader.createUniform("projection");
        backgroundShader.createUniform("view");
        backgroundShader.createUniform("environmentMap");

        backgroundShader.bind();
        backgroundShader.setUniform("environmentMap", 0);
    }

    public void render(Camera camera) {
        // Set up uniforms
        backgroundShader.bind();
        backgroundShader.setUniform("projection", projection);
        backgroundShader.setUniform("view", camera.calculateViewMatrix());
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, environmentCubemap);
        cubeMesh.render();
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

}

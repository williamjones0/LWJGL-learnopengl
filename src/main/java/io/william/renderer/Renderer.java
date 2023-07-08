package io.william.renderer;

import java.io.File;
import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import io.william.renderer.shadow.OmnidirectionalShadowRenderer;
import io.william.renderer.shadow.ShadowRenderer;
import io.william.renderer.shadow.SpotlightShadowRenderer;
import io.william.util.Maths;
import io.william.io.Window;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.opengl.GL43.*;

public class Renderer {

    private Framebuffer framebuffer;

    private ShaderProgram phongShader;
    private ShaderProgram pbrShader;
    private ShaderProgram filamentShader;
    private ShaderProgram frostbiteShader;

    private ShaderProgram lightShader;
    private ShaderProgram hdrShader;

    public enum Shader {
        PBR,
        FILAMENT,
        FROSTBITE
    }

    private Shader currentShader = Shader.PBR;

    private ShaderSettings shaderSettings;

    private float aspectRatio;
    private boolean wireframe;
    private boolean toneMapping = true;
    private boolean isNormalMapping = false;

    private float zNear = 0.1f;
    private float zFar = 500f;
    private static final int MAX_POINT_LIGHTS = 8;
    private static final int MAX_SPOT_LIGHTS = 4;
    private Matrix4f projection;

    public void init(Window window, Camera camera) throws Exception {
        framebuffer = new Framebuffer(
            new Texture(window.getWidth(), window.getHeight(), GL_RGBA16F, GL_RGBA),
            GL_DEPTH24_STENCIL8,
            GL_COLOR_ATTACHMENT0,
            GL_DEPTH_STENCIL_ATTACHMENT
        );

        shaderSettings = new ShaderSettings();

        phongShader = new ShaderProgram("Phong");
        phongShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/vertex.vs").toPath(), StandardCharsets.US_ASCII));
        phongShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/fragment_alt.glsl").toPath(), StandardCharsets.US_ASCII));
        phongShader.link();

        phongShader.createUniform("model");
        phongShader.createUniform("view");

        aspectRatio = (float) window.getWidth() / window.getHeight();
        projection = new Matrix4f().setPerspective(camera.getFOV(), aspectRatio, zNear, zFar);
        phongShader.createUniform("projection");

        phongShader.createUniform("viewPos");
        phongShader.createUniform("isNormalMapping");

        // Light uniforms
        phongShader.createMaterialUniform("material");
//        shaderProgram.createDirLightUniform("dirLight");
//        shaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
//        shaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);

        // PBR shader
        pbrShader = new ShaderProgram("PBR");
        pbrShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/pbr.vert").toPath(), StandardCharsets.US_ASCII));
        pbrShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/pbr.frag").toPath(), StandardCharsets.US_ASCII));
        pbrShader.link();

        createShaderUniforms(pbrShader);

//        // Filament shader
//        filamentShader = new ShaderProgram("Filament");
//        filamentShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/pbr.vert").toPath(), StandardCharsets.US_ASCII));
//        filamentShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/pbrfilament.frag").toPath(), StandardCharsets.US_ASCII));
//        filamentShader.link();
//
//        createShaderUniforms(filamentShader);
//
//        // Frostbite shader
//        frostbiteShader = new ShaderProgram("Frostbite");
//        frostbiteShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/pbr.vert").toPath(), StandardCharsets.US_ASCII));
//        frostbiteShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/frostbite.frag").toPath(), StandardCharsets.US_ASCII));
//        frostbiteShader.link();
//
//        createShaderUniforms(frostbiteShader);

        // Light shader
        lightShader = new ShaderProgram("LightCube");
        lightShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/light_cube.vs").toPath(), StandardCharsets.US_ASCII));
        lightShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/light_cube.fs").toPath(), StandardCharsets.US_ASCII));
        lightShader.link();

        lightShader.createUniform("model");
        lightShader.createUniform("view");
        lightShader.createUniform("projection");

        lightShader.createUniform("colour");

        // HDR shader
        hdrShader = new ShaderProgram("HDR");
        hdrShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/hdr.vert").toPath(), StandardCharsets.US_ASCII));
        hdrShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/hdr.frag").toPath(), StandardCharsets.US_ASCII));
        hdrShader.link();

        hdrShader.createUniform("hdrBuffer");
        hdrShader.createUniform("exposure");
        hdrShader.createUniform("toneMapping");
    }

    public void render(Camera camera, Scene scene, SceneMesh sceneMesh, int indirectBuffer, int drawCount, ShadowRenderer shadowRenderer, SpotlightShadowRenderer spotlightShadowRenderer, OmnidirectionalShadowRenderer omnidirectionalShadowRenderer, Window window) {
        DirLight dirLight = scene.getDirLight();
        List<PointLight> pointLights = scene.getPointLights();
        List<SpotLight> spotLights = scene.getSpotLights();
        EquirectangularMap equirectangularMap = scene.getEquirectangularMap();

        // First pass: render scene to floating point framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.getID());
        glEnable(GL_DEPTH_TEST);
        glViewport(0, 0, window.getWidth(), window.getHeight());
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glPolygonMode(GL_FRONT_AND_BACK, wireframe ? GL_LINE : GL_FILL);

//        shaderProgram.bind();

        Matrix4f view = camera.calculateViewMatrix();
        projection = new Matrix4f().setPerspective(camera.getFOV(), aspectRatio, zNear, zFar);
//        shaderProgram.setUniform("view", view);
//        shaderProgram.setUniform("projection", projection);
//
//        shaderProgram.setUniform("viewPos", camera.getPosition());
//        shaderProgram.setUniform("isNormalMapping", isNormalMapping);

//        // Update directional light uniforms
//        shaderProgram.setUniform("dirLight.direction", dirLight.getDirection());
//        shaderProgram.setUniform("dirLight.ambient", dirLight.getAmbient());
//        shaderProgram.setUniform("dirLight.diffuse", dirLight.getDiffuse());
//        shaderProgram.setUniform("dirLight.specular", dirLight.getSpecular());

//        // Update point light uniforms
//        for (int i = 0; i < pointLights.length; i++) {
//            shaderProgram.setUniform("pointLights[" + i + "].position", pointLights[i].getPosition());
//            shaderProgram.setUniform("pointLights[" + i + "].color", pointLights[i].getColor());
//        }

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

//        // Render containers
//        for (Entity entity : entities) {
//            // Material uniforms
//            shaderProgram.setUniform("material.diffuse", 0);
//            shaderProgram.setUniform("material.specular", 1);
//            shaderProgram.setUniform("material.shininess", entity.getMesh().getMaterial().getShininess());
//            shaderProgram.setUniform("material.normalMap", 2);
//
//            Matrix4f model = Maths.calculateModelMatrix(entity.getPosition(), entity.getRotation(), entity.getScale());
//            shaderProgram.setUniform("model", model);
//            entity.getMesh().render();
//        }
//
//        shaderProgram.unbind();

        ShaderProgram shader = switch (currentShader) {
            case PBR -> pbrShader;
            case FILAMENT -> filamentShader;
            case FROSTBITE -> frostbiteShader;
        };

        // PBR shader
        shader.bind();
        shader.setUniform("view", view);
        shader.setUniform("projection", projection);

        shader.setUniform("camPos", camera.getPosition());

//        shader.setUniform("material.albedo", 0);
//        shader.setUniform("material.normal", 1);
//        shader.setUniform("material.metallic", 2);
//        shader.setUniform("material.roughness", 3);
//        shader.setUniform("material.metallicRoughness", 4);
//        shader.setUniform("material.ao", 5);
//        shader.setUniform("material.emissive", 6);

        // Update point light uniforms
        for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
            if (i < pointLights.size()) {
                shader.setUniform("pointLights[" + i + "].position", pointLights.get(i).getPosition());
                shader.setUniform("pointLights[" + i + "].color", pointLights.get(i).getColor());
                shader.setUniform("pointLights[" + i + "].intensity", pointLights.get(i).getIntensity());
                shader.setUniform("pointLights[" + i + "].enabled", pointLights.get(i).isEnabled());
            } else {
                shader.setUniform("pointLights[" + i + "].position", new Vector3f(0.0f));
                shader.setUniform("pointLights[" + i + "].color", new Vector3f(0.0f));
                shader.setUniform("pointLights[" + i + "].intensity", 0.0f);
                shader.setUniform("pointLights[" + i + "].enabled", false);
            }
        }

        // Update spotlight uniforms
        for (int i = 0; i < MAX_SPOT_LIGHTS; i++) {
            if (i < spotLights.size()) {
                shader.setUniform("spotLights[" + i + "].position",    spotLights.get(i).getPosition());
                shader.setUniform("spotLights[" + i + "].direction",   spotLights.get(i).getDirection());
                shader.setUniform("spotLights[" + i + "].color",       spotLights.get(i).getColor());
                shader.setUniform("spotLights[" + i + "].intensity",   spotLights.get(i).getIntensity());
                shader.setUniform("spotLights[" + i + "].cutoff",      (float) Math.cos(Math.toRadians(spotLights.get(i).getCutoff())));
                shader.setUniform("spotLights[" + i + "].outerCutoff", (float) Math.cos(Math.toRadians(spotLights.get(i).getOuterCutoff())));
                shader.setUniform("spotLights[" + i + "].enabled",     spotLights.get(i).isEnabled());
            } else {
                shader.setUniform("spotLights[" + i + "].position",    new Vector3f(0.0f));
                shader.setUniform("spotLights[" + i + "].direction",   new Vector3f(0.0f));
                shader.setUniform("spotLights[" + i + "].color",       new Vector3f(0.0f));
                shader.setUniform("spotLights[" + i + "].intensity",   0.0f);
                shader.setUniform("spotLights[" + i + "].cutoff",      0.0f);
                shader.setUniform("spotLights[" + i + "].outerCutoff", 0.0f);
                shader.setUniform("spotLights[" + i + "].enabled",     false);
            }
        }

        // Update directional light uniforms
        shader.setUniform("dirLight.direction", dirLight.getDirection());
        shader.setUniform("dirLight.color", dirLight.getColor());

        // Update environment map uniforms
        shader.setUniform("irradianceMap", 7);
        glActiveTexture(GL_TEXTURE7);
        glBindTexture(GL_TEXTURE_CUBE_MAP, equirectangularMap.getIrradianceMap());

        shader.setUniform("prefilterMap", 8);
        glActiveTexture(GL_TEXTURE8);
        glBindTexture(GL_TEXTURE_CUBE_MAP, equirectangularMap.getPrefilterMap());

        shader.setUniform("brdfLUT", 9);
        glActiveTexture(GL_TEXTURE9);
        glBindTexture(GL_TEXTURE_2D, equirectangularMap.getBRDFLUT());

        // Update settings uniforms
        shader.setUniform("settings.specularOcclusion", shaderSettings.isSpecularOcclusion());
        shader.setUniform("settings.horizonSpecularOcclusion", shaderSettings.isHorizonSpecularOcclusion());
        shader.setUniform("settings.pointShadows", shaderSettings.isPointShadows());
        shader.setUniform("settings.pointShadowBias", shaderSettings.getPointShadowBias());
        shader.setUniform("settings.shadowMinBias", shaderSettings.getShadowMinBias());
        shader.setUniform("settings.shadowMaxBias", shaderSettings.getShadowMaxBias());

        // Update shadow mapping uniforms
        shader.setUniform("lightSpaceMatrix", shadowRenderer.getLightSpaceMatrix());
        shader.setUniform("spotlightSpaceMatrix", spotlightShadowRenderer.getLightSpaceMatrix());

        shader.setUniform("pointShadowMaps", 10);
        glActiveTexture(GL_TEXTURE10);
        glBindTexture(GL_TEXTURE_CUBE_MAP_ARRAY, omnidirectionalShadowRenderer.getTextureArrayID());

        shader.setUniform("directionalShadowMap", 11);
        glActiveTexture(GL_TEXTURE11);
        glBindTexture(GL_TEXTURE_2D, shadowRenderer.getTextureID());

        shader.setUniform("spotShadowMaps", 12);
        glActiveTexture(GL_TEXTURE12);
        glBindTexture(GL_TEXTURE_2D_ARRAY, spotlightShadowRenderer.getTextureArrayID());

        shader.setUniform("farPlane", omnidirectionalShadowRenderer.getFarPlane());

        // Render entities (indirect drawing)
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, indirectBuffer);
        glBindVertexArray(sceneMesh.getVAO());
        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, 0, drawCount, 20);
        glBindVertexArray(0);

        // Render lights
        lightShader.bind();
        lightShader.setUniform("view", view);
        lightShader.setUniform("projection", projection);
        lightShader.setUniform("view", view);

        for (PointLight light : pointLights) {
            Matrix4f model = new Matrix4f();
            model.translate(light.getPosition());
            lightShader.setUniform("model", model);

            Vector3f temp = new Vector3f(light.getColor());
            temp.mul(light.getIntensity());
            lightShader.setUniform("colour", temp);

            light.getMesh().render();
        }

        for (SpotLight light : spotLights) {
            if (spotLights.indexOf(light) == 0)
                continue;
            Matrix4f model = Maths.calculateModelMatrix(light.getPosition(), new Vector3f(0, 0, 0), 1);
            // This doesn't work but it's not important enough to bother fixing
            model.rotate(new Quaternionf().lookAlong(new Vector3f(-light.getDirection().x, light.getDirection().y, light.getDirection().z) , new Vector3f(0.0f, 1.0f, 0.0f)));
            lightShader.setUniform("model", model);

            Vector3f temp = new Vector3f(light.getColor());
            temp.mul(light.getIntensity());
            lightShader.setUniform("colour", temp);

            light.getMesh().render();
        }

        lightShader.unbind();

        // Render skybox
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        equirectangularMap.render(camera, projection);

        // Second pass: render floating point framebuffer to default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glDisable(GL_DEPTH_TEST);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        glClearColor(0.5f, 1.0f, 0.5f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        // Render framebuffer to screen
        hdrShader.bind();

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, framebuffer.getTexture().getID());
        hdrShader.setUniform("hdrBuffer", 0);
        hdrShader.setUniform("exposure", shaderSettings.getExposure());
        hdrShader.setUniform("toneMapping", toneMapping);

        // Render quad
        io.william.util.renderer.Quad.render();
    }

    public void cleanup() {
        phongShader.cleanup();
        pbrShader.cleanup();
        lightShader.cleanup();
        hdrShader.cleanup();
        framebuffer.cleanup();
    }

    private void createShaderUniforms(ShaderProgram shader) throws Exception {
        shader.createUniform("view");
        shader.createUniform("projection");

        shader.createUniform("camPos");

//        shader.createPBRMaterialUniform("material");

        shader.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
        shader.createDirLightUniform("dirLight");
        shader.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);

        shader.createUniform("irradianceMap");
        shader.createUniform("prefilterMap");
        shader.createUniform("brdfLUT");

        shader.createSettingsUniform("settings");

        shader.createUniform("lightSpaceMatrix");
        shader.createUniform("spotlightSpaceMatrix");
        shader.createUniform("directionalShadowMap");
        shader.createUniform("pointShadowMaps");
        shader.createUniform("spotShadowMaps");
        shader.createUniform("farPlane");
    }

    public void screenshot() {
        String filepath = System.getProperty("user.dir") + "/src/main/resources/screenshot_" + System.currentTimeMillis() + ".png";
        int width = framebuffer.getWidth();
        int height = framebuffer.getHeight();
        int channels = 3;
        int stride = width * channels;
        stride += (stride % 4) == 0 ? 0 : 4 - (stride % 4);
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * channels);
        glReadPixels(0, 0, width, height, GL_RGB, GL_UNSIGNED_BYTE, buffer);
        try {
            STBImageWrite.stbi_flip_vertically_on_write(true);
            STBImageWrite.stbi_write_png(filepath, width, height, channels, buffer, stride);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Screenshot saved to " + filepath);
    }

    public Shader getCurrentShader() {
        return currentShader;
    }

    public void setCurrentShader(Shader currentShader) {
        this.currentShader = currentShader;
    }

    public ShaderSettings getShaderSettings() {
        return shaderSettings;
    }

    public void setShaderSettings(ShaderSettings shaderSettings) {
        this.shaderSettings = shaderSettings;
    }

    public boolean isWireframe() {
        return wireframe;
    }

    public void setWireframe(boolean wireframe) {
        this.wireframe = wireframe;
    }

    public void setToneMapping(boolean toneMapping) {
        this.toneMapping = toneMapping;
    }

    public boolean isToneMapping() {
        return toneMapping;
    }

    public boolean isNormalMapping() {
        return isNormalMapping;
    }

    public void setNormalMapping(boolean normalMapping) {
        isNormalMapping = normalMapping;
    }

    public float getzNear() {
        return zNear;
    }

    public void setzNear(float zNear) {
        this.zNear = zNear;
    }

    public float getzFar() {
        return zFar;
    }

    public void setzFar(float zFar) {
        this.zFar = zFar;
    }
}

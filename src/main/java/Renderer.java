import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import Utils.Maths;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    private Framebuffer framebuffer;

    private ShaderProgram phongShader;
    private ShaderProgram pbrShader;
    private ShaderProgram pbrNoMaterialShader;
    private ShaderProgram lightCubeShader;
    private ShaderProgram hdrShader;

    private float FOV = (float) Math.toRadians(60.0);
    private float aspectRatio;
    private boolean wireframe;
    private boolean toneMapping = true;
    private boolean isNormalMapping = false;
    private float exposure = 1.0f;

    private float zNear = 0.1f;
    private float zFar = 100f;
    private static final int MAX_POINT_LIGHTS = 4;
    private static final int MAX_SPOT_LIGHTS = 1;
    private Matrix4f projection;

    public void init(Window window) throws Exception {
        framebuffer = new Framebuffer(
            new Texture(window.getWidth(), window.getHeight(), GL_RGBA16F, GL_RGBA),
            GL_DEPTH24_STENCIL8,
            GL_DEPTH_STENCIL_ATTACHMENT
        );

        phongShader = new ShaderProgram();
        phongShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/vertex.vs").toPath(), StandardCharsets.US_ASCII));
        phongShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/fragment_alt.glsl").toPath(), StandardCharsets.US_ASCII));
        phongShader.link();

        phongShader.createUniform("model");
        phongShader.createUniform("view");

        aspectRatio = (float) window.getWidth() / window.getHeight();
        projection = new Matrix4f().setPerspective(FOV, aspectRatio, zNear, zFar);
        phongShader.createUniform("projection");

        phongShader.createUniform("viewPos");
        phongShader.createUniform("isNormalMapping");

        // Light uniforms
        phongShader.createMaterialUniform("material");
//        shaderProgram.createDirLightUniform("dirLight");
//        shaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
//        shaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);

        // PBR shader
        pbrShader = new ShaderProgram();
        pbrShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/pbr.vert").toPath(), StandardCharsets.US_ASCII));
        pbrShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/pbr.frag").toPath(), StandardCharsets.US_ASCII));
        pbrShader.link();

        pbrShader.createUniform("model");
        pbrShader.createUniform("view");
        pbrShader.createUniform("projection");

        pbrShader.createUniform("camPos");

        pbrShader.createPBRMaterialUniform("material");

        pbrShader.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
        pbrShader.createDirLightUniform("dirLight");
        pbrShader.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);

        pbrShader.createUniform("irradianceMap");
        pbrShader.createUniform("prefilterMap");
        pbrShader.createUniform("brdfLUT");

        // PBRNoMaterial shader
        pbrNoMaterialShader = new ShaderProgram();
        pbrNoMaterialShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/pbr.vert").toPath(), StandardCharsets.US_ASCII));
        pbrNoMaterialShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/pbrnomaterial.frag").toPath(), StandardCharsets.US_ASCII));
        pbrNoMaterialShader.link();

        pbrNoMaterialShader.createUniform("model");
        pbrNoMaterialShader.createUniform("view");
        pbrNoMaterialShader.createUniform("projection");

        pbrNoMaterialShader.createUniform("camPos");

        pbrNoMaterialShader.createUniform("values.albedo");
        pbrNoMaterialShader.createUniform("values.metallic");
        pbrNoMaterialShader.createUniform("values.roughness");
        pbrNoMaterialShader.createUniform("values.ao");

        pbrNoMaterialShader.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
        pbrNoMaterialShader.createDirLightUniform("dirLight");
        pbrNoMaterialShader.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);

        pbrNoMaterialShader.createUniform("irradianceMap");
        pbrNoMaterialShader.createUniform("prefilterMap");
        pbrNoMaterialShader.createUniform("brdfLUT");

        // Light cube shader
        lightCubeShader = new ShaderProgram();
        lightCubeShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/light_cube.vs").toPath(), StandardCharsets.US_ASCII));
        lightCubeShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/light_cube.fs").toPath(), StandardCharsets.US_ASCII));
        lightCubeShader.link();

        lightCubeShader.createUniform("model");
        lightCubeShader.createUniform("view");
        lightCubeShader.createUniform("projection");

        lightCubeShader.createUniform("colour");

        // HDR shader
        hdrShader = new ShaderProgram();
        hdrShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/hdr.vert").toPath(), StandardCharsets.US_ASCII));
        hdrShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/hdr.frag").toPath(), StandardCharsets.US_ASCII));
        hdrShader.link();

        hdrShader.createUniform("hdrBuffer");
        hdrShader.createUniform("exposure");
        hdrShader.createUniform("toneMapping");
    }

    public void render(Camera camera, Scene scene, Window window) {
        List<Entity> entities = scene.getEntities();
        DirLight dirLight = scene.getDirLight();
        PointLight[] pointLights = scene.getPointLights();
        SpotLight[] spotLights = scene.getSpotLights();
        Skybox skybox = scene.getSkybox();
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
        projection = new Matrix4f().setPerspective(FOV, aspectRatio, zNear, zFar);
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

        // PBR shader
        pbrShader.bind();
        pbrShader.setUniform("view", view);
        pbrShader.setUniform("projection", projection);

        pbrShader.setUniform("camPos", camera.getPosition());

        pbrShader.setUniform("material.albedo", 0);
        pbrShader.setUniform("material.normal", 1);
        pbrShader.setUniform("material.metallic", 2);
        pbrShader.setUniform("material.roughness", 3);
        pbrShader.setUniform("material.metallicRoughness", 4);
        pbrShader.setUniform("material.ao", 5);
        pbrShader.setUniform("material.emissive", 6);

        // Update point light uniforms
        for (int i = 0; i < pointLights.length; i++) {
            pbrShader.setUniform("pointLights[" + i + "].position", pointLights[i].getPosition());
            pbrShader.setUniform("pointLights[" + i + "].color", pointLights[i].getColor());
        }

        // Update spotlight uniforms
        for (int i = 0; i < spotLights.length; i++) {
            pbrShader.setUniform("spotLights[" + i + "].position",    spotLights[i].getPosition());
            pbrShader.setUniform("spotLights[" + i + "].direction",   spotLights[i].getDirection());
            pbrShader.setUniform("spotLights[" + i + "].color",       spotLights[i].getColor());
            pbrShader.setUniform("spotLights[" + i + "].cutoff",      spotLights[i].getCutoff());
            pbrShader.setUniform("spotLights[" + i + "].outerCutoff", spotLights[i].getOuterCutoff());
            pbrShader.setUniform("spotLights[" + i + "].enabled",     spotLights[i].isEnabled());
        }

        // Update directional light uniforms
        pbrShader.setUniform("dirLight.direction", dirLight.getDirection());
        pbrShader.setUniform("dirLight.color", dirLight.getColor());

        // Render entities
        pbrShader.setUniform("irradianceMap", 7);
        glActiveTexture(GL_TEXTURE7);
        glBindTexture(GL_TEXTURE_CUBE_MAP, equirectangularMap.getIrradianceMap());

        pbrShader.setUniform("prefilterMap", 8);
        glActiveTexture(GL_TEXTURE8);
        glBindTexture(GL_TEXTURE_CUBE_MAP, equirectangularMap.getPrefilterMap());

        pbrShader.setUniform("brdfLUT", 9);
        glActiveTexture(GL_TEXTURE9);
        glBindTexture(GL_TEXTURE_2D, equirectangularMap.getBRDFLUT());

//        for (Entity entity : entities) {
//            Matrix4f model = Maths.calculateModelMatrix(entity.getPosition(), entity.getRotation(), entity.getScale());
//            pbrShader.setUniform("model", model);
//
//            entity.getMesh().render();
//        }

        pbrShader.unbind();

        // PBRNoMaterial shader
        pbrNoMaterialShader.bind();
        pbrNoMaterialShader.setUniform("view", view);
        pbrNoMaterialShader.setUniform("projection", projection);

        pbrNoMaterialShader.setUniform("camPos", camera.getPosition());

        pbrNoMaterialShader.setUniform("values.albedo", new Vector3f(1.0f, 1.0f, 1.0f));
        pbrNoMaterialShader.setUniform("values.ao", 1.0f);

        // Update point light uniforms
        for (int i = 0; i < pointLights.length; i++) {
            pbrNoMaterialShader.setUniform("pointLights[" + i + "].position", pointLights[i].getPosition());
            pbrNoMaterialShader.setUniform("pointLights[" + i + "].color", pointLights[i].getColor());
        }

        // Update spotlight uniforms
        for (int i = 0; i < spotLights.length; i++) {
            pbrNoMaterialShader.setUniform("spotLights[" + i + "].position",    spotLights[i].getPosition());
            pbrNoMaterialShader.setUniform("spotLights[" + i + "].direction",   spotLights[i].getDirection());
            pbrNoMaterialShader.setUniform("spotLights[" + i + "].color",       spotLights[i].getColor());
            pbrNoMaterialShader.setUniform("spotLights[" + i + "].cutoff",      spotLights[i].getCutoff());
            pbrNoMaterialShader.setUniform("spotLights[" + i + "].outerCutoff", spotLights[i].getOuterCutoff());
            pbrNoMaterialShader.setUniform("spotLights[" + i + "].enabled",     spotLights[i].isEnabled());
        }

        // Update directional light uniforms
        pbrNoMaterialShader.setUniform("dirLight.direction", dirLight.getDirection());
        pbrNoMaterialShader.setUniform("dirLight.color", dirLight.getColor());

        // Render entities
        pbrNoMaterialShader.setUniform("irradianceMap", 7);
        glActiveTexture(GL_TEXTURE7);
        glBindTexture(GL_TEXTURE_CUBE_MAP, equirectangularMap.getIrradianceMap());

        pbrNoMaterialShader.setUniform("prefilterMap", 8);
        glActiveTexture(GL_TEXTURE8);
        glBindTexture(GL_TEXTURE_CUBE_MAP, equirectangularMap.getPrefilterMap());

        pbrNoMaterialShader.setUniform("brdfLUT", 9);
        glActiveTexture(GL_TEXTURE9);
        glBindTexture(GL_TEXTURE_2D, equirectangularMap.getBRDFLUT());

//        for (Entity entity : entities) {
//            Matrix4f model = Maths.calculateModelMatrix(entity.getPosition(), entity.getRotation(), entity.getScale());
//            pbrShader.setUniform("model", model);
//
//            entity.getMesh().render();
//        }

        for (Entity entity : entities) {
            Matrix4f model = Maths.calculateModelMatrix(entity.getPosition(), entity.getRotation(), entity.getScale());
            pbrShader.bind();
            pbrShader.setUniform("model", model);

            Map<String, Boolean> usesTextures = entity.getMaterialMeshes()[0].getPbrMaterial().getUsesTextures();

            for (Map.Entry<String, Boolean> usesTexture: usesTextures.entrySet()) {
                System.out.println(usesTexture.getKey() + " " + usesTexture.getValue());
                pbrShader.setUniform("material.uses_" + usesTexture.getKey() + "_map", usesTexture.getValue());
            }

            if (!usesTextures.get("albedo")) {
                System.out.println(entity.getMaterialMeshes()[0].getPbrMaterial().getAlbedoColor());
                pbrShader.setUniform("material.albedo", entity.getMaterialMeshes()[0].getPbrMaterial().getAlbedoColor());
            }

            if (!usesTextures.get("metallic"))
                pbrShader.setUniform("material.metallic", entity.getMaterialMeshes()[0].getPbrMaterial().getMetallicFactor());

            if (!usesTextures.get("roughness"))
                pbrShader.setUniform("material.roughness", entity.getMaterialMeshes()[0].getPbrMaterial().getRoughnessFactor());

            entity.render();
        }

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

            lightCubeShader.setUniform("colour", light.getColor());

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

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        equirectangularMap.render(camera);

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
        hdrShader.setUniform("exposure", exposure);
        hdrShader.setUniform("toneMapping", toneMapping);

        // Render quad
        Utils.rendering.Quad.render();
    }

    public void cleanup() {
        phongShader.cleanup();
        pbrShader.cleanup();
        pbrNoMaterialShader.cleanup();
        lightCubeShader.cleanup();
        hdrShader.cleanup();
        framebuffer.cleanup();
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

    public float getFOV() {
        return FOV;
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

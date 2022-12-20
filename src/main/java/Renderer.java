import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import Utils.Maths;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    private Framebuffer framebuffer;

    private ShaderProgram phongShader;
    private ShaderProgram pbrShader;
    private ShaderProgram lightShader;
    private ShaderProgram hdrShader;

    private float FOV = (float) Math.toRadians(60.0);
    private float aspectRatio;
    private boolean wireframe;
    private boolean toneMapping = true;
    private boolean isNormalMapping = false;
    private float exposure = 1.0f;

    private float zNear = 0.1f;
    private float zFar = 100f;
    private static final int MAX_POINT_LIGHTS = 8;
    private static final int MAX_SPOT_LIGHTS = 4;
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

        // Light shader
        lightShader = new ShaderProgram();
        lightShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/light_cube.vs").toPath(), StandardCharsets.US_ASCII));
        lightShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/light_cube.fs").toPath(), StandardCharsets.US_ASCII));
        lightShader.link();

        lightShader.createUniform("model");
        lightShader.createUniform("view");
        lightShader.createUniform("projection");

        lightShader.createUniform("colour");

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
        List<PointLight> pointLights = scene.getPointLights();
        List<SpotLight> spotLights = scene.getSpotLights();
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
        for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
            if (i < pointLights.size()) {
                pbrShader.setUniform("pointLights[" + i + "].position", pointLights.get(i).getPosition());
                pbrShader.setUniform("pointLights[" + i + "].color", pointLights.get(i).getColor());
                pbrShader.setUniform("pointLights[" + i + "].intensity", pointLights.get(i).getIntensity());
            } else {
                pbrShader.setUniform("pointLights[" + i + "].position", new Vector3f(0.0f));
                pbrShader.setUniform("pointLights[" + i + "].color", new Vector3f(0.0f));
                pbrShader.setUniform("pointLights[" + i + "].intensity", 0.0f);
            }
        }

        // Update spotlight uniforms
        for (int i = 0; i < MAX_SPOT_LIGHTS; i++) {
            if (i < spotLights.size()) {
                pbrShader.setUniform("spotLights[" + i + "].position",    spotLights.get(i).getPosition());
                pbrShader.setUniform("spotLights[" + i + "].direction",   spotLights.get(i).getDirection());
                pbrShader.setUniform("spotLights[" + i + "].color",       spotLights.get(i).getColor());
                pbrShader.setUniform("spotLights[" + i + "].intensity",   spotLights.get(i).getIntensity());
                pbrShader.setUniform("spotLights[" + i + "].cutoff",      spotLights.get(i).getCutoff());
                pbrShader.setUniform("spotLights[" + i + "].outerCutoff", spotLights.get(i).getOuterCutoff());
                pbrShader.setUniform("spotLights[" + i + "].enabled",     spotLights.get(i).isEnabled());
            } else {
                pbrShader.setUniform("spotLights[" + i + "].position",    new Vector3f(0.0f));
                pbrShader.setUniform("spotLights[" + i + "].direction",   new Vector3f(0.0f));
                pbrShader.setUniform("spotLights[" + i + "].color",       new Vector3f(0.0f));
                pbrShader.setUniform("spotLights[" + i + "].intensity",   0.0f);
                pbrShader.setUniform("spotLights[" + i + "].cutoff",      0.0f);
                pbrShader.setUniform("spotLights[" + i + "].outerCutoff", 0.0f);
                pbrShader.setUniform("spotLights[" + i + "].enabled",     false);
            }
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

        for (Entity entity : entities) {
            if (entity.getMaterialMeshes() == null) {
                continue;
            }

            Matrix4f model = Maths.calculateModelMatrix(entity.getWorldPosition(), entity.getRotation(), entity.getScale());
            pbrShader.bind();
            pbrShader.setUniform("model", model);

            Map<String, Boolean> usesTextures = entity.getMaterialMeshes()[0].getPbrMaterial().getUsesTextures();

            for (Map.Entry<String, Boolean> usesTexture: usesTextures.entrySet()) {
                pbrShader.setUniform("material.uses_" + usesTexture.getKey() + "_map", usesTexture.getValue());
            }

            if (!usesTextures.get("albedo"))
                pbrShader.setUniform("material.albedoColor", entity.getMaterialMeshes()[0].getPbrMaterial().getAlbedoColor());

            if (!usesTextures.get("metallic"))
                pbrShader.setUniform("material.metallicFactor", entity.getMaterialMeshes()[0].getPbrMaterial().getMetallicFactor());

            if (!usesTextures.get("roughness"))
                pbrShader.setUniform("material.roughnessFactor", entity.getMaterialMeshes()[0].getPbrMaterial().getRoughnessFactor());

            entity.render();
        }

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
        hdrShader.setUniform("exposure", exposure);
        hdrShader.setUniform("toneMapping", toneMapping);

        // Render quad
        Utils.rendering.Quad.render();
    }

    public void cleanup() {
        phongShader.cleanup();
        pbrShader.cleanup();
        lightShader.cleanup();
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

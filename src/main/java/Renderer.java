import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import Utils.Maths;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    private Framebuffer framebuffer;

    private ShaderProgram shaderProgram;
    private ShaderProgram pbrShader;
    private ShaderProgram pbrNoMaterialShader;
    private ShaderProgram lightCubeShader;
    private ShaderProgram skyboxShader;
    private ShaderProgram hdrShader;

    private float FOV = (float) Math.toRadians(60.0);
    private float aspectRatio;
    private boolean wireframe;
    private boolean isNormalMapping = false;
    private float exposure = 1.0f;

    private final float Z_NEAR = 0.1f;
    private final float Z_FAR = 100f;
    private static final int MAX_POINT_LIGHTS = 4;
    private static final int MAX_SPOT_LIGHTS = 1;
    private Matrix4f projection;

    public void init(Window window) throws Exception {
        framebuffer = new Framebuffer(
            new Texture(window.getWidth(), window.getHeight(), GL_RGBA16F, GL_RGBA),
            GL_DEPTH24_STENCIL8,
            GL_DEPTH_STENCIL_ATTACHMENT
        );

        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Files.readString(new File("src/main/resources/shaders/vertex.vs").toPath(), StandardCharsets.US_ASCII));
        shaderProgram.createFragmentShader(Files.readString(new File("src/main/resources/shaders/fragment_alt.glsl").toPath(), StandardCharsets.US_ASCII));
        shaderProgram.link();

        shaderProgram.createUniform("model");
        shaderProgram.createUniform("view");

        aspectRatio = (float) window.getWidth() / window.getHeight();
        projection = new Matrix4f().setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
        shaderProgram.createUniform("projection");

        shaderProgram.createUniform("viewPos");
        shaderProgram.createUniform("isNormalMapping");

        // Light uniforms
        shaderProgram.createMaterialUniform("material");
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

        pbrShader.createUniform("irradianceMap");
        pbrShader.createUniform("prefilterMap");
        pbrShader.createUniform("brdfLUT");

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

        // Skybox shader
        skyboxShader = new ShaderProgram();
        skyboxShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/skybox.vs").toPath(), StandardCharsets.US_ASCII));
        skyboxShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/skybox.fs").toPath(), StandardCharsets.US_ASCII));
        skyboxShader.link();

        skyboxShader.createUniform("view");
        skyboxShader.createUniform("projection");

        // HDR shader
        hdrShader = new ShaderProgram();
        hdrShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/hdr.vert").toPath(), StandardCharsets.US_ASCII));
        hdrShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/hdr.frag").toPath(), StandardCharsets.US_ASCII));
        hdrShader.link();

        hdrShader.createUniform("hdrBuffer");
        hdrShader.createUniform("exposure");
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
        projection = new Matrix4f().setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
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

        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            Matrix4f model = Maths.calculateModelMatrix(entity.getPosition(), entity.getRotation(), entity.getScale());

            if (entity.getMesh().getPbrMaterial() != null) {
                pbrShader.bind();
                pbrShader.setUniform("model", model);
                pbrShader.setUniform("material.combinedMetallicRoughness", entity.getMesh().getPbrMaterial().isCombinedMetallicRoughness());
            } else {
                pbrNoMaterialShader.setUniform("model", model);

                int row = i / 7;
                int column = i % 7;
                pbrNoMaterialShader.setUniform("values.albedo", new Vector3f(1.0f, row * 1.0f, column * 1.0f).normalize());
                pbrNoMaterialShader.setUniform("values.metallic", (float) row / (float) 7);
                pbrNoMaterialShader.setUniform("values.roughness", Maths.clamp((float) column / (float) 7, 0.05f, 1.0f));
            }

            entity.getMesh().render();
        }

        pbrNoMaterialShader.unbind();

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

        // Render quad
        Utils.rendering.Quad.render();
    }

    public void cleanup() {
        shaderProgram.cleanup();
    }

    public void setWireframe(boolean wireframe) {
        this.wireframe = wireframe;
    }

    public boolean isWireframe() {
        return wireframe;
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
}

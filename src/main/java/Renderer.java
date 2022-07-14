import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.joml.Matrix4f;

public class Renderer {

    private ShaderProgram shaderProgram;
    private ShaderProgram lightCubeShader;

    private static final float FOV = (float) Math.toRadians(60.0);
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100f;
    private static final int MAX_POINT_LIGHTS = 2;
    private static final int MAX_SPOT_LIGHTS = 1;
    private Matrix4f projection;

    public void init(Window window) throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Files.readString(new File("src/main/resources/vertex.vs").toPath(), StandardCharsets.US_ASCII));
        shaderProgram.createFragmentShader(Files.readString(new File("src/main/resources/fragment.fs").toPath(), StandardCharsets.US_ASCII));
        shaderProgram.link();

        shaderProgram.createUniform("model");
        shaderProgram.createUniform("view");

        float aspectRatio = (float) window.getWidth() / window.getHeight();
        projection = new Matrix4f().setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
        shaderProgram.createUniform("projection");

        shaderProgram.createUniform("viewPos");

        // Light uniforms
        shaderProgram.createMaterialUniform("material");
        shaderProgram.createDirLightUniform("dirLight");
        shaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
        shaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);

        // Light cube shader
        lightCubeShader = new ShaderProgram();
        lightCubeShader.createVertexShader(Files.readString(new File("src/main/resources/light_cube.vs").toPath(), StandardCharsets.US_ASCII));
        lightCubeShader.createFragmentShader(Files.readString(new File("src/main/resources/light_cube.fs").toPath(), StandardCharsets.US_ASCII));
        lightCubeShader.link();

        lightCubeShader.createUniform("model");
        lightCubeShader.createUniform("view");
        lightCubeShader.createUniform("projection");
    }

    public void render(Camera camera, Entity[] entities, DirLight dirLight, PointLight[] pointLights, SpotLight[] spotLights, Material material) {
        shaderProgram.bind();

        Matrix4f view = camera.calculateViewMatrix();
        shaderProgram.setUniform("view", view);
        shaderProgram.setUniform("projection", projection);

        shaderProgram.setUniform("viewPos", camera.getPosition());

        // Update directional light uniforms
        shaderProgram.setUniform("dirLight.direction", dirLight.getDirection());
        shaderProgram.setUniform("dirLight.ambient", dirLight.getAmbient());
        shaderProgram.setUniform("dirLight.diffuse", dirLight.getDiffuse());
        shaderProgram.setUniform("dirLight.specular", dirLight.getSpecular());

        // Update point light uniforms
        for (int i = 0; i < pointLights.length; i++) {
            shaderProgram.setUniform("pointLights[" + i + "].position", pointLights[i].getPosition());
            shaderProgram.setUniform("pointLights[" + i + "].ambient", pointLights[i].getAmbient());
            shaderProgram.setUniform("pointLights[" + i + "].diffuse", pointLights[i].getDiffuse());
            shaderProgram.setUniform("pointLights[" + i + "].specular", pointLights[i].getSpecular());
            shaderProgram.setUniform("pointLights[" + i + "].constant", pointLights[i].getConstant());
            shaderProgram.setUniform("pointLights[" + i + "].linear", pointLights[i].getLinear());
            shaderProgram.setUniform("pointLights[" + i + "].quadratic", pointLights[i].getQuadratic());
        }

        // Update spotlight uniforms
        for (int i = 0; i < spotLights.length; i++) {
            shaderProgram.setUniform("spotLights[" + i + "].position",  spotLights[i].getPosition());
            shaderProgram.setUniform("spotLights[" + i + "].direction", spotLights[i].getDirection());
            shaderProgram.setUniform("spotLights[" + i + "].cutoff", spotLights[i].getCutoff());
            shaderProgram.setUniform("spotLights[" + i + "].outerCutoff", spotLights[i].getOuterCutoff());
            shaderProgram.setUniform("spotLights[" + i + "].ambient",   spotLights[i].getAmbient());
            shaderProgram.setUniform("spotLights[" + i + "].diffuse",   spotLights[i].getDiffuse());
            shaderProgram.setUniform("spotLights[" + i + "].specular",  spotLights[i].getSpecular());
            shaderProgram.setUniform("spotLights[" + i + "].constant",  spotLights[i].getConstant());
            shaderProgram.setUniform("spotLights[" + i + "].linear",    spotLights[i].getLinear());
            shaderProgram.setUniform("spotLights[" + i + "].quadratic", spotLights[i].getQuadratic());
        }

        // Material uniforms
        shaderProgram.setUniform("material.diffuse", 0);
        shaderProgram.setUniform("material.specular", 1);
        shaderProgram.setUniform("material.shininess", material.getShininess());

        // Render containers
        for (Entity entity : entities) {
            Matrix4f model = Maths.calculateModelMatrix(entity.getPosition(), entity.getRotation(), entity.getScale());
            shaderProgram.setUniform("model", model);
            entity.getMesh().render();
        }

        // Render lights
        lightCubeShader.bind();
        lightCubeShader.setUniform("projection", projection);
        lightCubeShader.setUniform("view", view);

        for (PointLight light : pointLights) {
            Matrix4f model = new Matrix4f();
            model.translate(light.getPosition());
            model.scale(0.5f);
            lightCubeShader.setUniform("model", model);

            light.getMesh().render();
        }

        shaderProgram.unbind();
    }

    public void cleanup() {
        shaderProgram.cleanup();
    }

}

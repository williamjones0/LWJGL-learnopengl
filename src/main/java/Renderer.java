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
        shaderProgram.createPointLightUniform("pointLight");

        // Light cube shader
        lightCubeShader = new ShaderProgram();
        lightCubeShader.createVertexShader(Files.readString(new File("src/main/resources/light_cube.vs").toPath(), StandardCharsets.US_ASCII));
        lightCubeShader.createFragmentShader(Files.readString(new File("src/main/resources/light_cube.fs").toPath(), StandardCharsets.US_ASCII));
        lightCubeShader.link();

        lightCubeShader.createUniform("model");
        lightCubeShader.createUniform("view");
        lightCubeShader.createUniform("projection");
    }

    public void render(Camera camera, Entity[] entities, DirLight dirLight, PointLight pointLight, Material material) {
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
        shaderProgram.setUniform("pointLight.position", pointLight.getPosition());
        shaderProgram.setUniform("pointLight.ambient", pointLight.getAmbient());
        shaderProgram.setUniform("pointLight.diffuse", pointLight.getDiffuse());
        shaderProgram.setUniform("pointLight.specular", pointLight.getSpecular());
        shaderProgram.setUniform("pointLight.constant", pointLight.getConstant());
        shaderProgram.setUniform("pointLight.linear", pointLight.getLinear());
        shaderProgram.setUniform("pointLight.quadratic", pointLight.getQuadratic());

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

        // Render light
        lightCubeShader.bind();
        lightCubeShader.setUniform("projection", projection);
        lightCubeShader.setUniform("view", view);
        Matrix4f model = new Matrix4f();
        model.translate(pointLight.getPosition());
        model.scale(0.5f);
        lightCubeShader.setUniform("model", model);

        pointLight.getMesh().render();

        shaderProgram.unbind();
    }

    public void cleanup() {
        shaderProgram.cleanup();
    }

}

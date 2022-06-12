import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

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
        shaderProgram.createLightUniform("light");

        // Light cube shader
        lightCubeShader = new ShaderProgram();
        lightCubeShader.createVertexShader(Files.readString(new File("src/main/resources/light_cube.vs").toPath(), StandardCharsets.US_ASCII));
        lightCubeShader.createFragmentShader(Files.readString(new File("src/main/resources/light_cube.fs").toPath(), StandardCharsets.US_ASCII));
        lightCubeShader.link();

        lightCubeShader.createUniform("model");
        lightCubeShader.createUniform("view");
        lightCubeShader.createUniform("projection");
    }

    public void render(Camera camera, Entity[] entities, PointLight pointLight, Material material) {
        shaderProgram.bind();

        Matrix4f view = camera.calculateViewMatrix();
        shaderProgram.setUniform("view", view);
        shaderProgram.setUniform("projection", projection);

        shaderProgram.setUniform("viewPos", camera.getPosition());

        // Update light uniforms
        shaderProgram.setUniform("light.position", pointLight.getPosition());
        shaderProgram.setUniform("light.ambient", pointLight.getAmbient());
        shaderProgram.setUniform("light.diffuse", pointLight.getDiffuse());
        shaderProgram.setUniform("light.specular", pointLight.getSpecular());

        // Material uniforms
        shaderProgram.setUniform("material.diffuse", 0);
        shaderProgram.setUniform("material.specular", material.getSpecular());
        shaderProgram.setUniform("material.shininess", material.getShininess());

        // Bind diffuse map
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, material.getDiffuse().getID());

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

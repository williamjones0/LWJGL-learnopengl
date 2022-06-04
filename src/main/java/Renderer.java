import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.joml.Matrix4f;

public class Renderer {

    private ShaderProgram shaderProgram;

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
    }

    public void render(Camera camera, Entity[] entities) {
        shaderProgram.bind();

        Matrix4f view = camera.calculateViewMatrix();
        shaderProgram.setUniform("view", view);

        shaderProgram.setUniform("projection", projection);

        for (Entity entity : entities) {
            Matrix4f model = Maths.calculateModelMatrix(entity.getPosition(), entity.getRotation(), entity.getScale());
            shaderProgram.setUniform("model", model);
            entity.getMesh().render();
        }

        shaderProgram.unbind();
    }

    public void cleanup() {
        shaderProgram.cleanup();
    }

}

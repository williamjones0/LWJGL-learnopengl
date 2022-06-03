import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Renderer {

    private ShaderProgram shaderProgram;

    private static final float FOV = (float) Math.toRadians(60.0);
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100f;
    private Matrix4f model;
    private Matrix4f view;
    private Matrix4f projection;

    public void init(Window window) throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Files.readString(new File("src/main/resources/vertex.vs").toPath(), StandardCharsets.US_ASCII));
        shaderProgram.createFragmentShader(Files.readString(new File("src/main/resources/fragment.fs").toPath(), StandardCharsets.US_ASCII));
        shaderProgram.link();

        model = new Matrix4f().identity();
        model.rotate((float) Math.toRadians(-55.0f), new Vector3f(1.0f, 0.0f, 0.0f));
        shaderProgram.createUniform("model");

        view = new Matrix4f().identity();
        view.translate(new Vector3f(0.0f, 0.0f, -3.0f));
        shaderProgram.createUniform("view");

        float aspectRatio = (float) window.getWidth() / window.getHeight();
        projection = new Matrix4f().setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
        shaderProgram.createUniform("projection");
    }

    public void render(Mesh mesh) {
        shaderProgram.bind();

        shaderProgram.setUniform("model", model);
        shaderProgram.setUniform("view", view);
        shaderProgram.setUniform("projection", projection);

        model.rotate((float) glfwGetTime() * 0.001f, new Vector3f(0.5f, 1.0f, 1.0f).normalize());

        mesh.render();

        shaderProgram.unbind();
    }

    public void cleanup() {
        shaderProgram.cleanup();
    }

}

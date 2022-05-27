import org.joml.Vector4f;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Vector;

import static java.lang.Math.sin;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    private ShaderProgram shaderProgram;

    public Renderer() {}

    public void init() throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Files.readString(new File("src/main/resources/vertex.vs").toPath(), StandardCharsets.US_ASCII));
        shaderProgram.createFragmentShader(Files.readString(new File("src/main/resources/fragment.fs").toPath(), StandardCharsets.US_ASCII));
        shaderProgram.link();

        shaderProgram.createUniform("ourColor");
    }

    public void render(Mesh mesh) {
        shaderProgram.bind();

        float timeValue = (float) glfwGetTime();
        float greenValue = (float) ((sin(timeValue) / 2.0f) + 0.5f);
        shaderProgram.setUniform("ourColor", new Vector4f(0.0f, greenValue, 0.0f, 1.0f));

        // Draw mesh
        glBindVertexArray(mesh.getVAO());
        glEnableVertexAttribArray(0);
        glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);

        // Restore state
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);

        shaderProgram.unbind();
    }

    public void cleanup() {
        shaderProgram.cleanup();
    }

}

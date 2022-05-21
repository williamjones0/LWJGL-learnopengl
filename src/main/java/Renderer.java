import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    private int VAO;
    private int VBO;

    private ShaderProgram shaderProgram;

    public Renderer() {}

    public void init() throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Files.readString(new File("src/main/resources/vertex.vs").toPath(), StandardCharsets.US_ASCII));
        shaderProgram.createFragmentShader(Files.readString(new File("src/main/resources/fragment.fs").toPath(), StandardCharsets.US_ASCII));
        shaderProgram.link();

        float[] vertices = {
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
            0.0f,  0.5f, 0.0f
        };

        // Store array of floats into a FloatBuffer so that it can be managed by OpenGL
        FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
        verticesBuffer.put(vertices).flip();

        // Create VAO and bind it
        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        // Create VBO, bind it and put the data into it
        VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

        // Define the structure of the data and store it in one of the attribute lists of the VAO
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        // Unbind VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        // Unbind VAO
        glBindVertexArray(0);

        // Free the off-heap memory allocated by the FloatBuffer
        MemoryUtil.memFree(verticesBuffer);
    }

    public void render() {
        shaderProgram.bind();

        // Bind to the VAO
        glBindVertexArray(VAO);
        glEnableVertexAttribArray(0);

        // Draw the vertices
        glDrawArrays(GL_TRIANGLES, 0, 3);

        // Restore stata
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);

        shaderProgram.unbind();
    }

    public void cleanup() {
        shaderProgram.cleanup();

        glDisableVertexAttribArray(0);

        // Delete the VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(VBO);

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(0);
    }

}

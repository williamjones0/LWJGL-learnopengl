import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {

    private final int VAO;
    private final int posVBO;
    private final int indVBO;
    private final int vertexCount;

    public Mesh(float[] positions, int[] indices) {
        // Store array of floats into a FloatBuffer so that it can be managed by OpenGL
        FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(positions.length);
        verticesBuffer.put(positions).flip();
        vertexCount = indices.length;

        IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();

        // Create VAO and bind it
        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        // Create positions VBO, bind it and put the data into it
        posVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, posVBO);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        // Create indices VBO
        indVBO = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indVBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

        // Unbind VAO
        glBindVertexArray(0);

        // Free the off-heap memory allocated by the FloatBuffer / IntBuffer
        MemoryUtil.memFree(verticesBuffer);
        MemoryUtil.memFree(indicesBuffer);
    }

    public int getVAO() {
        return VAO;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void cleanup() {
        glDisableVertexAttribArray(0);

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(posVBO);
        glDeleteBuffers(indVBO);

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(0);
    }
}

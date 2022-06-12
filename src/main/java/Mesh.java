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
    private final int VBO;
    private final int vertexCount;
    private final Texture texture;

    public Mesh(float[] vertices, Texture texture) {
        this.texture = texture;

        // Store array of floats into a FloatBuffer so that it can be managed by OpenGL
        FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
        verticesBuffer.put(vertices).flip();
        vertexCount = vertices.length;

        // Create VAO and bind it
        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        // Create vertices VBO, bind it and put the data into it
        VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

        // Position attribute
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 4 * 8, 0);
        glEnableVertexAttribArray(0);
        // Normals attribute
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 4 * 8, 4 * 3);
        glEnableVertexAttribArray(1);
        // Texture coordinates attribute
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 4 * 8, 4 * 6);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        // Unbind VAO
        glBindVertexArray(0);

        // Free the off-heap memory allocated by the FloatBuffer / IntBuffer
        MemoryUtil.memFree(verticesBuffer);
    }

    public void render() {
        // Activate and bind texture
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getID());

        glBindVertexArray(VAO);

        glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        glBindVertexArray(0);
    }

    public void cleanup() {
        glDisableVertexAttribArray(0);

        // Delete the VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(VBO);

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(0);
    }
}

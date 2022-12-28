package io.william.util.renderer;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

public class Quad {

    private static int VAO;

    public static void render() {
        if (VAO == 0) {
            io.william.renderer.primitive.Quad quad = new io.william.renderer.primitive.Quad();

            FloatBuffer positionsBuffer = MemoryUtil.memAllocFloat(quad.getPositions().length);
            positionsBuffer.put(quad.getPositions()).flip();

            FloatBuffer texCoordsBuffer = MemoryUtil.memAllocFloat(quad.getTexCoords().length);
            texCoordsBuffer.put(quad.getTexCoords()).flip();

            IntBuffer indicesBuffer = MemoryUtil.memAllocInt(quad.getIndices().length);
            indicesBuffer.put(quad.getIndices()).flip();

            // Set up VAO
            VAO = glGenVertexArrays();
            glBindVertexArray(VAO);

            int positionsVBO = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, positionsVBO);
            glBufferData(GL_ARRAY_BUFFER, positionsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);

            int texCoordsVBO = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, texCoordsVBO);
            glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(1);

            int indicesVBO = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesVBO);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
        }

        glBindVertexArray(VAO);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

}

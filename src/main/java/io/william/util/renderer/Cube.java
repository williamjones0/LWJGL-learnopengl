package io.william.util.renderer;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

public class Cube {

    private static int VAO;

    public static void render() {
        if (VAO == 0) {
            io.william.renderer.primitive.Cube cube = new io.william.renderer.primitive.Cube();

            FloatBuffer positionsBuffer = MemoryUtil.memAllocFloat(cube.getPositions().length);
            positionsBuffer.put(cube.getPositions()).flip();

            FloatBuffer texCoordsBuffer = MemoryUtil.memAllocFloat(cube.getTexCoords().length);
            texCoordsBuffer.put(cube.getTexCoords()).flip();

            IntBuffer indicesBuffer = MemoryUtil.memAllocInt(cube.getIndices().length);
            indicesBuffer.put(cube.getIndices()).flip();

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
        glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

}

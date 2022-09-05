package Utils.rendering;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

public class Quad {

    private static int VAO;

    public static void render() {
        if (VAO == 0) {
            float[] positions = {
               -1.0f,  1.0f, 0.0f,
               -1.0f, -1.0f, 0.0f,
                1.0f,  1.0f, 0.0f,
                1.0f, -1.0f, 0.0f
            };

            float[] texCoords = {
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 1.0f,
                1.0f, 0.0f,
            };

            FloatBuffer positionsBuffer = MemoryUtil.memAllocFloat(positions.length);
            positionsBuffer.put(positions).flip();

            FloatBuffer texCoordsBuffer = MemoryUtil.memAllocFloat(texCoords.length);
            texCoordsBuffer.put(texCoords).flip();

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
        }

        glBindVertexArray(VAO);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        glBindVertexArray(0);
    }

}

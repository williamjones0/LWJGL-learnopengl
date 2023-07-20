package io.william.renderer.terrain;

import org.joml.Vector2f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL40.*;

public class Patch {

    private int VAO;
    private int VBO;
    private int size;

    public Patch() {
        VAO = glGenVertexArrays();
        VBO = glGenBuffers();
    }

    public void allocate(Vector2f[] vertices) {
        size = vertices.length;
        glBindVertexArray(VAO);

        float[] verticesArray = new float[vertices.length * 2];
        for (int i = 0; i < vertices.length; i++) {
            verticesArray[i * 2] = vertices[i].x;
            verticesArray[i * 2 + 1] = vertices[i].y;
        }

        FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(verticesArray.length);
        verticesBuffer.put(verticesArray).flip();
        VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
        glPatchParameteri(GL_PATCH_VERTICES, size);

        glBindVertexArray(0);
    }

    public void draw() {
        glBindVertexArray(VAO);
        glEnableVertexAttribArray(0);

        glDrawArrays(GL_PATCHES, 0, size);

        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glBindVertexArray(VAO);
        glDeleteBuffers(VBO);
        glDeleteVertexArrays(VAO);
        glBindVertexArray(0);
    }

}

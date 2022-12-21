package io.william.renderer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

public class Skybox {

    private final int VAO;
    private final int VBO;
    public final int textureID;

    public Skybox(String[] fileNames, float[] vertices) throws Exception {
        textureID = loadCubemap(fileNames);

        // Store array of floats into a FloatBuffer so that it can be managed by OpenGL
        FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
        verticesBuffer.put(vertices).flip();

        VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 4 * 3, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glBindVertexArray(0);

        // Free the off-heap memory allocated by the FloatBuffer
        MemoryUtil.memFree(verticesBuffer);
    }

    private int loadCubemap(String[] fileNames) throws Exception {
        // Create and bind OpenGL texture
        int ID = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, ID);

        int width;
        int height;
        ByteBuffer buffer;

        for (int i = 0; i < fileNames.length; i++) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer channels = stack.mallocInt(3);

                buffer = stbi_load(fileNames[i], w, h, channels, 3);
                if (buffer == null) {
                    throw new Exception("Image file [" + fileNames[i] + "] not loaded: " + stbi_failure_reason());
                }

                width = w.get();
                height = h.get();
            }

            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, buffer);

            stbi_image_free(buffer);
        }

        // Texture parameters
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        return ID;
    }

    public void render() {
        glDepthFunc(GL_LEQUAL);

        glBindVertexArray(VAO);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureID);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindVertexArray(0);

        glDepthFunc(GL_LESS);
    }

}

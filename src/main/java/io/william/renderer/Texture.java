package io.william.renderer;

import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.ARBBindlessTexture.glGetTextureHandleARB;
import static org.lwjgl.opengl.ARBBindlessTexture.glMakeTextureHandleResidentARB;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

    private final int ID;
    private long handle;
    private int width;
    private int height;

    private String path;

    public Texture(String fileName, int internalFormat, int pixelFormat, int type, boolean flip) throws Exception {
        ID = loadTexture(fileName, internalFormat, pixelFormat, type, flip);
    }

    public Texture(String fileName, int internalFormat, int pixelFormat) throws Exception {
        ID = loadTexture(fileName, internalFormat, pixelFormat, GL_UNSIGNED_BYTE, false);
    }

    public Texture(String fileName, int internalFormat, boolean flip) throws Exception {
        ID = loadTexture(fileName, internalFormat, GL_RGBA, GL_UNSIGNED_BYTE, flip);
    }

    public Texture(String fileName, int internalFormat) throws Exception {
        ID = loadTexture(fileName, internalFormat, GL_RGBA, GL_UNSIGNED_BYTE, false);
    }

    public Texture(int width, int height, int internalFormat, int pixelFormat, int type) throws Exception {
        ID = createTexture(width, height, internalFormat, pixelFormat, type, null);
    }

    public Texture(int width, int height, int internalFormat, int pixelFormat) throws Exception {
        ID = createTexture(width, height, internalFormat, pixelFormat, GL_UNSIGNED_BYTE, null);
    }

    public Texture(int width, int height, ByteBuffer buffer) {
        ID = createTexture(width, height, GL_RGBA, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, ID);
    }

    private int loadTexture(String fileName, int internalFormat, int pixelFormat, int type, boolean flip) throws Exception {
        path = fileName;

        ByteBuffer buffer = null;
        FloatBuffer floatBuffer = null;

        stbi_set_flip_vertically_on_load(flip);

        // Load texture file
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(4);

            if (type == GL_FLOAT) {
                floatBuffer = stbi_loadf(fileName, w, h, channels, 4);
            } else {
                buffer = stbi_load(fileName, w, h, channels, 4);
            }

            if (type == GL_FLOAT) {
                if (floatBuffer == null) {
                    throw new Exception("Image file [" + fileName + "] not loaded: " + stbi_failure_reason());
                }
            } else {
                if (buffer == null) {
                    throw new Exception("Image file [" + fileName + "] not loaded: " + stbi_failure_reason());
                }
            }

            width = w.get();
            height = h.get();
        }

        // Create and bind OpenGL texture
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        // Upload texture data and generate mipmaps
        if (type == GL_FLOAT) {
            glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, pixelFormat, type, floatBuffer);
        } else {
            glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, pixelFormat, type, buffer);
        }
        glGenerateMipmap(GL_TEXTURE_2D);

        // Free image memory
        if (type == GL_FLOAT) {
            stbi_image_free(floatBuffer);
        } else {
            stbi_image_free(buffer);
        }

        return textureID;
    }

    private int createTexture(int width, int height, int internalFormat, int pixelFormat, int type, ByteBuffer buffer) {
        this.width = width;
        this.height = height;

        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, pixelFormat, type, buffer);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        return textureID;
    }

    public void generateHandle() {
        handle = glGetTextureHandleARB(ID);
        glMakeTextureHandleResidentARB(handle);
    }

    public int getID() {
        return ID;
    }

    public long getHandle() {
        return handle;
    }

    public int getTarget() {
        return GL_TEXTURE_2D;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getPath() {
        return path;
    }

}

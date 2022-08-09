import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL21.GL_SRGB;
import static org.lwjgl.opengl.GL21.GL_SRGB_ALPHA;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

    private final int ID;

    public enum Format {
        RGB, RGBA, SRGB, SRGBA
    }

    public Texture(String fileName, Format format) throws Exception {
        int textureFormat;
        if (format == Format.RGB) {
            textureFormat = GL_RGB;
        } else if (format == Format.RGBA) {
            textureFormat = GL_RGBA;
        } else if (format == Format.SRGB) {
            textureFormat = GL_SRGB;
        } else if (format == Format.SRGBA) {
            textureFormat = GL_SRGB_ALPHA;
        } else {
            throw new Exception("Invalid texture format");
        }
        ID = loadTexture(fileName, textureFormat);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, ID);
    }

    public int getID() {
        return ID;
    }

    private static int loadTexture(String fileName, int internalFormat) throws Exception {
        int width;
        int height;
        ByteBuffer buffer;

        // Load texture file
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(4);

            buffer = stbi_load(fileName, w, h, channels, 4);
            if (buffer == null) {
                throw new Exception("Image file [" + fileName + "] not loaded: " + stbi_failure_reason());
            }

            width = w.get();
            height = h.get();
        }

        // Create and bind OpenGL texture
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        // Upload texture data and generate mipmaps
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glGenerateMipmap(GL_TEXTURE_2D);

        stbi_image_free(buffer);

        return textureID;
    }

}

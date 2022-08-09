import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL21.GL_SRGB;
import static org.lwjgl.opengl.GL21.GL_SRGB_ALPHA;
import static org.lwjgl.opengl.GL30.GL_RGBA16F;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

    private final int ID;
    private int width;
    private int height;

    public enum Format {
        RGB, RGBA, SRGB, SRGBA, RGBA16F
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

    public Texture(int width, int height, Format internalFormat, Format pixelFormat) throws Exception {
        int internal_format;
        int pixel_format;

        if (internalFormat == Format.RGBA) {
            internal_format = GL_RGBA;
        } else if (internalFormat == Format.RGBA16F) {
            internal_format = GL_RGBA16F;
        } else {
            throw new Exception("Invalid internal format");
        }
        
        if (pixelFormat == Format.RGB) {
            pixel_format = GL_RGB;
        } else if (pixelFormat == Format.RGBA) {
            pixel_format = GL_RGBA;
        } else if (pixelFormat == Format.SRGB) {
            pixel_format = GL_SRGB;
        } else if (pixelFormat == Format.SRGBA) {
            pixel_format = GL_SRGB_ALPHA;
        } else {
            throw new Exception("Invalid texture format");
        }
        
        ID = createTexture(width, height, internal_format, pixel_format);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, ID);
    }

    private int loadTexture(String fileName, int internalFormat) throws Exception {
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

    private static int createTexture(int width, int height, int internalFormat, int pixelFormat) {
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, pixelFormat, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        return textureID;
    }

    public int getID() {
        return ID;
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

}

package io.william.renderer;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.glFramebufferTexture;

public class Framebuffer {

    private final int ID;
    private int renderbuffer;
    private Texture texture;
    private TextureArray textureArray;

    private int width;
    private int height;

    public Framebuffer(Texture texture, int framebufferAttachment, boolean draw) {
        this.texture = texture;

        ID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, ID);

        glFramebufferTexture2D(GL_FRAMEBUFFER, framebufferAttachment, texture.getTarget(), texture.getID(), 0);

        if (!draw) {
            glDrawBuffer(GL_NONE);
            glReadBuffer(GL_NONE);
        }

        int result = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (result != GL_FRAMEBUFFER_COMPLETE) {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glDeleteFramebuffers(ID);
            throw new RuntimeException("Framebuffer is not complete: " + result);
        }
    }

    public Framebuffer(Texture texture, int internalFormat, int framebufferAttachment, int renderbufferAttachment) {
        this.texture = texture;
        this.width = texture.getWidth();
        this.height = texture.getHeight();

        ID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, ID);

        glFramebufferTexture2D(GL_FRAMEBUFFER, framebufferAttachment, texture.getTarget(), texture.getID(), 0);

        // Set up renderbuffer object
        int renderbuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, renderbuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, internalFormat, texture.getWidth(), texture.getHeight());
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, renderbufferAttachment, GL_RENDERBUFFER, renderbuffer);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        int result = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (result != GL_FRAMEBUFFER_COMPLETE) {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glDeleteFramebuffers(ID);
            throw new RuntimeException("Framebuffer is not complete: " + result);
        }
    }

    public Framebuffer(Texture texture, int internalFormat, int framebufferAttachment, int renderbufferAttachment, int width, int height) {
        this.texture = texture;

        ID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, ID);

        glFramebufferTexture2D(GL_FRAMEBUFFER, framebufferAttachment, texture.getTarget(), texture.getID(), 0);

        // Set up renderbuffer object
        renderbuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, renderbuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, internalFormat, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, renderbufferAttachment, GL_RENDERBUFFER, renderbuffer);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        int result = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (result != GL_FRAMEBUFFER_COMPLETE) {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glDeleteFramebuffers(ID);
            throw new RuntimeException("Framebuffer is not complete: " + result);
        }
    }

    public Framebuffer(TextureArray textureArray, int framebufferAttachment) {
        this.textureArray = textureArray;

        ID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, ID);

        glFramebufferTexture(GL_FRAMEBUFFER, framebufferAttachment, textureArray.getID(), 0);

        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        int result = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (result != GL_FRAMEBUFFER_COMPLETE) {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glDeleteFramebuffers(ID);
            throw new RuntimeException("Framebuffer is not complete: " + result);
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void attachTexture2D(int attachment, int target, int texture) {
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachment, target, texture, 0);
    }

    public void attachTexture2D(int attachment, int target, int texture, int level) {
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachment, target, texture, level);
    }

    public void attachTexture(int attachment, int texture) {
        glFramebufferTexture(GL_FRAMEBUFFER, attachment, texture, 0);
    }

    public void setRenderbufferStorage(int internalFormat, int width, int height) {
        glBindRenderbuffer(GL_RENDERBUFFER, renderbuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, internalFormat, width, height);
    }

    public void cleanup() {
        glDeleteFramebuffers(ID);
    }

    public int getID() {
        return ID;
    }

    public Texture getTexture() {
        return texture;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}

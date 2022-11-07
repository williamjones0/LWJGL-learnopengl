import static org.lwjgl.opengl.GL30.*;

public class Framebuffer {

    private final int ID;
    private int renderbuffer;
    private final Texture texture;

    public Framebuffer(Texture texture, int internalFormat, int attachment) {
        this.texture = texture;

        ID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, ID);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, texture.getTarget(), texture.getID(), 0);

        // Set up renderbuffer object
        int renderbuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, renderbuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, internalFormat, texture.getWidth(), texture.getHeight());
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, attachment, GL_RENDERBUFFER, renderbuffer);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        int result = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (result != GL_FRAMEBUFFER_COMPLETE) {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glDeleteFramebuffers(ID);
            throw new RuntimeException("Framebuffer is not complete: " + result);
        }
    }

    public Framebuffer(Texture texture, int internalFormat, int attachment, int width, int height) {
        this.texture = texture;

        ID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, ID);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, texture.getTarget(), texture.getID(), 0);

        // Set up renderbuffer object
        renderbuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, renderbuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, internalFormat, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, attachment, GL_RENDERBUFFER, renderbuffer);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        int result = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (result != GL_FRAMEBUFFER_COMPLETE) {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glDeleteFramebuffers(ID);
            throw new RuntimeException("Framebuffer is not complete: " + result);
        }
    }

    public void attachTexture(int attachment, int target, int texture) {
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachment, target, texture, 0);
    }

    public void attachTexture(int attachment, int target, int texture, int level) {
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachment, target, texture, level);
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

}

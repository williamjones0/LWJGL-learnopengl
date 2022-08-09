import static org.lwjgl.opengl.GL30.*;

public class Framebuffer {

    private final int ID;
    private Texture texture;

    private final int SCREEN_WIDTH = 1280;
    private final int SCREEN_HEIGHT = 720;

    public Framebuffer(Texture texture) {
        this.texture = texture;

        ID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, ID);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, texture.getTarget(), texture.getID(), 0);

        // Set up renderbuffer object
        int renderbuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, renderbuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, 1280, 720);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, renderbuffer);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        int result = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (result != GL_FRAMEBUFFER_COMPLETE) {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glDeleteFramebuffers(ID);
            throw new RuntimeException("Framebuffer is not complete: " + result);
        }
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

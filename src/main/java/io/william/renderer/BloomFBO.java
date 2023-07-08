package io.william.renderer;

import io.william.util.renderer.Quad;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class BloomFBO {

    public class bloomMip {
        Vector2f size;
        Vector2i intSize;
        int texture;

        public bloomMip() { }

        public Vector2f getSize() {
            return size;
        }

        public void setSize(Vector2f size) {
            this.size = size;
        }

        public Vector2i getIntSize() {
            return intSize;
        }

        public void setIntSize(Vector2i intSize) {
            this.intSize = intSize;
        }

        public int getTexture() {
            return texture;
        }

        public void setTexture(int texture) {
            this.texture = texture;
        }
    }

    private List<bloomMip> mipChain;

    private int ID;

    private boolean initialised;

    public boolean init(int windowWidth, int windowHeight, int mipChainLength) {
        if (initialised) return true;

        ID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, ID);

        Vector2f mipSize = new Vector2f(windowWidth, windowHeight);
        Vector2i mipIntSize = new Vector2i((int) windowWidth, (int) windowHeight);
        mipChain = new ArrayList<>();

        for (int i = 0; i < mipChainLength; i++) {
            bloomMip mip = new bloomMip();

            mipSize.mul(0.5f);
            mipIntSize.x = (int) mipSize.x / 2;
            mipIntSize.y = (int) mipSize.y / 2;
            mip.size = new Vector2f(mipSize);
            mip.intSize = new Vector2i(mipIntSize);

            mip.texture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, mip.texture);

            // Downscaling HDR buffer: float texture format
            glTexImage2D(GL_TEXTURE_2D, 0, GL_R11F_G11F_B10F, (int) mipSize.x, (int) mipSize.y, 0, GL_RGB, GL_FLOAT, (ByteBuffer) null);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            System.out.println("Created bloom mip " + mipIntSize.x + "x" + mipIntSize.y);

            mipChain.add(mip);
        }

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, mipChain.get(0).texture, 0);

        // Set up attachments
        glDrawBuffers(GL_COLOR_ATTACHMENT0);

        // Check completion
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("FBO error, status: " + status);
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            return false;
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        initialised = true;
        return true;
    }

    public void destroy() {
        for (BloomFBO.bloomMip bloomMip : mipChain) {
            glDeleteTextures(bloomMip.texture);
            bloomMip.texture = 0;
        }

        glDeleteFramebuffers(ID);
        ID = 0;
        initialised = false;
    }

    public void bindForWriting() {
        glBindFramebuffer(GL_FRAMEBUFFER, ID);
    }

    public List<bloomMip> getMipChain() {
        return mipChain;
    }

}

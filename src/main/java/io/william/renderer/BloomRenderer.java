package io.william.renderer;

import io.william.util.renderer.Quad;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.*;

public class BloomRenderer {

    private boolean initialised;
    private BloomFBO FBO;
    private Vector2i srcViewportSize;
    private Vector2f srcViewportSizeFloat;
    private ShaderProgram downsampleShader;
    private ShaderProgram upsampleShader;

    public boolean init(int windowWidth, int windowHeight) throws Exception {
        if (initialised) return true;

        srcViewportSize = new Vector2i(windowWidth, windowHeight);
        srcViewportSizeFloat = new Vector2f((float) windowWidth, (float) windowHeight);

        // Framebuffer
        FBO = new BloomFBO();
        final int numBloomMips = 6;
        boolean status = FBO.init(windowWidth, windowHeight, numBloomMips);
        if (!status) {
            System.out.println("Failed to initialise bloom FBO - cannot create bloom renderer");
            return false;
        }

        downsampleShader = new ShaderProgram("Downsample");
        downsampleShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/bloom/quad.vert").toPath(), StandardCharsets.US_ASCII));
        downsampleShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/bloom/downsample.frag").toPath(), StandardCharsets.US_ASCII));
        downsampleShader.link();

        upsampleShader = new ShaderProgram("Upsample");
        upsampleShader.createVertexShader(Files.readString(new File("src/main/resources/shaders/bloom/quad.vert").toPath(), StandardCharsets.US_ASCII));
        upsampleShader.createFragmentShader(Files.readString(new File("src/main/resources/shaders/bloom/upsample.frag").toPath(), StandardCharsets.US_ASCII));
        upsampleShader.link();

        downsampleShader.createUniform("srcTexture");
        downsampleShader.createUniform("srcResolution");
        upsampleShader.createUniform("srcTexture");
        upsampleShader.createUniform("filterRadius");

        downsampleShader.bind();
        downsampleShader.setUniform("srcTexture", 0);
        downsampleShader.unbind();

        upsampleShader.bind();
        upsampleShader.setUniform("srcTexture", 0);
        upsampleShader.unbind();

        initialised = true;
        return true;
    }

    public void destroy() {
        FBO.destroy();
        initialised = false;
    }

    public void renderBloomTexture(int srcTexture, float filterRadius) {
        FBO.bindForWriting();

        renderDownsamples(srcTexture);
        renderUpsamples(filterRadius);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, srcViewportSize.x, srcViewportSize.y);
    }

    private void renderDownsamples(int srcTexture) {
        List<BloomFBO.bloomMip> mipChain = FBO.getMipChain();

        downsampleShader.bind();
        downsampleShader.setUniform("srcResolution", srcViewportSizeFloat);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, srcTexture);

        // Progressively downsample through the mip chain
        for (final BloomFBO.bloomMip mip : mipChain) {
            glViewport(0, 0, (int) mip.size.x, (int) mip.size.y);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, mip.texture, 0);

            // Render screen-size quad
            Quad.render();

            // Set current mip resolution as srcResolution for next iteration
            downsampleShader.setUniform("srcResolution", mip.size);
            glBindTexture(GL_TEXTURE_2D, mip.texture);
        }

        downsampleShader.unbind();
    }

    private void renderUpsamples(float filterRadius) {
        List<BloomFBO.bloomMip> mipChain = FBO.getMipChain();

        upsampleShader.bind();
        upsampleShader.setUniform("filterRadius", filterRadius);

        // Enable additive blending
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);
        glBlendEquation(GL_FUNC_ADD);

        for (int i = mipChain.size() - 1; i > 0; i--) {
            final BloomFBO.bloomMip mip = mipChain.get(i);
            final BloomFBO.bloomMip nextMip = mipChain.get(i - 1);

            // Bind viewport and texture from where to read
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, mip.texture);

            // Set framebuffer render target (we write to this texture)
            glViewport(0, 0, (int) nextMip.size.x, (int) nextMip.size.y);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, nextMip.texture, 0);

            // Render screen-size quad
            Quad.render();
        }

        // Disable additive blending
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_BLEND);

        downsampleShader.unbind();
    }

    public int getBloomTexture() {
        return FBO.getMipChain().get(0).texture;
    }

}

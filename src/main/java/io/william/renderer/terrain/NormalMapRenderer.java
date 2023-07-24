package io.william.renderer.terrain;

import io.william.renderer.ShaderProgram;
import io.william.renderer.Texture;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL30.GL_RGBA32F;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL42.glTexStorage2D;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

public class NormalMapRenderer {

    private float strength;
    private Texture normalMap;
    private final ShaderProgram shader;
    private int N;

    public NormalMapRenderer(int N) throws Exception {
        this.N = N;

        shader = new ShaderProgram("Normal Map Renderer");
        shader.createComputeShader("src/main/resources/shaders/terrain/normal_map.comp");
        shader.link();

        shader.createUniform("heightMap");
        shader.createUniform("N");
        shader.createUniform("strength");

        normalMap = new Texture();
        normalMap.bind();
        normalMap.bilinearFilter();
        glTexStorage2D(GL_TEXTURE_2D, (int) (Math.log(N) / Math.log(2)), GL_RGBA32F, N, N);
    }

    public void render(Texture heightMap) {
        shader.bind();

        glActiveTexture(GL_TEXTURE0);
        heightMap.bind();
        shader.setUniform("heightMap", 0);

        shader.setUniform("N", N);
        shader.setUniform("strength", strength);

        glBindImageTexture(0, normalMap.getID(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA32F);
        glDispatchCompute(N / 16, N / 16, 1);
        glFinish();

        normalMap.bind();
        normalMap.bilinearFilter();

        shader.unbind();
    }

    public float getStrength() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    public Texture getNormalMap() {
        return normalMap;
    }

    public void setNormalMap(Texture normalMap) {
        this.normalMap = normalMap;
    }

    public int getN() {
        return N;
    }

    public void setN(int n) {
        N = n;
    }
}

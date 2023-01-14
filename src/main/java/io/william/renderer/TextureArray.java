package io.william.renderer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.glTexImage3D;
import static org.lwjgl.opengl.GL30.GL_DEPTH_COMPONENT32F;

public class TextureArray {

    private final int ID;
    private final int width;
    private final int height;
    private final int target;

    public TextureArray(int width, int height, int depth, int target) {
        ID = glGenTextures();
        this.width = width;
        this.height = height;
        this.target = target;

        glBindTexture(target, ID);
        glTexImage3D(target, 0, GL_DEPTH_COMPONENT32F, width, height, depth, 0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
    }

    public void bind() {
        glBindTexture(target, ID);
    }

    public int getID() {
        return ID;
    }

    public int getTarget() {
        return target;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}

package io.william.renderer.terrain;

import io.william.renderer.ShaderProgram;
import io.william.util.Maths;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class TerrainNode extends Node {

    private boolean isLeaf;
    private TerrainConfig config;
    private int lod;
    private Vector2f location;
    private Vector3f worldPos;
    private Vector2f index;
    private float gap;
    private Patch buffer;

    public TerrainNode(Patch buffer, TerrainConfig config, Vector2f location, int lod, Vector2f index, Vector3f cameraPos) {
        this.buffer = buffer;
        this.config = config;
        this.location = location;
        this.lod = lod;
        this.index = index;
        this.gap = 1.0f / (TerrainQuadtree.getRootNodes() * (float) Math.pow(2, lod));

        Vector3f localScaling = new Vector3f(gap, 0, gap);
        Vector3f localPosition = new Vector3f(location.x, 0, location.y);

        setLocalScaling(localScaling);
        setLocalPosition(localPosition);

        setWorldScaling(new Vector3f(config.getScaleXZ(), config.getScaleY(), config.getScaleXZ()));
        setWorldPosition(new Vector3f(-config.getScaleXZ() / 2.0f, 0, -config.getScaleXZ() / 2.0f));

        computeWorldPos();
        updateQuadtree(cameraPos);
    }

    public void render(ShaderProgram shader) {
        if (isLeaf) {
            shader.setUniform("scaleY", config.getScaleY());
            shader.setUniform("lod", lod);
            shader.setUniform("index", index);
            shader.setUniform("location", location);
            shader.setUniform("gap", gap);

            for (int i = 0; i < 8; i++) {
                shader.setUniform("lod_morph_area[" + i + "]", config.getLod_morphing_area()[i]);
            }

            shader.setUniform("localMatrix", Maths.calculateModelMatrix(getLocalPosition(), getLocalRotation(), getLocalScaling()));
            shader.setUniform("worldMatrix", Maths.calculateModelMatrix(getWorldPosition(), getWorldRotation(), getWorldScaling()));
            buffer.draw();
        }

        for (Node child : getChildren()) {
            child.render(shader);
        }
    }

    public void updateQuadtree(Vector3f cameraPos) {
        worldPos.y = Math.min(cameraPos.y, config.getScaleY());

        updateChildNodes(cameraPos);

        for (Node child : getChildren()) {
            ((TerrainNode) child).updateQuadtree(cameraPos);
        }
    }

    private void addChildNodes(int lod, Vector3f cameraPos) {
        if (isLeaf) {
            isLeaf = false;
        }

        if (getChildren().size() == 0) {
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    addChild(new TerrainNode(buffer, config, new Vector2f(location).add(new Vector2f(i * gap / 2.0f, j * gap / 2.0f)), lod, new Vector2f(i, j), cameraPos));
                }
            }
        }
    }

    private void removeChildNodes() {
        if (!isLeaf) {
            isLeaf = true;
        }

        if (getChildren().size() > 0) {
            getChildren().clear();
        }
    }

    private void updateChildNodes(Vector3f cameraPos) {
        float distance = new Vector3f(cameraPos).sub(worldPos).length();

        if (distance < config.getLod_range()[lod]) {
            addChildNodes(lod + 1, cameraPos);
        } else {
            removeChildNodes();
        }
    }

    private void computeWorldPos() {
        Vector2f loc = new Vector2f(location).add(new Vector2f(gap / 2.0f)).mul(config.getScaleXZ()).sub(new Vector2f(config.getScaleXZ() / 2.0f));

        worldPos = new Vector3f(loc.x, 0, loc.y);
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    public TerrainConfig getConfig() {
        return config;
    }

    public void setConfig(TerrainConfig config) {
        this.config = config;
    }

    public int getLod() {
        return lod;
    }

    public void setLod(int lod) {
        this.lod = lod;
    }

    public Vector2f getLocation() {
        return location;
    }

    public void setLocation(Vector2f location) {
        this.location = location;
    }

    public Vector3f getWorldPos() {
        return worldPos;
    }

    public void setWorldPos(Vector3f worldPos) {
        this.worldPos = worldPos;
    }

    public Vector2f getIndex() {
        return index;
    }

    public void setIndex(Vector2f index) {
        this.index = index;
    }

    public float getGap() {
        return gap;
    }

    public void setGap(float gap) {
        this.gap = gap;
    }

    public Patch getBuffer() {
        return buffer;
    }

    public void setBuffer(Patch buffer) {
        this.buffer = buffer;
    }
}

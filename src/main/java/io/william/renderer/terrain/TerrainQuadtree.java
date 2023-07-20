package io.william.renderer.terrain;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class TerrainQuadtree extends Node {

    private static int rootNodes = 8;

    public TerrainQuadtree(TerrainConfig config, Vector3f cameraPos) {
        Patch buffer = new Patch();
        buffer.allocate(generatePatch());

        for (int i = 0; i < rootNodes; i++) {
            for (int j = 0; j < rootNodes; j++) {
                addChild(new TerrainNode(buffer, config, new Vector2f(i / (float) rootNodes, j / (float) rootNodes), 0, new Vector2f(i, j), cameraPos));
            }
        }

        setWorldScaling(new Vector3f(config.getScaleXZ(), config.getScaleY(), config.getScaleXZ()));
        setWorldPosition(new Vector3f(config.getScaleXZ() / 2.0f, 0, config.getScaleXZ() / 2.0f));
    }

    public void updateQuadtree(Vector3f cameraPos) {
        for (Node child : getChildren()) {
            ((TerrainNode) child).updateQuadtree(cameraPos);
        }
    }

    public Vector2f[] generatePatch() {
        Vector2f[] vertices = new Vector2f[16];

        int index = 0;

        vertices[index++] = new Vector2f(0, 0);
        vertices[index++] = new Vector2f(0.333f, 0);
        vertices[index++] = new Vector2f(0.666f, 0);
        vertices[index++] = new Vector2f(1, 0);

        vertices[index++] = new Vector2f(0, 0.333f);
        vertices[index++] = new Vector2f(0.333f, 0.333f);
        vertices[index++] = new Vector2f(0.666f, 0.333f);
        vertices[index++] = new Vector2f(1, 0.333f);

        vertices[index++] = new Vector2f(0, 0.666f);
        vertices[index++] = new Vector2f(0.333f, 0.666f);
        vertices[index++] = new Vector2f(0.666f, 0.666f);
        vertices[index++] = new Vector2f(1, 0.666f);

        vertices[index++] = new Vector2f(0, 1);
        vertices[index++] = new Vector2f(0.333f, 1);
        vertices[index++] = new Vector2f(0.666f, 1);
        vertices[index++] = new Vector2f(1, 1);

        return vertices;
    }

    public static int getRootNodes() {
        return rootNodes;
    }

    public static void setRootNodes(int rootNodes) {
        TerrainQuadtree.rootNodes = rootNodes;
    }
}

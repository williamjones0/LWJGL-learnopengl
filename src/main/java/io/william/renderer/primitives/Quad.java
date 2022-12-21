package io.william.renderer.primitives;

public class Quad {

    final float[] positions = {
        // Positions
       -1.0f,  1.0f, 0f,  // Top left
       -1.0f, -1.0f, 0f,  // Bottom left
        1.0f, -1.0f, 0f,  // Bottom right
        1.0f,  1.0f, 0f,  // Top right
    };

    private final float[] normals = {
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f
    };

    private final float[] texCoords = {
        0.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f,
        1.0f, 1.0f
    };

    private final int[] indices = {
        0, 1, 3,
        3, 1, 2
    };

    public float[] getPositions() {
        return positions;
    }

    public float[] getNormals() {
        return normals;
    }

    public float[] getTexCoords() {
        return texCoords;
    }

    public int[] getIndices() {
        return indices;
    }

}

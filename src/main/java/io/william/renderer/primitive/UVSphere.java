package io.william.renderer.primitive;

import java.util.ArrayList;
import java.util.List;

import static io.william.util.Utils.floatListToArray;
import static io.william.util.Utils.intListToArray;

public class UVSphere {

    private static final float PI = (float) Math.PI;
    private float radius;
    private int sectors;
    private int stacks;

    private float[] positions;
    private float[] normals;
    private float[] texCoords;

    private int[] indices;

    public UVSphere(float radius, int sectors, int stacks) {
        this.radius = radius;
        this.sectors = sectors;
        this.stacks = stacks;

        this.positions = new float[(sectors + 1) * (stacks + 1) * 3];
        this.normals = new float[(sectors + 1) * (stacks + 1) * 3];
        this.texCoords = new float[(sectors + 1) * (stacks + 1) * 2];

        this.indices = new int[sectors * stacks * 6];

        generateVertices();
        generateTriangles();
    }

    private void generateVertices() {
        List<Float> positionsList = new ArrayList<>();
        List<Float> normalsList = new ArrayList<>();
        List<Float> texCoordsList = new ArrayList<>();

        float x, y, z, xy;                            // Position
        float nx, ny, nz, lengthInv = 1.0f / radius;  // Normals
        float s, t;                                   // Texture coords

        float sectorStep = 2 * PI / sectors;
        float stackStep = PI / stacks;
        float sectorAngle, stackAngle;

        for (int i = 0; i <= stacks; i++) {
            stackAngle = PI / 2 - i * stackStep;
            xy = radius * (float) Math.cos(stackAngle);
            z = radius * (float) Math.sin(stackAngle);

            // Add (sectorCount+1) vertices per stack
            // The first and last vertices have the same position and normal, but different texture coords
            for (int j = 0; j <= sectors; j++) {
                sectorAngle = j * sectorStep;

                // Vertex position (x, y, z)
                x = xy * (float) Math.cos(sectorAngle);
                y = xy * (float) Math.sin(sectorAngle);
                positionsList.add(x);
                positionsList.add(y);
                positionsList.add(z);

                // Normal (nx, ny, nz)
                nx = x * lengthInv;
                ny = y * lengthInv;
                nz = z * lengthInv;
                normalsList.add(nx);
                normalsList.add(ny);
                normalsList.add(nz);

                // Texture coords (s, t) range from (0, 0) to (1, 1)
                s = (float) (j) / sectors;
                t = (float) (i) / stacks;
                texCoordsList.add(s);
                texCoordsList.add(t);
            }
        }

        // Convert lists to arrays
        positions = floatListToArray(positionsList);
        normals = floatListToArray(normalsList);
        texCoords = floatListToArray(texCoordsList);
    }

    private void generateTriangles() {
        List<Integer> indicesList = new ArrayList<>();

        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < sectors; j++) {
                // First stack has only one triangle
                // All other stacks have two triangles
                int i_j = i * (sectors + 1) + j;
                int i_j_plus_1 = i * (sectors + 1) + j + 1;
                int i_plus_1_j = (i + 1) * (sectors + 1) + j;
                int i_plus_1_j_plus_1 = (i + 1) * (sectors + 1) + j + 1;

                if (i != 0) {
                    // First triangle of each stack
                    indicesList.add(i_j);
                    indicesList.add(i_j_plus_1);
                    indicesList.add(i_plus_1_j);
                }
                if (i != (stacks - 1)) {
                    // Second triangle of each stack
                    indicesList.add(i_plus_1_j);
                    indicesList.add(i_j_plus_1);
                    indicesList.add(i_plus_1_j_plus_1);
                }
            }
        }

        indices = intListToArray(indicesList);
    }

    public void update() {
        generateVertices();
        generateTriangles();
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public int getSectors() {
        return sectors;
    }

    public void setSectors(int sectors) {
        this.sectors = sectors;
    }

    public int getStacks() {
        return stacks;
    }

    public void setStacks(int stacks) {
        this.stacks = stacks;
    }

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

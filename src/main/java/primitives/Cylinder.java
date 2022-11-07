package primitives;

import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

import static Utils.Utils.floatListToArray;
import static Utils.Utils.intListToArray;

public class Cylinder {

    private static final float PI = (float) Math.PI;
    private float topRadius;
    private float bottomRadius;
    private float height;
    private int sectors;

    private float[] positions;
    private float[] normals;
    private float[] texCoords;

    private int[] indices;

    private int bottomCenterIndex;
    private int topCenterIndex;

    public Cylinder(float topRadius, float bottomRadius, float height, int sectors) {
        this.topRadius = topRadius;
        this.bottomRadius = bottomRadius;
        this.height = height;
        this.sectors = sectors;

        generateVertices();
        generateTriangles();
    }

    private void generateVertices() {
        List<Float> positionsList = new ArrayList<>();
        List<Float> normalsList = new ArrayList<>();
        List<Float> texCoordsList = new ArrayList<>();

        float sectorStep = 2 * PI / sectors;
        List<Vector2f> unitCirclePositions = new ArrayList<>();

        for (int i = 0; i <= sectors; i++) {
            float sectorAngle = i * sectorStep;
            unitCirclePositions.add(new Vector2f((float) Math.cos(sectorAngle), (float) Math.sin(sectorAngle)));
        }

        // Side faces
        for (int i = 0; i < 2; i++) {
            float radius = i == 0 ? topRadius : bottomRadius;
            float z = i == 0 ? -height / 2 : height / 2;

            for (Vector2f unitCirclePosition : unitCirclePositions) {
                // Vertex position (x, y, z)
                positionsList.add(unitCirclePosition.x * radius);
                positionsList.add(unitCirclePosition.y * radius);
                positionsList.add(z);

                // Normal (nx, ny, nz)
                normalsList.add(unitCirclePosition.x);
                normalsList.add(unitCirclePosition.y);
                normalsList.add(0f);

                // Texture coords (s, t) range from (0, 0) to (1, 1)
                texCoordsList.add(unitCirclePositions.indexOf(unitCirclePosition) / (float) sectors);
                texCoordsList.add(1.0f - i);
            }
        }

        bottomCenterIndex = positionsList.size() / 3;
        topCenterIndex = bottomCenterIndex + sectors + 1;

        // Top and bottom faces
        for (int i = 0; i < 2; i++) {
            float radius = i == 0 ? topRadius : bottomRadius;
            float z = i == 0 ? -height / 2 : height / 2;

            // Center point
            positionsList.add(0f);
            positionsList.add(0f);
            positionsList.add(z);

            normalsList.add(0f);
            normalsList.add(0f);
            normalsList.add(i == 0 ? -1f : 1f);

            texCoordsList.add(0.5f);
            texCoordsList.add(0.5f);

            for (int j = 0; j < sectors; j++) {
                // Vertex position (x, y, z)
                positionsList.add(unitCirclePositions.get(j).x * radius);
                positionsList.add(unitCirclePositions.get(j).y * radius);
                positionsList.add(z);

                // Normal (nx, ny, nz)
                normalsList.add(0f);
                normalsList.add(0f);
                normalsList.add(i == 0 ? -1f : 1f);

                // Texture coords (s, t) range from (0, 0) to (1, 1)
                texCoordsList.add(-unitCirclePositions.get(j).x * 0.5f + 0.5f);
                texCoordsList.add(-unitCirclePositions.get(j).y * 0.5f + 0.5f);
            }
        }

        positions = floatListToArray(positionsList);
        normals = floatListToArray(normalsList);
        texCoords = floatListToArray(texCoordsList);
    }

    private void generateTriangles() {
        List<Integer> indicesList = new ArrayList<>();

        // Side faces
        for (int i = 0; i < sectors; i++) {
            // First triangle
            indicesList.add(i);
            indicesList.add(i + 1);
            indicesList.add(sectors + 1 + i);

            // Second triangle
            indicesList.add(sectors + i + 1);
            indicesList.add(i + 1);
            indicesList.add(sectors + i + 2);
        }

        // Bottom face
        for (int i = 0, k = bottomCenterIndex + 1; i < sectors; i++, k++) {
            if (i < sectors - 1) {
                indicesList.add(bottomCenterIndex);
                indicesList.add(k + 1);
                indicesList.add(k);
            } else {
                indicesList.add(bottomCenterIndex);
                indicesList.add(bottomCenterIndex + 1);
                indicesList.add(k);
            }
        }

        // Top face
        for (int i = 0, k = topCenterIndex + 1; i < sectors; i++, k++) {
            if (i < sectors - 1) {
                indicesList.add(topCenterIndex);
                indicesList.add(k);
                indicesList.add(k + 1);
            } else {
                indicesList.add(topCenterIndex);
                indicesList.add(k);
                indicesList.add(topCenterIndex + 1);
            }
        }

        indices = intListToArray(indicesList);
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

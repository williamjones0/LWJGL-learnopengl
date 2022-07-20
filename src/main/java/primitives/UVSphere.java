package primitives;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class UVSphere {

    private static final float PI = (float) Math.PI;
    private float radius;
    private int sectors;
    private int stacks;

    private final List<Vector3f> positions;
    private final List<Vector3f> normals;
    private final List<Vector2f> texCoords;

    private final List<Float> vertices;
    private final List<Integer> indices;

    public UVSphere(float radius, int sectors, int stacks) {
        this.radius = radius;
        this.sectors = sectors;
        this.stacks = stacks;

        this.positions = new ArrayList<>();
        this.normals = new ArrayList<>();
        this.texCoords = new ArrayList<>();

        this.vertices = new ArrayList<>();
        this.indices = new ArrayList<>();

        generateVertices();
        generateTriangles();
    }

    private void generateVertices() {
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
                positions.add(new Vector3f(x, y, z));

                // Normal (nx, ny, nz)
                nx = x * lengthInv;
                ny = y * lengthInv;
                nz = z * lengthInv;
                normals.add(new Vector3f(nx, ny, nz));

                // Texture coords (s, t) range from (0, 0) to (1, 1)
                s = (float) (j) / sectors;
                t = (float) (i) / stacks;
                texCoords.add(new Vector2f(s, t));

                vertices.add(x);
                vertices.add(y);
                vertices.add(z);
                vertices.add(nx);
                vertices.add(ny);
                vertices.add(nz);
                vertices.add(s);
                vertices.add(t);
            }
        }
    }

    private void generateTriangles() {
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
                    indices.add(i_j);
                    indices.add(i_j_plus_1);
                    indices.add(i_plus_1_j);
                }
                if (i != (stacks - 1)) {
                    // Second triangle of each stack
                    indices.add(i_plus_1_j);
                    indices.add(i_j_plus_1);
                    indices.add(i_plus_1_j_plus_1);
                }
            }
        }
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

    public List<Float> getVertices() {
        return vertices;
    }

    public List<Integer> getIndices() {
        return indices;
    }
}

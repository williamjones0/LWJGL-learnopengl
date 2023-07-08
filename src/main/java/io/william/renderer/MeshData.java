package io.william.renderer;

public class MeshData {

    private final float[] positions;
    private final float[] normals;
    private final float[] tangents;
    private final float[] bitangents;
    private final float[] texCoords;
    private final int[] indices;

    private int materialID;
    private float emissionStrength;

    public MeshData(float[] positions, float[] normals, float[] tangents, float[] bitangents, float[] texCoords, int[] indices) {
        this.positions = positions;
        this.normals = normals;
        this.tangents = tangents;
        this.bitangents = bitangents;
        this.texCoords = texCoords;
        this.indices = indices;

        this.materialID = 0;
        this.emissionStrength = 1.0f;
    }

    public float[] getPositions() {
        return positions;
    }

    public float[] getNormals() {
        return normals;
    }

    public float[] getTangents() {
        return tangents;
    }

    public float[] getBitangents() {
        return bitangents;
    }

    public float[] getTexCoords() {
        return texCoords;
    }

    public int[] getIndices() {
        return indices;
    }

    public int getMaterialID() {
        return materialID;
    }

    public void setMaterialID(int materialID) {
        this.materialID = materialID;
    }

    public float getEmissionStrength() {
        return emissionStrength;
    }

    public void setEmissionStrength(float emissionStrength) {
        this.emissionStrength = emissionStrength;
    }

}

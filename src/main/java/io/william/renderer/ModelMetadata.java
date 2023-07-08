package io.william.renderer;

import io.william.renderer.primitive.Cube;
import io.william.renderer.primitive.Cylinder;
import io.william.renderer.primitive.Quad;
import io.william.renderer.primitive.UVSphere;

import java.util.List;
import java.util.Map;

public class ModelMetadata {

    // Metadata
    public enum Type {
        QUAD,
        CUBE,
        CYLINDER,
        SPHERE,
        ASSIMP
    }

    private final Type type;

    // Cylinder
    private float topRadius;
    private float bottomRadius;
    private float height;
    private int sectors;

    // Sphere
    private float radius;
    //    private int sectors;
    private int stacks;

    // Assimp
    private String modelPath;
    private String texturesPath;

    // Map mesh data indexes to material IDs
    private Map<Integer, Integer> meshDataMaterialIDs;

    public ModelMetadata(Type type, Map<Integer, Integer> meshDataMaterialIDs) {
        this.type = type;
    }

    public ModelMetadata(Quad quad, Map<Integer, Integer> meshDataMaterialIDs) {
        type = Type.QUAD;

        this.meshDataMaterialIDs = meshDataMaterialIDs;
    }

    public ModelMetadata(Cube cube, Map<Integer, Integer> meshDataMaterialIDs) {
        type = Type.CUBE;

        this.meshDataMaterialIDs = meshDataMaterialIDs;
    }

    public ModelMetadata(Cylinder cylinder, Map<Integer, Integer> meshDataMaterialIDs) {
        type = Type.CYLINDER;
        this.topRadius = cylinder.getTopRadius();
        this.bottomRadius = cylinder.getBottomRadius();
        this.height = cylinder.getHeight();
        this.sectors = cylinder.getSectors();

        this.meshDataMaterialIDs = meshDataMaterialIDs;
    }

    public ModelMetadata(UVSphere sphere, Map<Integer, Integer> meshDataMaterialIDs) {
        type = Type.SPHERE;
        this.radius = sphere.getRadius();
        this.sectors = sphere.getSectors();
        this.stacks = sphere.getStacks();

        this.meshDataMaterialIDs = meshDataMaterialIDs;
    }

    public ModelMetadata(String modelPath, String texturesPath, Map<Integer, Integer> meshDataMaterialIDs) {
        type = Type.ASSIMP;
        this.modelPath = modelPath;
        this.texturesPath = texturesPath;

        this.meshDataMaterialIDs = meshDataMaterialIDs;
    }

    public Type getType() {
        return type;
    }

    public float getTopRadius() {
        return topRadius;
    }

    public float getBottomRadius() {
        return bottomRadius;
    }

    public float getHeight() {
        return height;
    }

    public int getSectors() {
        return sectors;
    }

    public float getRadius() {
        return radius;
    }

    public int getStacks() {
        return stacks;
    }

    public String getModelPath() {
        return modelPath;
    }

    public String getTexturesPath() {
        return texturesPath;
    }

    public Map<Integer, Integer> getMeshDataMaterialIDs() {
        return meshDataMaterialIDs;
    }
}

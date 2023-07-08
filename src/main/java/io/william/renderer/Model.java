package io.william.renderer;

import java.util.ArrayList;
import java.util.List;

public class Model {

    private int ID;
    private final List<Entity> entities;
    private final List<MeshData> meshDatas;
    private final List<SceneMesh.MeshDrawData> meshDrawDatas;

    private final ModelMetadata modelMetadata;

    private String name;

    public Model(MeshData meshData, ModelMetadata modelMetadata, String name) {
        this.entities = new ArrayList<>();
        this.meshDatas = new ArrayList<>();
        meshDatas.add(meshData);
        this.meshDrawDatas = new ArrayList<>();

        this.modelMetadata = modelMetadata;

        this.name = name;
    }

    public Model(List<MeshData> meshDatas, ModelMetadata modelMetadata, String name) {
        this.entities = new ArrayList<>();
        this.meshDatas = meshDatas;
        this.meshDrawDatas = new ArrayList<>();

        this.modelMetadata = modelMetadata;

        this.name = name;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }

    public List<MeshData> getMeshDatas() {
        return meshDatas;
    }

    public List<SceneMesh.MeshDrawData> getMeshDrawDatas() {
        return meshDrawDatas;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ModelMetadata getModelMetadata() {
        return modelMetadata;
    }
}

package io.william.renderer;

import java.util.ArrayList;
import java.util.List;

public class Model {

    private int ID;
    private final List<Entity> entities;
    private final List<MeshData> meshDatas;
    private final List<SceneMesh.MeshDrawData> meshDrawDatas;

    public Model(List<MeshData> meshDatas) {
        this.entities = new ArrayList<>();
        this.meshDatas = meshDatas;
        this.meshDrawDatas = new ArrayList<>();
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

    public List<MeshData> getMeshDatas() {
        return meshDatas;
    }

    public List<SceneMesh.MeshDrawData> getMeshDrawDatas() {
        return meshDrawDatas;
    }

}

package io.william.renderer;

import java.util.ArrayList;
import java.util.List;

public class Scene {

    private final List<Model> models;
    private final List<PBRMaterial> PBRMaterials;
    private final List<Entity> entities;
    private DirLight dirLight;
    private final List<PointLight> pointLights;
    private final List<SpotLight> spotLights;
    private EquirectangularMap equirectangularMap;

    private int currentModelID;
    private int currentPBRMaterialID = 0;
    private int currentEntityID = 0;

    public Scene() {
        this.models = new ArrayList<>();
        this.PBRMaterials = new ArrayList<>();
        this.entities = new ArrayList<>();
        this.pointLights = new ArrayList<>();
        this.spotLights = new ArrayList<>();
    }

    public Model getModelByID(int id) {
        for (Model model : models) {
            if (model.getID() == id) {
                return model;
            }
        }
        return null;
    }

    public List<Model> getModels() {
        return models;
    }

    public void addModel(Model model) {
        model.setID(currentModelID);
        currentModelID++;
        models.add(model);
    }

    public List<PBRMaterial> getPBRMaterials() {
        return PBRMaterials;
    }

    public void addPBRMaterial(PBRMaterial pbrMaterial) {
        pbrMaterial.setID(currentPBRMaterialID);
        currentPBRMaterialID++;
        this.PBRMaterials.add(pbrMaterial);
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void addEntity(Entity entity) {
        entity.setID(currentEntityID);
        currentEntityID++;
        entities.add(entity);
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }

    public DirLight getDirLight() {
        return dirLight;
    }

    public void setDirLight(DirLight dirLight) {
        this.dirLight = dirLight;
    }

    public List<PointLight> getPointLights() {
        return pointLights;
    }

    public void addPointLight(PointLight pointLight) {
        pointLights.add(pointLight);
    }

    public List<SpotLight> getSpotLights() {
        return spotLights;
    }

    public void addSpotLight(SpotLight spotLight) {
        spotLights.add(spotLight);
    }

    public EquirectangularMap getEquirectangularMap() {
        return equirectangularMap;
    }

    public void setEquirectangularMap(EquirectangularMap equirectangularMap) {
        this.equirectangularMap = equirectangularMap;
    }
}

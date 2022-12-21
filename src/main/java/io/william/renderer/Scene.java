package io.william.renderer;

import java.util.List;

public class Scene {

    private List<Entity> entities;
    private DirLight dirLight;
    private List<PointLight> pointLights;
    private List<SpotLight> spotLights;
    private Skybox skybox;
    private EquirectangularMap equirectangularMap;

    public Scene(List<Entity> entities, DirLight dirLight, List<PointLight> pointLights, List<SpotLight> spotLights, EquirectangularMap equirectangularMap) {
        this.entities = entities;
        this.dirLight = dirLight;
        this.pointLights = pointLights;
        this.spotLights = spotLights;
//        this.skybox = skybox;
        this.equirectangularMap = equirectangularMap;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public DirLight getDirLight() {
        return dirLight;
    }

    public List<PointLight> getPointLights() {
        return pointLights;
    }

    public List<SpotLight> getSpotLights() {
        return spotLights;
    }

    public Skybox getSkybox() {
        return skybox;
    }

    public EquirectangularMap getEquirectangularMap() {
        return equirectangularMap;
    }

}

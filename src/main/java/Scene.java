public class Scene {

    private Entity[] entities;
    private DirLight dirLight;
    private PointLight[] pointLights;
    private SpotLight[] spotLights;
    private Skybox skybox;

    public Scene(Entity[] entities, DirLight dirLight, PointLight[] pointLights, SpotLight[] spotLights, Skybox skybox) {
        this.entities = entities;
        this.dirLight = dirLight;
        this.pointLights = pointLights;
        this.spotLights = spotLights;
        this.skybox = skybox;
    }

    public Entity[] getEntities() {
        return entities;
    }

    public DirLight getDirLight() {
        return dirLight;
    }

    public PointLight[] getPointLights() {
        return pointLights;
    }

    public SpotLight[] getSpotLights() {
        return spotLights;
    }

    public Skybox getSkybox() {
        return skybox;
    }

}

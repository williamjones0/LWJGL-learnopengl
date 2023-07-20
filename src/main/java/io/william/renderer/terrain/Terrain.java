package io.william.renderer.terrain;

import org.joml.Vector3f;

public class Terrain extends Node {

    private TerrainConfig configuration;

    public void init(String config, Vector3f cameraPos) {
        this.configuration = new TerrainConfig();
        this.configuration.loadFile(config);

        addChild(new TerrainQuadtree(configuration, cameraPos));
    }

    public void updateQuadtree(Vector3f cameraPos) {
        ((TerrainQuadtree) getChildren().get(0)).updateQuadtree(cameraPos);
    }

    public TerrainConfig getConfiguration() {
        return configuration;
    }

    public void setConfiguration(TerrainConfig configuration) {
        this.configuration = configuration;
    }
}

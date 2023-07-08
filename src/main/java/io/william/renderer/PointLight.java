package io.william.renderer;

import io.william.game.component.MovementController;
import org.joml.Vector3f;
import io.william.renderer.primitive.UVSphere;

public class PointLight {

    private Mesh mesh;

    private Vector3f position;

    private Vector3f color;

    private float intensity;

    private boolean enabled;

    public PointLight(Vector3f position, Vector3f color, float intensity) {
        UVSphere uvSphere = new UVSphere(0.1f, 32, 32);

        this.mesh = new Mesh(new MeshData(
            uvSphere.getPositions(),
            uvSphere.getNormals(),
            uvSphere.getTexCoords(),
            new float[] {},
            new float[] {},
            uvSphere.getIndices()
        ));

        this.position = position;

        this.color = color;

        this.intensity = intensity;

        this.enabled = true;
    }

    public PointLight(Vector3f position, Vector3f color) {
        this(position, color, 1.0f);
    }

    public void updateRadius(float radius) {
        UVSphere uvSphere = new UVSphere(radius, 32, 32);

        this.mesh = new Mesh(new MeshData(
            uvSphere.getPositions(),
            uvSphere.getNormals(),
            uvSphere.getTexCoords(),
            new float[] {},
            new float[] {},
            uvSphere.getIndices()
        ));
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public void setPosition(float x, float y, float z) {
        this.position = new Vector3f(x, y, z);
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public void setColor(float r, float g, float b) {
        this.color = new Vector3f(r, g, b);
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

package io.william.renderer;

import org.joml.Vector3f;

public class DirLight {

    private Vector3f direction;

    private float azimuth;
    private float elevation;

    private Vector3f color;

    public DirLight(Vector3f direction, Vector3f color) {
        this.direction = direction;

        this.color = color;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
        this.azimuth = (float) Math.toDegrees(Math.atan2(direction.x, direction.z));
        this.elevation = (float) Math.toDegrees(Math.atan2(direction.z, Math.sqrt(direction.x * direction.x + direction.y * direction.y)));
    }

    public float getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
        this.direction = new Vector3f(
            (float) (Math.sin(Math.toRadians(azimuth)) * Math.cos(Math.toRadians(elevation))),
            (float) Math.sin(Math.toRadians(elevation)),
            (float) (-Math.cos(Math.toRadians(azimuth)) * Math.cos(Math.toRadians(elevation)))
        ).normalize();
    }

    public float getElevation() {
        return elevation;
    }

    public void setElevation(float elevation) {
        this.elevation = elevation;
        this.direction = new Vector3f(
            (float) (Math.sin(Math.toRadians(azimuth)) * Math.cos(Math.toRadians(elevation))),
            (float) Math.sin(Math.toRadians(elevation)),
            (float) (-Math.cos(Math.toRadians(azimuth)) * Math.cos(Math.toRadians(elevation)))
        ).normalize();
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }
}

import org.joml.Vector3f;
import primitives.Cylinder;

public class SpotLight {

    private final Mesh mesh;

    private Vector3f position;
    private Vector3f direction;

    private float azimuth;
    private float elevation;

    private Vector3f color;

    private float intensity;

    private float cutoff;
    private float outerCutoff;

    private boolean enabled;

    public SpotLight(Mesh mesh, Vector3f position, Vector3f direction, float cutoff, float outerCutoff, Vector3f color, float intensity) {
        this.mesh = mesh;

        this.position = position;
        this.direction = direction;

        this.azimuth = (float) Math.atan2(direction.x, direction.z);
        this.elevation = (float) Math.atan2(direction.z, Math.sqrt(direction.x * direction.x + direction.y * direction.y));

        this.color = color;

        this.intensity = intensity;

        this.cutoff = cutoff;
        this.outerCutoff = outerCutoff;

        this.enabled = true;
    }

    public SpotLight(Mesh mesh, Vector3f position, Vector3f direction, float cutoff, float outerCutoff, Vector3f color) {
        this.mesh = mesh;

        this.position = position;
        this.direction = direction;

        this.azimuth = (float) Math.toDegrees(Math.atan2(direction.x, direction.z));
        this.elevation = (float) Math.toDegrees(Math.atan2(direction.z, Math.sqrt(direction.x * direction.x + direction.y * direction.y)));

        this.color = color;

        this.intensity = 1f;

        this.cutoff = cutoff;
        this.outerCutoff = outerCutoff;

        this.enabled = true;
    }

    public SpotLight(Vector3f position, Vector3f direction, float cutoff, float outerCutoff, Vector3f color, float intensity) {
        Cylinder cylinder = new Cylinder(0f, 0.5f, 1f, 32);

        this.mesh = new Mesh(
            cylinder.getPositions(),
            cylinder.getNormals(),
            cylinder.getTexCoords(),
            cylinder.getIndices()
        );

        this.position = position;
        this.direction = direction;

        this.azimuth = (float) Math.toDegrees(Math.atan2(direction.x, direction.z));
        this.elevation = (float) Math.toDegrees(Math.atan2(direction.z, Math.sqrt(direction.x * direction.x + direction.y * direction.y)));

        this.color = color;

        this.intensity = intensity;

        this.cutoff = cutoff;
        this.outerCutoff = outerCutoff;

        this.enabled = true;
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
            (float) (Math.cos(Math.toRadians(azimuth)) * Math.cos(Math.toRadians(elevation))),
            (float) Math.sin(Math.toRadians(elevation)),
            (float) (Math.sin(Math.toRadians(azimuth)) * Math.cos(Math.toRadians(elevation)))
        ).normalize();
    }

    public float getElevation() {
        return elevation;
    }

    public void setElevation(float elevation) {
        this.elevation = elevation;
        this.direction = new Vector3f(
            (float) (Math.cos(Math.toRadians(azimuth)) * Math.cos(Math.toRadians(elevation))),
            (float) Math.sin(Math.toRadians(elevation)),
            (float) (Math.sin(Math.toRadians(azimuth)) * Math.cos(Math.toRadians(elevation)))
        ).normalize();
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    public float getCutoff() {
        return cutoff;
    }

    public void setCutoff(float cutoff) {
        this.cutoff = cutoff;
    }

    public float getOuterCutoff() {
        return outerCutoff;
    }

    public void setOuterCutoff(float outerCutoff) {
        this.outerCutoff = outerCutoff;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

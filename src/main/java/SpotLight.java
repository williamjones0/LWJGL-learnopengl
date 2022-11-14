import org.joml.Vector3f;

public class SpotLight {

    private final Mesh mesh;

    private Vector3f position;
    private Vector3f direction;

    private Vector3f color;

    private float cutoff;
    private float outerCutoff;

    private boolean enabled;

    public SpotLight(Mesh mesh, Vector3f position, Vector3f direction, float cutoff, float outerCutoff, Vector3f color) {
        this.mesh = mesh;

        this.position = position;
        this.direction = direction;

        this.color = color;

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
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
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

import org.joml.Vector3f;

public class PointLight {

    private final Mesh mesh;

    private Vector3f position;

    private Vector3f color;

    public PointLight(Mesh mesh, Vector3f position, Vector3f color) {
        this.mesh = mesh;

        this.position = position;

        this.color = color;
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
}

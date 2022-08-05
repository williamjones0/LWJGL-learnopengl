import org.joml.Vector3f;

public class Entity {

    private final Mesh mesh;

    private Vector3f position;
    private Vector3f rotation;
    private float scale;

    public Entity(Mesh mesh, Vector3f position, Vector3f rotation, float scale) {
        this.mesh = mesh;
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    public Entity(Mesh mesh) {
        this.mesh = mesh;
        this.position = new Vector3f();
        this.rotation = new Vector3f();
        this.scale = 1;
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

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}

import org.joml.Vector3f;

public class Entity {

    private final MaterialMesh[] meshes;

    private Vector3f position;
    private Vector3f rotation;
    private float scale;

    private String name;

    public Entity(MaterialMesh[] meshes, Vector3f position, Vector3f rotation, float scale) {
        this.meshes = meshes;

        this.position = position;
        this.rotation = rotation;
        this.scale = scale;

        this.name = null;
    }

    public Entity(MaterialMesh mesh, Vector3f position, Vector3f rotation, float scale) {
        this(new MaterialMesh[]{mesh}, position, rotation, scale);
    }

    public Entity(MaterialMesh mesh) {
        this(mesh, new Vector3f(), new Vector3f(), 1.0f);
    }

    public Entity(MaterialMesh[] meshes) {
        this(meshes, new Vector3f(), new Vector3f(), 1.0f);
    }

    public Entity(MaterialMesh[] meshes, Vector3f position, Vector3f rotation, float scale, String name) {
        this.meshes = meshes;

        this.position = position;
        this.rotation = rotation;
        this.scale = scale;

        this.name = name;
    }

    public void render() {
        for (MaterialMesh mesh : meshes) {
            mesh.render();
        }
    }

    public MaterialMesh[] getMaterialMeshes() {
        return meshes;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

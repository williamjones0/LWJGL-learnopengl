import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Entity {

    private final MaterialMesh[] meshes;

    private Vector3f position;
    private Vector3f rotation;
    private float scale;

    private String name;

    private Entity parent;
    private List<Entity> children;

    public Entity(MaterialMesh[] meshes, Vector3f position, Vector3f rotation, float scale, String name, Entity parent) {
        this.meshes = meshes;
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        this.name = name;
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    public Entity(MaterialMesh[] meshes, Vector3f position, Vector3f rotation, float scale, Entity parent) {
        this.meshes = meshes;
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        this.name = null;
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    public Entity(MaterialMesh[] meshes, Vector3f position, Vector3f rotation, float scale) {
        this.meshes = meshes;
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        this.name = null;
        this.parent = null;
        this.children = new ArrayList<>();
    }

    public Entity(MaterialMesh mesh, Vector3f position, Vector3f rotation, float scale, String name, Entity parent) {
        this.meshes = new MaterialMesh[] { mesh };
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        this.name = name;
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    public Entity(MaterialMesh mesh, Vector3f position, Vector3f rotation, float scale, Entity parent) {
        this.meshes = new MaterialMesh[] { mesh };
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        this.name = null;
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    public Entity(MaterialMesh mesh, Vector3f position, Vector3f rotation, float scale, String name) {
        this.meshes = new MaterialMesh[] { mesh };
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        this.name = name;
        this.parent = null;
        this.children = new ArrayList<>();
    }

    public Entity(MaterialMesh mesh, Vector3f position, Vector3f rotation, float scale) {
        this.meshes = new MaterialMesh[] { mesh };
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        this.name = null;
        this.parent = null;
        this.children = new ArrayList<>();
    }

    public Entity(MaterialMesh mesh, String name) {
        this.meshes = new MaterialMesh[] { mesh };
        this.position = new Vector3f();
        this.rotation = new Vector3f();
        this.scale = 1f;
        this.name = name;
        this.parent = null;
        this.children = new ArrayList<>();
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

        this.parent = null;
        this.children = new ArrayList<>();
    }

    // Used when creating a new entity in the GUI
    public Entity(Vector3f position, Vector3f rotation, float scale, String name) {
        this.meshes = new MaterialMesh[] {};

        this.position = position;
        this.rotation = rotation;
        this.scale = scale;

        this.name = name;

        this.parent = null;
        this.children = new ArrayList<>();
    }

    // Placeholder parent entity constructor
    public Entity(Vector3f position, String name, Entity parent) {
        this.meshes = null;
        this.position = position;
        this.rotation = new Vector3f();
        this.scale = 1.0f;
        this.name = name;
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    public Entity(Vector3f position, String name) {
        this.meshes = null;
        this.position = position;
        this.rotation = new Vector3f();
        this.scale = 1.0f;
        this.name = name;
        this.parent = null;

        this.children = new ArrayList<>();
    }

    public void render() {
        for (MaterialMesh mesh : meshes) {
            mesh.render();
        }
    }

    public Vector3f getWorldPosition() {
        if (parent != null) {
            return new Vector3f(position).add(parent.getWorldPosition());
        } else {
            return position;
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

    public Entity getParent() {
        return parent;
    }

    public void setParent(Entity parent) {
        this.parent = parent;
    }

    public List<Entity> getChildren() {
        return children;
    }

    public void addChild(Entity child) {
        children.add(child);
    }

    public void removeChild(Entity child) {
        children.remove(child);
    }
}

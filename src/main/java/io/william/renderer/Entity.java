package io.william.renderer;

import io.william.game.component.MovementController;
import io.william.game.component.RotationController;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Entity {

    private int ID;

    private int modelID;

    private boolean updated;

    private Vector3f position;
    private Vector3f rotation;
    private float scale;

    private String name;

    private Entity parent;
    private final List<Entity> children;

    private MovementController movementController;
    private RotationController rotationController;

    public Entity(Vector3f position, Vector3f rotation, float scale, String name) {
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;

        this.name = name;

        this.parent = null;
        this.children = new ArrayList<>();
    }

    public Entity(Vector3f position, Vector3f rotation, float scale) {
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;

        this.name = "Entity";

        this.parent = null;
        this.children = new ArrayList<>();
    }

    // Placeholder parent entity constructor
    public Entity(Vector3f position, String name, Entity parent) {
        this.position = position;
        this.rotation = new Vector3f();
        this.scale = 1.0f;
        this.name = name;
        this.parent = parent;
        this.children = new ArrayList<>();
    }

    public Entity(Vector3f position, String name) {
        this.position = position;
        this.rotation = new Vector3f();
        this.scale = 1.0f;
        this.name = name;
        this.parent = null;

        this.children = new ArrayList<>();
    }

    public void update(float deltaTime) {
        if (movementController != null) {
            switch (movementController.getType()) {
                case ORBIT -> movementController.orbitUpdate(this, deltaTime);
                case DIRECTION -> movementController.directionUpdate(this, deltaTime);
                case POINTS -> movementController.pointUpdate(this, deltaTime);
            }
            this.updated = true;
        }

        if (rotationController != null) {
            rotationController.update(this, deltaTime);
            this.updated = true;
        }
    }

    public void render() {
        // LOL
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public Vector3f getPosition() {
        if (parent != null) {
            return new Vector3f(position).add(parent.getPosition());
        } else {
            return position;
        }
    }

    public int getModelID() {
        return modelID;
    }

    public void setModelID(int modelID) {
        this.modelID = modelID;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
        for (Entity child : children) {
            child.setUpdated(updated);
        }
    }

    public MaterialMesh[] getMaterialMeshes() {
        // LOL
        return new MaterialMesh[] {};
    }

    public Vector3f getRelativePosition() {
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

    public MovementController getMovement() {
        return movementController;
    }

    public void setMovement(MovementController movementController) {
        this.movementController = movementController;
    }

    public RotationController getRotationController() {
        return rotationController;
    }

    public void setRotationController(RotationController rotationController) {
        this.rotationController = rotationController;
    }
}

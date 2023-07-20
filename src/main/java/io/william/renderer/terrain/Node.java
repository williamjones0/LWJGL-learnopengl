package io.william.renderer.terrain;

import io.william.renderer.ShaderProgram;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Node {

    private Node parent;
    private List<Node> children;

    private Vector3f worldPosition;
    private Vector3f worldRotation;
    private Vector3f worldScaling;

    private Vector3f localPosition;
    private Vector3f localRotation;
    private Vector3f localScaling;

    public Node() {
        children = new ArrayList<>();
        worldPosition = new Vector3f();
        worldRotation = new Vector3f();
        worldScaling = new Vector3f(1, 1, 1);
        localPosition = new Vector3f();
        localRotation = new Vector3f();
        localScaling = new Vector3f(1, 1, 1);
    }

    public void addChild(Node child) {
        child.setParent(this);
        children.add(child);
    }

    public void update() {
        for (Node child : children) {
            child.update();
        }
    }

    public void input() {
        for (Node child : children) {
            child.input();
        }
    }

    public void render(ShaderProgram shader) {
        for (Node child : children) {
            child.render(shader);
        }
    }

    public void cleanup() {
        for (Node child : children) {
            child.cleanup();
        }
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public Vector3f getWorldPosition() {
        return worldPosition;
    }

    public void setWorldPosition(Vector3f worldPosition) {
        this.worldPosition = worldPosition;
    }

    public Vector3f getWorldRotation() {
        return worldRotation;
    }

    public void setWorldRotation(Vector3f worldRotation) {
        this.worldRotation = worldRotation;
    }

    public Vector3f getWorldScaling() {
        return worldScaling;
    }

    public void setWorldScaling(Vector3f worldScaling) {
        this.worldScaling = worldScaling;
    }

    public Vector3f getLocalPosition() {
        return localPosition;
    }

    public void setLocalPosition(Vector3f localPosition) {
        this.localPosition = localPosition;
    }

    public Vector3f getLocalRotation() {
        return localRotation;
    }

    public void setLocalRotation(Vector3f localRotation) {
        this.localRotation = localRotation;
    }

    public Vector3f getLocalScaling() {
        return localScaling;
    }

    public void setLocalScaling(Vector3f localScaling) {
        this.localScaling = localScaling;
    }
}

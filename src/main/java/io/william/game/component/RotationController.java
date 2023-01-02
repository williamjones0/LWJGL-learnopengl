package io.william.game.component;

import io.william.renderer.Entity;
import org.joml.Vector3f;

public class RotationController {

    public enum Mode {
        NONE,
        CONSTANT,
        ACCELERATION,
        DECELERATION
    }

    private Mode mode;

    private Vector3f speed;
    private Vector3f acceleration;
    private boolean stopAtZeroSpeed = false;

    public RotationController(Vector3f speed, Vector3f acceleration) {
        this.mode = Mode.NONE;
        this.speed = speed;
        this.acceleration = acceleration;
    }

    public RotationController(Vector3f speed) {
        this.mode = Mode.NONE;
        this.speed = speed;
        this.acceleration = new Vector3f(0, 0, 0);
    }

    public RotationController() {
        this.mode = Mode.NONE;
        this.speed = new Vector3f(0, 0, 0);
        this.acceleration = new Vector3f(0, 0, 0);
    }

    public void update(Entity entity, float deltaTime) {
        Vector3f rotation = entity.getRotation();
        switch (mode) {
            case NONE -> { return; }
            case ACCELERATION -> {
                if (stopAtZeroSpeed) {
                    if (speed.x < 0) {
                        speed.x = Math.min(speed.x - acceleration.x * deltaTime, 0);
                    } else {
                        speed.x = Math.max(speed.x + acceleration.x * deltaTime, 0);
                    }
                    if (speed.y < 0) {
                        speed.y = Math.min(speed.y - acceleration.y * deltaTime, 0);
                    } else {
                        speed.y = Math.max(speed.y + acceleration.y * deltaTime, 0);
                    }
                    if (speed.z < 0) {
                        speed.z = Math.min(speed.z - acceleration.z * deltaTime, 0);
                    } else {
                        speed.z = Math.max(speed.z + acceleration.z * deltaTime, 0);
                    }
                } else {
                    speed.x += acceleration.x * deltaTime;
                    speed.y += acceleration.y * deltaTime;
                    speed.z += acceleration.z * deltaTime;
                }
            }
            case DECELERATION -> {
                if (stopAtZeroSpeed) {
                    if (speed.x > 0) {
                        speed.x = Math.max(speed.x - acceleration.x * deltaTime, 0);
                    } else {
                        speed.x = Math.min(speed.x + acceleration.x * deltaTime, 0);
                    }
                    if (speed.y > 0) {
                        speed.y = Math.max(speed.y - acceleration.y * deltaTime, 0);
                    } else {
                        speed.y = Math.min(speed.y + acceleration.y * deltaTime, 0);
                    }
                    if (speed.z > 0) {
                        speed.z = Math.max(speed.z - acceleration.z * deltaTime, 0);
                    } else {
                        speed.z = Math.min(speed.z + acceleration.z * deltaTime, 0);
                    }
                } else {
                    speed.x -= acceleration.x * deltaTime;
                    speed.y -= acceleration.y * deltaTime;
                    speed.z -= acceleration.z * deltaTime;
                }
            }
        }
        rotation.add(speed.mul(deltaTime, new Vector3f()));
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Vector3f getSpeed() {
        return speed;
    }

    public void setSpeed(Vector3f speed) {
        this.speed = speed;
    }

    public Vector3f getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Vector3f acceleration) {
        this.acceleration = acceleration;
    }

    public void addSpeed(Vector3f speed) {
        this.speed.add(speed);
    }

    public void addAcceleration(Vector3f acceleration) {
        this.acceleration.add(acceleration);
    }

    public void addSpeed(float x, float y, float z) {
        this.speed.add(x, y, z);
    }

    public void addAcceleration(float x, float y, float z) {
        this.acceleration.add(x, y, z);
    }

    public void addSpeedX(float x) {
        this.speed.add(x, 0, 0);
    }

    public void addSpeedY(float y) {
        this.speed.add(0, y, 0);
    }

    public void addSpeedZ(float z) {
        this.speed.add(0, 0, z);
    }

    public void addAccelerationX(float x) {
        this.acceleration.add(x, 0, 0);
    }

    public void addAccelerationY(float y) {
        this.acceleration.add(0, y, 0);
    }

    public void addAccelerationZ(float z) {
        this.acceleration.add(0, 0, z);
    }

    public boolean isStopAtZeroSpeed() {
        return stopAtZeroSpeed;
    }

    public void setStopAtZeroSpeed(boolean stopAtZeroSpeed) {
        this.stopAtZeroSpeed = stopAtZeroSpeed;
    }

}

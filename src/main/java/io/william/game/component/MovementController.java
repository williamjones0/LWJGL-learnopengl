package io.william.game.component;

import io.william.renderer.*;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class MovementController {

    // Movement types: orbit, move in a direction, move between points, keyboard controls
    public enum Type {
        NONE,
        ORBIT,
        DIRECTION,
        PATH,
        KEYBOARD
    }

    public enum Mode {
        CONSTANT,
        ACCELERATION,
        DECELERATION
    }

    private Type type;
    private Mode mode;

    // Common to all movement types
    private float speed;

    // Orbit
    private Vector3f center;
    private Vector3f axis;
    private float radius;
    private float anglePerSecond;
    private boolean pointTowardsCenter;

    // Direction
    private Vector3f origin;
    private Vector3f direction;
    private float acceleration;
    private float distance;
    private boolean noMaxDistance;
    private boolean stopAtZeroSpeed;

    // Path
    private List<Vector3f> path;
    private int pathIndex = 0;

    // Keyboard
    private float deceleration;

//    // Rendering
//    private final List<Entity> pointEntities;
//    private final Model pointModel;

    private MovementController(Scene scene, Type type, Mode mode, float speed, float deceleration, Vector3f center, Vector3f axis, float radius, float anglePerSecond, Vector3f origin, Vector3f direction, float acceleration, float distance, List<Vector3f> path) {
        this.type = type;
        this.mode = mode;
        this.speed = speed;
        this.deceleration = deceleration;
        this.center = center;
        this.axis = axis;
        this.radius = radius;
        this.anglePerSecond = anglePerSecond;
        this.origin = origin;
        this.direction = direction;
        this.acceleration = acceleration;
        this.distance = distance;
        this.path = path;

//        this.pointEntities = new ArrayList<>();
//
//        UVSphere sphere = new UVSphere(0.1f, 10, 10);
//        Map<Integer, Integer> meshDataMaterialIDs = new HashMap<>();
//        meshDataMaterialIDs.put(0, 0);
//
//        Model sphereModel = new Model(
//            new MeshData(
//                sphere.getPositions(),
//                sphere.getNormals(),
//                new float[] {},
//                new float[] {},
//                sphere.getTexCoords(),
//                sphere.getIndices()
//            ),
//            new ModelMetadata(sphere, meshDataMaterialIDs),
//            "Point"
//        );
//        scene.addModel(sphereModel);
//        this.pointModel = sphereModel;
    }

    // Orbits are initialised with a center point, a rotation axis, a radius, and a speed or rotation angle per second
    // Directional movement is initialised with an origin, a direction vector, a maximum distance, a speed and an acceleration
    // Point movement is initialised with a list of points and a speed
    // Keyboard movement is initialised with a speed and a deceleration

    // Orbits are updated by rotating the entity around the center point about the rotation axis at the radius by the rotation angle per second, or at the speed
    // Directional movement is updated by moving the entity in the direction vector by the speed
    // Point movement is updated by moving the entity to the next point in the list by the speed
    // Keyboard movement is updated by moving the entity in the direction of the velocity vector by the speed

    public static MovementController none(Scene scene) {
        return new MovementController(scene, Type.NONE, Mode.CONSTANT, 0, 0, null, null, 0, 0, null, null, 0, 0, null);
    }

    public static MovementController orbit(Scene scene, Mode mode, float speed, Vector3f center, Vector3f axis, float radius) {
        return new MovementController(scene, Type.ORBIT, mode, speed, 0, center, axis, radius, 0, null, null, 0, 0, null);
    }

    public static MovementController orbit(Scene scene, Mode mode, Vector3f center, Vector3f axis, float radius, float anglePerSecond) {
        return new MovementController(scene, Type.ORBIT, mode, radius * anglePerSecond, 0, center, axis, radius, anglePerSecond, null, null, 0, 0, null);
    }

    public static MovementController direction(Scene scene, Mode mode, float speed, Vector3f origin, Vector3f direction, float acceleration, float distance) {
        return new MovementController(scene, Type.DIRECTION, mode, speed, 0, null, null, 0, 0, origin, direction, distance, acceleration, null);
    }

    public static MovementController path(Scene scene, Mode mode, float speed, List<Vector3f> path) {
        return new MovementController(scene, Type.PATH, mode, speed, 0, null, null, 0, 0, null, null, 0, 0, path);
    }

    public void orbitUpdate(Entity entity, float deltaTime) {
        Vector3f position = entity.getPosition();
        float threshold = 0.01f;

        // Avoid errors when the axis is the zero vector
        boolean reset = false;
        if (axis.x == 0 && axis.y == 0 && axis.z == 0) {
            axis = new Vector3f(0, 1, 0);
            reset = true;
        }

        Vector3f v;
        if (position.equals(center)) {
            v = new Vector3f(1, 1, 1);
        } else {
            v = new Vector3f(position).sub(center);
        }

        Vector3f k = new Vector3f(axis).normalize();
        float angle = anglePerSecond * deltaTime;

        // If entity is not in the orbit plane, move it to the orbit plane
        if (Math.abs(v.dot(k)) > threshold) {
            System.out.println("Entity is not in orbit plane");
            v.cross(k);
            v.normalize().mul(radius);
        }

        // If entity is not at the correct radius, move it to the correct radius
        if (Math.abs(v.length() - radius) > threshold) {
            System.out.println("Entity is not at correct radius");
            v.normalize().mul(radius);
        }

        // Use Rodrigues' rotation formula to rotate the vector
        // https://en.wikipedia.org/wiki/Rodrigues%27_rotation_formula
//        Vector3f t1 = v.mul((float) Math.cos(angle), new Vector3f());
//        Vector3f t2 = k.cross(v, new Vector3f()).mul((float) Math.sin(angle), new Vector3f());
//        Vector3f t3 = k.mul(k.dot(v) * (1 - (float) Math.cos(angle)), new Vector3f());
//        Vector3f rotated = t1.add(t2).add(t3);
        v.rotateAxis(angle, k.x, k.y, k.z);
//        v = new Vector3f(rotated);
        entity.setPosition(new Vector3f(center).add(v));

        // Update rotation (z doesn't work at the moment)
        if (pointTowardsCenter) {
            Vector3f direction = new Vector3f(v).negate();
            // (0, 0, 1) is the default direction of the entity and is equivalent to (0, 0, 0) rotation
            // A direction of (1, 0, 0) is equivalent to (0, 90, 0) rotation
            // A direction of (0, 0, -1) is equivalent to (0, 180, 0) rotation
            // A direction of (-1, 0, 0) is equivalent to (0, 270, 0) rotation
            // A direction of (0, -1, 0) is equivalent to (90, 0, 0) rotation
            // A direction of (0, 1, 0) is equivalent to (270, 0, 0) rotation
            entity.setRotation(new Vector3f(
                (float) Math.toDegrees(Math.asin(direction.y)) != entity.getRotation().x ? (float) Math.toDegrees(Math.asin(direction.y)) : entity.getRotation().x,
                (float) Math.toDegrees(Math.atan2(direction.x, direction.z)),
                entity.getRotation().z
            ));
        }

        if (reset) axis = new Vector3f(0, 0, 0);
    }

    public void orbitUpdate(PointLight pointLight, float deltaTime) {
        Vector3f position = pointLight.getPosition();
        float threshold = 0.01f;

        // Avoid errors when the axis is the zero vector
        boolean reset = false;
        if (axis.x == 0 && axis.y == 0 && axis.z == 0) {
            axis = new Vector3f(0, 1, 0);
            reset = true;
        }

        Vector3f v;
        if (position.equals(center)) {
            v = new Vector3f(1, 1, 1);
        } else {
            v = new Vector3f(position).sub(center);
        }

        Vector3f k = new Vector3f(axis).normalize();
        float angle = anglePerSecond * deltaTime;

        // If light is not in the orbit plane, move it to the orbit plane
        if (Math.abs(v.dot(k)) > threshold) {
            System.out.println("Entity is not in orbit plane");
            v.cross(k);
            v.normalize().mul(radius);
        }

        // If light is not at the correct radius, move it to the correct radius
        if (Math.abs(v.length() - radius) > threshold) {
            System.out.println("Entity is not at correct radius");
            v.normalize().mul(radius);
        }

        // Use Rodrigues' rotation formula to rotate the vector
        // https://en.wikipedia.org/wiki/Rodrigues%27_rotation_formula
//        Vector3f t1 = v.mul((float) Math.cos(angle), new Vector3f());
//        Vector3f t2 = k.cross(v, new Vector3f()).mul((float) Math.sin(angle), new Vector3f());
//        Vector3f t3 = k.mul(k.dot(v) * (1 - (float) Math.cos(angle)), new Vector3f());
//        Vector3f rotated = t1.add(t2).add(t3);
        v.rotateAxis(angle, k.x, k.y, k.z);
//        v = new Vector3f(rotated);
        pointLight.setPosition(new Vector3f(center).add(v));

        if (reset) axis = new Vector3f(0, 0, 0);
    }

    public void directionUpdate(Entity entity, float deltaTime) {
        Vector3f position = entity.getPosition();
        if (noMaxDistance || position.distance(origin) < distance) {
            switch (mode) {
                case ACCELERATION -> {
                    if (stopAtZeroSpeed && speed + acceleration * deltaTime <= 0) {
                        speed = 0;
                    } else {
                        speed += acceleration * deltaTime;
                    }
                }
                case DECELERATION -> {
                    if (stopAtZeroSpeed && speed - acceleration * deltaTime <= 0) {
                        speed = 0;
                    } else {
                        speed -= acceleration * deltaTime;
                    }
                }
            }
            position.add(new Vector3f(direction).normalize().mul(speed * deltaTime));
        }
    }

    public void directionUpdate(PointLight pointLight, float deltaTime) {
        Vector3f position = pointLight.getPosition();
        if (noMaxDistance || position.distance(origin) < distance) {
            switch (mode) {
                case ACCELERATION -> {
                    if (stopAtZeroSpeed && speed + acceleration * deltaTime <= 0) {
                        speed = 0;
                    } else {
                        speed += acceleration * deltaTime;
                    }
                }
                case DECELERATION -> {
                    if (stopAtZeroSpeed && speed - acceleration * deltaTime <= 0) {
                        speed = 0;
                    } else {
                        speed -= acceleration * deltaTime;
                    }
                }
            }
            position.add(new Vector3f(direction).normalize().mul(speed * deltaTime));
        }
    }

    public void pathUpdate(Entity entity, float deltaTime) {
        if (path == null || path.size() == 0) {
            return;
        }

        if (pathIndex >= path.size()) {
            pathIndex = 0;
        }

        Vector3f position = entity.getPosition();

        Vector3f target = path.get(pathIndex);

        Vector3f direction = new Vector3f(target).sub(position);

        if (direction.length() < 0.01f * speed) {
            pathIndex++;
            return;
        }

        position.add(direction.normalize().mul(speed * deltaTime));

        entity.setPosition(position);

        if (position.sub(target, new Vector3f()).length() < 0.1f) {
            pathIndex++;
        }
    }

    public void pathUpdate(PointLight pointLight, float deltaTime) {
        if (path == null || path.size() == 0) {
            return;
        }

        if (pathIndex >= path.size()) {
            pathIndex = 0;
        }

        Vector3f position = pointLight.getPosition();

        Vector3f target = path.get(pathIndex);

        Vector3f direction = new Vector3f(target).sub(position);

        if (direction.length() < 0.01f * speed) {
            pathIndex++;
            return;
        }

        position.add(direction.normalize().mul(speed * deltaTime));

        pointLight.setPosition(position);

        if (position.sub(target, new Vector3f()).length() < 0.1f) {
            pathIndex++;
        }
    }

    public void render() {
//        for (Entity entity : pointEntities) {
//            entity.render();
//        }
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
        this.anglePerSecond = speed / radius;
    }

    public float getDeceleration() {
        return deceleration;
    }

    public void setDeceleration(float deceleration) {
        this.deceleration = deceleration;
    }

    public Vector3f getCenter() {
        return center;
    }

    public void setCenter(Vector3f center) {
        this.center = center;
    }

    public Vector3f getAxis() {
        return axis;
    }

    public void setAxis(Vector3f axis) {
        this.axis = axis;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        this.speed = radius * anglePerSecond;
    }

    public float getAnglePerSecond() {
        return anglePerSecond;
    }

    public void setAnglePerSecond(float anglePerSecond) {
        this.anglePerSecond = anglePerSecond;
        this.speed = radius * anglePerSecond;
    }

    public boolean isPointTowardsCenter() {
        return pointTowardsCenter;
    }

    public void setPointTowardsCenter(boolean pointTowardsCenter) {
        this.pointTowardsCenter = pointTowardsCenter;
    }

    public Vector3f getOrigin() {
        return origin;
    }

    public void setOrigin(Vector3f origin) {
        this.origin = origin;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }

    public float getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public boolean isNoMaxDistance() {
        return noMaxDistance;
    }

    public void setNoMaxDistance(boolean noMaxDistance) {
        this.noMaxDistance = noMaxDistance;
    }

    public boolean isStopAtZeroSpeed() {
        return stopAtZeroSpeed;
    }

    public void setStopAtZeroSpeed(boolean stopAtZeroSpeed) {
        this.stopAtZeroSpeed = stopAtZeroSpeed;
    }

    public List<Vector3f> getPath() {
        return path;
    }

    public void setPath(List<Vector3f> path) {
        this.path = path;
    }

    public void addPoint(Vector3f point, Scene scene) {
        if (path == null) {
            path = new ArrayList<>();
        }
        path.add(point);

//        Entity pointEntity = new Entity(point, new Vector3f(), 1);
//        pointModel.addEntity(pointEntity);
//        scene.addEntity(pointEntity);
//        pointEntities.add(pointEntity);
    }

    public void setPoint(int index, Vector3f point) {
        if (path == null) {
            path = new ArrayList<>();
        }
        path.set(index, point);

//        Entity pointEntity = pointEntities.get(index);
//        pointEntity.setPosition(point);
    }

    public void removePoint(int index) {
        if (path == null) {
            path = new ArrayList<>();
        }
        path.remove(index);
//        pointEntities.remove(index);
    }

//    public List<Entity> getPointEntities() {
//        return pointEntities;
//    }
}

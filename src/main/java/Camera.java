import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    private Vector3f position;
    private Vector3f dVelocity;
    private Vector3f front;
    private Vector3f up;
    private Vector3f right;
    private final Vector3f worldUp;

    private float yaw;
    private float pitch;

    private float movementSpeed = 2f;
    private float mouseSensitivity = 0.02f;
    private float deceleration = 0.95f;

    private MovementMode movementMode = MovementMode.SMOOTH;

    private Entity focus;

    public enum Movement {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    public enum MovementMode {
        CONSTANT,
        SMOOTH
    }

    public Camera(Vector3f position, float yaw, float pitch) {
        this.position = position;
        this.dVelocity = new Vector3f();
        up = new Vector3f();
        right = new Vector3f();

        worldUp = new Vector3f(0, 1, 0);
        this.yaw = yaw;
        this.pitch = pitch;
        updateCameraVectors();

        focus = null;
    }

    public void update() {
        position.add(dVelocity);
        dVelocity.mul(deceleration);
        updateCameraVectors();
    }

    public void processKeyboard(Movement direction, float deltaTime) {
        float velocity = movementSpeed * deltaTime;

        if (movementMode == MovementMode.CONSTANT) {
            if (direction == Movement.FORWARD)
                position.add(front.mul(velocity, new Vector3f()));
            if (direction == Movement.BACKWARD)
                position.sub(front.mul(velocity, new Vector3f()));
            if (direction == Movement.LEFT)
                position.sub(right.mul(velocity, new Vector3f()));
            if (direction == Movement.RIGHT)
                position.add(right.mul(velocity, new Vector3f()));
            if (direction == Movement.UP)
                position.add(worldUp.mul(velocity, new Vector3f()));
            if (direction == Movement.DOWN)
                position.sub(worldUp.mul(velocity, new Vector3f()));
        }

        if (movementMode == MovementMode.SMOOTH) {
            if (direction == Movement.FORWARD)
                dVelocity.add(front.mul(velocity, new Vector3f()));
            if (direction == Movement.BACKWARD)
                dVelocity.sub(front.mul(velocity, new Vector3f()));
            if (direction == Movement.LEFT)
                dVelocity.sub(right.mul(velocity, new Vector3f()));
            if (direction == Movement.RIGHT)
                dVelocity.add(right.mul(velocity, new Vector3f()));
            if (direction == Movement.UP)
                dVelocity.add(worldUp.mul(velocity, new Vector3f()));
            if (direction == Movement.DOWN)
                dVelocity.sub(worldUp.mul(velocity, new Vector3f()));
        }
    }

    public void processMouse(float xoffset, float yoffset) {
        xoffset *= mouseSensitivity;
        yoffset *= mouseSensitivity;

        yaw += xoffset;
        pitch += yoffset;

        if (pitch > 90.0f)
            pitch = 90.0f;
        if (pitch < -90.0f)
            pitch = -90.0f;

        updateCameraVectors();
    }

    public Matrix4f calculateViewMatrix() {
        if (focus == null) {
            return new Matrix4f().lookAt(position, new Vector3f(position).add(front), up);
        } else {
            return new Matrix4f().lookAt(position, focus.getPosition(), up);
        }
    }

    private void updateCameraVectors() {
        // Calculate the new front vector
        front = new Vector3f(
            (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch))),
            (float) Math.sin(Math.toRadians(pitch)),
            (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)))
        );
        // Calculate the right and up vectors
        front.cross(worldUp, right);
        right.normalize();
        right.cross(front, up);
        up.normalize();
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getFront() {
        return front;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getMovementSpeed() {
        return movementSpeed;
    }

    public void setMovementSpeed(float movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

    public float getMouseSensitivity() {
        return mouseSensitivity;
    }

    public void setMouseSensitivity(float mouseSensitivity) {
        this.mouseSensitivity = mouseSensitivity;
    }

    public float getDeceleration() {
        return deceleration;
    }

    public void setDeceleration(float deceleration) {
        this.deceleration = deceleration;
    }

    public MovementMode getMovementMode() {
        return movementMode;
    }

    public void setMovementMode(MovementMode movementMode) {
        this.movementMode = movementMode;
    }

    public Entity getFocus() {
        return focus;
    }

    public void setFocus(Entity focus) {
        this.focus = focus;
    }
}

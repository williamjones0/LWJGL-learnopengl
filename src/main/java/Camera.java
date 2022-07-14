import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    private Vector3f position;
    private Vector3f front;
    private Vector3f up;
    private Vector3f right;
    private final Vector3f worldUp;

    private float yaw;
    private float pitch;

    private final float movementSpeed = 10f;
    private final float mouseSensitivity = 0.02f;

    public enum Movement {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    public Camera(Vector3f position, float yaw, float pitch) {
        this.position = position;
        up = new Vector3f();
        right = new Vector3f();

        worldUp = new Vector3f(0, 1, 0);
        this.yaw = yaw;
        this.pitch = pitch;
        updateCameraVectors();
    }

    public void processKeyboard(Movement direction,  float deltaTime) {
        float velocity = movementSpeed * deltaTime;

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
        Vector3f temp = new Vector3f();
        position.add(front, temp);
        return new Matrix4f().setLookAt(position, temp, up);
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

    public Vector3f getFront() {
        return front;
    }
}

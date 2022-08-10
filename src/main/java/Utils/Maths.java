package Utils;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Maths {

    public static Matrix4f calculateModelMatrix(Vector3f position, Vector3f rotation, float scale) {
        return new Matrix4f().translation(position).
            rotateX((float) Math.toRadians(rotation.x)).
            rotateX((float) Math.toRadians(rotation.y)).
            rotateX((float) Math.toRadians(rotation.z)).
            scale(scale);
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

}

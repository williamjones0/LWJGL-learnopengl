package Utils;

import org.joml.Vector3f;

import java.util.List;

public class Utils {

    public static int[] intListToArray(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static float[] floatListToArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static float[] vector3fToArray(Vector3f vector) {
        return new float[] {
            vector.x,
            vector.y,
            vector.z
        };
    }

}

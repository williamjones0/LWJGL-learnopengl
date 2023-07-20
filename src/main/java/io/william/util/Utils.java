package io.william.util;

import org.joml.Vector3f;

import java.util.ArrayList;
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

    public static Vector3f arrayToVector3f(float[] array) {
        return new Vector3f(array[0], array[1], array[2]);
    }

    public static String[] removeEmptyStrings(String[] data) {
        ArrayList<String> result = new ArrayList<>();

        for (String datum : data)
            if (!datum.equals(""))
                result.add(datum);

        String[] res = new String[result.size()];
        result.toArray(res);

        return res;
    }

}

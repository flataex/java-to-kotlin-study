package chapter1.java.sum;

import java.util.Vector;

public class Vectors {
    private Vectors() {}
    public static Object fold(Vector l, Object initial, BiFunction f) {
        Object result = initial;
        for (int i = 0; i < l.size(); i++) {
            result = f.apply(result, l.get(i));
        }
        return result;
    }
}

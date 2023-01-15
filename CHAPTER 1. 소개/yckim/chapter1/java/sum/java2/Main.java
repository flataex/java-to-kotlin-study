package chapter1.java.sum.java2;

import chapter1.java.sum.BiFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Main {

    public static void main(String[] args) {
        List<Integer> counts = new ArrayList<>();
        counts.add(1);
        counts.add(2);
        counts.add(3);

        int sum = ((Integer) Lists.fold(counts, new Integer(0), new BiFunction() {
            @Override
            public Object apply(Object arg1, Object arg2) {
                int i1 = ((Integer) arg1).intValue();
                int i2 = ((Integer) arg2).intValue();
                return new Integer(i1 + i2);
            }
        }));
        System.out.println(sum);
    }

}
class Lists {
    private Lists() {}
    public static Object fold(List<?> l, Object initial, BiFunction f) {
        Object result = initial;
        for (int i = 0; i < l.size(); i++) {
            result = f.apply(result, l.get(i));
        }
        return result;
    }
}

package chapter1.java.sum.java5;

import java.util.ArrayList;
import java.util.List;

interface BiFunction<A, B, R> {
    R apply(A arg1, B arg2);
}
public class Main {

    public static void main(String[] args) {
        List<Integer> counts = new ArrayList<>();
        counts.add(1);
        counts.add(2);
        counts.add(3);

        int sum = Lists.fold(counts, 0, new BiFunction<Integer, Integer, Integer>() {
            @Override
            public Integer apply(Integer arg1, Integer arg2) {
                return arg1 + arg2;
            }
        });

        System.out.println(sum);
    }
}
class Lists {
    private Lists() {}
    public static <N> N fold(List<N> l, N initial, BiFunction<N, N, N> f) {
        N result = initial;
        for (int i = 0; i < l.size(); i++) {
            result = f.apply(result, l.get(i));
        }
        return result;
    }
}
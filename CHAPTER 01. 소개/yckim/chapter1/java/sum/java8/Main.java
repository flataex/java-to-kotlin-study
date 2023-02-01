package chapter1.java.sum.java8;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<Integer> counts = new ArrayList<>();
        counts.add(1);
        counts.add(2);
        counts.add(3);

        int sum = counts.stream().reduce(0, Integer::sum);
        System.out.println(sum);
    }
}

package chapter1.java.sum;

import java.util.Vector;

public class Main {

    public static void main(String[] args) {
        Vector counts = new Vector();
        counts.add(1);
        counts.add(2);
        counts.add(3);

        int sum = ((Integer) Vectors.fold(counts, new Integer(0), new AddIntegers())).intValue();

        int sum2 = 0;
        for (int i = 0; i < counts.size(); i++) {
            sum2 += ((Integer) counts.get(i)).intValue();
        }

        System.out.println(sum);
        System.out.println(sum2);
    }
}

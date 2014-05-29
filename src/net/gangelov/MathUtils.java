package net.gangelov;

import org.apfloat.Apfloat;

public class MathUtils {
    public static Apfloat factorial(int num) {
        if (num == 0) {
            return Apfloat.ONE;
        }

        return factorial(1, num);
    }

    public static Apfloat factorial(int from, int to) {
        if (from == to) {
            return new Apfloat(from);
        }

        int middle = (from + to) / 2;
        Apfloat a = factorial(from, middle);
        Apfloat b = factorial(middle + 1, to);

        return a.multiply(b);
    }
}

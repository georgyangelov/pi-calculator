package net.gangelov;

import java.math.BigInteger;

public class MathUtils {
    public static BigInteger factorial(int num) {
        if (num == 0) {
            return BigInteger.ONE;
        }

        return factorial(1, num);
    }

    public static BigInteger factorial(int from, int to) {
        if (from == to) {
            return BigInteger.valueOf(from);
        }

        int middle = (from + to) / 2;
        BigInteger a = factorial(from, middle);
        BigInteger b = factorial(middle + 1, to);

        return a.multiply(b);
    }
}

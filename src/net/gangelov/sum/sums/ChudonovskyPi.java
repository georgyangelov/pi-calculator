package net.gangelov.sum.sums;

import net.gangelov.MathUtils;
import net.gangelov.sum.CalculatorResult;
import net.gangelov.sum.InfiniteSum;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

import java.math.RoundingMode;

public class ChudonovskyPi implements InfiniteSum {
    private static final int minPrecisionPerTerm = 15;

    private Apfloat A,
            B,
            C,
            C3,
            minusOne;

    public ChudonovskyPi(int numTerms) {
        int precision = preferredPrecision(numTerms);

        A        = new Apfloat(13591409, precision);
        B        = new Apfloat(545140134, precision);
        C        = new Apfloat(640320, precision);
        C3       = ApfloatMath.pow(C, 3);
        minusOne = new Apfloat(-1, precision);
    }

    @Override
    public Apfloat nextPartialTerm(Apfloat lastPartialTerm, int termIndex) {
        return lastPartialTerm.multiply(
                minusOne
        ).multiply(
                MathUtils.factorial(6 * (termIndex - 1) + 1, 6 * termIndex)
        ).divide(
                ApfloatMath.pow(new Apfloat(termIndex), 3).multiply(
                        MathUtils.factorial(3 * (termIndex - 1) + 1, 3 * termIndex)
                ).multiply(C3)
        );
    }

    @Override
    public Apfloat calculateTerm(Apfloat partialTerm, int index) {
        return partialTerm.multiply(
                A.add(
                        B.multiply(
                                new Apfloat(index)
                        )
                )
        );
    }

    @Override
    public Apfloat finalizeSum(CalculatorResult result) {
        Apfloat pi1 = finalizeSum(result.getSum());
        Apfloat pi2 = finalizeSum(result.getSum().add(calculateTerm(result.getLastPartialTerm(), result.getNumTerms())));

        long numDigits = pi1.equalDigits(pi2);

        return ApfloatMath.round(pi1, numDigits - 2, RoundingMode.FLOOR);
    }

    private Apfloat finalizeSum(Apfloat sum) {
        // TODO: Will this be more precise by multiplying by C3 after the inversion?
        return Apfloat.ONE.divide(
                sum.multiply(new Apfloat(12)).divide(
                        ApfloatMath.sqrt(C3)
                )
        );
    }

    @Override
    public int preferredPrecision(int numTerms) {
        return numTerms * minPrecisionPerTerm;
    }
}

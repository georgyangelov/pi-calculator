package net.gangelov.sum.sums;

import net.gangelov.MathUtils;
import net.gangelov.sum.CalculatorResult;
import net.gangelov.sum.InfiniteSum;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class RamanujanPi implements InfiniteSum, Serializable {
    private static final int minPrecisionPerTerm = 6;

    private Apfloat A,
                    B,
                    C,
                    C2,
                    four,
                    minusOne;

    public RamanujanPi(int numTerms) {
        int precision = preferredPrecision(numTerms);

        A        = new Apfloat(1123, precision);
        B        = new Apfloat(21460, precision);
        C        = new Apfloat(882, precision);
        C2       = ApfloatMath.pow(C, 2);
        four     = new Apfloat(4, precision);
        minusOne = new Apfloat(-1, precision);
    }

    @Override
    public Apfloat nextPartialTerm(Apfloat lastPartialTerm, int termIndex) {
        return lastPartialTerm.multiply(
                minusOne
        ).multiply(
                MathUtils.factorial(4 * (termIndex - 1) + 1, 4 * termIndex)
        ).divide(
                ApfloatMath.pow(four.multiply(
                        new Apfloat(termIndex)
                ), 4).multiply(C2)
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
        // TODO: Will this be more precise by multiplying by 4C after the inversion?
        return Apfloat.ONE.divide(
                sum.divide(
                        four.multiply(C)
                )
        );
    }

    @Override
    public int preferredPrecision(int numTerms) {
        return numTerms * minPrecisionPerTerm;
    }
}

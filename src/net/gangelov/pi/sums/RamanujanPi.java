package net.gangelov.pi.sums;

import net.gangelov.MathUtils;
import net.gangelov.pi.CalculatorResult;
import net.gangelov.pi.InfiniteSum;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public class RamanujanPi implements InfiniteSum {
    private static final int minPrecisionPerTerm = 6,
                             minDigitsPerTerm    = 4;

    private static final BigDecimal A        = BigDecimal.valueOf(1123),
                                    B        = BigDecimal.valueOf(21460),
                                    C        = BigDecimal.valueOf(882),
                                    C2       = C.pow(2),
                                    four     = BigDecimal.valueOf(4),
                                    minusOne = BigDecimal.valueOf(-1);

    private MathContext mathContext;

    public RamanujanPi(int numTerms) {
        mathContext = new MathContext(preferredPrecision(numTerms), RoundingMode.FLOOR);
    }

    @Override
    public BigDecimal nextPartialTerm(BigDecimal lastPartialTerm, int termIndex) {
        return lastPartialTerm.multiply(
                minusOne
        ).multiply(
                new BigDecimal(
                        MathUtils.factorial(4 * (termIndex - 1) + 1, 4 * termIndex)
                ),
                mathContext
        ).divide(
                four.multiply(
                        BigDecimal.valueOf(termIndex)
                ).pow(4).multiply(C2),
                mathContext
        );
    }

    @Override
    public BigDecimal calculateTerm(BigDecimal partialTerm, int index) {
        return partialTerm.multiply(
                A.add(
                        B.multiply(
                                BigDecimal.valueOf(index)
                        )
                ),
                mathContext // TODO: Verify this doesn't actually slow down the calculation
        );
    }

    @Override
    public BigDecimal finalizeSum(CalculatorResult result) {
        BigDecimal pi1 = finalizeSum(result.getSum());
        BigDecimal pi2 = finalizeSum(result.getSum().add(calculateTerm(result.getLastPartialTerm(), result.getNumTerms())));
        BigDecimal difference = pi1.subtract(pi2).abs();
        BigDecimal approximatedDifference;

        int numDigits = 0;

        if (difference.compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("Not enough precision! Increase the minPrecisionPerTerm constant!");
        }

        numDigits = result.getNumTerms() * minDigitsPerTerm;
        approximatedDifference = difference.movePointRight(numDigits);

        if (approximatedDifference.compareTo(BigDecimal.ZERO) == 0) {
            numDigits = 0;
        } else {
            difference = approximatedDifference;
        }

        while (difference.compareTo(BigDecimal.ONE) < 0) {
            numDigits++;
            difference = difference.movePointRight(1);
        }

        return pi1.round(new MathContext(numDigits - 2, RoundingMode.FLOOR));
    }

    private BigDecimal finalizeSum(BigDecimal sum) {
        // TODO: Will this be more precise by multiplying by 4C after the inversion?
        return BigDecimal.ONE.divide(
                sum.divide(
                        four.multiply(C),
                        mathContext
                ),
                mathContext
        );
    }

    @Override
    public int preferredPrecision(int numTerms) {
        return numTerms * minPrecisionPerTerm;
    }
}

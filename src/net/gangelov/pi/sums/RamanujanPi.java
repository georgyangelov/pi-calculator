package net.gangelov.pi.sums;

import net.gangelov.MathUtils;
import net.gangelov.pi.InfiniteSum;

import java.math.BigDecimal;

public class RamanujanPi implements InfiniteSum {
    private static final int minPrecisionPerTerm = 7;

    private static final BigDecimal A        = BigDecimal.valueOf(1123),
                                    B        = BigDecimal.valueOf(21460),
                                    C        = BigDecimal.valueOf(882),
                                    C2       = C.pow(2),
                                    four     = BigDecimal.valueOf(4),
                                    minusOne = BigDecimal.valueOf(-1);

    @Override
    public BigDecimal nextPartialTerm(BigDecimal lastPartialTerm, int termIndex) {
        return lastPartialTerm.multiply(
                minusOne
        ).multiply(
                new BigDecimal(
                        MathUtils.factorial(4 * (termIndex - 1) + 1, 4 * termIndex)
                )
        ).divide(
                four.multiply(
                        BigDecimal.valueOf(termIndex)
                ).pow(4).multiply(C2)
        );
    }

    @Override
    public BigDecimal calculateTerm(BigDecimal partialTerm, int index) {
        return partialTerm.multiply(
                A.add(
                        B.multiply(
                                BigDecimal.valueOf(index)
                        )
                )
        );
    }

    @Override
    public int preferredPrecision(int numTerms) {
        return numTerms * minPrecisionPerTerm;
    }
}

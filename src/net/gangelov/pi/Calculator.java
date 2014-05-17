package net.gangelov.pi;

import net.gangelov.MathUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public class Calculator {
    private int numTerms;

    static final BigDecimal A = BigDecimal.valueOf(1123),
                            B = BigDecimal.valueOf(21460),
                            C = BigDecimal.valueOf(882);
    static final BigDecimal C2 = BigDecimal.valueOf(882).pow(2);
    private MathContext mc;

    public long subterm1Time = 0,
                subterm2Time = 0,
                subtermCTime = 0,
                multiplication1Time = 0,
                multiplication2Time = 0,
                multiplication3Time = 0,
                divisionTime = 0,
                summationTime = 0,
                finalDivisionTime = 0,
                inverseTime = 0;

    public Calculator(int numTerms) {
        this.numTerms = numTerms;
        this.mc = new MathContext(numTerms * 10, RoundingMode.FLOOR);
    }

    public BigDecimal calculate(CalculationProgress progress) {
        long startTime;
        BigDecimal sum = calculateSum(progress),
                   result;

//        startTime = System.currentTimeMillis();
        sum = sum.divide(BigDecimal.valueOf(4).multiply(C), mc);
//        finalDivisionTime = System.currentTimeMillis() - startTime;

//        startTime = System.currentTimeMillis();
        result = BigDecimal.ONE.divide(sum, mc);
//        inverseTime = System.currentTimeMillis() - startTime;

        return result.round(new MathContext(numTerms * 10 + 1, RoundingMode.FLOOR));
    }

    private BigDecimal calculateSum(CalculationProgress progress) {
        long startTime;

        BigDecimal sum = BigDecimal.ZERO,
                   tmp,
                   term;
        BigDecimal subterm1 = BigDecimal.ONE,
                   subterm2 = BigDecimal.ONE,
                   subtermC = BigDecimal.ONE;

        startTime = System.currentTimeMillis();
        sum = sum.add(A, mc);
        summationTime += System.currentTimeMillis() - startTime;

        progress.update(1);

        for (int n = 1; n < numTerms; n++) {
            startTime = System.currentTimeMillis();
            subterm1 = subterm1.multiply(
                    new BigDecimal(MathUtils.factorial(4 * (n - 1) + 1, 4 * n))
            );
            subterm1Time += System.currentTimeMillis() - startTime;

            startTime = System.currentTimeMillis();
            subterm2 = subterm2.multiply(
                    BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(n)).pow(4)
            ).multiply(C2);
            subterm2Time += System.currentTimeMillis() - startTime;

            startTime = System.currentTimeMillis();
            term = subterm1.multiply(A.add(B.multiply(BigDecimal.valueOf(n))));
            multiplication2Time += System.currentTimeMillis() - startTime;

            startTime = System.currentTimeMillis();
            tmp = subterm2.multiply(subtermC);
            multiplication3Time += System.currentTimeMillis() - startTime;

            startTime = System.currentTimeMillis();
            term = term.divide(tmp, mc);
            divisionTime += System.currentTimeMillis() - startTime;

            startTime = System.currentTimeMillis();
            if (n % 2 == 0) {
                sum = sum.add(term);
            } else {
                sum = sum.subtract(term);
            }
            summationTime += System.currentTimeMillis() - startTime;

            progress.update(n + 1);
        }

        return sum;
    }
}

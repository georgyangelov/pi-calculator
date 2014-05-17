package net.gangelov.pi;

import net.gangelov.MathUtils;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;

public class TermProducer implements Runnable {
    public static final BigDecimal A  = BigDecimal.valueOf(1123),
                                   B  = BigDecimal.valueOf(21460),
                                   C  = BigDecimal.valueOf(882),
                                   C2 = C.pow(2);

    private int numTerms;
    private Term[] terms;

    private CalculationProgress progress;

    public TermProducer(int numTerms, Term[] terms, CalculationProgress progress) {
        this.numTerms = numTerms;
        this.terms    = terms;
        this.progress = progress;
    }

    @Override
    public void run() {
        BigDecimal subterm1 = BigDecimal.ONE,
                   subterm2 = BigDecimal.ONE;

        // The first term (n = 0) is A/1
        terms[0] = new Term(0, A, BigDecimal.ONE);

        for (int n = 1; n < numTerms; n++) {
            BigDecimal numerator = BigDecimal.ONE;

            subterm1 = subterm1.multiply(
                    new BigDecimal(MathUtils.factorial(4 * (n - 1) + 1, 4 * n))
            );
            subterm2 = subterm2.multiply(
                    BigDecimal.valueOf(4).multiply(
                            BigDecimal.valueOf(n)
                    ).pow(4)
            );
            subterm2 = subterm2.multiply(C2);

            numerator = numerator
                    .multiply(subterm1)
                    .multiply(
                            A.add(
                                    B.multiply(
                                            BigDecimal.valueOf(n)
                                    )
                            )
                    );

            if (n % 2 != 0) {
                numerator = numerator.negate();
            }

            terms[n] = new Term(n, numerator, subterm2);
        }
    }
}

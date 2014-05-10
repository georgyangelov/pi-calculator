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
    private BlockingQueue<Term> terms;

    private CalculationProgress progress;

    public TermProducer(int numTerms, BlockingQueue<Term> terms, CalculationProgress progress) {
        this.numTerms = numTerms;
        this.terms    = terms;
        this.progress = progress;
    }

    @Override
    public void run() {
        BigDecimal subterm1 = BigDecimal.ONE,
                   subterm2 = BigDecimal.ONE,
                   subtermC = BigDecimal.ONE;

        try {
            // The first term (n = 0) is A/1
            terms.put(new Term(0, A, BigDecimal.ONE));

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
                subtermC = subtermC.multiply(C2);

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

                terms.put(new Term(n, numerator, subterm2.multiply(subtermC)));
            }

            terms.put(new Term(numTerms, BigDecimal.ZERO, BigDecimal.ZERO));
        } catch (InterruptedException e) {
            // Ignore
        }
    }
}

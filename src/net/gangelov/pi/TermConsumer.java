package net.gangelov.pi;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.BlockingQueue;

public class TermConsumer implements Runnable {
    private BigDecimal sum = BigDecimal.ZERO;
    private Term[] terms;
    private int threadIndex, numThreads;
    private MathContext mc;

    private CalculationProgress progress;

    public TermConsumer(Term[] terms, int threadIndex, int numThreads, int precision, CalculationProgress progress) {
        this.terms       = terms;
        this.progress    = progress;
        this.threadIndex = threadIndex;
        this.numThreads  = numThreads;

        mc = new MathContext(precision, RoundingMode.FLOOR);
    }

    public BigDecimal getSum() {
        return sum;
    }

    @Override
    public void run() {
        BigDecimal term;
        Term termRational;

        for (int i = threadIndex; i < terms.length; i += numThreads) {
            termRational = terms[i];

            // Calculate the term
            term = termRational.getNumerator().divide(termRational.getDenominator(), mc);

            // Add to the sum
            sum = sum.add(term);

            progress.update(termRational.getN());
        }
    }
}

package net.gangelov.pi;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.BlockingQueue;

public class TermConsumer implements Runnable {
    private BigDecimal sum = BigDecimal.ZERO;
    private BlockingQueue<Term> terms;
    private MathContext mc;

    private CalculationProgress progress;

    public TermConsumer(BlockingQueue<Term> terms, int precision, CalculationProgress progress) {
        this.terms    = terms;
        this.progress = progress;

        mc = new MathContext(precision, RoundingMode.FLOOR);
    }

    public BigDecimal getSum() {
        return sum;
    }

    @Override
    public void run() {
        BigDecimal term;
        Term termRational;

        try {
            while (true) {
                termRational = terms.take();

                if (termRational.getDenominator().equals(BigDecimal.ZERO)) {
                    // No more terms are being produced
                    // Put the term back in the queue so other
                    // consumers don't block indefinitely.
                    terms.put(termRational);
                    break;
                }

                // Calculate the term
                term = termRational.getNumerator().divide(termRational.getDenominator(), mc);

                // Add to the sum
                sum = sum.add(term);

                progress.update(termRational.getN());
            }
        } catch (InterruptedException e) {
            // Ignore
        }
    }
}

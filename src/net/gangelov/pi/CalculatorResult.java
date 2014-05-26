package net.gangelov.pi;

import java.math.BigDecimal;

/**
 * This class is used as a structure which contains the result of a Calculator object.
 *
 * It contains the resulting sum and the last partial term.
 */
public class CalculatorResult {
    private BigDecimal sum;
    private BigDecimal lastPartialTerm;

    private int numTerms;

    public BigDecimal getSum() {
        return sum;
    }

    public BigDecimal getLastPartialTerm() {
        return lastPartialTerm;
    }

    public int getNumTerms() {
        return numTerms;
    }

    public CalculatorResult(BigDecimal sum, BigDecimal lastPartialTerm, int numTerms) {
        this.sum = sum;
        this.lastPartialTerm = lastPartialTerm;
        this.numTerms = numTerms;
    }
}

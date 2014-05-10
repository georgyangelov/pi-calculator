package net.gangelov.pi;

import java.math.BigDecimal;

public class Term {
    private int n;
    private BigDecimal numerator;
    private BigDecimal denominator;

    public Term(int n, BigDecimal numerator, BigDecimal denominator) {
        this.n = n;
        this.numerator   = numerator;
        this.denominator = denominator;
    }

    public int getN() {
        return n;
    }

    public BigDecimal getNumerator() {
        return numerator;
    }

    public BigDecimal getDenominator() {
        return denominator;
    }
}

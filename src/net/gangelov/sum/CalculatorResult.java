package net.gangelov.sum;

import org.apfloat.Apfloat;

import java.io.Serializable;

/**
 * This class is used as a structure which contains the result of a Calculator object.
 *
 * It contains the resulting sum and the last partial term.
 */
public class CalculatorResult implements Serializable {
    private Apfloat sum;
    private Apfloat lastPartialTerm;

    private int numTerms;

    public Apfloat getSum() {
        return sum;
    }

    public Apfloat getLastPartialTerm() {
        return lastPartialTerm;
    }

    public int getNumTerms() {
        return numTerms;
    }

    public CalculatorResult(Apfloat sum, Apfloat lastPartialTerm, int numTerms) {
        this.sum = sum;
        this.lastPartialTerm = lastPartialTerm;
        this.numTerms = numTerms;
    }
}

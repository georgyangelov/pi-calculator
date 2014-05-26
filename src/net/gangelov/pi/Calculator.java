package net.gangelov.pi;

import java.math.BigDecimal;

/**
 * This interface abstracts the way a sum (or a part of one) is calculated.
 *
 * Possible implementations may include:
 *      - SequentialCalculator
 *      - ThreadCalculator
 *      - RemoteCalculator
 */
public interface Calculator {

    /**
     * Calculates the given range of the sum (starting from 1 as the first partial term).
     *
     * @param sum The sum to calculate.
     * @param startIndex The start term index.
     * @param termCount The number of terms to sum.
     * @param progress A Progress object which can be used to monitor the calculation progress.
     *
     * @return The sum and the last partial term
     */
    CalculatorResult calculate(InfiniteSum sum, int startIndex, int termCount, Progress progress) throws InterruptedException;

}

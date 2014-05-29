package net.gangelov.sum;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;

/**
 * This interface abstracts the way a sum (or a part of one) is calculated.
 *
 * Possible implementations may include:
 *      - SequentialCalculator
 *      - ConcurrentCalculator
 *      - RemoteCalculator
 */
public interface Calculator extends Remote {

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
    CalculatorResult calculate(InfiniteSum sum, int startIndex, int termCount, Progress progress) throws InterruptedException, ExecutionException, RemoteException;

    /**
     * The performance score for a calculator is an integer approximating how fast the calculation can be done.
     * It's used by implementations which split the work on multiple calculators to decide how much of the
     * work to give the specific calculator.
     *
     * Usually this score is approximately the number of threads this calculator runs on.
     *
     * The value returned is required to be greater than 0!
     *
     * @return The performance score.
     */
    int getPerformanceScore();
}

package net.gangelov.pi.calculators;

import net.gangelov.pi.Calculator;
import net.gangelov.pi.CalculatorResult;
import net.gangelov.pi.InfiniteSum;
import net.gangelov.pi.Progress;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConcurrentCalculator implements Calculator {
    private List<Calculator> calculators;

    public ConcurrentCalculator(List<Calculator> calculators) {
        this.calculators = calculators;
    }

    @Override
    public CalculatorResult calculate(final InfiniteSum sum, int startIndex, int termCount, final Progress progress) throws InterruptedException {
        int calculatorCount     = calculators.size();
        int termsPerCalculator  = termCount / calculatorCount;
        int lastCalculatorTerms = termsPerCalculator + termCount % calculatorCount;

        final List<Thread>       threads = new ArrayList<Thread>(calculatorCount);
        final CalculatorResult[] results = new CalculatorResult[calculatorCount];

        for (int i = 0; i < calculatorCount; i++) {
            final int calculatorIndex = i;
            final int startTerm, numTerms;
            final Calculator calculator = calculators.get(calculatorIndex);

            startTerm = termsPerCalculator * calculatorIndex;

            if (calculatorIndex == calculatorCount - 1) {
                numTerms = lastCalculatorTerms;
            } else {
                numTerms = termsPerCalculator;
            }

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        results[calculatorIndex] = calculator.calculate(sum, startTerm, numTerms, progress);
                    } catch (InterruptedException e) {
                        // Propagate the interruption
                        Thread.currentThread().interrupt();
                    }
                }
            });

            threads.add(thread);

            thread.start();
        }

        BigDecimal resultSum       = BigDecimal.ZERO,
                   lastPartialTerm = BigDecimal.ONE;
        for (int i = 0; i < calculatorCount; i++) {
            // Ensure calculation is finished
            threads.get(i).join();

            CalculatorResult partialResult = results[i];

            resultSum = resultSum.add(
                    lastPartialTerm.multiply(
                            partialResult.getSum()
                    )
            );

            lastPartialTerm = lastPartialTerm.multiply(partialResult.getLastPartialTerm());
        }

        return new CalculatorResult(resultSum, lastPartialTerm, termCount);
    }

    public static ConcurrentCalculator getLocalThreadedCalculator(int numThreads) {
        List<Calculator> calculators = new ArrayList<Calculator>(numThreads);

        for (int i = 0; i < numThreads; i++) {
            calculators.add(new SequentialCalculator());
        }

        return new ConcurrentCalculator(calculators);
    }
}

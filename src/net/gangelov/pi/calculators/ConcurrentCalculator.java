package net.gangelov.pi.calculators;

import net.gangelov.pi.Calculator;
import net.gangelov.pi.CalculatorResult;
import net.gangelov.pi.InfiniteSum;
import net.gangelov.pi.Progress;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class ConcurrentCalculator implements Calculator {
    private List<Calculator> calculators;
    private ExecutorService executor;

    public ConcurrentCalculator(List<Calculator> calculators) {
        this.calculators = calculators;
        this.executor = Executors.newFixedThreadPool(calculators.size());
    }

    @Override
    public CalculatorResult calculate(final InfiniteSum sum, int startIndex, int termCount, final Progress progress) throws InterruptedException, ExecutionException {
        int calculatorCount     = calculators.size();
        int termsPerCalculator  = termCount / calculatorCount;
        int lastCalculatorTerms = termsPerCalculator + termCount % calculatorCount;

        final List<Future<CalculatorResult>> results = new ArrayList<Future<CalculatorResult>>(calculatorCount);

        for (int calculatorIndex = 0; calculatorIndex < calculatorCount; calculatorIndex++) {
            int numTerms;
            Calculator calculator = calculators.get(calculatorIndex);

            if (calculatorIndex == calculatorCount - 1) {
                numTerms = lastCalculatorTerms;
            } else {
                numTerms = termsPerCalculator;
            }

            Callable<CalculatorResult> task = getCallableForCalculation(
                    calculator,
                    sum,
                    termsPerCalculator * calculatorIndex,
                    numTerms,
                    progress
            );
            results.add(executor.submit(task));
        }

        BigDecimal resultSum       = BigDecimal.ZERO,
                   lastPartialTerm = BigDecimal.ONE;
        for (int i = 0; i < calculatorCount; i++) {
            CalculatorResult partialResult = results.get(i).get();

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

    public static Callable<CalculatorResult> getCallableForCalculation(final Calculator calculator,
                                                                           final InfiniteSum sum,
                                                                           final int startIndex,
                                                                           final int termCount,
                                                                           final Progress progress) {
        return new Callable<CalculatorResult>() {
            @Override
            public CalculatorResult call() throws Exception {
                return calculator.calculate(sum, startIndex, termCount, progress);
            }
        };
    }
}

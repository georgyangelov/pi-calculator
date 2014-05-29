package net.gangelov.sum.calculators;

import net.gangelov.sum.Calculator;
import net.gangelov.sum.CalculatorResult;
import net.gangelov.sum.InfiniteSum;
import net.gangelov.sum.Progress;
import org.apfloat.Apfloat;

import java.math.BigDecimal;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
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

        Apfloat resultSum       = Apfloat.ZERO,
                lastPartialTerm = Apfloat.ONE;
        for (int i = 0; i < calculatorCount; i++) {
            results.get(i).get();
        }

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

    @Override
    public int getPerformanceScore() {
        int score = 0;

        for (Calculator calculator : calculators) {
            score += calculator.getPerformanceScore();
        }

        return score;
    }

    public void cleanup() {
        executor.shutdown();
    }

    public static ConcurrentCalculator getLocalThreadedCalculator(int numThreads) {
        List<Calculator> calculators = new ArrayList<Calculator>(numThreads);

        for (int i = 0; i < numThreads; i++) {
            calculators.add(new SequentialCalculator());
        }

        return new ConcurrentCalculator(calculators);
    }

    public static ConcurrentCalculator getFromRemoteCalculators(List<String> rmiRegistries, int defaultPort) throws RemoteException, NotBoundException {
        List<Calculator> calculators = new ArrayList<Calculator>(rmiRegistries.size());

        for (String registry : rmiRegistries) {
            String[] address = registry.split(":");
            int port = defaultPort;

            if (address.length > 1) {
                port = Integer.parseInt(address[1]);
            }

            calculators.add(getRemoteCalculator(address[0], port));
        }

        return new ConcurrentCalculator(calculators);
    }

    public static Calculator getRemoteCalculator(String rmiRegistry, int port) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(rmiRegistry, port);
        return (Calculator)registry.lookup("Calculator");
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

package net.gangelov.sum.calculators;

import net.gangelov.sum.Calculator;
import net.gangelov.sum.CalculatorResult;
import net.gangelov.sum.InfiniteSum;
import net.gangelov.sum.ProgressHandler;
import net.gangelov.sum.progresses.MultiProgressHandler;
import org.apfloat.Apfloat;

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

    private int[] distributeTermCounts(int termCount) throws RemoteException {
        int performanceScore = getPerformanceScore();
        int termsLeft = termCount;
        int[] terms = new int[calculators.size()];

        for (int i = 0; i < calculators.size() - 1; i++) {
            terms[i] = termCount * calculators.get(i).getPerformanceScore() / performanceScore;
            termsLeft -= terms[i];
        }

        terms[terms.length - 1] = termsLeft;

        return terms;
    }

    @Override
    public CalculatorResult calculate(final InfiniteSum sum, int startIndex, int termCount, final ProgressHandler progressHandler)
            throws InterruptedException, ExecutionException, RemoteException {
        long time;
        int termOffset = startIndex, calculatorCount = calculators.size();
        int[] terms = distributeTermCounts(termCount);

        final MultiProgressHandler multiProgressHandler = new MultiProgressHandler(terms, progressHandler);
        final List<Future<CalculatorResult>> results = new ArrayList<Future<CalculatorResult>>(calculatorCount);

        time = System.currentTimeMillis();

        for (int calculatorIndex = 0; calculatorIndex < calculatorCount; calculatorIndex++) {
            Calculator calculator = calculators.get(calculatorIndex);

            Callable<CalculatorResult> task = getCallableForCalculation(
                    calculator,
                    sum,
                    termOffset,
                    terms[calculatorIndex],
                    multiProgressHandler.getProgressHandlers().get(calculatorIndex)
            );
            results.add(executor.submit(task));

            termOffset += terms[calculatorIndex];
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

        time = System.currentTimeMillis() - time;

        return new CalculatorResult(resultSum, lastPartialTerm, termCount, time);
    }

    @Override
    public int getPerformanceScore() throws RemoteException {
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

    public static ConcurrentCalculator getFromRemoteCalculators(List<String> rmiRegistries, int defaultPort)
            throws RemoteException, NotBoundException {
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

    public static Calculator getRemoteCalculator(String rmiRegistry, int port)
            throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(rmiRegistry, port);
        return (Calculator)registry.lookup("Calculator");
    }

    public static Callable<CalculatorResult> getCallableForCalculation(final Calculator calculator,
                                                                       final InfiniteSum sum,
                                                                       final int startIndex,
                                                                       final int termCount,
                                                                       final ProgressHandler progress) {
        return new Callable<CalculatorResult>() {
            @Override
            public CalculatorResult call() throws Exception {
                return calculator.calculate(sum, startIndex, termCount, progress);
            }
        };
    }
}

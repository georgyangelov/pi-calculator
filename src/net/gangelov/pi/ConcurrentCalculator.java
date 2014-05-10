package net.gangelov.pi;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConcurrentCalculator {
    private int numTerms;
    private int numThreads;

    public final static int digitsPerTerm    = 5,
                            precisionPerTerm = 7;

    public ConcurrentCalculator(int numTerms, int numThreads) {
        this.numTerms   = numTerms;
        this.numThreads = numThreads;
    }

    public BigDecimal calculate(CalculationProgress progress) throws InterruptedException {
        BlockingQueue<Term> terms = new ArrayBlockingQueue<Term>(20, false);
        TermProducer producer     = new TermProducer(numTerms, terms, progress);
        BigDecimal result         = BigDecimal.ZERO;

        if (numThreads == 1) {
            TermConsumer consumer = new TermConsumer(terms, numTerms * precisionPerTerm, progress);

            producer.run();
            consumer.run();

            result = consumer.getSum();
        } else {
            List<TermConsumer> consumers = new ArrayList<TermConsumer>(numThreads);
            List<Thread> consumerThreads = new ArrayList<Thread>(numThreads);

            for (int i = 0; i < numThreads; i++) {
                TermConsumer consumer = new TermConsumer(terms, numTerms * precisionPerTerm, progress);
                Thread consumerThread = new Thread(consumer);

                consumers.add(consumer);
                consumerThreads.add(consumerThread);

                consumerThread.start();
            }

            producer.run();

            for (int i = 0; i < numThreads; i++) {
                consumerThreads.get(i).join();

                result = result.add(consumers.get(i).getSum());
            }
        }

        return BigDecimal.ONE.divide(
                result.divide(
                        BigDecimal.valueOf(4).multiply(TermProducer.C),
                        new MathContext(numTerms * 10)
                ),
                new MathContext(numTerms * 10)
        ).round(new MathContext(numTerms * digitsPerTerm, RoundingMode.FLOOR));
    }
}
package net.gangelov.sum.calculators;

import net.gangelov.sum.Calculator;
import net.gangelov.sum.CalculatorResult;
import net.gangelov.sum.InfiniteSum;
import net.gangelov.sum.ProgressHandler;
import net.gangelov.sum.progresses.Progress;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatContext;

import java.util.Properties;

public class SequentialCalculator implements Calculator {

    @Override
    public CalculatorResult calculate(InfiniteSum sum, int startIndex, int termCount, ProgressHandler progress) {
        Apfloat partialTerm = Apfloat.ONE;
        Apfloat result = Apfloat.ZERO;
        long time = System.currentTimeMillis();

        Progress progressReporter = new Progress(termCount, progress);

        int endIndex = startIndex + termCount;

        // Prevent Apfloat for using multiple threads (we're already doing the parallelization ourselves)
        ApfloatContext context = new ApfloatContext(new Properties());
        context.setNumberOfProcessors(1);

        ApfloatContext.setThreadContext(context);

        for (int n = startIndex; n < endIndex; n++) {
            result = result.add(sum.calculateTerm(partialTerm, n));
            progressReporter.next();

            partialTerm = sum.nextPartialTerm(partialTerm, n + 1);
        }

        time = System.currentTimeMillis() - time;
        System.out.println("Thread " + startIndex + " -> " + time + "ms");

        return new CalculatorResult(result, partialTerm, termCount, time);
    }

    @Override
    public int getPerformanceScore() {
        return 1;
    }

}

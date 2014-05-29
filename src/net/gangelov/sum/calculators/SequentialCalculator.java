package net.gangelov.sum.calculators;

import net.gangelov.sum.Calculator;
import net.gangelov.sum.CalculatorResult;
import net.gangelov.sum.InfiniteSum;
import net.gangelov.sum.Progress;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatContext;

import java.math.BigDecimal;
import java.util.Properties;

public class SequentialCalculator implements Calculator {

    @Override
    public CalculatorResult calculate(InfiniteSum sum, int startIndex, int termCount, Progress progress) {
        Apfloat partialTerm = Apfloat.ONE;
        Apfloat result = Apfloat.ZERO;

        int endIndex = startIndex + termCount;

        ApfloatContext context = new ApfloatContext(new Properties());
        context.setNumberOfProcessors(1);

        ApfloatContext.setThreadContext(context);

        long startTime = System.currentTimeMillis();
        for (int n = startIndex; n < endIndex; n++) {
            result = result.add(sum.calculateTerm(partialTerm, n));
            progress.next();

            partialTerm = sum.nextPartialTerm(partialTerm, n + 1);
        }
        System.out.println("\nThread " + startIndex + " calculation time: " + (System.currentTimeMillis() - startTime) + "ms");

        return new CalculatorResult(result, partialTerm, termCount);
    }

    @Override
    public int getPerformanceScore() {
        return 1;
    }

}

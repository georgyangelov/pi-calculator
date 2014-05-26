package net.gangelov.pi.calculators;

import net.gangelov.pi.Calculator;
import net.gangelov.pi.CalculatorResult;
import net.gangelov.pi.InfiniteSum;
import net.gangelov.pi.Progress;

import java.math.BigDecimal;

public class SequentialCalculator implements Calculator {

    @Override
    public CalculatorResult calculate(InfiniteSum sum, int startIndex, int termCount, Progress progress) {
        BigDecimal partialTerm = BigDecimal.ONE;
        BigDecimal result = BigDecimal.ZERO;

        int endIndex = startIndex + termCount;

        for (int n = startIndex; n < endIndex; n++) {
            result = result.add(sum.calculateTerm(partialTerm, n));
            progress.next();

            partialTerm = sum.nextPartialTerm(partialTerm, n + 1);
        }

        return new CalculatorResult(result, partialTerm, termCount);
    }

}

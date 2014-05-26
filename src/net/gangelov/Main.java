package net.gangelov;

import net.gangelov.pi.CalculatorResult;
import net.gangelov.pi.Progress;
import net.gangelov.pi.calculators.ConcurrentCalculator;
import net.gangelov.pi.calculators.SequentialCalculator;
import net.gangelov.pi.sums.RamanujanPi;

import java.io.IOException;
import java.math.BigDecimal;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        long startTime, time, sequentialTime;

        BigDecimal pi1, pi2;

        final int numThreads = 4;
        final int numTerms = 4000;

        Progress progress = new Progress(numTerms);
        progress.setHandler(new Progress.Handler() {
            @Override
            public void progress(int current, int max) {
                System.out.format("Progress: %02d%%\n", (int)(((float)current)/max * 100));
            }
        });

        RamanujanPi ramanujanPi = new RamanujanPi(numTerms);
        ConcurrentCalculator calculator = ConcurrentCalculator.getLocalThreadedCalculator(numThreads);
        SequentialCalculator calculator1 = new SequentialCalculator();
        CalculatorResult result;

        startTime = System.currentTimeMillis();
        result    = calculator.calculate(ramanujanPi, 0, numTerms, progress);
        pi1       = ramanujanPi.finalizeSum(result);
        time      = System.currentTimeMillis() - startTime;

        progress.reset();

//        startTime      = System.currentTimeMillis();
//        result         = calculator1.calculate(ramanujanPi, 0, numTerms, progress);
//        pi2            = ramanujanPi.finalizeSum(result);
//        sequentialTime = System.currentTimeMillis() - startTime;

        System.out.println("Concurrent time:  " + time           + "ms");
//        System.out.println("Sequential time:  " + sequentialTime + "ms");
        System.out.println(pi1.toString());
//        System.out.println(pi2.toString());
    }
}

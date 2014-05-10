package net.gangelov;

import net.gangelov.pi.CalculationProgress;
import net.gangelov.pi.Calculator;
import net.gangelov.pi.ConcurrentCalculator;

import java.io.IOException;
import java.math.BigInteger;
import java.math.BigDecimal;

public class Main {

//    public static void main(String[] args) {
//        BigInteger sum1 = BigInteger.ZERO,
//                   sum2 = BigInteger.ZERO;
//
//        long startTime, naiveTime, splitTime;
//
//        startTime = System.currentTimeMillis();
//        for (int i = 0; i < 100; i++) {
//            sum1 = sum1.add(MathUtils.naiveFactorial(10000));
//        }
//        naiveTime = System.currentTimeMillis() - startTime;
//
//        startTime = System.currentTimeMillis();
//        for (int i = 0; i < 100; i++) {
//            sum2 = sum2.add(MathUtils.factorial(10000));
//        }
//        splitTime = System.currentTimeMillis() - startTime;
//
//        System.out.println("Naive: " + naiveTime + "ms");
//        System.out.println("Split: " + splitTime + "ms");
//        System.out.println(sum1.equals(sum2) ? "The factorials are correct" : "The factorials are NOT correct");
//
//        if (!sum1.equals(sum2)) {
//            System.out.println(MathUtils.naiveFactorial(10));
//            System.out.println(MathUtils.factorial(10));
//        }
//    }

    public static void main(String[] args) throws IOException {
        BigDecimal pi1 = BigDecimal.ZERO, pi2 = BigDecimal.ZERO;
        long startTime, sequentialTime, concurrentTime;

        final int numDigits = 10000;
        final int maxTerms = (numDigits / ConcurrentCalculator.digitsPerTerm + 1);

        Calculator calc = new Calculator((numDigits / ConcurrentCalculator.digitsPerTerm + 1));
        ConcurrentCalculator ccalc = new ConcurrentCalculator(maxTerms, 5);

        startTime = System.currentTimeMillis();
//        pi1 = calc.calculate();
        sequentialTime = System.currentTimeMillis() - startTime;

        CalculationProgress progress = new CalculationProgress(maxTerms);

        progress.setHandler(new CalculationProgress.Handler() {
            @Override
            public void progress(int current, int max) {
                System.out.format("Progress: %02d%%\n", (int)(((float)current)/max * 100));
            }
        });

        startTime = System.currentTimeMillis();
        try {
            pi2 = ccalc.calculate(progress);
        } catch (InterruptedException e) {
            System.err.println("Concurrent PI calculation interrupted!");
        }
        concurrentTime = System.currentTimeMillis() - startTime;

        System.out.println("Sequential time:  " + sequentialTime + "ms");
        System.out.println("Concurrent time:  " + concurrentTime + "ms");

        System.out.println(pi1.toString());
        System.out.println(pi2.toString());


        System.out.println("Subterm 1:        " + calc.subterm1Time + "ms");
        System.out.println("Subterm 2:        " + calc.subterm2Time + "ms");
        System.out.println("Subterm C:        " + calc.subtermCTime + "ms");
        System.out.println("* (4n)!:          " + calc.multiplication1Time + "ms");
        System.out.println("* (A + Bn):       " + calc.multiplication2Time + "ms");
        System.out.println("sub2 * subC:      " + calc.multiplication3Time + "ms");
        System.out.println("/ (sub2 * subC):  " + calc.divisionTime + "ms");
        System.out.println("Summation:        " + calc.summationTime + "ms");
        System.out.println("/ 4C:             " + calc.finalDivisionTime + "ms");
        System.out.println("1 / sum:          " + calc.inverseTime + "ms");


//        startTime = System.currentTimeMillis();
//        MathUtils.factorial(10000, 40000);
//        System.out.println(System.currentTimeMillis() - startTime);
//
//        startTime = System.currentTimeMillis();
//        MathUtils.naiveFactorial(10000, 40000);
//        System.out.println(System.currentTimeMillis() - startTime);
    }
}

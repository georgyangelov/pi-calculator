package net.gangelov.pi;

import java.util.concurrent.Callable;

public class CalculationProgress {
    private int updatePercentInterval;
    private int currentTerms = 0;
    private int maxTerms;

    private int nextTermThreshold = 1;
    private Handler handler;

    public interface Handler {
        void progress(int current, int max);
    }

    public CalculationProgress(int maxTerms) {
        this.maxTerms = maxTerms;
        this.updatePercentInterval = 1;
    }

    public CalculationProgress(int maxTerms, int updatePercentInterval) {
        this.maxTerms = maxTerms;
        this.updatePercentInterval = updatePercentInterval;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public synchronized void update(int terms) {
        if (terms >= nextTermThreshold) {
            currentTerms = terms;
            nextTermThreshold = terms + maxTerms / 100;

            handler.progress(currentTerms, maxTerms);
        }
    }
}

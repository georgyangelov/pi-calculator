package net.gangelov.pi;

import java.util.concurrent.Callable;

public class Progress {
    private int currentTerms = 0;
    private int maxTerms;

    private int nextTermThreshold = 1;
    private Handler handler;

    public interface Handler {
        void progress(int current, int max);
    }

    public Progress(int maxTerms) {
        this.maxTerms = maxTerms;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public synchronized void next() {
        if (currentTerms++ >= nextTermThreshold) {
            nextTermThreshold = currentTerms + maxTerms / 100;

            handler.progress(currentTerms, maxTerms);
        }
    }

    public void reset() {
        currentTerms = 0;
        nextTermThreshold = 1;
    }
}

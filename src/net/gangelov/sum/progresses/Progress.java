package net.gangelov.sum.progresses;

import net.gangelov.sum.ProgressHandler;

import java.rmi.RemoteException;

public class Progress {
    private int currentTerms = 0;
    private int maxTerms;

    private int nextTermThreshold = 1;
    private ProgressHandler handler;

    public Progress(int maxTerms, ProgressHandler handler) {
        this.maxTerms = maxTerms;
        this.handler = handler;
    }

    public void next() {
        if (currentTerms++ >= nextTermThreshold) {
            nextTermThreshold = currentTerms + maxTerms / 100;

            try {
                handler.progress(currentTerms, maxTerms);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void reset() {
        currentTerms = 0;
        nextTermThreshold = 1;
    }
}

package net.gangelov.sum;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public class Progress implements Serializable {
    private int currentTerms = 0;
    private int maxTerms;

    private int nextTermThreshold = 1;
    private Handler handler;

    public interface Handler extends Remote {
        void progress(int current, int max) throws RemoteException;
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

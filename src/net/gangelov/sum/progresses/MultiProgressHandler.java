package net.gangelov.sum.progresses;

import net.gangelov.sum.ProgressHandler;

import java.io.Serializable;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiProgressHandler implements Serializable {

    private interface AggregationProgressHandler extends Remote {
        public void progress(int current, int max, String id) throws RemoteException;
    }

    public static final AggregationProgressHandler aggregationHandler = new AggregationProgressHandler() {
        @Override
        public void progress(int current, int max, String id) throws RemoteException {
            String[] idParts = id.split(":");

            instances.get(Integer.parseInt(idParts[0])).update(Integer.parseInt(idParts[1]), current, max);
        }
    };
    public static AggregationProgressHandler aggregationHandlerStub;

    private static int instanceIdCounter = 1;
    private static final Map<Integer, MultiProgressHandler> instances = new HashMap<Integer, MultiProgressHandler>();

    public static void exportHandler(int stubPort) throws RemoteException {
        aggregationHandlerStub = (AggregationProgressHandler)UnicastRemoteObject.exportObject(aggregationHandler, stubPort);
    }

    public static void unexportHandler() {
        try {
            UnicastRemoteObject.unexportObject(aggregationHandlerStub, true);
        } catch (NoSuchObjectException e) {
        }
    }

    private int instanceId;
    private ProgressHandler handler;
    private final List<ProgressHandler> progressHandlers = new ArrayList<ProgressHandler>();
    private final int[] current, total;

    private int currentPercentage = 0;

    public MultiProgressHandler(int[] progressTotals, ProgressHandler handler) {
        registerInstance();

        this.handler = handler;
        current = new int[progressTotals.length];
        total   = progressTotals;

        final String instanceIdString = Integer.valueOf(instanceId).toString();

        for (int i = 0; i < progressTotals.length; i++) {
            final int progressId = i;
            final AggregationProgressHandler handlerStub = aggregationHandlerStub;
            ProgressHandler progressHandler =
                    new ProgressHandler() {
                        @Override
                        public void progress(int current, int max) throws RemoteException {
                            handlerStub.progress(current, max, instanceIdString + ":" + progressId);
                        }
                    };
            progressHandlers.add(progressHandler);

            current[i] = 0;
        }
    }

    public synchronized void dispose() {
        instances.remove(instanceId);
    }

    private synchronized void registerInstance() {
        instanceId = instanceIdCounter++;
        instances.put(instanceId, this);
    }

    private synchronized void update(int progressId, int current, int max) {
        this.current[progressId] = current;
        notifyIfNeeded();
    }

    public List<ProgressHandler> getProgressHandlers() {
        return progressHandlers;
    }

    private void notifyIfNeeded() {
        int current = 0, total = 0;

        for (int i = 0; i < this.current.length; i++) {
            current += this.current[i];
            total   += this.total[i];
        }

        int percentage = (int)((float)current / total * 100);

        if (percentage != currentPercentage) {
            currentPercentage = percentage;

            try {
                handler.progress(current, total);
            } catch (RemoteException e) {
                System.err.println("Cannot notify progress handler");
                e.printStackTrace();
            }
        }
    }
}

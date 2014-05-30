package net.gangelov.sum;

import java.io.Serializable;
import java.rmi.RemoteException;

public interface ProgressHandler extends Serializable {

    /**
     * This method is called when a progress is made.
     *
     * @param current The current progress.
     * @param max     The maximum progress.
     *
     * @throws java.rmi.RemoteException Allows the handler to be a remote object.
     */
    void progress(int current, int max) throws RemoteException;

}

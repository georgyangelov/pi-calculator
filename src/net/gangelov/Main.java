package net.gangelov;

import net.gangelov.sum.Calculator;
import net.gangelov.sum.CalculatorResult;
import net.gangelov.sum.Progress;
import net.gangelov.sum.calculators.ConcurrentCalculator;
import net.gangelov.sum.sums.RamanujanPi;
import org.apfloat.Apfloat;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args)
            throws IOException, InterruptedException, ExecutionException, NotBoundException {
        int numThreads = 0;
        int numTerms = 0;
        String outFile = "pi";

        boolean server = false;
        List<String> remotes = new ArrayList<String>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("--server")) {
                server = true;
            } else if (arg.equals("-r") || arg.equals("--remote")) {
                remotes.add(args[i + 1]);
                i++;
            } else if (arg.equals("-p") || arg.equals("--terms")) {
                numTerms = Integer.parseInt(args[i + 1]);
                i++;
            } else if (arg.equals("-t") || arg.equals("--threads")) {
                numThreads = Integer.parseInt(args[i + 1]);
                i++;
            } else if (arg.equals("-o") || arg.equals("--out")) {
                outFile = args[i + 1];
                i++;
            } else {
                System.err.println("Unknown option " + arg);
                System.exit(1);
            }
        }

        if (server) {
            runServer(numThreads);
        } else {
            if (numTerms == 0) {
                System.err.println("Number of terms should be specified with -p");
                System.exit(1);
            }

            if (numThreads == 0) {
                numThreads = Runtime.getRuntime().availableProcessors();
                System.out.println("Using " + numThreads + " threads");
            }

            if (remotes.size() > 0) {
                runCalculator(numTerms, outFile, remotes);
                return;
            }

            runLocalCalculator(numTerms, numThreads, outFile);
        }
    }

    private static String getPolicyFile()
            throws IOException {
        File policyFile      = File.createTempFile("security", "policy");
        FileOutputStream out = new FileOutputStream(policyFile);
        InputStream in       = Main.class.getResourceAsStream("/security.policy");

        byte[] buffer = new byte[1024];
        int len = in.read(buffer);
        while (len != -1) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }

        return policyFile.getAbsolutePath();
    }

    private static void runServer(int numThreads)
            throws IOException {
        System.setProperty("java.security.policy", getPolicyFile());
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            String name = "Calculator";

            Calculator calculator = ConcurrentCalculator.getLocalThreadedCalculator(numThreads);
            Calculator stub       = (Calculator)UnicastRemoteObject.exportObject(calculator, 0);

            Registry registry = LocateRegistry.createRegistry(42424);
            registry.rebind(name, stub);

            System.out.println("Calculator bound");
        } catch (RemoteException e) {
            System.err.println("Cannot bind Calculator instance");
            e.printStackTrace();
        }
    }

    private static void runCalculator(int numTerms, String outFile, List<String> remotes)
            throws IOException, InterruptedException, ExecutionException, NotBoundException {
        long startTime, time, finalizeTime;

        Apfloat pi;

        Progress progress        = new Progress(numTerms);
        Progress.Handler handler = new Progress.Handler() {
            @Override
            public void progress(int current, int max) {
                System.out.format("\r\r\r\r\r\r\r\r\r\r\r\rProgress: %02d%%", (int)(((float)current + 1)/max * 100));
            }
        };

        // Export the handler so it can be called from the remote
        Progress.Handler handlerStub = (Progress.Handler)UnicastRemoteObject.exportObject(handler, 42425);

        progress.setHandler(handlerStub);

        RamanujanPi ramanujanPi = new RamanujanPi(numTerms);
        ConcurrentCalculator calculator = null;
//        try {
            calculator = ConcurrentCalculator.getFromRemoteCalculators(remotes, 42424);
//        } catch (NotBoundException e) {
//            System.err.println("Cannot locate registry or Calculator object");
//            e.printStackTrace();
//            return;
//        }
        CalculatorResult result;

        startTime = System.currentTimeMillis();
        result    = calculator.calculate(ramanujanPi, 0, numTerms, progress);
        time      = System.currentTimeMillis() - startTime;

        startTime    = System.currentTimeMillis();
        pi           = ramanujanPi.finalizeSum(result);
        finalizeTime = System.currentTimeMillis() - startTime;

        calculator.cleanup();

        System.out.println("\nCalculation time: " + time          + "ms");
        System.out.println("Finalization time: "  + finalizeTime  + "ms");

        UnicastRemoteObject.unexportObject(handler, true);

        PrintWriter file = new PrintWriter(new FileOutputStream(outFile, false));
        file.println(pi.toString());
        file.flush();
        file.close();
    }

    private static void runLocalCalculator(int numTerms, int numThreads, String outFile)
            throws IOException, InterruptedException, ExecutionException {
        long startTime, time, finalizeTime;

        Apfloat pi;

        Progress progress = new Progress(numTerms);
        progress.setHandler(new Progress.Handler() {
            @Override
            public void progress(int current, int max) {
                System.out.format("\r\r\r\r\r\r\r\r\r\r\r\rProgress: %02d%%", (int)(((float)current + 1)/max * 100));
            }
        });

        RamanujanPi ramanujanPi = new RamanujanPi(numTerms);
        ConcurrentCalculator calculator = ConcurrentCalculator.getLocalThreadedCalculator(numThreads);
        CalculatorResult result;

        startTime = System.currentTimeMillis();
        result    = calculator.calculate(ramanujanPi, 0, numTerms, progress);
        time      = System.currentTimeMillis() - startTime;

        startTime = System.currentTimeMillis();
        pi       = ramanujanPi.finalizeSum(result);
        finalizeTime = System.currentTimeMillis() - startTime;

        calculator.cleanup();

        System.out.println("\nCalculation time: " + time          + "ms");
        System.out.println("Finalization time: "  + finalizeTime  + "ms");

        PrintWriter file = new PrintWriter(new FileOutputStream(outFile, false));
        file.println(pi.toString());
        file.flush();
        file.close();
    }
}

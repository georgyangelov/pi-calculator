package net.gangelov;

import net.gangelov.sum.Calculator;
import net.gangelov.sum.CalculatorResult;
import net.gangelov.sum.InfiniteSum;
import net.gangelov.sum.ProgressHandler;
import net.gangelov.sum.progresses.MultiProgressHandler;
import net.gangelov.sum.calculators.ConcurrentCalculator;
import net.gangelov.sum.sums.ChudonovskyPi;
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

    final static int defaultProgressStubPort = 42425,
                     defaultRegistryPort     = 42424;

    public static boolean quiet = false;


    public static void main(String[] args)
            throws IOException, InterruptedException, ExecutionException, NotBoundException {
        int numThreads = 0,
            numSplitParts = 0,
            numTerms = 0,
            progressStubPort = defaultProgressStubPort,
            registryPort     = defaultRegistryPort;
        String outFile = "pi",
               sumName = null;

        boolean server = false;
        List<String> remotes = new ArrayList<String>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("--server")) {
                server = true;
            } else if (arg.equals("-r") || arg.equals("--remote")) {
                remotes.add(args[i + 1]);
                i++;
            } else if (arg.equals("--progress-port")) {
                progressStubPort = Integer.parseInt(args[i + 1]);
                i++;
            } else if (arg.equals("--registry-port")) {
                registryPort = Integer.parseInt(args[i + 1]);
                i++;
            } else if (arg.equals("-p") || arg.equals("--terms")) {
                numTerms = Integer.parseInt(args[i + 1]);
                i++;
            } else if (arg.equals("-t") || arg.equals("--threads")) {
                numThreads = Integer.parseInt(args[i + 1]);
                i++;
            } else if (arg.equals("--splits")) {
                numSplitParts = Integer.parseInt(args[i + 1]);
                i++;
            } else if (arg.equals("-o") || arg.equals("--out")) {
                outFile = args[i + 1];
                i++;
            } else if (arg.equals("--sum")) {
                sumName = args[i + 1];
                i++;
            } else if (arg.equals("-q") || arg.equals("--quiet")) {
                quiet = true;
            } else {
                System.err.println("Unknown option " + arg);
                System.exit(1);
            }
        }

        MultiProgressHandler.exportHandler(progressStubPort);

        if (server) {
            if (numThreads == 0) {
                numThreads = Runtime.getRuntime().availableProcessors();
                System.out.println("Using " + numThreads + " threads");
            }

            runServer(numThreads, registryPort);
        } else {
            if (numTerms == 0) {
                System.err.println("Number of terms should be specified with -p");
                System.exit(1);
            }

            if (sumName == null) {
                System.out.println("Using Ramanujan's sum");
                sumName = "ramanujan";
            }

            InfiniteSum sum = null;
            if (sumName.equalsIgnoreCase("ramanujan")) {
                sum = new RamanujanPi(numTerms);
            } else if (sumName.equalsIgnoreCase("chudonovsky")) {
                sum = new ChudonovskyPi(numTerms);
            } else {
                System.err.println("Unknown sum " + sumName);
                System.exit(1);
            }

            if (remotes.size() > 0) {
                runCalculator(sum, numTerms, outFile, remotes);
            } else {
                if (numThreads == 0) {
                    numThreads = Runtime.getRuntime().availableProcessors();
                    System.out.println("Using " + numThreads + " threads");
                }

                if (numSplitParts == 0) {
                    numSplitParts = numThreads;
                }

                runLocalCalculator(sum, numTerms, numSplitParts, numThreads, outFile);
            }
            MultiProgressHandler.unexportHandler();
            System.exit(0);
        }

        MultiProgressHandler.unexportHandler();
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

    private static void runServer(int numThreads, int registryPort)
            throws IOException {
        System.setProperty("java.security.policy", getPolicyFile());
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            String name = "Calculator";

            Calculator calculator = ConcurrentCalculator.getLocalThreadedCalculator(numThreads);
            Calculator stub       = (Calculator)UnicastRemoteObject.exportObject(calculator, 0);

            Registry registry = LocateRegistry.createRegistry(registryPort);
            registry.rebind(name, stub);

            System.out.println("Calculator bound");
        } catch (RemoteException e) {
            System.err.println("Cannot bind Calculator instance");
            e.printStackTrace();
        }
    }

    private static void runCalculator(InfiniteSum sum, int numTerms, String outFile, List<String> remotes)
            throws IOException, InterruptedException, ExecutionException, NotBoundException {
        long startTime, time, finalizeTime;

        Apfloat pi;

        ProgressHandler progressHandler = new ProgressHandler() {
            @Override
            public void progress(int current, int max) {
                if (!quiet) {
                    System.out.format("\rProgress: %02d%%", (int) (((float) current + 1) / max * 100));
                }
            }
        };

        ConcurrentCalculator calculator = null;
        calculator = ConcurrentCalculator.getFromRemoteCalculators(remotes, 42424);
        CalculatorResult result;

        startTime = System.currentTimeMillis();
        result    = calculator.calculate(sum, 0, numTerms, progressHandler);
        time      = System.currentTimeMillis() - startTime;

        startTime    = System.currentTimeMillis();
        pi           = sum.finalizeSum(result);
        finalizeTime = System.currentTimeMillis() - startTime;

        calculator.cleanup();

        System.out.println("\nCalculation time: " + time          + "ms");
        System.out.println("Finalization time: "  + finalizeTime  + "ms");

        PrintWriter file = new PrintWriter(new FileOutputStream(outFile, false));
        file.println(pi.toString());
        file.flush();
        file.close();
    }

    private static void runLocalCalculator(InfiniteSum sum, int numTerms, int numSplitParts, int numThreads, String outFile)
            throws IOException, InterruptedException, ExecutionException {
        long startTime, time, finalizeTime;

        Apfloat pi;

        ProgressHandler progressHandler = new ProgressHandler() {
            @Override
            public void progress(int current, int max) {
                if (!quiet) {
                    System.out.format("\rProgress: %02d%%", (int) (((float) current + 1) / max * 100));
                }
            }
        };

        ConcurrentCalculator calculator = ConcurrentCalculator.getLocalThreadedCalculator(numSplitParts, numThreads);
        CalculatorResult result;

        startTime = System.currentTimeMillis();
        result    = calculator.calculate(sum, 0, numTerms, progressHandler);
        time      = System.currentTimeMillis() - startTime;

        startTime    = System.currentTimeMillis();
        pi           = sum.finalizeSum(result);
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

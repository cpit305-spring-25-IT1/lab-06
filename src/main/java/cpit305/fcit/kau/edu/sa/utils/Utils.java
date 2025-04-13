package cpit305.fcit.kau.edu.sa.utils;

public class Utils {
    public static void simulateNetworkLatency() {
        try {
            System.out.println();
            for (int i=0; i < 10; i++) {
                System.out.print(".");
                Thread.sleep(100);
            }
        } catch (InterruptedException ex) {
            System.err.println(ex.getMessage());
        }
    }

}

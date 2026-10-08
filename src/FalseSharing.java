import java.util.concurrent.atomic.AtomicLongArray;

public class FalseSharing {
    static final long ITER = 100_000_000L;

    static long run(int gap, int threads) throws Exception {
        AtomicLongArray arr = new AtomicLongArray(threads * gap + 1);
        Thread[] t = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            final int idx = i * gap;
            t[i] = new Thread(() -> { for (long k = 0; k < ITER; k++) arr.set(idx, k); });
        }
        long s = System.nanoTime();
        for (Thread x : t) x.start();
        for (Thread x : t) x.join();
        return (System.nanoTime() - s) / 1_000_000;
    }

    public static void main(String[] args) throws Exception {
        int threads = Math.min(4, Runtime.getRuntime().availableProcessors());
        if (threads < 2) { System.out.println("Need at least 2 cores"); return; }
        System.out.println("Threads: " + threads);
        run(1, threads); run(16, threads); // warm-up
        for (int r = 0; r < 3; r++) {
            System.out.println("Adjacent (gap=1):  " + run(1, threads) + " ms");
            System.out.println("Padded (gap=16):    " + run(16, threads) + " ms");
        }
    }
}
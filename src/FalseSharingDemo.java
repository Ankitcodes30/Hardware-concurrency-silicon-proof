public class FalseSharingDemo {
    private static final long ITERATIONS = 50_000_000L;

    // 1. Without Padding (False Sharing Hogi - SLOW)
    static class Unpadded {
        public volatile long a = 0L;
        public volatile long b = 0L; // Dono variables same 64-byte cache line me aayenge
    }

    // 2. With Padding (False Sharing Fix - FAST)
    static class Padded {
        public volatile long a = 0L;
        // 7 longs = 56 bytes + object header -> 'b' alag cache line me chala jayega
        public long p1, p2, p3, p4, p5, p6, p7;
        public volatile long b = 0L;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Running Phase 2 Benchmark on: " + 
            System.getProperty("os.name") + " | Cores: " + 
            Runtime.getRuntime().availableProcessors());

        // Test Unpadded
        Unpadded unpadded = new Unpadded();
        long startUnpadded = System.currentTimeMillis();

        Thread t1 = new Thread(() -> {
            for (long i = 0; i < ITERATIONS; i++) unpadded.a++;
        });
        Thread t2 = new Thread(() -> {
            for (long i = 0; i < ITERATIONS; i++) unpadded.b++;
        });

        t1.start(); t2.start();
        t1.join(); t2.join();
        long timeUnpadded = System.currentTimeMillis() - startUnpadded;
        System.out.println("1. Unpadded Execution Time: " + timeUnpadded + " ms");

        // Test Padded
        Padded padded = new Padded();
        long startPadded = System.currentTimeMillis();

        Thread t3 = new Thread(() -> {
            for (long i = 0; i < ITERATIONS; i++) padded.a++;
        });
        Thread t4 = new Thread(() -> {
            for (long i = 0; i < ITERATIONS; i++) padded.b++;
        });

        t3.start(); t4.start();
        t3.join(); t4.join();
        long timePadded = System.currentTimeMillis() - startPadded;
        System.out.println("2. Padded Execution Time:   " + timePadded + " ms");

        System.out.printf("Speedup: %.2fx faster with cache padding!\n", 
            (double) timeUnpadded / timePadded);
    }
}
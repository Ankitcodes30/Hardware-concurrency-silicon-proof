import java.util.concurrent.atomic.*;

public class LockFree {
    static final AtomicLong counter = new AtomicLong();
    static void casIncrement() {
        long cur;
        do { cur = counter.get(); } while (!counter.compareAndSet(cur, cur + 1));
    }

    static class SpscRing<T> {
        private final Object[] buf;
        private final int mask;
        private final AtomicLong head = new AtomicLong(), tail = new AtomicLong();
        SpscRing(int pow2) {
            if (Integer.bitCount(pow2) != 1) throw new IllegalArgumentException("size must be power of 2");
            buf = new Object[pow2]; mask = pow2 - 1;
        }
        boolean offer(T x) {
            long t = tail.get();
            if (t - head.get() == buf.length) return false;
            buf[(int) (t & mask)] = x;
            tail.lazySet(t + 1);
            return true;
        }
        @SuppressWarnings("unchecked")
        T poll() {
            long h = head.get();
            if (h == tail.get()) return null;
            T x = (T) buf[(int) (h & mask)];
            buf[(int) (h & mask)] = null;
            head.lazySet(h + 1);
            return x;
        }
    }

    public static void main(String[] args) throws Exception {
        Thread[] ts = new Thread[4];
        for (int i = 0; i < 4; i++) {
            ts[i] = new Thread(() -> { for (int k = 0; k < 1_000_000; k++) casIncrement(); });
            ts[i].start();
        }
        for (Thread t : ts) t.join();
        System.out.println("CAS counter: " + counter.get() + " (expected 4000000)");

        final int N = 10_000_000;
        SpscRing<Integer> ring = new SpscRing<>(1024);
        long start = System.nanoTime();
        Thread prod = new Thread(() -> { for (int i = 0; i < N; ) if (ring.offer(i)) i++; });
        Thread cons = new Thread(() -> {
            int expect = 0;
            while (expect < N) {
                Integer v = ring.poll();
                if (v == null) continue;
                if (v != expect) throw new IllegalStateException("Order broke at " + expect);
                expect++;
            }
        });
        prod.start(); cons.start(); prod.join(); cons.join();
        System.out.println("Ring OK, " + N + " items in " + (System.nanoTime() - start) / 1_000_000 + " ms");
    }
}
public class UnsafePublication {
    static int data = 0;
    static boolean ready = false;

    public static void main(String[] args) throws Exception {
        Thread writer = new Thread(() -> {
            data = 42;
            ready = true;
        });
        Thread reader = new Thread(() -> {
            while (!ready) { }
            System.out.println("data = " + data);
        });
        reader.start();
        writer.start();
        writer.join(2000);
        reader.join(2000);
        System.out.println("Main finished");
    }
}
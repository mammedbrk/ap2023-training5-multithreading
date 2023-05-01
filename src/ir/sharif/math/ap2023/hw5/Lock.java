package ir.sharif.math.ap2023.hw5;

public class Lock {
    private final Object o = new Object();
    private int wasSignalled = 0;

    public void doWait() {
        synchronized (o) {
            if (wasSignalled == 0){
                try{
                    o.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            wasSignalled--;
        }
    }

    public void doNotify() {
        synchronized (o) {
            wasSignalled++;
            o.notify();
        }
    }
}

package ir.sharif.math.ap2023.hw5;

public class DownloadThreadManager extends Thread {
    private final long SAFE_MARGIN;
    private final int threadCount;
    private final DownloadThread[] threads;
    private final SourceProvider sourceProvider;

    private int finishedThreads;
    private final Lock lock;


    public DownloadThreadManager(long safeMargin, int threadCount, DownloadThread[] threads, SourceProvider sourceProvider, Lock lock) {
        SAFE_MARGIN = safeMargin;
        this.threadCount = threadCount;
        this.threads = threads;
        this.sourceProvider = sourceProvider;
        this.lock = lock;
        finishedThreads = 0;
    }

    @Override
    public void run() {
        while (finishedThreads < threadCount) {
            lock.doWait();

            for (DownloadThread waitedThread: threads) {
                if (waitedThread.isAlive() && !waitedThread.isDownloading() && !waitedThread.isFinished()) {
                    waitedThread.setFinished(true);
                    DownloadThread maxRemainingTread = getMaxRemainingThread();
                    if (maxRemainingTread != null) {
                        // todo split its work
                        long offset = maxRemainingTread.getOffset();
                        long remaining = maxRemainingTread.getRemaining();

                        maxRemainingTread.setRemaining(remaining / 2 + remaining % 2);

                        waitedThread.init(offset + remaining / 2 + remaining % 2, remaining / 2, sourceProvider.connect(offset + remaining / 2 + remaining % 2));
                    }
                    else {
                        finishedThreads++;
//                        System.out.println(waitedThread.getNo() + " completely finished.");
                    }
                    waitedThread.getLock().doNotify();
                    break;
                }
            }
        }
    }

    private DownloadThread getMaxRemainingThread() {
        DownloadThread ret = null;
        for (DownloadThread remainingThread: threads) {
            if (remainingThread.isAlive() && remainingThread.isDownloading() && !remainingThread.isFinished()) {
                if ((ret == null || ret.getRemaining() < remainingThread.getRemaining()) && remainingThread.getRemaining() >= SAFE_MARGIN) {
                    ret = remainingThread;
                }
            }
        }
        return ret;
    }
}

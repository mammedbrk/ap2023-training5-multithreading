package ir.sharif.math.ap2023.hw5;

import java.io.IOException;
import java.io.RandomAccessFile;

public class MultiThreadCopier {
    public static final long SAFE_MARGIN = 6;
    private final SourceProvider sourceProvider;
    private final String dest;
    private final int workerCount;
    private final long size;
    private final DownloadThread[] threads;

    public MultiThreadCopier(SourceProvider sourceProvider, String dest, int workerCount) {
        this.sourceProvider = sourceProvider;
        this.dest = dest;
        this.workerCount = workerCount;

        this.size = sourceProvider.size();
        threads = new DownloadThread[workerCount];
    }

    public void start() {
        try {
            initFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Lock lock = new Lock();

        for (int i = 0; i < workerCount; i++) {
            long offset, remaining;
            offset = i * (size / workerCount);
            if (i < workerCount - 1) remaining = size / workerCount;
            else remaining = size - offset;
            DownloadThread thread = new DownloadThread(i, dest, lock, offset, remaining, sourceProvider.connect(offset));

            thread.start();
            threads[i] = thread;
        }

        DownloadThreadManager manager = new DownloadThreadManager(SAFE_MARGIN, workerCount, threads, sourceProvider, lock);
        manager.start();
    }

    private void initFile() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(dest, "rw");
        randomAccessFile.write(new byte[(int) size]);
        randomAccessFile.close();
    }
}


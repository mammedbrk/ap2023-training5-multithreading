package ir.sharif.math.ap2023.hw5;

import java.io.IOException;
import java.io.RandomAccessFile;

public class MultiThreadCopier {
    public static final long SAFE_MARGIN = 6;
    private final SourceProvider sourceProvider;
    private final String dest;
    private final int workerCount;
    private final long size;

    public MultiThreadCopier(SourceProvider sourceProvider, String dest, int workerCount) {
        this.sourceProvider = sourceProvider;
        this.dest = dest;
        this.workerCount = workerCount;

        this.size = sourceProvider.size();
    }

    public void start() {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(dest, "rw");
            randomAccessFile.write(new byte[(int) size]);
            randomAccessFile.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < workerCount; i++) {
            int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long length = size / workerCount;
                    long offset = finalI * length;

                    if (finalI == workerCount - 1) {
                        length = size - offset;
                    }
                    long end = offset + length;

                    RandomAccessFile randomAccessFile;
                    try {
                        randomAccessFile = new RandomAccessFile(dest, "rw");
                        randomAccessFile.seek(offset);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    SourceReader sourceReader = sourceProvider.connect(offset);

                    while (offset < end) {
                        try {
                            randomAccessFile.writeByte(sourceReader.read());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        offset++;
                    }

                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
    }
}

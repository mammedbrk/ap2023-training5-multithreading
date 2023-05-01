package ir.sharif.math.ap2023.hw5;

import java.io.IOException;
import java.io.RandomAccessFile;

public class DownloadThread extends Thread {
    private final int no;
    private final String dest;
    private final Lock managerLock;
    private final Lock lock;
    private boolean downloading;
    private boolean finished;
    private long offset;
    private long remaining;
    private SourceReader sourceReader;

    public DownloadThread(int no, String dest, Lock managerLock, long offset, long remaining, SourceReader sourceReader) {
        this.no = no;
        this.dest = dest;
        this.managerLock = managerLock;
        lock = new Lock();
        init(offset, remaining, sourceReader);
    }

    public void init(long offset, long remaining, SourceReader sourceReader) {
        downloading = true;
        finished = false;
        this.offset = offset;
        this.remaining = remaining;
        this.sourceReader = sourceReader;
    }

    @Override
    public void run() {
        downloading = true;
        while (downloading) {
            try {
                download();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            managerLock.doNotify();
            lock.doWait();
        }
    }

    private void download() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(dest, "rw");
        randomAccessFile.seek(offset);
        while (remaining > 0) {
//            System.out.println("Thread: " + no + ", remaining: " + remaining);
            randomAccessFile.writeByte(sourceReader.read() + (byte) no);
            remaining--;
            offset++;
        }
        randomAccessFile.close();

        downloading = false;
    }

    // getters and setters

    public boolean isDownloading() {
        return downloading;
    }

    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getRemaining() {
        return remaining;
    }

    public void setRemaining(long remaining) {
        this.remaining = remaining;
    }

    public void setSourceReader(SourceReader sourceReader) {
        this.sourceReader = sourceReader;
    }

    public int getNo() {
        return no;
    }

    public Lock getLock() {
        return lock;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}

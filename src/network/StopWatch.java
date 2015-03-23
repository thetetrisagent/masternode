package network;

public class StopWatch {

    private long startTime;
    
    public StopWatch() {
        startTime = System.currentTimeMillis();
    }
    
    public void reset() {
        startTime = System.currentTimeMillis();
    }
    
    public long getElapsedTime() {
        long endTime = System.currentTimeMillis();
        return (endTime - startTime);
    }
}


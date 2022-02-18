import java.util.HashMap;
import java.util.List;

public class ThreadInfo {
    int numSuccess;
    int numFail;
    HashMap<Long, long[]> latencyMap;
    long minLatency;
    long maxLatency;
    long start;
    long end;

    public ThreadInfo(int numSuccess, int numFail, HashMap<Long, long[]> latencyMap,
                      long maxLatency, long minLatency,
                      long start, long end) {
        this.numSuccess = numSuccess;
        this.numFail = numFail;
        this.latencyMap = latencyMap;
        this.maxLatency = maxLatency;
        this.minLatency = minLatency;
        this.start = start;
        this.end = end;
    }
}

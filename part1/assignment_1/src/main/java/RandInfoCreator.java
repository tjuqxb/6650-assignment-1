import domain.SkierPost;
import java.util.Random;

public class RandInfoCreator {
    int startId;
    int endId;
    int startTime;
    int endTime;
    int numLifts;
    int skierId;
    int liftId;
    int time;
    int waitTime;
    int resortId;
    int seasonId;
    int dayId;


    Random r = new Random();

    public RandInfoCreator(int startId, int endId, int startTime, int endTime, int numLifts) {
        this.startId = startId;
        this.endId = endId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.numLifts = numLifts;
    }

    /**
     * A function to generate random integer numbers in range [start, end].
     *
     * @param start the start of range
     * @param end the end of range
     */
    public int generateRand(int start, int end) {
        return r.nextInt(end - start + 1) + start;
    }

    /**
     * A function to generate random information in each run.
     */
    public void generateRandInfo() {
        this.skierId = generateRand(startId, endId);
        this.liftId = generateRand(1, numLifts);
        this.time = generateRand(startTime, endTime);
        this.waitTime = generateRand(0,10);
        this.resortId = generateRand(1, 2000);
        this.seasonId = generateRand(2000, 2022);
        this.dayId = generateRand(1, 366);
    }

    /**
     * Create a SkierPost object based on generated information
     *
     * @return  A SkierPost object containing generated information
     */
    public SkierPost createPost() {
        generateRandInfo();
        return new SkierPost(liftId, time, waitTime);
    }

    public int getSkierId() {
        return skierId;
    }

    public int getLiftId() {
        return liftId;
    }

    public int getTime() {
        return time;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public int getResortId() {
        return resortId;
    }

    public int getSeasonId() {
        return seasonId;
    }

    public int getDayId() {
        return dayId;
    }
}

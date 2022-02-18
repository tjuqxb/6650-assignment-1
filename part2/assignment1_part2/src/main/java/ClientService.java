import com.opencsv.CSVWriter;

import javax.xml.bind.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class ClientService {

    private static int numThreads;
    private static int numSkiers;
    private static int numLifts;
    private static int numRuns;
    private static String addrAndPort;
    private static int success = 0;
    private static int fail = 0;
    private static List<HashMap<Long, long[]>> results = new ArrayList<>();
    private static long minLatency = Long.MAX_VALUE;
    private static long maxLatency = Long.MIN_VALUE;
    private static long startTime = Long.MAX_VALUE;
    private static long endTime = Long.MIN_VALUE;



    /**
     * Entry point for the program. Can read arguments either from commandline or from "src/main/java/parameters.xml" file
     *
     * @param args user specified arguments.
     *              * arg0: maximum number of threads to run (numThreads - max 1024)
     *              * arg1: number of skier to generate lift rides for (numSkiers - max 100000),
     *                      This is effectively the skier’s ID (skierID)
     *              * arg2: number of ski lifts (numLifts - range 5-60, default 40)
     *              * arg3: mean numbers of ski lifts each skier rides each day (numRuns - default 10, max 20)
     *              * arg4: IP address and port number of the server
     */
    public static void main(String[] args) throws JAXBException, InterruptedException, ExecutionException, IOException {
        Integer[] argsRec = new Integer[4];
        if (args.length == 0) {
            JAXBContext jc = JAXBContext.newInstance(Parameters.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            unmarshaller.setEventHandler(new ValidationEventHandler() {
                @Override
                public boolean handleEvent(ValidationEvent event) {
                    System.out.println(event.getMessage());
                    return true;
                }}

            );
            File xml = new File("src/main/java/parameters.xml");
            Parameters root = (Parameters) unmarshaller.unmarshal(xml);
            argsRec[0] = root.numThreads;
            argsRec[1] = root.numSkiers;
            argsRec[2] = root.numLifts;
            argsRec[3] = root.numRuns;
            addrAndPort = root.InetAddrAndPort;
        } else if (args.length == 5){
            for (int i = 0; i < 4; i++) {
                argsRec[i] = Utils.parseSingleNumber(args[i]);
            }
            addrAndPort = args[4];
        } else {
            System.err.println("wrong number of arguments");
            return;
        }
        if (!validateCommands(argsRec)) {
            System.err.println("wrong arguments");
            return;
        }
        numThreads = argsRec[0];
        numSkiers = argsRec[1];
        numLifts = argsRec[2];
        numRuns = argsRec[3];
        System.out.println("number of threads: " + numThreads);
        //Phase 1
        int numThreads1 = numThreads / 4;
        System.out.println("Phase 1 starts!");
        CountDownLatch countDownLatch1 = new CountDownLatch((int)(numThreads1*0.2));
        ExecutorService executor1 = Executors.newFixedThreadPool(numThreads * 2);
        List<Future<ThreadInfo>> res1 = new ArrayList<>();
        Long pre = System.currentTimeMillis();
        submitThreads(numThreads1, countDownLatch1,
                        executor1, res1, 1, 90, (int)(numRuns * 0.2));
        countDownLatch1.await();
        // Phase 2
        System.out.println("Phase 2 starts!");
        int numThreads2 = numThreads;
        CountDownLatch countDownLatch2 = new CountDownLatch((int)(numThreads2 * 0.2));
        List<Future<ThreadInfo>> res2 = new ArrayList<>();
        submitThreads(numThreads2, countDownLatch2,
                        executor1, res2, 91, 360,(int)(numRuns * 0.6));
        countDownLatch2.await();
        // Phase 3
        System.out.println("Phase 3 starts!");
        int numThreads3 = numThreads/10 ;
        CountDownLatch countDownLatch3 = new CountDownLatch(numThreads3);
        List<Future<ThreadInfo>> res3 = new ArrayList<>();
        submitThreads(numThreads3, countDownLatch3,
                        executor1, res3, 361, 420,(int)(numRuns * 0.1));
        executor1.shutdown();
        executor1.awaitTermination(3600, TimeUnit.SECONDS);
        Long after = System.currentTimeMillis();
        processFuture(res1);
        processFuture(res2);
        processFuture(res3);
        System.out.println("number of success requests: " + success);
        System.out.println("number of failed requests: " + fail);
        System.out.println("wall time: " + (after - pre));
        System.out.println("throughput: " + (success + fail)*1000/(after - pre));
        processRecordsAndWriteFile();
    }

    /**
     * Submit threads to the thread pool and futures list for collecting return values.
     *
     * @param numThreads1 the number of threads to submit
     * @param countDownLatch1 the countdown latch to pass to the constructor of SingleClientThread
     * @param executor the threads executor
     * @param res1 list of futures
     * @param startTime the start time to pass to the constructor of SingleClientThread
     * @param endTime   the end time to pass to the constructor of SingleClientThread
     * @param numRuns1 number of runs to pass to the constructor of SingleClientThread
     */
    private static void submitThreads(int numThreads1, CountDownLatch countDownLatch1,
                                      ExecutorService executor, List<Future<ThreadInfo>> res1,
                                      int startTime, int endTime,int numRuns1) {
        int perThreadNumSkiers1 = numSkiers / numThreads1;
        int cnt = numSkiers - perThreadNumSkiers1 * numThreads1;
        int startId = 1;
        for (int i = 0; i < numThreads1; i++) {
            int endId;
            if (i < cnt) {
                endId = startId + perThreadNumSkiers1;
            } else {
                endId = startId + perThreadNumSkiers1 - 1;
            }
            Callable<ThreadInfo> t = new SingleClientThread(countDownLatch1,
                                                            startId, endId, startTime, endTime,
                                                            numLifts, numRuns1, addrAndPort);
            res1.add(executor.submit(t));
            startId = endId + 1;
        }
    }


    /**
     * Process the return values from threads.
     *
     * @param list the future list for receiving return values
     */
    private static void processFuture(List<Future<ThreadInfo>> list) throws ExecutionException, InterruptedException {
        for (Future<ThreadInfo> future: list) {
            ThreadInfo info = future.get();
            results.add(info.latencyMap);
            success += info.numSuccess;
            fail += info.numFail;
            if (info.minLatency < minLatency) {
                minLatency = info.minLatency;
            }
            if (info.maxLatency > maxLatency) {
                maxLatency = info.maxLatency;
            }
            long tStart = info.start;
            long tEnd = info.end;
            if ( tStart< startTime) {
                startTime = tStart;
            }
            if (tEnd > endTime) {
                endTime = tEnd;
            }
        }
    }

    /**
     * Process and sort the records of each request and write a CSV file containing all the information.
     * This function would also call other functions to generate a time graph and other statistics.
     */
    private static void processRecordsAndWriteFile() throws IOException {
        File oldObj = new File( numThreads+"_threads_records.csv");
        oldObj.delete();
        CSVWriter writer = new CSVWriter(new FileWriter(numThreads +"_threads_records.csv"));
        String[] header = {"star time", "request type", "latency", "response code"};
        writer.writeNext(header);
        HashMap<Long, List<long[]>> timeRecords = new HashMap<>();
        int[] latencies = new int[(int)maxLatency + 1];
        long total = 0;
        int cnt = 0;
        for (HashMap<Long, long[]> latencyMap: results) {
            for (Long k: latencyMap.keySet()) {
                timeRecords.put(k, timeRecords.getOrDefault(k,new ArrayList<>()));
                timeRecords.get(k).add(latencyMap.get(k));
                long index = latencyMap.get(k)[0];
                latencies[(int)index]++;
                cnt++;
                total += index;
            }
        }
        createTimeGraphFromMap(timeRecords);
        calculateMeanMedianP99(latencies, (double) total, cnt);
        for (long i = startTime; i <= endTime; i++) {
            List<long[]> recordList = timeRecords.get(i);
            if (recordList != null) {
                for (long[] record: recordList) {
                    String[] line = {String.valueOf(i), "POST", String.valueOf(record[0]),String.valueOf(record[1])};
                    writer.writeNext(line);
                }
            }
        }
        writer.close();
    }

    /**
     * Calculate the mean , median and P99 response time.
     *
     * @param latencies the array containing response time information (counting information)
     * @param total the sum of all the response time
     * @param cnt the total number of records
     */
    private static void calculateMeanMedianP99(int[] latencies, double total, int cnt) {
        double mean = total / (double) cnt;
        mean = (double) Math.round(mean * 10) / 10;
        System.out.println("mean response time: " + mean + " milliseconds");
        int index1 = (cnt + 1)/2;
        int median1 = 0;
        int index2 = (cnt + 2)/2;
        int median2 = 0;
        int index99Percent = (int)(cnt * 0.99);
        int latency99 = 0;
        int acc = 0;
        for (int i = 0; i < latencies.length; i++) {
            acc += latencies[i];
            if (acc >= index1) {
                median1 = i;
            }
            if (acc >= index2) {
                median2 = i;
                double median = ((double) (median1 + median2)) / 2;
                System.out.println("median response time: " + median + " milliseconds");
                break;
            }
        }
        acc = 0;
        for (int i = latencies.length - 1; i >= 0; i--) {
            acc += latencies[i];
            if (acc >= cnt - index99Percent) {
                latency99 = i;
                System.out.println("P99 response time: " + latency99 + " milliseconds");
                break;
            }
        }
        System.out.println("min response time: " + minLatency + " milliseconds");
        System.out.println("max response time: " + maxLatency + " milliseconds");
    }

    /**
     * Process records and create a time graph for all the records.
     * It would calculate the average response time in every second.
     * @param timeRecords the map containing records information (key (millisecond) -> number of records in that millisecond)
     */
    private static void createTimeGraphFromMap(HashMap<Long, List<long[]>> timeRecords) throws IOException {
        int[] sumRecords = new int[(int)(endTime - startTime)/1000 + 1];
        int[] cntRecords = new int[(int)(endTime - startTime)/1000 + 1];
        for (Long k: timeRecords.keySet()) {
            List<long[]> list = timeRecords.get(k);
            long sum = 0;
            for (int i = 0; i < list.size(); i++) {
                sum += list.get(i)[0];
            }
            sumRecords[(int)((k - startTime)/1000)] += sum;
            cntRecords[(int)((k - startTime)/1000)] += list.size();
        }
        for (int i = 0; i < sumRecords.length; i++) {
            if (cntRecords[i] != 0) {
                sumRecords[i] = sumRecords[i] / cntRecords[i];
            }
        }
        ImageCreator.createTimeGraph(sumRecords, String.valueOf(numThreads) + " Threads");
    }

    /**
     * validate number range of arguments
     *
     * @param argsRec the parsed numeric arguments
     */
    private static boolean validateCommands(Integer[] argsRec) {
        if (argsRec[0] == null || argsRec[0] < 1 || argsRec[0] > 1024) return false;
        if (argsRec[1] == null || argsRec[1] < 1 || argsRec[1] > 100000) return false;
        if (argsRec[2] == null || argsRec[2] < 5 || argsRec[2] > 60) return false;
        if (argsRec[3] == null || argsRec[3] < 1 || argsRec[3] > 20) return false;
        return true;
    }


}

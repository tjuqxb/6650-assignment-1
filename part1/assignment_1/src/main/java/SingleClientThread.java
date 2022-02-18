import com.google.gson.Gson;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import domain.SkierPost;

public class SingleClientThread implements Callable<ThreadInfo> {
    CountDownLatch countDownLatch;
    int startId;
    int endId;
    int startTime;
    int endTime;
    int numLifts;
    int numRunsAverage;
    int success = 0;
    int fail = 0;
    URLBuilder urlBuilder;
    RandInfoCreator randInfoCreator;
    private static final Logger LOG = LoggerFactory.getLogger(SingleClientThread.class);

//    public static void main(String[] args) throws IOException {
//        CountDownLatch countDownLatch = new CountDownLatch(10);
//        SingleClientThread t = new SingleClientThread(countDownLatch ,
//                1, 1, 1, 10, 10, 10000, "http://34.216.68.235/LAB2_2_war");
//        long before = System.currentTimeMillis();
//        t.call();
//        long after = System.currentTimeMillis();
//        // latency = 28 ms
//        // 23 ms
//        // service = 1 - 5 ms
//        System.out.println("latency: " + (after - before)/10000);
//    }

    public SingleClientThread(CountDownLatch countDownLatch,
                              int startId, int endId,
                              int startTime, int endTime,
                              int numLifts,
                              int numRunsAverage,
                              String url) {
        this.countDownLatch = countDownLatch;
        this.startId = startId;
        this.endId = endId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.numLifts = numLifts;
        this.numRunsAverage = numRunsAverage;
        this.randInfoCreator = new RandInfoCreator(startId, endId, startTime, endTime, numLifts);
        this.urlBuilder = new URLBuilder(url);
    }

    /**
     *  This function runs in multi threads and would return some value containing recorded information from running.
     *  It would call other functions to generate random information and make POST requests to the specified URL.
     *  It would record the number of successful and failed requests.
     */
    @Override
    public ThreadInfo call() {
        CloseableHttpClient client = HttpClients.createDefault();
        int times = numRunsAverage * (endId - startId + 1);
        for (int i = 0; i < times; i++) {
            try {
                SkierPost skierPost = randInfoCreator.createPost();
                Integer skierId = randInfoCreator.getSkierId();
                Integer resortId = randInfoCreator.getResortId();
                Integer seasonId = randInfoCreator.getSeasonId();
                Integer dayId = randInfoCreator.getDayId();
                String postURL = urlBuilder.createSkierPostURL(resortId, seasonId, dayId, skierId);
                String json = new Gson().toJson(skierPost);
                StringEntity entity = new StringEntity(json);
                int cnt = 5;
                // at most fail 5 times
                for (int j = 0; j < cnt; j++) {
                    HttpPost post = null;
                    try {
                        post = new HttpPost(postURL);
                        post.setHeader("Accept", "application/json");
                        post.setHeader("Content-type", "application/json");
                        post.setEntity(entity);
                        HttpResponse res = client.execute(post);
                        if (res.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                            //LOG.info(EntityUtils.toString(res.getEntity(), Consts.UTF_8));
                            //LOG.info("" + res.getStatusLine().getStatusCode());
                            // close to avoid getting stuck here
                            res.getEntity().getContent().close();
                            success++;
                            post.releaseConnection();
                            break;
                        } else {
                            //LOG.info(EntityUtils.toString(res.getEntity(), Consts.UTF_8));
                            res.getEntity().getContent().close();
                            post.releaseConnection();
                            if (j == cnt - 1) fail++;
                        }
                    } catch (ConnectTimeoutException e) {
                        post.releaseConnection();
                        e.printStackTrace();
                        if (j == cnt - 1) {
                            fail++;
                        }
                    } catch (Exception e) {
                        if (post != null) {
                            post.releaseConnection();
                        }
                        e.printStackTrace();
                        if (j == cnt - 1) {
                            fail++;
                        }
                    } finally {
                        if (post != null) post.releaseConnection();
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        countDownLatch.countDown();
        return new ThreadInfo(success, fail);
    }
}

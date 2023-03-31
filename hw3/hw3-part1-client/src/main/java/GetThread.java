import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class GetThread implements Runnable {
    // local host:
//    private final static String basePath1 = "http://localhost:8080/hw3_part2_app_war_exploded/matches/";
//    private final static String basePath2 = "http://localhost:8080/hw3_part2_app_war_exploded/stats/";
    // ec2 host:
    private final static String basePath1 = "http://ec2-54-213-194-84.us-west-2.compute.amazonaws.com:8080/hw3-part2-app_war/matches/";
    private final static String basePath2 = "http://ec2-54-213-194-84.us-west-2.compute.amazonaws.com:8080/hw3-part2-app_war/stats/";
    private final static Integer REQUESTS_PER_SECOND = 5;
    private final List<Long> latencies = new ArrayList<>();
    private final CountDownLatch latch;

    public GetThread(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void run() {
        /*
        creates a single-threaded executor that repeatedly executes a task that sends a request five times at a fixed rate of 1 second
         */
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            for (int i = 0; i < REQUESTS_PER_SECOND; i++) {
                // take a timestamp before sending the request
                Long startTime = new Timestamp(System.currentTimeMillis()).getTime();
                String userId = generateUserId();
                String basePath = generateBasePath();
                // send request
                try {
                    makeGetRequest(userId, basePath);
//                    System.out.println("sent" + i  + "th the GET request");
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                // when the HTTP response is received, take another timestamp
                Long endTime = new Timestamp(System.currentTimeMillis()).getTime();
                latencies.add(endTime - startTime);
            }
        }, 0, 1, TimeUnit.SECONDS);

        // wait for all posting threads to finish
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // wait for the executor to finish executing all tasks before shutdown
        try {
            executor.awaitTermination(REQUESTS_PER_SECOND, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
    }

    private void makeGetRequest(String userId, String basePath) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // build the url using base path and user id
        StringBuilder url = new StringBuilder();
        url.append(basePath).append(userId);
        HttpGet httpGet = new HttpGet(url.toString());
        try {
            CloseableHttpResponse response = httpclient.execute(httpGet);
            try {
//                System.out.println("user id: " + userId + "; " + EntityUtils.toString(response.getEntity()));
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }



    protected void processList() {
        if (latencies.isEmpty()) {
            System.out.println("The latencies list is empty");
            return;
        }
        Integer minResponseTime = Math.toIntExact(latencies.stream().min(Long::compare).orElse(0L));
        Integer maxResponseTime = Math.toIntExact(latencies.stream().max(Long::compare).orElse(0L));
        double meanResponseTime = latencies.stream().mapToLong(Long::longValue).average().orElse(0.0);
        System.out.println("The min response time of GET (millisecs): " + minResponseTime);
        System.out.println("The max response time of GET (millisecs): " + maxResponseTime);
        System.out.println("The mean response time of GET requests (millisecs): " + meanResponseTime);
    }

    private static String generateBasePath() {
        String[] arr = {basePath1, basePath2};
        Random random = new Random();
        int select = random.nextInt(arr.length);
        return arr[select];
    }


    private static String generateUserId() {
        String swiper = Integer.toString(generateNumberInRange(100, 500));
        return swiper;
    }

    private static Integer generateNumberInRange(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }


}

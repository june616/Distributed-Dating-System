import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;

import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;

public class ThreadSingle implements Runnable {
    // local host:
//    private static String basePath = "http://localhost:8080/part2_war_exploded/";
    // ec2 host:
//    private static String basePath = "http://ec2-35-87-48-12.us-west-2.compute.amazonaws.com:8080/part2_war/";
    // load balancer:
    private static String basePath = "http://hw2-servlets-load-balancer-2122426993.us-west-2.elb.amazonaws.com:8080/part2_war/";
    private Integer REQUEST_PER_THREAD;
    private BlockingDeque<SwipeData> bq;
    private SwipeApi swipeApi;
    private CountDownLatch countDownLatch;

    public ThreadSingle(Integer REQUEST_PER_THREAD, BlockingDeque<SwipeData> bq, CountDownLatch countDownLatch) {
        this.REQUEST_PER_THREAD = REQUEST_PER_THREAD;
        this.bq = bq;
        this.countDownLatch = countDownLatch;
        this.swipeApi = new SwipeApi();
        this.swipeApi.getApiClient().setBasePath(basePath);
    }

    @Override
    public void run() {
        for (int i = 0; i < REQUEST_PER_THREAD; i++) {
            SwipeData swipeData = null;
            SwipeDetails swipeDetails = new SwipeDetails();

            try {
                swipeData = this.bq.take();
                swipeDetails.setSwiper(swipeData.getSwiper());
                swipeDetails.setSwipee(swipeData.getSwipee());
                swipeDetails.setComment(swipeData.getComment());
            } catch (InterruptedException e) {
                System.out.println("Blocking queue is empty, cannot take");
            }

            // Interact with this.swipeApi
            ApiResponse<Void> response = null;
            int max_retry = 5;
            // retry at most 5 times
            for (int j = 0; j <= max_retry; j++) {
                try {
                    response = this.swipeApi.swipeWithHttpInfo(swipeDetails, swipeData.getSwipe());
                    if (response.getStatusCode() == HttpServletResponse.SC_OK) {
                        ThreadPool.atomicCount.getAndIncrement();
                        break;
                    }
                } catch (ApiException e) {
                    System.out.println("Error. Retrying times: " + j);
                    if (j == max_retry) {
                        System.out.println("Failed execution: " + e.getMessage());
                        System.out.println("Retried times: " + j);
                    }
                }
            }
        }
        this.countDownLatch.countDown();
    }
}



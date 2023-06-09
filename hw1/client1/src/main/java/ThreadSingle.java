zimport io.swagger.client.ApiException;
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
//    private static String basePath = "http://localhost:8080/SwipeServlet_war_exploded/";
    // ec2 host:
    private static String basePath = "http://ec2-54-185-42-81.us-west-2.compute.amazonaws.com:8080/SwipeServlet_war/";
    private Integer requestPerThread = 10000;
    private BlockingDeque<SwipeData> bq;
    private SwipeApi swipeApi;
    private CountDownLatch countDownLatch;

    public ThreadSingle(BlockingDeque<SwipeData> bq, CountDownLatch countDownLatch) {
        this.bq = bq;
        this.countDownLatch = countDownLatch;
        this.swipeApi = new SwipeApi();
        this.swipeApi.getApiClient().setBasePath(basePath);
    }

    @Override
    public void run() {
        for (int i = 0; i < requestPerThread; i++) {
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


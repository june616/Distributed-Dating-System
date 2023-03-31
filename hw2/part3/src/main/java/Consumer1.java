import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.concurrent.*;

public class Consumer1 {
    private final static String QUEUE_NAME = "fanout_queue_1";
    private final static String FANOUT_EXCHANGE = "fanout_exchange";
    private final static Integer NUM_THREADS = 50;
    private static Integer REQUEST_PER_THREAD = 1;

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        // key: Swiper id, value: LikeData object
        ConcurrentHashMap<String, LikeData> map = new ConcurrentHashMap<>();
        ConnectionFactory factory = new ConnectionFactory();

        // local host
//        factory.setHost("localhost");
        // remote host -> connect to servlet 1's RMQ, change every time based on servlet 1's IP address
        // update and generate part3.jar every time
        factory.setHost("54.190.32.46");
        factory.setPort(5672);
        factory.setUsername("test");
        factory.setPassword("test");

        Connection connection = factory.newConnection();
        CountDownLatch countDownLatch = new CountDownLatch(NUM_THREADS);

        // TODO: use thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; i++) {
            Runnable runnable = new Rec1_Single(FANOUT_EXCHANGE, QUEUE_NAME, connection, REQUEST_PER_THREAD, map, factory, countDownLatch);
            executorService.execute(runnable);
//            new Thread(new Rec1_Single(FANOUT_EXCHANGE, QUEUE_NAME, connection, REQUEST_PER_THREAD, map, factory, countDownLatch)).start();
        }
        countDownLatch.await();
        executorService.shutdown();

    }

}

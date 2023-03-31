import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.*;

public class ThreadPool {
    protected static AtomicInteger atomicCount = new AtomicInteger(0);
    private static Integer TOTAL_REQUEST = 500000;
    private static Integer NUM_THREADS = 50;
    private static Integer REQUEST_PER_THREAD = TOTAL_REQUEST / NUM_THREADS;

    public static void main(String[] args) throws InterruptedException, IOException {
        // create blocking queue
        BlockingDeque<SwipeData> bq = new LinkedBlockingDeque<>(NUM_THREADS);
        // create producer
        Producer producer = new Producer(TOTAL_REQUEST, bq);
        Thread producerThread = new Thread(producer);
        producerThread.start();
        // create consumer
        CountDownLatch completed = new CountDownLatch(NUM_THREADS);

        Long startTime = new Timestamp(System.currentTimeMillis()).getTime();

        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; i++) {
            Runnable runnable = new ThreadSingle(REQUEST_PER_THREAD, bq, completed);
            executorService.execute(runnable);
        }
        completed.await();
        executorService.shutdown();

        // print metrics related to requests
        Integer successRequests = atomicCount.get();
        System.out.println("Number of successful requests: " + successRequests);

        Integer failRequests = TOTAL_REQUEST - successRequests;
        System.out.println("Number of unsuccessful requests: " + failRequests);

        Long endTime = new Timestamp(System.currentTimeMillis()).getTime();
        double millisToSeconds = 0.001;
        double lastingTime = (endTime - startTime) * millisToSeconds;
        System.out.println("Total run time for all phases to complete (seconds): " + lastingTime);

        double throughput = TOTAL_REQUEST / lastingTime;
        System.out.println("Total throughput in requests per second: " + throughput);
    }
}


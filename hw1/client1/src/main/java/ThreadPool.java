import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.*;

public class ThreadPool {
    protected static AtomicInteger atomicCount = new AtomicInteger(0);
    private static Integer NUMTHREADS = 50;
    private static Integer totalRequests = 500000;

    public static void main(String[] args) throws InterruptedException, IOException {
            // create blocking queue
            BlockingDeque<SwipeData> bq = new LinkedBlockingDeque<>(NUMTHREADS);
            // create producer
            Producer producer = new Producer(bq);
            Thread producerThread = new Thread(producer);
            producerThread.start();
            // create consumer
            CountDownLatch completed = new CountDownLatch(NUMTHREADS);

            Long startTime = new Timestamp(System.currentTimeMillis()).getTime();

            ExecutorService executorService = Executors.newFixedThreadPool(NUMTHREADS);
            for (int i = 0; i < NUMTHREADS; i++) {
                Runnable runnable = new ThreadSingle(bq, completed);
                executorService.execute(runnable);
            }
            completed.await();
            executorService.shutdown();

            // print metrics related to requests
            Integer successRequests = atomicCount.get();
            System.out.println("Number of successful requests: " + successRequests);

            Integer failRequests = totalRequests - successRequests;
            System.out.println("Number of unsuccessful requests: " + failRequests);

            Long endTime = new Timestamp(System.currentTimeMillis()).getTime();
            double millisToSeconds = 0.001;
            double lastingTime = (endTime - startTime) * millisToSeconds;
            System.out.println("Total run time for all phases to complete (seconds): " + lastingTime);

            double throughput = totalRequests / lastingTime;
            System.out.println("Total throughput in requests per second: " + throughput);
    }
}

import Client.PostDataGenerator;
import DataModel.Record;
import DataModel.SwipeData;
import Metric.RecordListProcess;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    protected static AtomicInteger atomicCount = new AtomicInteger(0);
    private static Integer TOTAL_REQUEST = 500000;
    private static Integer NUM_THREADS = 50;
    private static Integer REQUEST_PER_THREAD = TOTAL_REQUEST / NUM_THREADS;

    public static void main(String[] args) throws InterruptedException, IOException {
        // create blocking queue
        BlockingDeque<SwipeData> bq = new LinkedBlockingDeque<>(NUM_THREADS);
        // create list storing Record objects
        List<Record> recordList = Collections.synchronizedList(new ArrayList<>());
        // create postDataGenerator
        PostDataGenerator postDataGenerator = new PostDataGenerator(TOTAL_REQUEST, bq);
        Thread postDataGeneratorThread = new Thread(postDataGenerator);
        postDataGeneratorThread.start();
        CountDownLatch completed = new CountDownLatch(NUM_THREADS);

        Long startTime = new Timestamp(System.currentTimeMillis()).getTime();

        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; i++) {
            Runnable runnable = new PostClient(REQUEST_PER_THREAD, bq, recordList, completed);
            executorService.execute(runnable);
        }
        // TODO: Create and start the GetThread
        GetThread getThread = new GetThread(completed);
        Thread getThreadThread = new Thread(getThread);
        getThreadThread.start();

        completed.await();
        executorService.shutdown();

        // TODO: shutdown the GetThread, print metrics related to GET response time
        getThreadThread.join();
        getThread.processList();

        Integer successRequests = atomicCount.get();
        System.out.println("Number of successful POST requests: " + successRequests);

        Integer failRequests = TOTAL_REQUEST - successRequests;
        System.out.println("Number of unsuccessful POST requests: " + failRequests);

        Long endTime = new Timestamp(System.currentTimeMillis()).getTime();
        double millisToSeconds = 0.001;
        double lastingTime = (endTime - startTime) * millisToSeconds;
        System.out.println("Total run time for all phases to complete (seconds): " + lastingTime);

        // print metrics related to POST response time
        RecordListProcess process = new RecordListProcess(recordList);
        process.execute();

        double throughput = TOTAL_REQUEST / lastingTime;
        System.out.println("Total throughput in requests per second: " + throughput);
    }
}


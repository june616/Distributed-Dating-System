package consumer1;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.*;

public class Consumer1 {
    // localhost
    private final static String BOOTSTRAP_SERVERS = "localhost:9092";
    // cloud
//    private final static String BOOTSTRAP_SERVERS = "ec2-35-91-242-100.us-west-2.compute.amazonaws.com:9092";
    private final static String TOPIC_NAME = "swipe";
    private static final String GROUP_ID = "consumer1";
    private final static Integer NUM_THREADS = 200;

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        Properties prop = new Properties();
        prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        prop.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        prop.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        prop.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(200);
        poolConfig.setMaxIdle(200);
        poolConfig.setMinIdle(40);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        JedisPool jedisPool = new JedisPool(poolConfig, "localhost", 6379);

        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; i++) {
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(prop);
            consumer.subscribe(Collections.singleton(TOPIC_NAME));
//            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
            Runnable runnable = new ConsumerWorker1(jedisPool, consumer);
            executorService.execute(runnable);
        }
        executorService.shutdown();
    }
}

package consumer2;

import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.time.Duration;

public class ConsumerWorker2 implements Runnable {
    private JedisPool jedisPool;
    private KafkaConsumer<String, String> consumer;

    public ConsumerWorker2(JedisPool jedisPool, KafkaConsumer<String, String> consumer) {
        this.jedisPool = jedisPool;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        Gson gson = new Gson();
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    String message = record.value();
                    System.out.println(" [x] Received '" + message + "'");
                    SwipeData swipeData = gson.fromJson(message, SwipeData.class);
                    String swiperID = swipeData.getSwiper();
                    String swipeeID = swipeData.getSwipee();
                    // only process when swipe right
                    String swipe = swipeData.getSwipe();
                    try (Jedis jedis = this.jedisPool.getResource()) {
                        if (swipe.equals("right")) {
                            // For GET request: /matches/{userID}/ -> return a maximum of 100 matches for a user
                            // Schema: user_likes_ids:<userId>:likerSet
                            String userLikesKey = "user_likes_ids:" + swiperID;
                            Long userLikesSize = jedis.scard(userLikesKey);
                            if (userLikesSize < 100) {
                                jedis.sadd(userLikesKey, swipeeID);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (WakeupException e) {
            // Ignore for shutdown
        } finally {
            consumer.close();
        }
    }
}



package consumer1;

import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import java.time.Duration;

public class ConsumerWorker1 implements Runnable {
    private JedisPool jedisPool;
    private KafkaConsumer<String, String> consumer;

    public ConsumerWorker1(JedisPool jedisPool, KafkaConsumer<String, String> consumer) {
        this.jedisPool = jedisPool;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        Gson gson = new Gson();
        try {
            // a while loop that continuously polls for new messages
            while (true) {
                // receive a ConsumerRecord object that contains the message payload and metadata
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    // Parse the message payload
                    String message = record.value();
                    System.out.println(" [x] Received '" + message + "'");
                    SwipeData swipeData = gson.fromJson(message, SwipeData.class);
                    String swiperID = swipeData.getSwiper();
                    // swipe left - dislike; swipe right - like
                    String swipe = swipeData.getSwipe();
                    try (Jedis jedis = this.jedisPool.getResource()) {
                        Transaction t = jedis.multi();
                        if (swipe.equals("right")) {
                            // For GET request: /stats/{userID}/ -> return number of likes and dislikes for a user
                            // Schema: user_likes_num:<userId>:numberOfLikes
                            t.incr("user_likes_num:" + swiperID);
                        } else {
                            // For GET request: /stats/{userID}/ -> return number of likes and dislikes for a user
                            // Schema: user_dislikes_num:<userId>:numberOfDislikes
                            t.incr("user_dislikes_num:" + swiperID);
                        }
                        t.exec();
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

    /*
    Use consumer.commitSync() to commit the offset of the last consumed message: This ensures that the consumer won't consume the same message multiple times in case of failure or restart. Note that we're using commitSync() instead of commitAsync() here to make sure that the offset is committed before moving on to the next message.
     */

}


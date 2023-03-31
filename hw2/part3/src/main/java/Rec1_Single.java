import com.google.gson.Gson;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class Rec1_Single implements Runnable {
    private String FANOUT_EXCHANGE;
    private String QUEUE_NAME;
    private Connection connection;
    private Integer REQUEST_PER_THREAD;
    private ConcurrentHashMap<String, LikeData> map;
    private ConnectionFactory factory;
    private CountDownLatch countDownLatch;


    public Rec1_Single(String FANOUT_EXCHANGE, String QUEUE_NAME, Connection connection, Integer REQUEST_PER_THREAD, ConcurrentHashMap<String, LikeData> map, ConnectionFactory factory, CountDownLatch countDownLatch) {
        this.FANOUT_EXCHANGE = FANOUT_EXCHANGE;
        this.QUEUE_NAME = QUEUE_NAME;
        this.connection = connection;
        this.REQUEST_PER_THREAD = REQUEST_PER_THREAD;
        this.map = map;
        this.factory = factory;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        for (int i = 0; i < REQUEST_PER_THREAD; i++) {
            Channel channel;
            try {
                channel = connection.createChannel();
                channel.exchangeDeclare(FANOUT_EXCHANGE, BuiltinExchangeType.FANOUT);
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                channel.queueBind(QUEUE_NAME, FANOUT_EXCHANGE, "");

                System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

                Gson gson = new Gson();
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    System.out.println(" [x] Received '" + message + "'");
                    SwipeData swipeData = gson.fromJson(message, SwipeData.class);
                    // TODO: process swipeData
                    processSwipeDataLikes(swipeData);
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                };

                channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.countDownLatch.countDown();
    }

    private void processSwipeDataLikes(SwipeData swipeData) {
        String swiperID = swipeData.getSwiper();
        String swipe = swipeData.getSwipe();
        // swipe left - dislike; swipe right - like
        if (map.containsKey(swiperID)) {
            LikeData existData = map.get(swiperID);
            if (swipe.equals("right")) {
                Integer existLikesNum = existData.getNumLikes();
                existData.setNumLikes(existLikesNum + 1);
            } else {
                Integer existDislikesNum = existData.getNumDislikes();
                existData.setNumDislikes(existDislikesNum + 1);
            }
        } else {
            LikeData newLikeData;
            if (swipe.equals("right")) {
                newLikeData = new LikeData(swiperID, 1, 0);
            } else {
                newLikeData = new LikeData(swiperID, 0, 1);
            }
            map.put(swiperID, newLikeData);
        }
    }

}

import com.google.gson.Gson;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class Rec2_Single implements Runnable {
    private String FANOUT_EXCHANGE;
    private String QUEUE_NAME;
    private Connection connection;
    private Integer REQUEST_PER_THREAD;
    private ConcurrentHashMap<String, MatchData> map;
    private ConnectionFactory factory;
    private CountDownLatch countDownLatch;

    public Rec2_Single(String FANOUT_EXCHANGE, String QUEUE_NAME, Connection connection, Integer REQUEST_PER_THREAD, ConcurrentHashMap<String, MatchData> map, ConnectionFactory factory, CountDownLatch countDownLatch) {
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
                    processSwipeDataMatches(swipeData);
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                };

                channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.countDownLatch.countDown();
    }

    private void processSwipeDataMatches(SwipeData swipeData) {
        String swiperID = swipeData.getSwiper();
        String swipeeID = swipeData.getSwipee();
        String swipe = swipeData.getSwipe();
        // only process when swipe right
        if (swipe.equals("right")) {
            if (map.containsKey(swiperID)) {
                MatchData existMatchData = map.get(swiperID);
                existMatchData.updateSwipeeQueue(swipeeID);
            } else {
                Queue<String> newSwipeeList = new LinkedList<>();
                MatchData newMatchData = new MatchData(swiperID, newSwipeeList);
                newMatchData.updateSwipeeQueue(swipeeID);
                map.put(swiperID, newMatchData);
            }
        }
    }

}

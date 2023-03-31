package Client;

import DataModel.SwipeData;

import java.util.Random;
import java.util.concurrent.BlockingDeque;

public class PostDataGenerator implements Runnable {

    private Integer TOTAL_REQUEST;
    private BlockingDeque<SwipeData> bq;

    public PostDataGenerator(Integer TOTAL_REQUEST, BlockingDeque<SwipeData> bq) {
        this.TOTAL_REQUEST = TOTAL_REQUEST;
        this.bq = bq;
    }

    @Override
    public void run() {
        for (int i = 0; i < TOTAL_REQUEST; i++) {
            try {
                this.bq.put(generateData());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private SwipeData generateData() {
        String swipe = generateLeftOrRight();
        String swiper = Integer.toString(generateNumberInRange(100, 500));
        String swipee = Integer.toString(generateNumberInRange(100, 500));
        String comment = generateComment(swipe);

        SwipeData swipeData = new SwipeData(swipe, swiper, swipee, comment);
        return swipeData;
    }

    private String generateLeftOrRight() {
        String[] arr = {"left", "right"};
        Random random = new Random();
        int select = random.nextInt(arr.length);
        return arr[select];
    }

    private String generateComment(String swipe) {
        String[] arr = {"not my type", "totally my type"};
        if (swipe.equals("right")) {
            return arr[1];
        } else {
            return arr[0];
        }
    }

    private Integer generateNumberInRange(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}

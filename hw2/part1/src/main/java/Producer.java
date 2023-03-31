import java.util.Random;
import java.util.concurrent.BlockingDeque;

public class Producer implements Runnable {

    private Integer TOTAL_REQUEST;
    private BlockingDeque<SwipeData> bq;

    public Producer(Integer TOTAL_REQUEST, BlockingDeque<SwipeData> bq) {
        this.TOTAL_REQUEST = TOTAL_REQUEST;
        this.bq = bq;
    }


    @Override
    public void run() {
        for (int i = 0; i < TOTAL_REQUEST; i++) {
            try {
                this.bq.put(generateData());
//                System.out.println("Producer finished");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private SwipeData generateData() {
        String swipe = generateLeftOrRight();
        String swiper = Integer.toString(generateNumberInRange(1, 5000));
        String swipee = Integer.toString(generateNumberInRange(1, 1000000));
        String comment = generateComment();

        SwipeData swipeData = new SwipeData(swipe, swiper, swipee, comment);
        return swipeData;
    }

    private String generateLeftOrRight() {
        String [] arr = {"left", "right"};
        Random random = new Random();
        int select = random.nextInt(arr.length);
        return arr[select];
    }

    private String generateComment() {
        int targetLength = 256;
        String upperAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerAlphabet = "abcdefghijklmnopqrstuvwxyz";

        // combine all strings
        String alphabet = upperAlphabet + lowerAlphabet;

        // create random string builder
        StringBuilder sb = new StringBuilder();

        // create an object of Random class
        Random random = new Random();

        for (int i = 0; i < targetLength; i++) {
            int index = random.nextInt(alphabet.length());
            char randomChar = alphabet.charAt(index);
            sb.append(randomChar);
        }

        String randomComment = sb.toString();
        return randomComment;
    }

    private Integer generateNumberInRange(int min, int max){
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}


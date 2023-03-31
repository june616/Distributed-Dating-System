import java.util.LinkedList;
import java.util.Queue;

public class MatchData {
    private String swiper;
    private Queue<String> matchSwipee;

    public MatchData(String swiper, Queue<String> matchSwipee) {
        this.swiper = swiper;
        this.matchSwipee = matchSwipee;
    }

    public String getSwiper() {
        return swiper;
    }

    public void setSwiper(String swiper) {
        this.swiper = swiper;
    }

    public Queue<String> getMatchSwipee() {
        return matchSwipee;
    }

    public void updateSwipeeQueue(String newSwipee) {
        if (matchSwipee.size() == 100) {
            matchSwipee.poll();
        }
        matchSwipee.offer(newSwipee);
    }

    public void setMatchSwipee(Queue<String> matchSwipee) {
        this.matchSwipee = matchSwipee;
    }

    @Override
    public String toString() {
        return "MatchData{" +
                "swiper='" + swiper + '\'' +
                ", matchSwipee=" + matchSwipee +
                '}';
    }

//    public static void main(String[] args) {
//        MatchData matchData = new MatchData("1", new LinkedList<>());
//        matchData.updateSwipeeQueue("like 1");
//        System.out.println(matchData.toString());
//    }
}

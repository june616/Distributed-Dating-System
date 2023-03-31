public class LikeData {
    private String swiper;
    private Integer numLikes;
    private Integer numDislikes;

    public LikeData(String swiper, Integer numLikes, Integer numDislikes) {
        this.swiper = swiper;
        this.numLikes = numLikes;
        this.numDislikes = numDislikes;
    }

    public String getSwiper() {
        return swiper;
    }

    public void setSwiper(String swiper) {
        this.swiper = swiper;
    }

    public Integer getNumLikes() {
        return numLikes;
    }

    public void setNumLikes(Integer numLikes) {
        this.numLikes = numLikes;
    }

    public Integer getNumDislikes() {
        return numDislikes;
    }

    public void setNumDislikes(Integer numDislikes) {
        this.numDislikes = numDislikes;
    }

    @Override
    public String toString() {
        return "LikeData{" +
                "swiper='" + swiper + '\'' +
                ", numLikes=" + numLikes +
                ", numDislikes=" + numDislikes +
                '}';
    }
}

package Redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RedisHelper {
    private JedisPool jedisPool;

    public RedisHelper() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(200);
        poolConfig.setMaxIdle(200);
        poolConfig.setMinIdle(40);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        this.jedisPool = new JedisPool(poolConfig, "localhost", 6379);
    }

    public int getNumberOfLikes(String swiperID) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String likesKey = "user_likes_num:" + swiperID;
            String numberOfLikesString = jedis.get(likesKey);
            int numberOfLikes = numberOfLikesString != null ? Integer.parseInt(numberOfLikesString) : 0;
            return numberOfLikes;
        }
    }

    public int getNumberOfDislikes(String swiperID) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String dislikesKey = "user_dislikes_num:" + swiperID;
            String numberOfDislikesString = jedis.get(dislikesKey);
            int numberOfDislikes = numberOfDislikesString != null ? Integer.parseInt(numberOfDislikesString) : 0;
            return numberOfDislikes;
        }
    }

    public List<String> getMatchIdList(String swiperID) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String matchKey = "user_likes_ids:" + swiperID;
            Set<String> matchIdSet = jedis.smembers(matchKey);
            List<String> matchIdList = new ArrayList<>();
            matchIdList.addAll(matchIdSet);
            return matchIdList;
        }
    }
}

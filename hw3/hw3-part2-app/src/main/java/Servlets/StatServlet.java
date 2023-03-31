package Servlets;

import Redis.RedisHelper;
import com.google.gson.JsonObject;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

@WebServlet(name = "Servlets.StatServlet", value = "/stats/*")
public class StatServlet extends HttpServlet {
    private RedisHelper redisHelper;
    private Cache<String, int[]> cache;

    @Override
    public void init() {
        this.redisHelper = new RedisHelper();
        // option 2: using cache
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(100)
                .build();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        JsonObject resObj = new JsonObject();

        // get the url
        String urlPath = req.getPathInfo();
        // url validation
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing parameters");
            return;
        }
        String[] urlParts = urlPath.split("/");
        Boolean validUrl = isUrlValid(urlParts);

        if (validUrl == false) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("parameters invalid");
            return;
        } else {
            String userID = urlParts[1];
            // option 1: no cache
            /*
            int numberOfLikes = this.redisHelper.getNumberOfLikes(userID);
            int numberOfDislikes = this.redisHelper.getNumberOfDislikes(userID);
             */
            // option 2: using cache
            int[] likesAndDislikes = cache.get(userID, k -> {
                int[] values = new int[2];
                values[0] = redisHelper.getNumberOfLikes(k);
                values[1] = redisHelper.getNumberOfDislikes(k);
                return values;
            });
            int numberOfLikes = likesAndDislikes[0];
            int numberOfDislikes = likesAndDislikes[1];
            res.setStatus(HttpServletResponse.SC_OK);
            resObj.addProperty("numLikes", numberOfLikes);
            resObj.addProperty("numDislikes", numberOfDislikes);
            res.getWriter().write(resObj.toString());
        }
    }


    private boolean isUrlValid(String[] urlParts) {
        // urlPath  = "/[userID]" -> urlParts = [, [userID]]
        // if length of parameters = 2 and the second parameter is numeric
        if (urlParts.length == 2){
            try {
                int userID = Integer.parseInt(urlParts[1]);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }
}

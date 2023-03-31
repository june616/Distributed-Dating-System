import com.google.gson.Gson;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.swagger.client.model.SwipeDetails;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@WebServlet(name = "SwipeServlet", value = "/swipe/*")
public class SwipeServlet extends HttpServlet {
    private final static String FANOUT_QUEUE_1 = "fanout_queue_1";
    private final static String FANOUT_QUEUE_2 = "fanout_queue_2";
    private final static String FANOUT_EXCHANGE = "fanout_exchange";
    private final static Integer POOL_SIZE = 20;
    private Connection connection;
    private RMQChannelPool rmqChannelPool;


    @Override
    public void init() throws ServletException {
        ConnectionFactory factory = new ConnectionFactory();
        // connect to a RabbitMQ node on the localhost
        // for servlet 1, connect to local RMQ (part2_war), no change
//        factory.setHost("localhost");
        // connect to remote RMQ
        // for servlet 2, connect to servlet 1's RMQ (part2_war2), change every time based on servlet 1's IP address
        factory.setHost("54.190.32.46");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("admin");

        try {
            // use ConnectionFactory to create a connection
            connection = factory.newConnection();
            // use connection to create a channel factory
            RMQChannelFactory rmqChannelFactory = new RMQChannelFactory(connection);
            // use channel factory to create a channel pool
            rmqChannelPool = new RMQChannelPool(POOL_SIZE, rmqChannelFactory);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        Gson gson = new Gson();
        // get the url
        String urlPath = request.getPathInfo();

        // get the request body, modify to SwipeDetails object
        String body = request.getReader().lines().collect(Collectors.joining());
        SwipeDetails swipeDetails = gson.fromJson(body, SwipeDetails.class);

        // url validation
        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");
        Boolean validUrl = isUrlValid(urlParts);
        // swipe left or right
        String swipe = urlParts[1];


        // body validation
        if (body == null || body.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("missing request body");
            return;
        }

        Integer swiper = Integer.valueOf(swipeDetails.getSwiper());
        Integer swipee = Integer.valueOf(swipeDetails.getSwipee());
        String comment = swipeDetails.getComment();
        Boolean validBody = isBodyValid(swiper, swipee, comment);

        // return status code
        if (validUrl == false || validBody == false){
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("parameters invalid");
        } else {
            // 1. format the incoming Swipe data
            String dataString = formatSwipeData(gson, swipe, swiper, swipee, comment);
            // 2. send it as a payload to a remote queue
            publishMessage(dataString, body);
            // 3. return success to the client
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("It works! The data string is: " + dataString);
        }
    }

    private String formatSwipeData(Gson gson, String swipe, Integer swiper, Integer swipee, String comment) {
        // wrap url parameters and request body in swipeData
        SwipeData swipeData = new SwipeData(swipe, Integer.toString(swiper), Integer.toString(swipee), comment);
        // convert to string
        String dataString = gson.toJson(swipeData);
        return dataString;
    }

    private void publishMessage(String dataString, String body) throws IOException {
        // borrow a channel from the pool
        Channel channel = rmqChannelPool.borrowObject();
        // declare exchange for the channel
        channel.exchangeDeclare(FANOUT_EXCHANGE, BuiltinExchangeType.FANOUT);
        // create two queues for that channel
        channel.queueDeclare(FANOUT_QUEUE_1, false, false,false,null);
        channel.queueDeclare(FANOUT_QUEUE_2, false, false,false,null);
        // bind the queues with exchange
        channel.queueBind(FANOUT_QUEUE_1, FANOUT_EXCHANGE, "");
        channel.queueBind(FANOUT_QUEUE_2, FANOUT_EXCHANGE, "");

        // send to the queues
        channel.basicPublish(FANOUT_EXCHANGE, "", null, dataString.getBytes(StandardCharsets.UTF_8));
        System.out.println(" [x] Sent '" + body + "'");

        // return the channel back to the pool
        try {
            rmqChannelPool.returnObject(channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isUrlValid(String[] urlPath) {
        // urlPath  = "/left" -> urlParts = [, left]
        // if length of parameters = 2 and the second parameter is 'left' or 'right'
        if (urlPath.length == 2){
            if (urlPath[1].equals("left") || urlPath[1].equals("right")){
                return true;
            }
        }
        return false;
    }

    private boolean isBodyValid(Integer swiper, Integer swipee, String comment){
        /*
        swiper - between 1 and 5000
        swipee - between 1 and 1000000
        comment - random string of 256 characters
         */
        if (swiper < 1 || swiper > 5000){
            return false;
        }
        if (swipee < 1 || swipee > 1000000){
            return false;
        }
        // assuming is length should not be greater than 256
        if (comment.length() > 256) {
            return false;
        }
        return true;
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        String urlPath = request.getPathInfo();
        String urlFullPath = request.getRequestURI();
        System.out.println("get urlFullPath: " + urlFullPath);
        PrintWriter out = response.getWriter();
        out.println("<h1>" + "get urlPath: " + urlPath + "</h1>");
        response.getWriter().write("get urlPath: " + urlPath);
    }
}





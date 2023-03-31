package Servlets;

import DataModel.SwipeData;
import com.google.gson.Gson;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import io.swagger.client.model.SwipeDetails;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@WebServlet(name = "Servlets.SwipeServlet", value = "/swipe/*")
public class SwipeServlet extends HttpServlet {
    private final static Integer POOL_SIZE = 200;
    private final static String BOOTSTRAP_SERVERS = "localhost:9092";
    private final static String TOPIC_NAME = "swipe";
    private KafkaProducer<String, String> producer;
    private ExecutorService executor;


    @Override
    public void init() throws ServletException {
        Properties prop = new Properties();
        prop.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // producer will receive an acknowledgement from the leader replica of the partition once the message is written to its log (set synchronous producers)
        prop.put("acks", "1");
        producer = new KafkaProducer<>(prop);
        executor = Executors.newFixedThreadPool(POOL_SIZE);
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
            executor.submit(() -> publishMessage(dataString, body));
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

    private void publishMessage(String dataString, String body) {
        producer.send(new ProducerRecord<>(TOPIC_NAME, dataString),
                    (metadata, exception) -> {
                        if (exception != null) {
                            System.err.println("Error publishing message: " + exception.getMessage());
                        } else {
                            System.out.println("Message published successfully: " + body);
                        }
                    });
    }

    @Override
    public void destroy() {
        super.destroy();
        producer.close();
        executor.shutdown();
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        String urlPath = request.getPathInfo();
        response.getWriter().write("get urlPath: " + urlPath);
    }
}





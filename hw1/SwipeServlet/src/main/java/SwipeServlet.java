import com.google.gson.Gson;
import io.swagger.client.model.SwipeDetails;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

@WebServlet(name = "SwipeServlet", value = "/swipe/*")
public class SwipeServlet extends HttpServlet {
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        // get the url
        String urlPath = request.getPathInfo();

        // get the request body, modify to SwipeDetails object
        String body = request.getReader().lines().collect(Collectors.joining());
        Gson gson = new Gson();
        SwipeDetails swipeDetails = gson.fromJson(body, SwipeDetails.class);

        // url validation
        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");
        Boolean validUrl = isUrlValid(urlParts);


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
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("It works! The comment is: " + comment);
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
        // TODO: comment - random string of 256 characters?
        // assuming is length should not be greater than 256
        if (comment.length() > 256) {
            return false;
        }
        return true;
    }
}


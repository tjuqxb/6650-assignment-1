import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import domain.SkierPost;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@WebServlet(name = "SkierServlet0",
        value = {"/SkierServlet"})
public class SkierServlet extends HttpServlet {
    private Gson gson = new Gson();
    private static final Logger LOG = LoggerFactory.getLogger(SkierServlet.class);

    public SkierServlet() {
        super();
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(Utils.getReturnMessage(gson,"invalid url"));
            return;
        }
        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!isUrlValid(urlParts, "POST")) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(Utils.getReturnMessage(gson,"invalid url"));
        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream(),"utf-8"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            if (!validatePostJson(sb.toString())) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write(Utils.getReturnMessage(gson,"invalid json POST"));
                return;
            }
            res.setStatus(HttpServletResponse.SC_CREATED);
            // do any sophisticated processing with urlParts which contains all the url params
            res.getWriter().write("It works for POST!");
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(Utils.getReturnMessage(gson,"invalid url"));
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!isUrlValid(urlParts, "GET")) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(Utils.getReturnMessage(gson,"invalid url"));
        } else {
            if (urlParts.length == 3) {
                String resortStr = req.getParameter("resort");
                String skierIDStr = req.getParameter("skierID");
                String seasonIDStr = req.getParameter("season");
                Integer resortID = Utils.parseNum(resortStr);
                Integer skierID = Utils.parseNum(skierIDStr);
                Integer seasonID = Utils.parseNum(skierIDStr);
                if (resortID == null || skierID == null || (seasonIDStr != null && seasonID == null)) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    res.getWriter().write(Utils.getReturnMessage(gson,"invalid parameters"));
                    return;
                }
            }
            res.setStatus(HttpServletResponse.SC_OK);
            // do any sophisticated processing with urlParts which contains all the url params
            res.getWriter().write("It works for GET!");
            res.getWriter().write(urlPath);
        }
    }

    /**
     * Validate the url based on categories.
     *
     * @param urlPath the segmented url path to be validated
     * @param source indicating the category of request, e.g., POST or GET
     * @return a boolean value indicating the url is valid or not
     */
    private boolean isUrlValid(String[] urlPath, String source) {
        // urlPath  = "/LAB2_2_war_exploded/skiers/1/seasons/2019/days/1/skiers/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]

        //  /skiers   /{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID} (GET POST)
        //  /skiers    /{skierID}/vertical (GET)

        if (urlPath.length == 8){
            if (urlPath[0].length() != 0) return false;
            Integer resortID = Utils.parseNum(urlPath[1]);
            if (resortID == null) return false;
            if (!urlPath[2].equals("seasons")) return false;
            Integer seasonID = Utils.parseNum(urlPath[3]);
            if (seasonID == null) return false;
            if (!urlPath[4].equals("days")) return false;
            Integer daysID = Utils.parseNum(urlPath[5]);
            if (daysID == null) return false;
            if (daysID < 1 || daysID > 366) return false;
            if (!urlPath[6].equals("skiers")) return false;
            Integer skierID = Utils.parseNum(urlPath[7]);
            return skierID != null;
        } else if (source.equals("GET") && urlPath.length == 3) {
            if (urlPath[0].length() != 0) return false;
            Integer skierID = Utils.parseNum(urlPath[1]);
            if (skierID == null) return false;
            return urlPath[2].equals("vertical");
        }
        return false;
    }

    /**
     * Validate the JSON string.
     *
     * @param str the JSON string to be validated
     * @return a boolean value indicating whether the JSON string is valid or not
     */
    private boolean validatePostJson(String str) {
        SkierPost post = gson.fromJson(str, SkierPost.class);
        return post.getLiftID() != null && post.getTime() != null && post.getWaitTime() != null;
    }



}

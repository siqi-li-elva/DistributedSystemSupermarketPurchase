import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.swagger.client.model.PurchaseItems;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "PurchaseServlet")
public class PurchaseServlet extends javax.servlet.http.HttpServlet {
  public enum HttpMethod {
    POST,
    GET,
  }

  protected void doPost(javax.servlet.http.HttpServletRequest req,
      javax.servlet.http.HttpServletResponse res)
      throws javax.servlet.ServletException, IOException {
    processHttpMethod(req, res, HttpMethod.POST);
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    processHttpMethod(req, res, HttpMethod.GET);
  }

  private void processHttpMethod(HttpServletRequest req, HttpServletResponse res, HttpMethod method)
      throws IOException {
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)
    try {
      readPurchaseItem(req);
    } catch (Exception e) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

//    System.out.println("url split length: " + urlPath.length());
    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    } else {
      if (method == HttpMethod.GET) {
        res.setStatus(HttpServletResponse.SC_OK);
      }
      if (method == HttpMethod.POST) {
        res.setStatus(HttpServletResponse.SC_CREATED);
      }
      // do any sophisticated processing with urlParts which contains all the url params
      // TODO: process url params in `urlParts`

      String storeID = urlParts[1];
      String customerID = urlParts[3];
      String date = urlParts[5];
      String resBody = "Response Msg -> Customer: " + customerID
          + ", successfully Purchased at Store:" + storeID
          + " @ " + date;
//      System.out.println(resBody);
      JsonObject jsonObject = new JsonObject();
      writeJasonObj(res, resBody, jsonObject);
    }
  }

  private void writeJasonObj(HttpServletResponse res, String resBody, JsonObject jsonObj)
      throws IOException {
    try {
      jsonObj.addProperty("message", resBody);
      res.setContentType("application/json");
      res.getWriter().print(jsonObj.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private PurchaseItems readPurchaseItem(HttpServletRequest req) throws IOException {
    BufferedReader reader = req.getReader();
    String content = readIntoString(reader);

    JsonObject jsonObject = new JsonObject().getAsJsonObject(content);
    Gson gson = new Gson();
    return gson.fromJson(jsonObject, PurchaseItems.class);
  }

  private String readIntoString(BufferedReader reader) throws IOException {
    StringBuilder sb = new StringBuilder();
    String line;
    while( (line = reader.readLine()) != null) {
      sb.append(line);
    }
    return sb.toString();
  }

  private boolean isUrlValid(String[] urlPath) {
    // TODO: validate the request url path according to the API spec
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]

    if (!urlPath[2].equals("customer")
        || !urlPath[4].equals("date")) return false;

    String storeID = urlPath[1];
    String customerID = urlPath[3];
    String date = urlPath[5];

    if (!isValidNumber(storeID) || !isValidNumber(customerID)) return false;
    if (!isValidDate(date)) return false;

    return true;
  }

  private boolean isValidNumber(String ids) {
    if (ids == null || ids.isEmpty()) return false;

    try {
      int digits = Integer.parseInt(ids);
//      System.out.println("digits: " + digits);
    } catch (NumberFormatException e) {
//      e.printStackTrace();
      return false;
    }
    return true;
  }

  private  boolean isValidDate(String date) {
    if (date == null || date.isEmpty()) return false;

    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      Date check = sdf.parse(date);
//      System.out.println("date: " + check.toString());
      if (!date.equals(sdf.format(check))) return false;
    } catch (ParseException e) {
      return false;
//      e.printStackTrace();
    }
    return true;
  }
}


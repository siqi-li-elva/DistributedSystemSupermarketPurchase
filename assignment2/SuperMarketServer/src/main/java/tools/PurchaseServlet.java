package tools;

import com.google.gson.JsonObject;
import dal.DBCPDataSource;
import dal.PurchasesDao;
import io.swagger.client.model.Purchase;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet(name = "tools.PurchaseServlet")
public class PurchaseServlet extends javax.servlet.http.HttpServlet {
  private static DataSource pool = DBCPDataSource.createPool();


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

//     and now validate url path and return the response status code
//     (and maybe also some value if input is valid)
    StringBuilder sb = new StringBuilder();
    String line;
    try {
      BufferedReader reader = req.getReader();
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
    } catch (Exception e) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("not purchase body");
    }
    String[] urlParts = urlPath.split("/");
    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("incorrect url format");
    } else {
      // processing with urlParts which contains all the url params
      String storeID = urlParts[1];
      String customerID = urlParts[3];
      String date = urlParts[5];
      long currTime = System.currentTimeMillis();
      Timestamp time = new Timestamp(currTime);

      if (method == HttpMethod.GET) {
        res.setStatus(HttpServletResponse.SC_OK);
      }
      if (method == HttpMethod.POST) {
        // insert to DB
        PurchasesDao purchasesDao = new PurchasesDao(pool);
        Purchase purchaseBody = getPurchaseBody(req);

        boolean isSuccess = purchasesDao.insertIntoPurchases(UUID.randomUUID().toString(),
            Integer.valueOf(storeID), Integer.valueOf(customerID),
            date, time.toString(), sb.toString());
        purchasesDao.closeConnection();

        // generate response message
        res.setStatus(HttpServletResponse.SC_CREATED);
        String resBody = "Response Msg -> Customer: " + customerID
            + ", successfully Purchased at Store:" + storeID
            + " @ " + date
            + "inserted at" + time.toString()
            + "Success? >> " + isSuccess;
        JsonObject jsonObject = new JsonObject();
        writeJasonObj(res, resBody, jsonObject);

      }
    }
  }

  private Purchase getPurchaseBody(HttpServletRequest req) {
    return null;
  }

  private void writeJasonObj(HttpServletResponse res, String resBody, JsonObject jsonObj)
      throws IOException {
    try {
      jsonObj.addProperty("message", resBody);
      res.setContentType("application/json");
      res.getWriter().write(jsonObj.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

//  private Purchase readPurchaseItem(HttpServletRequest req) throws IOException {
//    BufferedReader reader = req.getReader();
//    String content = readIntoString(reader);
//
//    JsonObject jsonObject = new JsonObject().getAsJsonObject(content);
//    Gson gson = new Gson();
//    return gson.fromJson(jsonObject, Purchase.class);
//  }

//  private String readIntoString(BufferedReader reader) throws IOException {
//    StringBuilder sb = new StringBuilder();
//    String line;
//    while( (line = reader.readLine()) != null) {
//      sb.append(line);
//    }
//    return sb.toString();
//  }

  private boolean isUrlValid(String[] urlPath) {
    // urlParts = [ServerName, storeID, customer, customerID, date, dateInfo]

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


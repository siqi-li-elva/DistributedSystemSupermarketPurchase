package tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import io.swagger.client.model.Purchase;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.PurchaseRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import utils.ChannelPool;
import utils.KafkaProducerObject;
import utils.RabbitMQClient;

@WebServlet(name = "tools.PurchaseServlet")
public class PurchaseServlet extends javax.servlet.http.HttpServlet {
//  private static final ChannelPool channelPool = new ChannelPool();
  private static final KafkaProducerObject kafkaProducer = new KafkaProducerObject("purchase-log");
  
  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  private static final Integer PURCHASE_URL_LENGTH = 6;
  private static final Integer ITEMS_URL_LENGTH = 3;

  public enum HttpMethod {
    POST,
    GET,
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    processHttpPost(req, res);
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    processHTTPGet(req, res);
  }

  private void processHTTPGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();
    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing parameters\n");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)
    if (!isUrlValid(urlParts, HttpMethod.GET)) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("incorrect url format\n");
      return;
    }
    // process the get request to get top item or store results
    String message = "";
    try (RabbitMQClient client = new RabbitMQClient()){
      message = client.call(urlPath);
    } catch (Exception e) {
      e.printStackTrace();
    }
    res.setStatus(HttpServletResponse.SC_OK);
    JsonObject jsonObject = new JsonObject();
    writeJasonObj(res, message, jsonObject);
  }

  private void processHttpPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();
    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing parameters\n");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)
    if (!isUrlValid(urlParts, HttpMethod.POST)) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("incorrect url format\n");
      return;
    }
    // check if have valid content given
    boolean hasContent;
    StringBuilder sb = new StringBuilder();
    String line;
    try {
      BufferedReader reader = req.getReader();
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
      reader.close();
      hasContent = true;
    } catch (Exception e) {
      hasContent = false;
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("not purchase body");
      return;
    }

    if (hasContent) {
      // process the post request
      // processing with urlParts which contains all the url params
      String storeID = urlParts[1];
      String customerID = urlParts[3];
      String date = urlParts[5];
      long currTime = System.currentTimeMillis();
      Timestamp time = new Timestamp(currTime);
      // insert to DB
      Purchase purchase = gson.fromJson(sb.toString(), Purchase.class);
      PurchaseRecord purchaseRecord = new PurchaseRecord(UUID.randomUUID().toString(),
          Integer.valueOf(storeID), Integer.valueOf(customerID), date, time,
          purchase);
      // kafka producer
      kafkaProducer.kafkaProduce(purchaseRecord);

      // rabbitmq producer
//      boolean isSuccess = produce(purchaseRecord);
      // generate response message
      res.setStatus(HttpServletResponse.SC_CREATED);
//      String resBody = "Success? >> " + isSuccess;
//      JsonObject jsonObject = new JsonObject();
//      writeJasonObj(res, resBody, jsonObject);
    }


  }

  private void writeJasonObj(HttpServletResponse res, String resBody, JsonObject jsonObj)
      throws IOException {
    try {
      jsonObj.addProperty("message", resBody);
      res.setContentType("application/json");
      PrintWriter printWriter = res.getWriter();
      printWriter.write(jsonObj.toString());
      printWriter.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }


  private boolean isUrlValid(String[] urlPath, HttpMethod method) {
//     urlParts = [purchase, storeID, customer, customerID, date, dateInfo]
    if (method == HttpMethod.POST) {
      if (urlPath.length != PURCHASE_URL_LENGTH) return false;
      if (!urlPath[2].equals("customer") || !urlPath[4].equals("date")) return false;

      String storeID = urlPath[1];
      String customerID = urlPath[3];
      String date = urlPath[5];

      if (!isValidNumber(storeID) || !isValidNumber(customerID)) return false;
      if (!isValidDate(date)) return false;
    }
  // /items/store/{storeID}
  // /items/top10/{itemID}
  // urlPaths = [items, store/top10, storeID/itemID]
    else if (method == HttpMethod.GET) {
      if (urlPath.length != ITEMS_URL_LENGTH) return false;
      String query = urlPath[1];
      String ids = urlPath[2];
      if (!query.equals("top10") && !query.equals("store")) return false;
      if (!isValidNumber(ids)) return false;
    }
    return true;
  }

  private boolean isValidNumber(String ids) {
    if (ids == null || ids.isEmpty()) return false;

    try {
      int digits = Integer.parseInt(ids);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  private  boolean isValidDate(String date) {
    if (date == null || date.isEmpty()) return false;

    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      Date check = sdf.parse(date);
      if (!date.equals(sdf.format(check))) return false;
    } catch (ParseException e) {
      return false;
    }
    return true;
  }

  /**
   * the rabbitmq producer
   * @param purchaseRecord
   */
//  private boolean produce(PurchaseRecord purchaseRecord) {
//    final String EXCHANGE_NAME = "logs";
//    try {
//      String purchaseInJson = gson.toJson(purchaseRecord);
//      Channel channel = channelPool.getChannel();
//      channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
//      // non-persistent queue
////      channel.basicPublish(EXCHANGE_NAME, "", null,
////          purchaseInJson.getBytes("UTF-8"));
//
//      // persistent queue
//      channel.basicPublish(EXCHANGE_NAME, "", MessageProperties.PERSISTENT_TEXT_PLAIN,
//          purchaseInJson.getBytes("UTF-8"));
////      System.out.println(" [x] Sent '" + purchaseInJson + "'");
//      channelPool.returnChannel(channel);
//
//    } catch (IOException ioException) {
//      ioException.printStackTrace();
//      return false;
//    }
//    return true;
//  }

}


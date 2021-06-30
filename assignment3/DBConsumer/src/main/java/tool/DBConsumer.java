package tool;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import dal.DBCPDataSource;
import dal.PurchasesDao;
import javax.sql.DataSource;
import model.PurchaseRecord;

public class DBConsumer {
  private static final String EXCHANGE_NAME = "logs";
  private static final Gson gson = new Gson();
  private static DataSource pool = DBCPDataSource.createPool();

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
    //non-persistent queue
//    String queueName = channel.queueDeclare().getQueue();

    // use persistent queue
    String queueName = "PurchaseInDataBase";
    channel.queueDeclare(queueName, true, false, true, null);
    channel.queueBind(queueName, EXCHANGE_NAME, "");

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
//      System.out.println("message:" + message);
      PurchaseRecord pr = gson.fromJson(message, PurchaseRecord.class);
//      System.out.println("pr :" + pr.toString());
      // insert to DB
      PurchasesDao purchasesDao = new PurchasesDao(pool);
      boolean isSuccess = purchasesDao.insertIntoPurchases(pr.getUuid(),
            pr.getStoreID(), pr.getCustomerID(), pr.getDate(), pr.getCreateTime().toString(),
            pr.getPurchaseBody().toString());
      purchasesDao.closeConnection();
//      System.out.println("isSuccess： " + isSuccess);
//      System.out.println(" [x] Received '" + message + "'");

    };

    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
  }
}

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import io.swagger.client.model.PurchaseItems;
import redis.clients.jedis.Jedis;

public class RedisConsumer {
  private static final String EXCHANGE_NAME = "logs";
  private static final Gson gson = new Gson();

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
    // non-persistent queue
//    String queueName = channel.queueDeclare().getQueue();

    //persistent queue
    String queueName = "StoreCache";
    channel.queueDeclare(queueName, true, false, true, null);
    channel.queueBind(queueName, EXCHANGE_NAME, "");

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      PurchaseRecord pr = gson.fromJson(message, PurchaseRecord.class);

      Jedis jedis = new Jedis("localhost");
      addPurchaseRecordToCache(jedis, pr);
//      System.out.println(" [x] Received '" + message + "'");

    };

    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
  }

  private static void addPurchaseRecordToCache(Jedis jedis, PurchaseRecord pr) {
    String storeID = "store:" + String.valueOf(pr.getStoreID());

    for (PurchaseItems item : pr.getPurchaseBody().getItems()) {
      String itemID = "item:" + item.getItemID();
      Double cnt = (double) item.getNumberOfItems();

      // add to cache
      // each store with top sold items and its quantity
      addOneItemToCache(jedis, storeID, itemID, cnt);
      // each item sold in different stores and its sold quantity
      addOneItemToCache(jedis, itemID, storeID, cnt);
    }

  }

  private static void addOneItemToCache(Jedis jedis, String key, String scoreMember, Double score){
    // if not exist in cache, add scoreMember
    if (jedis.zscore(key, scoreMember) == null) {
      jedis.zadd(key, score, scoreMember);
    } else {
      // increment existing scoreMember score, by given score
      jedis.zincrby(key, score, scoreMember);
    }
  }
}

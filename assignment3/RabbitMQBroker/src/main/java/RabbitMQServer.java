import com.google.gson.Gson;
import com.rabbitmq.client.*;
import io.swagger.client.model.TopItems;
import io.swagger.client.model.TopItemsStores;
import io.swagger.client.model.TopStores;
import io.swagger.client.model.TopStoresStores;
import java.util.Set;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

/**
 * RabbitMQServer as microservice, get information from redis in StoreConsumer,
 * use to process doGet in PurchaseServlet for TOP K purchase info
 * [**topItems**](ItemsApi.md#topItems) | **GET** /items/store/{storeID} | get top 10 items sold at store
 * [**topStores**](ItemsApi.md#topStores) | **GET** /items/top10/{itemID} | get top 10 stores for item sales
 */
public class RabbitMQServer {

  private static final String RPC_QUEUE_NAME = "rpc_queue";
  private static Gson gson = new Gson();
  private static final Integer ITEM_START_INDEX = 5;
  private static final Integer STORE_START_INDEX = 6;
  private static final Integer API_START_INDEX = 7;


  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");

    try (Connection connection = factory.newConnection();
        Channel channel = connection.createChannel()) {
      channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
      channel.queuePurge(RPC_QUEUE_NAME);

      channel.basicQos(1);

      System.out.println(" [x] Awaiting RPC requests");

      Object monitor = new Object();
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
            .Builder()
            .correlationId(delivery.getProperties().getCorrelationId())
            .build();

        String response = "";

        try {
          String message = new String(delivery.getBody(), "UTF-8");
          System.out.println(" [.] msg(" + message + ")");
          Jedis jedis = new Jedis("localhost");
          if (message.contains("/top10/")) {
            String itemID = "item:" + message.substring(API_START_INDEX);
            TopStores topStores = getTop10Stores(jedis, itemID);
            response += gson.toJson(topStores) + "\n";
          } else if (message.contains("/store/")){
            String storeID = "store:" + message.substring(API_START_INDEX);
            TopItems topItems = getTop10items(jedis, storeID);
            response += gson.toJson(topItems) + "\n";
          }


        } catch (RuntimeException e) {
          System.out.println(" [.] " + e.toString());
        } finally {
          channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          // RabbitMq consumer worker thread notifies the RPC server owner thread
          synchronized (monitor) {
            monitor.notify();
          }
        }
      };

      channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> { }));
      // Wait and be prepared to consume the message from RPC client.
      while (true) {
        synchronized (monitor) {
          try {
            monitor.wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  private static TopItems getTop10items(Jedis jedis, String storeID){
    TopItems topItems = new TopItems();
//    jedis.zrevrange(key, 0, -1);
    //We could get the elements with the associated score, all the elements sorted from top to bottom
    Set<Tuple> elements = jedis.zrevrangeWithScores(storeID, 0, 9);
    for(Tuple tuple: elements){
//      System.out.println(tuple.getElement() + "-" + tuple.getScore());
      String itemID = tuple.getElement().substring(ITEM_START_INDEX);
      Integer qty =  (int) tuple.getScore();
      // create new top item
      TopItemsStores oneItem = new TopItemsStores();
      oneItem.setItemID(Integer.parseInt(itemID));
      oneItem.setNumberOfItems(qty);
      // add this item to final list
      topItems.addStoresItem(oneItem);
    }
    return topItems;
  }

  private static TopStores getTop10Stores(Jedis jedis, String itemID){
    TopStores topStores = new TopStores();
    //    jedis.zrevrange(key, 0, -1);
    //We could get the elements with the associated score, all the elements sorted from top to bottom
    Set<Tuple> elements = jedis.zrevrangeWithScores(itemID, 0, 9);
    for(Tuple tuple: elements){
//      System.out.println(tuple.getElement() + "-" + tuple.getScore());
      String storeID = tuple.getElement().substring(STORE_START_INDEX);
      Integer qty =  (int) tuple.getScore();
      // create new top store
      TopStoresStores oneStore = new TopStoresStores();
      oneStore.setStoreID(Integer.parseInt(storeID));
      oneStore.setNumberOfItems(qty);
      // add this item to final list
      topStores.addStoresItem(oneStore);
    }
    return topStores;

  }
}
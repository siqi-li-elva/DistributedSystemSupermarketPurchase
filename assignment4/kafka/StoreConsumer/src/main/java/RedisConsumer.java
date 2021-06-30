import com.google.gson.Gson;
import io.swagger.client.model.PurchaseItems;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import kafka.consumer.RoundRobinAssignor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import redis.clients.jedis.Jedis;

public class RedisConsumer {
  private static final String EXCHANGE_NAME = "logs";
  private static final Gson gson = new Gson();

  public static void main(String[] argv) throws Exception {
//    ConnectionFactory factory = new ConnectionFactory();
//    factory.setHost("localhost");
//    Connection connection = factory.newConnection();
//    Channel channel = connection.createChannel();
//
//    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
//    // non-persistent queue
////    String queueName = channel.queueDeclare().getQueue();
//
//    //persistent queue
//    String queueName = "StoreCache";
//    channel.queueDeclare(queueName, true, false, true, null);
//    channel.queueBind(queueName, EXCHANGE_NAME, "");

//    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
//
//    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//      String message = new String(delivery.getBody(), "UTF-8");
//      PurchaseRecord pr = gson.fromJson(message, PurchaseRecord.class);
//      System.out.println(" [x] Received '" + message + "'");
//    };
//    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });

    // use kafka consumer
    String bootstrapServers = "localhost:9092";
    String groupId = "my-cache-application";
    String topic = "purchase-log";
    // create consumer configs
    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    // create consumer
    KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);

    // subscribe consumer to our topic(s)
    consumer.subscribe(Collections.singletonList(topic));

    // poll for new data
    consumer.subscribe(Arrays.asList(topic));

    // poll for new data
    while(true){
      ConsumerRecords<String, String> records =
          consumer.poll(Duration.ofMillis(100)); // new in Kafka 2.0.0

      for (ConsumerRecord<String, String> record : records){
        System.out.println("value" + record.value());
        PurchaseRecord pr = gson.fromJson(record.value(), PurchaseRecord.class);
        Jedis jedis = new Jedis("localhost");
        addPurchaseRecordToCache(jedis, pr);
      }
    }


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

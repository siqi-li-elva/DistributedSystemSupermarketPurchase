package tool;

import com.google.gson.Gson;
import dal.DBCPDataSource;
import dal.PurchasesDao;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import javax.sql.DataSource;
import model.PurchaseRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class DBConsumer {
//  private static final String EXCHANGE_NAME = "logs";
  private static final Integer PARTITION_CNT = 4;
  private static final Gson gson = new Gson();
//  private static DataSource pool = DBCPDataSource.createPool("localhost");
  private static DataSource pool0 = DBCPDataSource.createPool("database-1.cozkx1v7urc2.us-west-2.rds.amazonaws.com");
  private static DataSource pool1 = DBCPDataSource.createPool("database-2.cozkx1v7urc2.us-west-2.rds.amazonaws.com");
  private static DataSource pool2 = DBCPDataSource.createPool("database-3.cozkx1v7urc2.us-west-2.rds.amazonaws.com");
  private static DataSource pool3 = DBCPDataSource.createPool("database-4.cozkx1v7urc2.us-west-2.rds.amazonaws.com");

  public static void main(String[] argv) throws Exception {
    // use rabbitmq
//    ConnectionFactory factory = new ConnectionFactory();
//    factory.setHost("localhost");
//    Connection connection = factory.newConnection();
//    Channel channel = connection.createChannel();
//
//    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
//    //non-persistent queue
////    String queueName = channel.queueDeclare().getQueue();
//
//    // use persistent queue
//    String queueName = "PurchaseInDataBase";
//    channel.queueDeclare(queueName, true, false, true, null);
//    channel.queueBind(queueName, EXCHANGE_NAME, "");
//
//    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
//
//    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//      String message = new String(delivery.getBody(), "UTF-8");
////      System.out.println("message:" + message);
//      PurchaseRecord pr = gson.fromJson(message, PurchaseRecord.class);
////      System.out.println("pr :" + pr.toString());
//      // insert to DB
//      PurchasesDao purchasesDao = new PurchasesDao(pool);
//      boolean isSuccess = purchasesDao.insertIntoPurchases(pr.getUuid(),
//            pr.getStoreID(), pr.getCustomerID(), pr.getDate(), pr.getCreateTime().toString(),
//            pr.getPurchaseBody().toString());
//      purchasesDao.closeConnection();
////      System.out.println("isSuccess： " + isSuccess);
////      System.out.println(" [x] Received '" + message + "'");
//
//    };
//
//    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });

    // use Kafka
    consumeAndInsertToDB();

  }
  private static void consumeAndInsertToDB() {
    String bootstrapServers = "52.12.233.191:9092";
    String groupId = "my-database-application";
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
      ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100)); // new in Kafka 2.0.0
      for (ConsumerRecord<String, String> record : records){
        System.out.println("value" + record.value());
          PurchaseRecord pr = gson.fromJson(record.value(), PurchaseRecord.class);
          insertToDB(pr);

      }
    }
  }

  private static void insertToDB(PurchaseRecord pr) {
    DataSource pool;
    if (pr.getStoreID() % PARTITION_CNT == 0) pool = pool0;
    else if (pr.getStoreID() % PARTITION_CNT == 1) pool = pool1;
    else if (pr.getStoreID() % PARTITION_CNT == 2) pool = pool2;
    else pool = pool3;
    PurchasesDao purchasesDao = new PurchasesDao(pool);
    boolean isSuccess = purchasesDao.insertIntoPurchases(pr.getUuid(),
        pr.getStoreID(), pr.getCustomerID(), pr.getDate(), pr.getCreateTime().toString(),
        pr.getPurchaseBody().toString());
    purchasesDao.closeConnection();
  }

}

package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Properties;
import model.PurchaseRecord;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class KafkaProducerObject {
  private final KafkaProducer<String, String> producer;
  private final String topic;
  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public KafkaProducerObject(String topic) {
    this.topic = topic;
    Properties properties = new Properties();
    // run on localhost
//    properties.put("bootstrap.servers", "localhost:9092");
    properties.put("bootstrap.servers", "52.12.233.191:9092");

    properties.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaProducerObject");
    properties.put(ProducerConfig.ACKS_CONFIG, "-1");

    properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, MyPartitioner.class.getName());

    producer = new KafkaProducer<String, String>(properties);
//    System.out.println("successfully initialized kafka producer");
  }

  public void kafkaProduce(PurchaseRecord purchaseRecord){
    String purchaseInJson = gson.toJson(purchaseRecord);
    String key = String.valueOf(purchaseRecord.getStoreID() % 4);
    ProducerRecord record = new ProducerRecord<String, String>(topic, key, purchaseInJson);
    producer.send(record, new Callback() {
      @Override
      public void onCompletion(RecordMetadata metadata, Exception exception) {
        if (metadata != null) {
          System.out
              .println("async-offset：" + metadata.offset() + "-> partition" + metadata.partition());
        }
      }
    });

  }

}

package utils;

import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

public class MyPartitioner implements Partitioner {
  private static final int PARTITION_COUNT= 4 ;

  @Override
  public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
    //获取当前 topic 有多少个分区（分区列表）
    List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
    Integer keyInt=Integer.parseInt(key.toString());
    return keyInt % PARTITION_COUNT;
  }

  @Override
  public void close() {

  }

  @Override
  public void configure(Map<String, ?> configs) {

  }
}

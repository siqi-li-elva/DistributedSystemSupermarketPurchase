package part2;
import static part2.Client2.*;
import static part2.RecordWriter.COMMA;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import model.HttpMethod;


public class ReportGenerator {
  private static final int LATENCY_INDEX = 2;
  private static final int TYPE_INDEX = 1;


  private static List<Long>  readInAndSortAllLatency(String pathToCsv) {
    List<Long> allLatency = new ArrayList<>();
    try {
      BufferedReader br = new BufferedReader(new FileReader(pathToCsv));
      String line;
      while ((line = br.readLine()) != null) {
        String[] oneLine = line.split(COMMA);
//        System.out.println("CURRENT LATENCY: " + oneLine[TYPE_INDEX]);
//        System.out.println("isPost>>> " + oneLine[TYPE_INDEX].equals(HttpMethod.POST.name()));
        if (oneLine[TYPE_INDEX].equals(HttpMethod.POST.name())) {
          allLatency.add(Long.parseLong(oneLine[LATENCY_INDEX]));
        }
      }
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    allLatency.sort(Long::compareTo);
    return allLatency;
  }

  private static Double getMeanResponseTime(List<Long> allLatency) {
    return allLatency.stream()
        .mapToLong(i -> i).average().getAsDouble();
  }

  private static Double getMedianResponseTime(List<Long> allLatency) {
    long median = allLatency.get(allLatency.size()/2);
    if (allLatency.size() % 2 == 1) return (double)median;
    long left = allLatency.get(allLatency.size()/2 - 1);
    return (double) (median + left) / 2;
  }

  private static long getMaxResponseTime(List<Long> allLatency) {
    return allLatency.get(allLatency.size()-1);
  }

  private static long getP99(List<Long> allLatency) {
    int n = allLatency.size();
    int index = (int) Math.ceil(0.99 * n);
    return allLatency.get(index-1);
  }

  protected static void generateResult(long start, long end, String pathToCsv,
      AtomicInteger totalSuccessPurchase, AtomicInteger totalFailPurchase) {
    List<Long> allLatency = readInAndSortAllLatency(pathToCsv);
//    System.out.println("readin size: " + allLatency.size());
    //1. mean response time for POSTs (millisecs)
    //2. median response time for POSTs (millisecs)
    //3. the total wall time
    //4. throughput = requests per second = total number of requests/wall time
    //5. p99 (99th percentile) response time
    //6. max response time for POSTs
    System.out.println("Generate Output Result For Part2.Client2 Based On Following Parameters:");
    System.out.println("IP Address: " + IPAddress + ", Max Store: " + maxStores
        + ", numOfCustomersPerStore: " + numOfCustomersPerStore + ", maxItemId: " + maxItemId);
    System.out.println("numPurchasesPerHour: " + numPurchases + ", numItemsPerPurchase: " + numItemsPerPurchase
        + ", @ Date: " + date);
    System.out.println("=======================================================================================");
    double mean = getMeanResponseTime(allLatency);
    System.out.println("1. Total number of successful requests sent: "
        + totalSuccessPurchase.get());
    System.out.println("2. Total number of unsuccessful requests: "
        + totalFailPurchase.get());

    System.out.printf("3. Mean response time for POSTs:  %.3f milliseconds %n", mean);

    double median = getMedianResponseTime(allLatency);
    System.out.printf("4. Median response time for POSTs:  %.3f milliseconds %n", median);

    double runtime = (end - start) / 1000.0;
    System.out.println("5. The total run time (wall time) for all phases to complete: " + runtime + " second");

    double throughput = (totalSuccessPurchase.get()) / runtime;
    System.out.printf("6. Throughput: %.3f requests/second %n", throughput);

    long p99 = getP99(allLatency);
    System.out.println("7. P99 (99th percentile) response time for POSTs: " + p99 + " milliseconds");

    long max = getMaxResponseTime(allLatency);
    System.out.println("8. Max response time for POSTs: " + max + " milliseconds");

  }
}

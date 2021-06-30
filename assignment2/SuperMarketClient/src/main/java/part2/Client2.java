package part2;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import model.CmdParser;
import model.HttpMethod;
import model.LogStderr;
import model.Record;
import org.apache.commons.cli.*;


public class Client2 {
  protected static final Integer RUNNING_HOURS = 9;
  protected static String IPAddress;
  protected static Integer maxStores;
  protected static Integer numOfCustomersPerStore = 1000;
  protected static Integer maxItemId = 100000;
  protected static Integer numPurchases = 300;
  protected static Integer numItemsPerPurchase = 5;
  protected static String date =  "2021-01-01";
  protected static String pathToCSV = "records";
  private static final String csvSuffix = ".csv";

  public static void main(String[] args) throws InterruptedException {
    // parse commandline input, and validate input
    CommandLine cmd = CmdParser.parseCmd(args);
    if (!isValidCli(cmd)) {
      System.out.println("Invalid Input Parameters, Please Start Again!");
      System.exit(1);
    }

    CountDownLatch phaseTwo = new CountDownLatch(1);
    CountDownLatch phaseThree = new CountDownLatch(1);
    AtomicInteger totalSuccessPurchase = new AtomicInteger(0);
    AtomicInteger totalFailPurchase = new AtomicInteger(0);
    BlockingQueue<Record> queue = new LinkedBlockingDeque<>();
    Thread[] allThreads = new Thread[maxStores];
    int threadIndex = 0;

    // generate statistic for each purchase call in csv file
    String csv = pathToCSV + csvSuffix;
    RecordWriter recordWriter = new RecordWriter(queue, csv);
    Thread consumer = new Thread(recordWriter);
    consumer.start();

    // initialize all threads
    for (int storeID = 1; storeID <= maxStores; storeID++) {
      SingleThread currentStore = new SingleThread(storeID, IPAddress, numOfCustomersPerStore,
          maxItemId, numPurchases, numItemsPerPurchase, date, totalSuccessPurchase,
          totalFailPurchase, phaseTwo, phaseThree, queue);
      Thread t = new Thread(currentStore);
      allThreads[threadIndex++] = t;
    }

    threadIndex = 0;
    // phase 1 thread start working
    long startTime = System.currentTimeMillis();
    for (int storeID = 1; storeID <= maxStores/4; storeID++) {
      Thread t = allThreads[threadIndex++];
      t.start();
    }
    // initiate phase 1 countdown latch
    phaseTwo.await();

    // phase 2 thread start working
    for (int storeID = maxStores/4 + 1; storeID <= maxStores/2; storeID++) {
      Thread t = allThreads[threadIndex++];
      t.start();
    }
    // initiate phase 2 countdown latch
    phaseThree.await();

    // phase 3 thread start working
    for (int storeID = maxStores/2 + 1; storeID <= maxStores; storeID++) {
      Thread t = allThreads[threadIndex++];
      t.start();
    }

    // wait all thread to close together
    try {
      for (Thread t : allThreads) t.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    long entTime = System.currentTimeMillis();

    // send signal for consumer to stop
    queue.put(new Record(-1, -1, HttpMethod.POST, -1));
    consumer.join();

    try {
      LogStderr.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    ReportGenerator.generateResult(startTime, entTime, csv, totalSuccessPurchase, totalFailPurchase);

  }

  private static boolean isValidCli(CommandLine cmd) {
    // optional csv file path
    String cliCSV = cmd.getOptionValue("csv");
    if (cliCSV != null && !cliCSV.isEmpty()) pathToCSV = cliCSV;

    // must have valid IP Address
    String cliIP = cmd.getOptionValue("ipAddress");
    if (cliIP == null || cliIP.isEmpty()) return false;
    IPAddress = cliIP;

//     must have maxStore > 0
    String cliMaxStore = cmd.getOptionValue("maxStore");
    if (cliMaxStore == null || cliMaxStore.isEmpty()) return false;
    if (!validateNumbers(cliMaxStore, 0)) return false;
    maxStores = Integer.parseInt(cliMaxStore);

    // optional valid customer per store > 0
    String cliCustomer = cmd.getOptionValue("numOfCustomersPerStore");
    if (cliCustomer != null && !cliCustomer.isEmpty()) {
      if (!validateNumbers(cliCustomer, 0)) {
        return false;
      }
      numOfCustomersPerStore = Integer.parseInt(cliCustomer);
    }

    // optional valid maxItemsID > 0
    String cliItem = cmd.getOptionValue("maxItem");
    if (cliItem != null && !cliItem.isEmpty()) {
      if(!validateNumbers(cliItem, 0)) return false;
      maxItemId = Integer.parseInt(cliItem);
    }

    // optional valid numPurchase > 0
    String cliPurchase = cmd.getOptionValue("numPurchasesPerHour");
    if (cliPurchase != null && !cliPurchase.isEmpty()) {
      if (!validateNumbers(cliPurchase, 0)) return false;
      numPurchases = Integer.parseInt(cliPurchase);
    }

    // optional valid numOfItemPerPurchase > 0 && < 20
    String cliItemPurchase = cmd.getOptionValue("numItemsPerPurchase");
    if (cliItemPurchase != null && !cliItemPurchase.isEmpty()) {
      if (!validateNumbers(cliItemPurchase, 0, 20)) return false;
      numItemsPerPurchase = Integer.parseInt(cliItemPurchase);
    }

    // optional valid date format
    String cliDate = cmd.getOptionValue("date");
    try {
      if (cliDate != null && !cliDate.isEmpty()) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date check = sdf.parse(cliDate);
        if (cliDate.equals(sdf.format(check))) date = cliDate;
      }
    } catch (java.text.ParseException e) {
      System.out.println("Date format should be \"yyyy-MM-dd\"");
      return false;
    }
    return true;
  }

  private static boolean validateNumbers(String cliInput, int min) {
    try {
      int num = Integer.parseInt(cliInput);
      if (num <= min) {
        System.out.println("input numbers too small");
        return false;
      }
    } catch (Exception e) {
      System.out.println("invalid input arguments, enter valid numbers");
      return false;
    }
    return true;
  }

  private static boolean validateNumbers(String cliInput, int min, int max) {
    try {
      int num = Integer.parseInt(cliInput);
      if (num <= min || num > max) {
        System.out.println("input numbers out of range");
        return false;
      }
    } catch (Exception e) {
      System.out.println("invalid input arguments");
      return false;
    }
    return true;
  }

}

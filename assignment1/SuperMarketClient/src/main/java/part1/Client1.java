package part1;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import model.LogStderr;
import org.apache.commons.cli.*;


public class Client1 {
  protected static final Integer RUNNING_HOURS = 9;
  protected static String IPAddress;
  protected static Integer maxStores;
  protected static Integer numOfCustomersPerStore = 1000;
  protected static Integer maxItemId = 100000;
  protected static Integer numPurchases = 60;
  protected static Integer numItemsPerPurchase = 5;
  protected static String date =  "2021-01-01";

  public static void main(String[] args) throws InterruptedException {
    parseCmd(args);

    CountDownLatch phaseTwo = new CountDownLatch(1);
    CountDownLatch phaseThree = new CountDownLatch(1);
    AtomicInteger totalSuccessPurchase = new AtomicInteger(0);
    AtomicInteger totalFailPurchase = new AtomicInteger(0);
    Thread[] allThreads = new Thread[maxStores];
    int threadIndex = 0;

    long startTime = System.currentTimeMillis();
    for (int storeID = 1; storeID <= maxStores; storeID++) {
      SingleThread currentStore = new SingleThread(storeID, IPAddress, numOfCustomersPerStore,
          maxItemId, numPurchases, numItemsPerPurchase, date, totalSuccessPurchase,
          totalFailPurchase, phaseTwo, phaseThree);
      Thread t = new Thread(currentStore);
      allThreads[threadIndex++] = t;
      t.start();
      // initiate phase 2 countdown latch when phase 1 thread start working
      if (storeID >= maxStores/4 + 1) phaseTwo.await();
      // initiate phase 3 countdown latch when phase 2 thread start working
      if (storeID >= maxStores/2 + 1) phaseThree.await();
    }

    // wait all thread to close together
    try {
      for (Thread t : allThreads) t.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    long entTime = System.currentTimeMillis();
    try {
      LogStderr.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    generateResult(startTime, entTime, totalSuccessPurchase, totalFailPurchase);
  }

  private static void generateResult(long start, long end, AtomicInteger totalSuccessPurchase,
      AtomicInteger totalFailPurchase) {
//    1. Total number of successful requests sent
//    2. Total number of unsuccessful requests (ideally should be 0)
//    3. The total run time (wall time) for all phases to complete.
//    4. Throughput = requests per second = total number of requests/wall time
    System.out.println("Generate Output Result For Part1.Client1 Based On Following Parameters:");
    System.out.println("IP Address: " + IPAddress + ", Max Store: " + maxStores
        + ", numOfCustomersPerStore: " + numOfCustomersPerStore + ", maxItemId: " + maxItemId);
    System.out.println("numPurchasesPerHour: " + numPurchases + ", numItemsPerPurchase: " + numItemsPerPurchase
        + ", @ Date: " + date);
    System.out.println("=======================================================================================");
    System.out.println("1. Total number of successful requests sent: "
        + totalSuccessPurchase.get());
    System.out.println("2. Total number of unsuccessful requests: "
        + totalFailPurchase.get());
    Double runtime = (end - start) / 1000.0;
    System.out.println("3. The total run time (wall time) for all phases to complete: "
        + runtime + " second");
    Double throughput = (numPurchases * RUNNING_HOURS * maxStores) / runtime;
    System.out.printf("4. Throughput: %.3f requests/second %n", throughput);

  }

  private static void parseCmd(String[] args) {
    // setting up options for command line input
    Options options = new Options();
    Option ipOpt = new Option("ip", "ipAddress", true,
        "ip and port address of the server");
    ipOpt.setRequired(true);
    options.addOption(ipOpt);

    Option maxStoreOpt = new Option("s", "maxStore", true, "setting up max stores");
    maxStoreOpt.setRequired(true);
    options.addOption(maxStoreOpt);

    Option custPerStoreOpt = new Option("ct", "numOfCustomersPerStore", true,
        "setting up number of customers per store");
    custPerStoreOpt.setOptionalArg(true);
    options.addOption(custPerStoreOpt);

    Option maxItemOpt = new Option("it", "maxItem", true,
        "setting up the max item id");
    maxItemOpt.setOptionalArg(true);
    options.addOption(maxItemOpt);

    Option numPurchasesOpt = new Option("p", "numPurchasesPerHour", true,
        "setting up number of purchase per hour");
    numPurchasesOpt.setOptionalArg(true);
    options.addOption(numPurchasesOpt);

    Option itPerPurchaseOpt = new Option("n", "numItemsPerPurchase", true,
        "setting up number of items per purchase");
    itPerPurchaseOpt.setOptionalArg(true);
    options.addOption(itPerPurchaseOpt);

    Option dateOpt = new Option("d", "date", true,
        "setting up start date");
    dateOpt.setOptionalArg(true);
    options.addOption(dateOpt);

    // parse input from args
    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("utility-name", options);
      System.exit(1);
    }
    if (!isValidCli(cmd)) {
      System.out.println("Invalid Input Parameters, Please Start Again!");
      System.exit(1);
    }
  }

  private static boolean isValidCli(CommandLine cmd) {
    // must have valid IP Address
    String cliIP = cmd.getOptionValue("ipAddress");
    if (cliIP == null || cliIP.isEmpty()) return false;
    IPAddress = cliIP;

    // must have maxStore > 0
    String cliMaxStore = cmd.getOptionValue("maxStore");
    if (cliMaxStore == null || cliMaxStore.isEmpty()) return false;
    if (!validateNumbers(cliMaxStore, 0)) return false;
    maxStores = Integer.parseInt(cliMaxStore);

    // optional valid customer per store > 0
    String cliCustomer = cmd.getOptionValue("numOfCustomersPerStore");
    if (cliCustomer != null && !cliCustomer.isEmpty()) {
      if (!validateNumbers(cliCustomer, 0)) {
//        System.out.println("valid customer per store too small");
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

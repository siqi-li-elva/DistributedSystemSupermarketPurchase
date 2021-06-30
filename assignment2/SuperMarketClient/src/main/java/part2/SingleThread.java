package part2;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.PurchaseApi;
import io.swagger.client.model.Purchase;
import io.swagger.client.model.PurchaseItems;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import model.HttpMethod;
import model.LogStderr;
import model.Record;

public class SingleThread  implements Runnable{
  private static final Integer HTTP_OK = 200;
  private static final Integer HTTP_Created = 201;
  private static final Integer PHASE_TWO_HOURS = 3;
  private static final Integer PHASE_THREE_HOURS = 5;
  private Integer totalPurchaseCall;
  private Integer storeID;
  private String IPAddress;
  private Integer numOfCustomersPerStore;
  private Integer maxItemId;
  private Integer numPurchases;
  private Integer numItemsPerPurchase;
  private String date;
  AtomicInteger successPurchaseCnt;
  AtomicInteger failPurchaseCnt;
  CountDownLatch phaseTwo;
  CountDownLatch phaseThree;
  private Random rand = new Random();
  private Integer storeSuccessPurchase = 0;
  private Integer storeFailPurchase = 0;
  private static Integer TIME_OUT = 45000;

  BlockingQueue<Record> queue;

  public SingleThread(Integer storeID, String IPAddress, Integer numOfCustomersPerStore,
      Integer maxItemId, Integer numPurchases, Integer numItemsPerPurchase, String date,
      AtomicInteger successPurchaseCnt, AtomicInteger failPurchaseCnt,
      CountDownLatch phaseTwo, CountDownLatch phaseThree, BlockingQueue<Record> queue) {
    this.storeID = storeID;
    this.IPAddress = IPAddress;
    this.numOfCustomersPerStore = numOfCustomersPerStore;
    this.maxItemId = maxItemId;
    this.numPurchases = numPurchases;
    this.numItemsPerPurchase = numItemsPerPurchase;
    this.date = date;
    this.successPurchaseCnt = successPurchaseCnt;
    this.failPurchaseCnt = failPurchaseCnt;
    this.phaseTwo = phaseTwo;
    this.phaseThree = phaseThree;
    this.totalPurchaseCall = Client2.RUNNING_HOURS * numPurchases;
    this.queue = queue;
  }

  @Override
  public void run() {
    String url = "http://" + this.IPAddress + ":8080/SuperMarketServer_war";
//    PurchaseApi apiInstance = new PurchaseApi();
//    apiInstance.getApiClient().setBasePath(url);
    ApiClient client = new ApiClient();
    client.setBasePath(url).setConnectTimeout(TIME_OUT);
    client.setReadTimeout(TIME_OUT);
    PurchaseApi apiInstance = new PurchaseApi(client);

    Integer storeID = this.storeID; // Integer | ID of the store the purchase takes place at
    String date = this.date; // String | date of purchase

    for (int i = 0; i < totalPurchaseCall; i++) {
      // Integer | customer ID making purchase
      Integer custID = this.rand.ints(this.storeID * 1000,
          this.storeID * 1000 + numOfCustomersPerStore).findFirst().getAsInt();
      Purchase body = createPurchaseCall(); // Purchase | items purchased
      boolean isSuccess = sendRequest(apiInstance, body, storeID, custID, date);

      // retry 3 times for failed purchase
      int retry = 3;
      while (!isSuccess) {
        if (retry <= 0) break;
        retry--;
        isSuccess = sendRequest(apiInstance, body, storeID, custID, date);
      }
      // add current store's success and failed call
    }

    this.successPurchaseCnt.addAndGet(this.storeSuccessPurchase);
    this.failPurchaseCnt.addAndGet(this.storeFailPurchase);
  }

  private boolean sendRequest(PurchaseApi apiInstance, Purchase body, Integer storeID,
      Integer custID, String date) {
      try {
        long startTime = System.currentTimeMillis();
        ApiResponse res = apiInstance.newPurchaseWithHttpInfo(body, storeID, custID, date);
        long endTime = System.currentTimeMillis();

        int resCode = res.getStatusCode();
        if (resCode == HTTP_Created || resCode == HTTP_OK) {
          this.storeSuccessPurchase++;
          // initiate phase 2
          if (storeSuccessPurchase == numPurchases * PHASE_TWO_HOURS) {
            phaseTwo.countDown();
          }
          // initiate phase 3
          if (storeSuccessPurchase == numPurchases * PHASE_THREE_HOURS) {
            phaseThree.countDown();
          }
          // add current call record in to queue
          Record record = new Record(startTime, endTime, HttpMethod.POST, resCode);
          queue.put(record);
          return true;
        } else {
          this.storeFailPurchase++;
          return false;
        }
      } catch (InterruptedException ie) {
        this.storeFailPurchase++;
        ie.printStackTrace();
        return false;
      } catch (ApiException e) {
        this.storeFailPurchase++;
        LogStderr.setErrToFile();
        System.err.println(">>> Begin of current error log");
        System.err.println("Exception when calling PurchaseApi#newPurchase");
        int errorCode = e.getCode();
        System.err.println("HTTP Error Code: " + errorCode);
        System.err.println("HTTP Error Header: " + e.getResponseHeaders());
        System.err.println("HTTP Error Body: " + e.getResponseBody());
        System.err.println(">>> End of current error log");
        LogStderr.EndErrorLogToFile();
        e.printStackTrace();
        return false;
      }
    }

  private Purchase createPurchaseCall() {
    Purchase newPurchase = new Purchase();
    for (int i = 0; i < numItemsPerPurchase; i++) {
      int itemID = rand.ints(0, maxItemId).findFirst().getAsInt();
      int qty = 1;
      PurchaseItems item = new PurchaseItems();
      item.setItemID(String.valueOf(itemID));
      item.setNumberOfItems(qty);
      newPurchase.addItemsItem(item);
    }
    return newPurchase;
  }
}


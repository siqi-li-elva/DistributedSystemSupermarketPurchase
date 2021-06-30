package part1;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.PurchaseApi;
import io.swagger.client.model.Purchase;
import io.swagger.client.model.PurchaseItems;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import model.LogStderr;

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

  public SingleThread(Integer storeID, String IPAddress, Integer numOfCustomersPerStore,
      Integer maxItemId, Integer numPurchases, Integer numItemsPerPurchase, String date,
      AtomicInteger successPurchaseCnt, AtomicInteger failPurchaseCnt,
      CountDownLatch phaseTwo, CountDownLatch phaseThree) {
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
    this.totalPurchaseCall = Client1.RUNNING_HOURS * numPurchases;
  }

  @Override
  public void run() {
    Integer storeSuccessPurchase = 0;
    String url = "http://" + this.IPAddress + ":8080/SuperMarketServer_war";
    PurchaseApi apiInstance = new PurchaseApi();
    apiInstance.getApiClient().setBasePath(url);

    Integer storeID = this.storeID; // Integer | ID of the store the purchase takes place at
    String date = this.date; // String | date of purchase

    for (int i = 0; i < totalPurchaseCall; i++) {
      try {
        // Integer | customer ID making purchase

        Integer custID = this.rand.ints(this.storeID * 1000,
            this.storeID * 1000 + numOfCustomersPerStore).findFirst().getAsInt();
        Purchase body = createPurchaseCall(); // Purchase | items purchased
//        ApiResponse errorTestRes = apiInstance.newPurchaseWithHttpInfo (body, 0, 0, "2888-77-66");
        ApiResponse res = apiInstance.newPurchaseWithHttpInfo (body, storeID, custID, date);
        if (res.getStatusCode() == HTTP_Created || res.getStatusCode() == HTTP_OK) {
          this.storeSuccessPurchase++;
          successPurchaseCnt.getAndIncrement();
          // initiate phase 2
          if (storeSuccessPurchase == numPurchases * PHASE_TWO_HOURS) {
//            System.out.println("phase 2 start at : " + successPurchaseCnt.get());
            phaseTwo.countDown();
          }
          // initiate phase 3
          if (storeSuccessPurchase == numPurchases * PHASE_THREE_HOURS) {
//            System.out.println("phase 3 start at : " + successPurchaseCnt.get() );
            phaseThree.countDown();
          }
        } else {
          failPurchaseCnt.getAndIncrement();
        }
      } catch (ApiException e) {
        failPurchaseCnt.getAndIncrement();
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
      }
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

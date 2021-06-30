package model;

import io.swagger.client.JSON;
import java.sql.Timestamp;
import org.threeten.bp.LocalDateTime;

public class PurchaseRecord {
  private String uuid;
  private Integer storeID;
  private Integer customerID;
  private String date;
  private Timestamp createTime;
  private JSON purchaseBody;

  public PurchaseRecord(String uuid, Integer storeID, Integer customerID, String date,
      Timestamp createTime, JSON purchaseBody) {
    this.uuid = uuid;
    this.storeID = storeID;
    this.customerID = customerID;
    this.date = date;
    this.createTime = createTime;
    this.purchaseBody = purchaseBody;
  }

  public String getUuid() {
    return uuid;
  }

  public Integer getStoreID() {
    return storeID;
  }

  public Integer getCustomerID() {
    return customerID;
  }

  public String getDate() {
    return date;
  }

  public Timestamp getCreateTime() {
    return createTime;
  }

  public JSON getPurchaseBody() {
    return purchaseBody;
  }
}

package dal;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

public class PurchasesDao {
  private Connection connection;
  private PreparedStatement ps;

  public PurchasesDao(DataSource pool) {
    try {
      connection = pool.getConnection();
      ps = null;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void createSchema() {
    String sql = null;
    try {
      sql = "CREATE SCHEMA IF NOT EXISTS SuperMarketDatabase";
      ps = connection.prepareStatement(sql);
      ps.executeUpdate();

      sql = "USE SuperMarketDatabase";
      ps = connection.prepareStatement(sql);
      ps.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  public void createTable() {
    String sql = null;
    try {
      sql = "DROP TABLE IF EXISTS Purchases";
      ps = connection.prepareStatement(sql);
      ps.executeUpdate();

      sql = "CREATE TABLE Purchases ("
          + "  UUID INT NOT NULL"
          + "  StoreID INT NOT NULL,"
          + "  CustomerID INT NOT NULL,"
          + "  Date VARCHAR(255) NOT NULL,"
          + "  CreateTime TIMESTAMP NOT NULL,"
          + "  PurchaseBody VARCHAR(2000) NOT NULL,"
          + "  CONSTRAINT pk_PurchaseUUID_Purchases PRIMARY KEY(StoreID, CustomerID, Date, CreateTime)"
          + ");";
      ps = connection.prepareStatement(sql);
      ps.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void deleteTable() {
    String sql = "DROP TABLE IF EXISTS Purchases";
    try {
      ps = connection.prepareStatement(sql);
      ps.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void closeConnection() {
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public boolean insertIntoPurchases(String UUID, Integer storeID, Integer customerID, String date,
      String createTime, String purchaseBody) {
    Boolean isSuccess = false;

    while (!isSuccess) {
      // keep retry when failed
      isSuccess = insert(UUID, storeID, customerID, date, createTime, purchaseBody);
    }
    return true;
  }

  private boolean insert(String UUID, Integer storeID, Integer customerID, String date,
      String createTime, String purchaseBody) {
//      Purchase purchaseBody) {
    String sql;
    try {
      sql = "USE SuperMarketDatabase";
      ps = connection.prepareStatement(sql);
      ps.executeUpdate();

      sql = "INSERT INTO Purchases (UUID, StoreID, CustomerID, Date, CreateTime, PurchaseBody) " +
          "VALUES (?,?,?,?,?,?)";
      ps = connection.prepareStatement(sql);
      ps.setString(1, UUID);
      ps.setInt(2, storeID);
      ps.setInt(3, customerID);
      ps.setString(4, date);
      ps.setString(5, createTime);
      ps.setString(6, purchaseBody);
      // execute insert SQL statement
      ps.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

}

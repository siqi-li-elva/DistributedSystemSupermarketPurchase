CREATE SCHEMA IF NOT EXISTS SuperMarketDatabase;
USE SuperMarketDatabase;

DROP TABLE IF EXISTS Purchases;

CREATE TABLE Purchases (
  UUID VARCHAR(255) NOT NULL,
  StoreID INT NOT NULL,
  CustomerID INT NOT NULL,
  Date VARCHAR(255) NOT NULL,
  CreateTime TIMESTAMP NOT NULL,
  PurchaseBody JSON NOT NULL,
  CONSTRAINT pk_PurchaseUUID_Purchases PRIMARY KEY(UUID)
);

--   CONSTRAINT pk_PurchaseUUID_Purchases PRIMARY KEY(UUID, StoreID, CustomerID, Date, CreateTime)
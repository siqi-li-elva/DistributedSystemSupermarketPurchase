CREATE SCHEMA IF NOT EXISTS SuperMarketDatabase;
USE SuperMarketDatabase;

DROP TABLE IF EXISTS Purchases;

CREATE TABLE Purchases (
  UUID VARCHAR(255) NOT NULL,
  StoreID INT NOT NULL,
  CustomerID INT NOT NULL,
  Date VARCHAR(255) NOT NULL,
  CreateTime TIMESTAMP NOT NULL,
  PurchaseBody VARCHAR(2000) NOT NULL,
  CONSTRAINT pk_PurchaseUUID_Purchases PRIMARY KEY(UUID)
);
#CONSTRAINT pk_PurchaseUUID_Purchases PRIMARY KEY(UUID, StoreID, CustomerID, Date, CreateTime)
# '008ab1a7-7e34-4512-81db-a703d291f218', '27', '27156', '2021-01-01', '2021-03-22 22:44:43', '{\"items\": [{\"ItemID\": \"44126\", \"numberOfItems:\": 1}, {\"ItemID\": \"5269\", \"numberOfItems:\": 1}, {\"ItemID\": \"70191\", \"numberOfItems:\": 1}, {\"ItemID\": \"50839\", \"numberOfItems:\": 1}, {\"ItemID\": \"68305\", \"numberOfItems:\": 1}]}'

-- INSERT INTO Purchases (UUID, StoreID, CustomerID, Date, CreateTime, PurchaseBody)
-- 	VALUES ('014c7476-1b40-4db1-8c6a-2624072e10eb', '12', '12086', '2021-01-01', '2021-03-22 22:44:42', '{\"items\": [{\"ItemID\": \"51771\", \"numberOfItems:\": 1}, {\"ItemID\": \"20274\", \"numberOfItems:\": 1}, {\"ItemID\": \"59925\", \"numberOfItems:\": 1}, {\"ItemID\": \"10337\", \"numberOfItems:\": 1}, {\"ItemID\": \"70195\", \"numberOfItems:\": 1}]}'
-- );
--This script is used for unit test cases, DO NOT CHANGE!

DROP TABLE IF EXISTS Customer;
DROP SEQUENCE IF EXISTS CUST_ID_SEQ; 

CREATE SEQUENCE CUST_ID_SEQ START WITH 10000000 INCREMENT BY 1;

CREATE TABLE Customer (
 CustomerId LONG DEFAULT CUST_ID_SEQ.NEXTVAL PRIMARY KEY NOT NULL,
 CustomerName VARCHAR(30) NOT NULL,
 Email VARCHAR(30) NOT NULL);


CREATE UNIQUE INDEX cust_index on Customer(CustomerId, Email);

INSERT INTO Customer (CustomerName, Email) VALUES ('John Smith','john.smith@yahoo.com');
INSERT INTO Customer (CustomerName, Email) VALUES ('Ashish Mishra','ashish.mishra@yahoo.com');
INSERT INTO Customer (CustomerName, Email) VALUES ('Red Taylor','red.taylor@yahoo.com');

DROP TABLE IF EXISTS Account;
DROP SEQUENCE IF EXISTS ACCT_ID_SEQ; 
CREATE SEQUENCE ACCT_ID_SEQ START WITH 10000000 INCREMENT BY 1;

CREATE TABLE Account (AccountId LONG DEFAULT ACCT_ID_SEQ.NEXTVAL PRIMARY KEY NOT NULL,
CustomerId LONG,
Balance DECIMAL(19,4),
CurrencyCode VARCHAR(30),
Status VARCHAR(5)
);

CREATE UNIQUE INDEX acct_index on Account(AccountId, CurrencyCode);

INSERT INTO Account (CustomerId, Balance, CurrencyCode, Status) VALUES (10000000, 100.0000, 'USD', 'true');
INSERT INTO Account (CustomerId, Balance, CurrencyCode, Status) VALUES (10000001, 200.0000, 'USD', 'true');
INSERT INTO Account (CustomerId, Balance, CurrencyCode, Status) VALUES (10000002, 500.0000, 'USD', 'false');

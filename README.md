# Fund Transfer Project

This is a **REST** project to transfer fund between customers bank accounts using **JAX-RS** and **Jetty** as an embedded server. The **H2 DB** is being used as in-memory DB which holds customers and accounts related necessary information for demo and test purpose.

This project mainly focus on **fund-transfer** operation between accounts. For testing purpose, only account creation can be possible. No other operations such as account/customer deletion, account/customer retrieval, etc. available. By default currency code is **USD** and account status is **true**.


**Tech specification :**

	1. Java (v1.8) 
	2. JAX-RS (v2.1.1) 
	3. H2 DB - In memory DB (v1.4.199) 
	4. Jetty (9.2.3.v20140905) 
	5. Glassfish-Jersey (2.7) 
	6. HikariCP (v3.3.1) 
	7. Jackson (v2.9.9) 
	8. Self4J (v1.7.28) 
	7. Maven - (v4.0.0) 
	8. JUnit - (v4.12) 
	9. Mockito - (v1.10.19) 
	10.PowerMock - (v2.0.2) 

**DB Tables/Sequences:**
	
	Tables: 
	- CUSTOMER (CUSTOMERID, CUSTOMERNAME, EMAIL) 
	  - PK : CUSTOMERID
	- ACCOUNT (ACCOUNTID, CUSTOMERID, CURRENCYCODE, STATUS) 
	  - PK : ACCOUNTID, FK: CUSTOMERID
	  
	Sequences:
		1. CUST_ID_SEQ (Initial starts from 10000000)
		2. ACCT_ID_SEQ (Initial starts from 10000000)
  
**Classes, interfaces, entities and exceptions :***
	
	- FundTransferApp.java		: Main class of the application
	- DBConfig.java			: DB specific configuration
	- RepositoryFactory.java	: Abstract factory class to get DB specific repository instance
	  - H2DBRepository.java		: H2 DB specific repository class which extends the RepositoryFactory class
	- AccountRepository.java	: Repository Interface for account related DB operations
	  - AccountRepositoryImpl.java	: Implementation for account related DB operations
	- CustomerRepository.java	: Repository Interface for customer related DB operations
	  - CustomerRepositoryImpl.java	: Implementation for customer related DB operations
	- FundTransferService.java	: Interface for fund transfer related operations 
	  - FundTransferServiceImpl.java: Implementation for fund transfer related operations
	- DataValidator.java		: Data validator class
	- ValidatorFactory.java		: Factory class to get specific bean validator instance
	  - BeanValidator.java		: Abstract Bean validator class having validator method
	  - FundTransferValidator.java	: Actual implementation of bean validation by extending BeanValidator class for FundTransfer
	  - AccountRequestValidator.java: Actual implementation of bean validation by extending BeanValidator class for AccountRequest
	- FundTransferController.java	: REST controller which serves the POST requests
	- AccountController.java:	: REST controller for testing purpose to create an account
	- PropertyReader.java		: Application specific property reader class
	- APIUtil.java			: Class with utility methods
	- APIExceptionMapper.java	: Application wide exception mapper class for various kind of API exception
	  - APIException.java		: API Exception class
	- JettyServer.java		: Embedded Jetty server class implementation with server initialization and startup
	- Account.java			: Account entity class and mapped with ACCOUNT table
	- APISuccess.java		: Entity class for final API success response
	- Customer.java			: Customer entity class and mapped with CUSTOMER table
	- ErrorDetail.java		: Place holder entity for bean validation for properties
	- FundTransfer.java		: Request entity for fund transfer
	- AccountRequest.java		: Request entity for account creation
	

**API Constraints**
- Fund transfer payload's fields and format:  
	- formAccount 
		- This must contains digits only. 
		- The account length: min of 8 digits and max of 16 digits. 
	- toAccount 
		- This must contains digits only. 
		- The account length: min of 8 digits and max of 16 digits. 
	- amount 
		- This must contains digits only along with decimal point(.) if any. 
- Fund transfer between same account is not possible. 
- Fund transfer will failed if any of the account not exist.
- Fund transfer can't be possible between any of the inactive account.
- Fund transfer can't be possible if debit account doesn't have sufficient fund.

**Configurations and other settings** 

This project has **src/main/resources/application.properties** file for application specific configuration like server, db and logging, etc.
You can use *fund.transfer.default.db* key to specify default db and that db specific properties. For demo and test, H2 db as default DB. The DB specific schema also you can create and execute by specifying the location with key *db.schema.file.location*. For test cases and demo, the schema file has been placed in **src/main/resources/H2DB-SCHEMA.sql** with sample tables and sequence creation along with few dummy entries.

	src/main/resource/application.properties
	
	To specify jetty server port, change below key 
	jetty.server.port=8080
	
	To specify default DB of application, change below key
	fund.transfer.default.db=H2
	You can use MYSQL, SQLSERVER, ORACLE for future upgrade.
	
	To specify db schema file,
	db.schema.file.location=src/main/resources/{DB-NAME}DB-SCHEMA.sql
	
	To specify db schema recreation on server startup.
	db.schema.recreate=true

### Build and run the project

**To build the project**

``` 
mvn clean install
```
**To build the project without test cases**

``` 
mvn clean install -Dmaven.test.skip=true
```
**To run the project**

``` 
mvn exec:java
```

**API Endpoint**

	Endpoint for Fund Transfer:
	
	URL: http://localhost:8080/api/fund/transfer
	Method: POST
	Request Headers: 
		Content-Type: application/json
		
	Endpoint for creating an account:
	URL: http://localhost:8080/api/account
	Method: POST
	Request Headers: 
		Content-Type: application/json


### API Response Code Meaning

| API Code | HTTP Response Code | Code Description |
| -----------| ------ | ------ |
| - | 200 OK | The request has succeeded |
| API100 | 404 Not Found | The given account not exist. Account no. {ac_no} |
| API200 | 412 Precondition Failed | The given account is not active. Account no. {ac_no} |
| API300 | 412 Precondition Failed | The debit account doesn't have sufficient fund. |
| API400 | 400 Bad Request | Payload is empty or null. |
| API401 | 400 Bad Request | Payload contains data in invalid format. |
| API402 | 400 Bad Request | The debit and credit account number are same. |
| API500 | 500 Internal Server Error| There was an internal API problem. |


**Response Message Structure**

| Field Name | Meaning |
| -----------| ------ |
| message	| This field contains information about request success/failure |
| timestamp	| This field contains date and time about server response |
| transactionId	| Unique transaction reference number generated at server and available in server log as well |
| code	| API failure code. Refer API response code meaning table |
| errors	| API payload field wise data validation failure along with field name and failure reason |


### API Requests / Responses.

**Sample test data available in table**

| CustomerID | AccountId | Balance |CurrencyCode | Status |
| -----------| ------ | ------ |  ------ | ------ |
| 10000000 | 10000000 | 100.0000 | USD | true |
| 10000001 | 10000001 | 200.0000 | USD | true |
| 10000002 | 10000002 | 500.0000 | USD | false |

**API Request/Response for /api/fund/transfer**

``` 
{
    "fromAccount": "10000000",
    "toAccount": "10000001",
    "amount": "50.00"
}
```

**Valid Payload Response: 200 OK**

```
{
    "message": "Fund transfer has been successful.",
    "timestamp": "2019-08-20T10:19:05.279",
    "transactionId": "37784222-2444-423c-928c-207dc6662a59"
}
```

**Invalid Payload, Response: 400 BAD REQUEST**

```
1. Account no. with invalid format data

{
    "code": "API401",
    "errors": [
        {
            "desc": "Invalid account no. Valid account contains digits only of min 8 and max 16 in length.",
            "name": "fromAccount"
        }
    ],
    "message": "Payload contains data invalid format.",
    "timestamp": "2019-08-20T10:09:17.468",
    "transactionId": "efa4c4b3-7d1e-401c-8646-07cc2e3f2dce"
}

2. Amount and account no. with invalid data

{
    "code": "API401",
    "errors": [
        {
            "desc": "Invalid account no. Valid account contains digits only of min 8 and max 16 in length.",
            "name": "fromAccount"
        },
        {
            "desc": "Invalid format data supplied with amount field.",
            "name": "amount"
        }
    ],
    "message": "Payload contains data invalid format.",
    "timestamp": "2019-08-20T10:10:07.449",
    "transactionId": "bacbb2c9-478f-4cd9-83b7-e073944d533c"
}

3. All the field with invalid data

{
    "code": "API401",
    "errors": [
        {
            "desc": "Invalid account no. Valid account contains digits only of min 8 and max 16 in length.",
            "name": "fromAccount"
        },
        {
            "desc": "Invalid account no. Valid account contains digits only of min 8 and max 16 in length.",
            "name": "toAccount"
        },
        {
            "desc": "Invalid format data supplied with amount field.",
            "name": "amount"
        }
    ],
    "message": "Payload contains data invalid format.",
    "timestamp": "2019-08-20T10:11:19.98",
    "transactionId": "fe6df7f8-f653-417f-adfa-659d54705e19"
}

```

**Account not exists, Response: 404 NOT FOUND**

```
{
    "code": "API100",
    "message": "The given account not exist. Account no. 1000000011",
    "timestamp": "2019-08-20T10:19:47.643",
    "transactionId": "cead1eb2-4859-4c9c-a3de-f8faeac3df3e"
}
```

**Account is not active, Response: 412 PRECONDITION FAILED**

```
{
    "code": "API200",
    "message": "The given account is not active. Account no. 10000002",
    "timestamp": "2019-08-20T10:21:05.442",
    "transactionId": "94f0e086-37d6-40fa-a3a7-7cc2a70524dc"
}
```

**Account doesn't have sufficient fund, Response: 412 PRECONDITION FAILED**

```
{
    "code": "API300",
    "message": "The debit account doesn't have sufficient fund.",
    "timestamp": "2019-08-20T10:22:07.926",
    "transactionId": "11f7fdc1-90d9-45f0-9c63-a2a68c925b5f"
}
```

**API Request/Response for /api/account**

```
{
    "customerName": "XXX-YYY",
    "email": "abc@test.com",
    "initialDeposit": "10000"
}
```
**Valid Payload Response: 200 OK**

```
	{
	    "message": "Customer Id: 10000006, accountId: 10000006",
	    "timestamp": "2019-08-20T19:44:56.452",
	    "transactionId": "ae3e8b45-c726-4a79-9832-06a0015ac5bd"
	}
```

**Invalid Payload Response: 400 BAD REQUEST**

``` 
{
    "code": "API401",
    "errors": [
        {
            "desc": "Invalid amount data. It must contains digits along with decimal point.",
            "name": "initialDeposit"
        }
    ],
    "message": "Payload contains data in invalid format.",
    "timestamp": "2019-08-20T19:47:40.973",
    "transactionId": "52f7698f-dc46-4667-be4b-fabd9c1342dc"
}
``` 
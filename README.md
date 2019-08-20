# Fund Transfer Project

This is simple fund-transfer REST project between bank accounts using JAX-RS and Jetty as an embedded server. The H2 in memory DB has been used with simple db schema where there are only two tables *customer* and *account* which holds necessary information.


**Required Tech-spec :**
1. 	Java (v1.8)
2. 	JAX-RS (v2.1.1)
3. 	H2 DB - In memory DB (v1.4.199)
4. 	Jetty (9.2.3.v20140905)
5. 	Glassfish-Jersey (2.7)
6. 	Hikari (v3.3.1)
7.	Jackson (v2.9.9)
8.	Self4J (v1.7.28)
7. 	Maven - (v4.0.0)
8. 	JUnit - (v4.12)
9. 	Mockito - (v1.10.19)
10. PowerMock - (v2.0.2)

**Additional Attachements:**
- Postman rest client project
- Code coverage report

**DB Tables:**

- CUSTOMER (CUSTOMERID, CUSTOMERNAME, EMAIL) 
  - PK : CUSTOMERID
- ACCOUNT (ACCOUNTID, CUSTOMERID, CURRENCYCODE, STATUS) 
  - PK : ACCOUNTID, FK: CUSTOMERID
  
### Build and run the project

To build the project

``` 
mvn clean install
```
To run the project

``` 
mvn exec:java
```


### API Response Code Meaning

| API Code | HTTP Response Code | Code Description |
| -----------| ------ | ------ |
|  | 200 OK | The request has succeeded |
| API400 | 400 Bad Request | Payload is empty or null. |
| API401 | 400 Bad Request | Payload contains data in invalid format. |
| API402 | 400 Bad Request | The debit and credit accounts are same. |
| API100 | 404 Not Found | The given account not exist. Account no. {ac. no} |
| API200 | 412 Precondition Failed | The given account is not active. Account no. {ac. no} |
| API300 | 412 Precondition Failed | The debit account doesn't have sufficient fund. |
| API500 | 500 Internal Server Error| There was an internal API problem. |


###API Request and various responses with response code.

**API Request**

``` 
{
    "fromAccount": "10000000",
    "toAccount": "10000001",
    "amount": "50.00"
}
```

**Success Response: 200 OK**

```
{
    "message": "Fund transfer has been successful.",
    "timestamp": "2019-08-20T10:19:05.279",
    "transactionId": "37784222-2444-423c-928c-207dc6662a59"
}
```

**Payload data with invalid format response: 400 BAD REQUEST**

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

**Resource not found response: 404 NOT FOUND**

```
{
    "code": "API100",
    "message": "The given account not exist. Account no. 1000000011",
    "timestamp": "2019-08-20T10:19:47.643",
    "transactionId": "cead1eb2-4859-4c9c-a3de-f8faeac3df3e"
}
```

**Resource is not active response: 412 PRECONDITION FAILED**

```
{
    "code": "API200",
    "message": "The given account is not active. Account no. 10000002",
    "timestamp": "2019-08-20T10:21:05.442",
    "transactionId": "94f0e086-37d6-40fa-a3a7-7cc2a70524dc"
}
```

**Resource doesn't have sufficient fund response: 412 PRECONDITION FAILED**

```
{
    "code": "API300",
    "message": "The debit account doesn't have sufficient fund.",
    "timestamp": "2019-08-20T10:22:07.926",
    "transactionId": "11f7fdc1-90d9-45f0-9c63-a2a68c925b5f"
}
``` 


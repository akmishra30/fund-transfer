package com.fund.transfer.persistence; // Matching the main package structure as best practice

import com.demo.fund.transfer.db.RepositoryFactory;
import com.demo.fund.transfer.entity.Account;
import com.demo.fund.transfer.exception.APIException;
import com.demo.fund.transfer.persistence.AccountRepositoryImpl;
import com.demo.fund.transfer.util.APIConstants; // For potential exceptions
import com.demo.fund.transfer.util.APIUtil;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountRepositoryImplTest {

    @Mock
    private RepositoryFactory mockRepositoryFactory;
    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;
    
    @Captor
    private ArgumentCaptor<Boolean> booleanArgumentCaptor;
    @Captor
    private ArgumentCaptor<Long> longArgumentCaptor;
    @Captor
    private ArgumentCaptor<BigDecimal> bigDecimalArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;


    // Class under test
    private AccountRepositoryImpl accountRepository;

    private static final String INSERT_ACCT_SQL = "INSERT INTO ACCOUNT (CUSTOMERID, BALANCE, CURRENCYCODE, STATUS) VALUES (?, ?, ?, ?);";
    private static final String SELECT_ACCT_SQL = "SELECT * FROM ACCOUNT WHERE ACCOUNTID = ?";
    private static final String SELECT_ACCT_FOR_LOCK_SQL = "SELECT * FROM ACCOUNT WHERE ACCOUNTID = ? FOR UPDATE";
    private static final String UPDATE_FUND_ACCT_SQL = "UPDATE ACCOUNT SET BALANCE = ? WHERE ACCOUNTID = ?";

    @Mock
    private PreparedStatement mockDebitLockStmt; // Or lockStmt1
    @Mock
    private PreparedStatement mockCreditLockStmt; // Or lockStmt2
    @Mock
    private PreparedStatement mockUpdateStmt;
    @Mock
    private ResultSet mockDebitRs; // Or rs1
    @Mock
    private ResultSet mockCreditRs; // Or rs2

    @Before
    public void setUp() throws SQLException {
        // Initialize AccountRepositoryImpl with the mocked factory
        accountRepository = new AccountRepositoryImpl(mockRepositoryFactory);
        
        // Common mocking behavior for getting a connection
        when(mockRepositoryFactory.getDBConnection()).thenReturn(mockConnection);
    }

    @Test
    public void testCreateAccount_success_setsBooleanStatus() throws APIException, SQLException {
        // Arrange
        Account newAccount = new Account(100L, BigDecimal.valueOf(1000), "USD", true);
        long expectedGeneratedAccountId = 12345L;

        when(mockConnection.prepareStatement(eq(INSERT_ACCT_SQL), eq(Statement.RETURN_GENERATED_KEYS)))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // 1 row affected
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong(1)).thenReturn(expectedGeneratedAccountId);

        // Act
        Account createdAccount = accountRepository.createAccount(newAccount);

        // Assert
        // Verify that setBoolean was called for the status
        verify(mockPreparedStatement).setLong(eq(1), eq(newAccount.getCustomerId()));
        verify(mockPreparedStatement).setBigDecimal(eq(2), eq(newAccount.getBalance()));
        verify(mockPreparedStatement).setString(eq(3), eq(newAccount.getCurrency()));
        verify(mockPreparedStatement).setBoolean(eq(4), eq(newAccount.getActive())); // Key assertion for boolean status
        
        assertEquals(expectedGeneratedAccountId, createdAccount.getAccountNo());
        assertTrue(createdAccount.getActive()); // Ensure the status is correctly set in the returned object
        
        // Verify resources are closed (using APIUtil.closeDbObjects)
        // This requires mocking APIUtil.closeDbObjects if we want to verify it.
        // For now, let's assume it's called. More detailed verification can be added if needed.
    }
    
    @Test
    public void testCreateAccount_noGeneratedKey_throwsAPIException() throws SQLException {
        Account newAccount = new Account(100L, BigDecimal.valueOf(1000), "USD", true);

        when(mockConnection.prepareStatement(eq(INSERT_ACCT_SQL), eq(Statement.RETURN_GENERATED_KEYS)))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // Simulate no generated key

        APIException exception = assertThrows(APIException.class, () -> {
            accountRepository.createAccount(newAccount);
        });
        assertEquals(APIConstants.API_INTERNAL_ERROR, exception.getCode());
        assertEquals("There was a problem while creating an account.", exception.getMessage());
    }

    @Test
    public void testGetAccount_success_readsBooleanStatus() throws APIException, SQLException {
        // Arrange
        long accountNoToFetch = 500L;
        Account expectedAccount = new Account(101L, accountNoToFetch, BigDecimal.valueOf(2000), "EUR", false);

        when(mockConnection.prepareStatement(SELECT_ACCT_SQL)).thenReturn(mockPreparedStatement);
        // mockPreparedStatement.setLong(1, accountNoToFetch) will be called
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true); // Simulate account found
        when(mockResultSet.getLong("CustomerId")).thenReturn(expectedAccount.getCustomerId());
        when(mockResultSet.getLong("AccountId")).thenReturn(expectedAccount.getAccountNo());
        when(mockResultSet.getBigDecimal("Balance")).thenReturn(expectedAccount.getBalance());
        when(mockResultSet.getString("CurrencyCode")).thenReturn(expectedAccount.getCurrency());
        when(mockResultSet.getBoolean("Status")).thenReturn(expectedAccount.getActive()); // Key assertion for reading boolean

        // Act
        Account fetchedAccount = accountRepository.getAccount(accountNoToFetch);

        // Assert
        verify(mockPreparedStatement).setLong(1, accountNoToFetch);
        assertNotNull(fetchedAccount);
        assertEquals(expectedAccount.getCustomerId(), fetchedAccount.getCustomerId());
        assertEquals(expectedAccount.getAccountNo(), fetchedAccount.getAccountNo());
        assertEquals(0, expectedAccount.getBalance().compareTo(fetchedAccount.getBalance())); // BigDecimal comparison
        assertEquals(expectedAccount.getCurrency(), fetchedAccount.getCurrency());
        assertEquals(expectedAccount.getActive(), fetchedAccount.getActive()); // Key assertion for boolean status
    }

    @Test
    public void testGetAccount_notFound() throws APIException, SQLException {
        long accountNoToFetch = 501L;
        when(mockConnection.prepareStatement(SELECT_ACCT_SQL)).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // Simulate account not found

        Account fetchedAccount = accountRepository.getAccount(accountNoToFetch);

        assertNull(fetchedAccount);
    }
    
    @Test
    public void testCreateAccount_sqlException_throwsAPIException() throws SQLException {
        Account newAccount = new Account(100L, BigDecimal.valueOf(1000), "USD", true);
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenThrow(new SQLException("DB connection error"));

        APIException exception = assertThrows(APIException.class, () -> {
            accountRepository.createAccount(newAccount);
        });
        assertEquals(APIConstants.API_INTERNAL_ERROR, exception.getCode());
    }

    @Test
    public void testGetAccount_sqlException_throwsAPIException() throws SQLException {
        long accountNoToFetch = 500L;
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB query error"));
        
        APIException exception = assertThrows(APIException.class, () -> {
            accountRepository.getAccount(accountNoToFetch);
        });
        assertEquals(APIConstants.API_INTERNAL_ERROR, exception.getCode());
    }

    // Test for fromAccountId < toAccountId
    @Test
    public void testFundTransfer_success_fromAccountLessThanToAccount_locksInOrder() throws APIException, SQLException {
        FundTransfer transfer = new FundTransfer("100", "200", "50.00", "USD", "txn123");
        long fromAccId = Long.parseLong(transfer.getFromAccount()); // 100
        long toAccId = Long.parseLong(transfer.getToAccount());     // 200

        Account fromAccountInitial = new Account(1L, fromAccId, new BigDecimal("100.00"), "USD", true);
        Account toAccountInitial = new Account(2L, toAccId, new BigDecimal("100.00"), "USD", true);

        // Mocking connection behavior
        when(mockConnection.prepareStatement(SELECT_ACCT_FOR_LOCK_SQL))
            .thenReturn(mockDebitLockStmt)  // This will be lockStmt1 (for fromAccId=100)
            .thenReturn(mockCreditLockStmt); // This will be lockStmt2 (for toAccId=200)

        when(mockConnection.prepareStatement(UPDATE_FUND_ACCT_SQL)).thenReturn(mockUpdateStmt);

        // Mocking locking fromAccount (ID: 100) - lockStmt1
        when(mockDebitLockStmt.executeQuery()).thenReturn(mockDebitRs);
        when(mockDebitRs.next()).thenReturn(true);
        when(mockDebitRs.getLong("CustomerId")).thenReturn(fromAccountInitial.getCustomerId());
        when(mockDebitRs.getLong("AccountId")).thenReturn(fromAccountInitial.getAccountNo());
        when(mockDebitRs.getBigDecimal("Balance")).thenReturn(fromAccountInitial.getBalance());
        when(mockDebitRs.getString("CurrencyCode")).thenReturn(fromAccountInitial.getCurrency());
        when(mockDebitRs.getBoolean("Status")).thenReturn(fromAccountInitial.getActive());

        // Mocking locking toAccount (ID: 200) - lockStmt2
        when(mockCreditLockStmt.executeQuery()).thenReturn(mockCreditRs);
        when(mockCreditRs.next()).thenReturn(true);
        when(mockCreditRs.getLong("CustomerId")).thenReturn(toAccountInitial.getCustomerId());
        when(mockCreditRs.getLong("AccountId")).thenReturn(toAccountInitial.getAccountNo());
        when(mockCreditRs.getBigDecimal("Balance")).thenReturn(toAccountInitial.getBalance());
        when(mockCreditRs.getString("CurrencyCode")).thenReturn(toAccountInitial.getCurrency());
        when(mockCreditRs.getBoolean("Status")).thenReturn(toAccountInitial.getActive());
        
        when(mockUpdateStmt.executeBatch()).thenReturn(new int[]{1, 1}); // Simulate 2 updates

        // Act
        boolean result = accountRepository.fundTransfer(transfer);

        // Assert
        assertTrue(result);
        // Verify lock order (lockStmt1 uses fromAccId, lockStmt2 uses toAccId)
        verify(mockDebitLockStmt).setLong(1, fromAccId); 
        verify(mockCreditLockStmt).setLong(1, toAccId);  
        
        // Verify parameters for batch update
        // Debit account (fromAccId=100) update: Balance 100.00 - 50.00 = 50.00
        verify(mockUpdateStmt).setBigDecimal(eq(1), eq(new BigDecimal("50.00")));
        verify(mockUpdateStmt).setLong(eq(2), eq(fromAccId));
        verify(mockUpdateStmt).addBatch(); // First addBatch call

        // Credit account (toAccId=200) update: Balance 100.00 + 50.00 = 150.00
        verify(mockUpdateStmt).setBigDecimal(eq(1), eq(new BigDecimal("150.00")));
        verify(mockUpdateStmt).setLong(eq(2), eq(toAccId));
        verify(mockUpdateStmt).addBatch(); // Second addBatch call
        
        verify(mockConnection).commit();
        verify(mockConnection, Mockito.never()).rollback();
        
        verify(mockDebitRs).close();
        verify(mockCreditRs).close();
        verify(mockDebitLockStmt).close();
        verify(mockCreditLockStmt).close();
        verify(mockUpdateStmt).close();
        verify(mockConnection).close(); // Verifying connection close is part of the finally block in SUT
    }

    @Test
    public void testFundTransfer_success_fromAccountGreaterThanToAccount_locksInOrder() throws APIException, SQLException {
        FundTransfer transfer = new FundTransfer("200", "100", "20.00", "USD", "txn124");
        long fromAccId = Long.parseLong(transfer.getFromAccount()); // 200
        long toAccId = Long.parseLong(transfer.getToAccount());     // 100

        Account fromAccountInitial = new Account(2L, fromAccId, new BigDecimal("100.00"), "USD", true);
        Account toAccountInitial = new Account(1L, toAccId, new BigDecimal("100.00"), "USD", true);
        
        // Mocking connection behavior: lockStmt1 (for toAccId=100), lockStmt2 (for fromAccId=200)
        when(mockConnection.prepareStatement(SELECT_ACCT_FOR_LOCK_SQL))
            .thenReturn(mockCreditLockStmt) // This will be lockStmt1 (for toAccId=100)
            .thenReturn(mockDebitLockStmt);  // This will be lockStmt2 (for fromAccId=200)
        when(mockConnection.prepareStatement(UPDATE_FUND_ACCT_SQL)).thenReturn(mockUpdateStmt);
            
        // Mocking locking toAccount (ID: 100) - lockStmt1 (which is mockCreditLockStmt here)
        when(mockCreditLockStmt.executeQuery()).thenReturn(mockCreditRs);
        when(mockCreditRs.next()).thenReturn(true);
        when(mockCreditRs.getLong("CustomerId")).thenReturn(toAccountInitial.getCustomerId());
        when(mockCreditRs.getLong("AccountId")).thenReturn(toAccountInitial.getAccountNo());
        when(mockCreditRs.getBigDecimal("Balance")).thenReturn(toAccountInitial.getBalance());
        when(mockCreditRs.getString("CurrencyCode")).thenReturn(toAccountInitial.getCurrency());
        when(mockCreditRs.getBoolean("Status")).thenReturn(toAccountInitial.getActive());

        // Mocking locking fromAccount (ID: 200) - lockStmt2 (which is mockDebitLockStmt here)
        when(mockDebitLockStmt.executeQuery()).thenReturn(mockDebitRs);
        when(mockDebitRs.next()).thenReturn(true);
        when(mockDebitRs.getLong("CustomerId")).thenReturn(fromAccountInitial.getCustomerId());
        when(mockDebitRs.getLong("AccountId")).thenReturn(fromAccountInitial.getAccountNo());
        when(mockDebitRs.getBigDecimal("Balance")).thenReturn(fromAccountInitial.getBalance());
        when(mockDebitRs.getString("CurrencyCode")).thenReturn(fromAccountInitial.getCurrency());
        when(mockDebitRs.getBoolean("Status")).thenReturn(fromAccountInitial.getActive());

        when(mockUpdateStmt.executeBatch()).thenReturn(new int[]{1, 1});

        // Act
        boolean result = accountRepository.fundTransfer(transfer);

        // Assert
        assertTrue(result);
        // Verify lock order (lockStmt1 uses toAccId, lockStmt2 uses fromAccId)
        verify(mockCreditLockStmt).setLong(1, toAccId); 
        verify(mockDebitLockStmt).setLong(1, fromAccId);  
        
        // Verify updates. Debit account (fromAccId=200) is added to batch first.
        // Balances: fromAccount (200): 100.00 - 20.00 = 80.00
        verify(mockUpdateStmt).setBigDecimal(eq(1), eq(new BigDecimal("80.00"))); 
        verify(mockUpdateStmt).setLong(eq(2), eq(fromAccId));
        verify(mockUpdateStmt).addBatch(); // First addBatch

        // Credit account (toAccId=100): Balance 100.00 + 20.00 = 120.00
        verify(mockUpdateStmt).setBigDecimal(eq(1), eq(new BigDecimal("120.00"))); 
        verify(mockUpdateStmt).setLong(eq(2), eq(toAccId));
        verify(mockUpdateStmt).addBatch(); // Second addBatch
        
        verify(mockConnection).commit();
        verify(mockConnection, Mockito.never()).rollback();
        
        verify(mockCreditRs).close();
        verify(mockDebitRs).close();
        verify(mockCreditLockStmt).close();
        verify(mockDebitLockStmt).close();
        verify(mockUpdateStmt).close();
        verify(mockConnection).close(); // Verifying connection close is part of the finally block in SUT
    }

    @Test
    public void testFundTransfer_insufficientFunds_throwsAPIExceptionAndRollsBack() throws SQLException, APIException {
        FundTransfer transfer = new FundTransfer("100", "200", "150.00", "USD", "txn125");
        long fromAccId = Long.parseLong(transfer.getFromAccount());
        long toAccId = Long.parseLong(transfer.getToAccount());

        Account fromAccountInitial = new Account(1L, fromAccId, new BigDecimal("100.00"), "USD", true); 
        Account toAccountInitial = new Account(2L, toAccId, new BigDecimal("100.00"), "USD", true);    

        when(mockConnection.prepareStatement(SELECT_ACCT_FOR_LOCK_SQL))
            .thenReturn(mockDebitLockStmt).thenReturn(mockCreditLockStmt);

        when(mockDebitLockStmt.executeQuery()).thenReturn(mockDebitRs);
        when(mockDebitRs.next()).thenReturn(true);
        when(mockDebitRs.getLong("CustomerId")).thenReturn(fromAccountInitial.getCustomerId());
        when(mockDebitRs.getLong("AccountId")).thenReturn(fromAccountInitial.getAccountNo());
        when(mockDebitRs.getBigDecimal("Balance")).thenReturn(fromAccountInitial.getBalance());
        when(mockDebitRs.getString("CurrencyCode")).thenReturn(fromAccountInitial.getCurrency());
        when(mockDebitRs.getBoolean("Status")).thenReturn(fromAccountInitial.getActive());

        when(mockCreditLockStmt.executeQuery()).thenReturn(mockCreditRs);
        when(mockCreditRs.next()).thenReturn(true);
        when(mockCreditRs.getLong("CustomerId")).thenReturn(toAccountInitial.getCustomerId());
        when(mockCreditRs.getLong("AccountId")).thenReturn(toAccountInitial.getAccountNo());
        when(mockCreditRs.getBigDecimal("Balance")).thenReturn(toAccountInitial.getBalance());
        when(mockCreditRs.getString("CurrencyCode")).thenReturn(toAccountInitial.getCurrency());
        when(mockCreditRs.getBoolean("Status")).thenReturn(toAccountInitial.getActive());

        APIException exception = assertThrows(APIException.class, () -> {
            accountRepository.fundTransfer(transfer);
        });

        assertEquals(APIConstants.NOT_SUFFICIENT_FUND, exception.getCode());
        verify(mockConnection).rollback(); 
        verify(mockConnection, Mockito.never()).commit();
        
        verify(mockDebitRs).close();
        verify(mockCreditRs).close();
        verify(mockDebitLockStmt).close();
        verify(mockCreditLockStmt).close();
        verify(mockConnection).close();
    }

    @Test
    public void testFundTransfer_sqlExceptionDuringUpdate_throwsAPIExceptionAndRollsBack() throws SQLException, APIException {
        FundTransfer transfer = new FundTransfer("100", "200", "50.00", "USD", "txn126");
        long fromAccId = Long.parseLong(transfer.getFromAccount());
        long toAccId = Long.parseLong(transfer.getToAccount());

        Account fromAccountInitial = new Account(1L, fromAccId, new BigDecimal("100.00"), "USD", true); 
        Account toAccountInitial = new Account(2L, toAccId, new BigDecimal("100.00"), "USD", true);    

        when(mockConnection.prepareStatement(SELECT_ACCT_FOR_LOCK_SQL))
            .thenReturn(mockDebitLockStmt).thenReturn(mockCreditLockStmt);
        when(mockConnection.prepareStatement(UPDATE_FUND_ACCT_SQL)).thenReturn(mockUpdateStmt);

        when(mockDebitLockStmt.executeQuery()).thenReturn(mockDebitRs);
        when(mockDebitRs.next()).thenReturn(true);
        when(mockDebitRs.getLong("CustomerId")).thenReturn(fromAccountInitial.getCustomerId());
        when(mockDebitRs.getLong("AccountId")).thenReturn(fromAccountInitial.getAccountNo());
        when(mockDebitRs.getBigDecimal("Balance")).thenReturn(fromAccountInitial.getBalance());
        when(mockDebitRs.getString("CurrencyCode")).thenReturn(fromAccountInitial.getCurrency());
        when(mockDebitRs.getBoolean("Status")).thenReturn(fromAccountInitial.getActive());

        when(mockCreditLockStmt.executeQuery()).thenReturn(mockCreditRs);
        when(mockCreditRs.next()).thenReturn(true);
        when(mockCreditRs.getLong("CustomerId")).thenReturn(toAccountInitial.getCustomerId());
        when(mockCreditRs.getLong("AccountId")).thenReturn(toAccountInitial.getAccountNo());
        when(mockCreditRs.getBigDecimal("Balance")).thenReturn(toAccountInitial.getBalance());
        when(mockCreditRs.getString("CurrencyCode")).thenReturn(toAccountInitial.getCurrency());
        when(mockCreditRs.getBoolean("Status")).thenReturn(toAccountInitial.getActive());

        when(mockUpdateStmt.executeBatch()).thenThrow(new SQLException("DB update failed"));

        APIException exception = assertThrows(APIException.class, () -> {
            accountRepository.fundTransfer(transfer);
        });

        assertEquals(APIConstants.API_INTERNAL_ERROR, exception.getCode());
        assertNotNull(exception.getCause()); 
        assertTrue(exception.getCause() instanceof SQLException);
        assertEquals("DB update failed", exception.getCause().getMessage());
        
        verify(mockConnection).rollback(); 
        verify(mockConnection, Mockito.never()).commit();
        
        verify(mockDebitRs).close();
        verify(mockCreditRs).close();
        verify(mockDebitLockStmt).close();
        verify(mockCreditLockStmt).close();
        verify(mockUpdateStmt).close(); 
        verify(mockConnection).close();
    }

    @Test
    public void testFundTransfer_lockAccountFails_fromAccountNotFound_RollsBack() throws SQLException, APIException {
        FundTransfer transfer = new FundTransfer("100", "200", "50.00", "USD", "txn127");
        
        when(mockConnection.prepareStatement(SELECT_ACCT_FOR_LOCK_SQL)).thenReturn(mockDebitLockStmt); 
        
        when(mockDebitLockStmt.executeQuery()).thenReturn(mockDebitRs);
        when(mockDebitRs.next()).thenReturn(false); 

        APIException exception = assertThrows(APIException.class, () -> {
            accountRepository.fundTransfer(transfer);
        });

        assertEquals(APIConstants.ACCOUNT_NOT_EXIST, exception.getCode());
        assertTrue(exception.getMessage().contains(transfer.getFromAccount()));
        verify(mockConnection).rollback(); 
        verify(mockConnection, Mockito.never()).commit(); 

        verify(mockDebitRs).close();
        verify(mockDebitLockStmt).close();
        verify(mockConnection).close(); 
    }
    
    @Test
    public void testFundTransfer_debitAccountInactive_throwsAPIExceptionAndRollsBack() throws SQLException, APIException {
        FundTransfer transfer = new FundTransfer("100", "200", "50.00", "USD", "txn128");
        long fromAccId = Long.parseLong(transfer.getFromAccount());
        long toAccId = Long.parseLong(transfer.getToAccount());

        Account fromAccountInitial = new Account(1L, fromAccId, new BigDecimal("200.00"), "USD", false); 
        Account toAccountInitial = new Account(2L, toAccId, new BigDecimal("100.00"), "USD", true);     

        when(mockConnection.prepareStatement(SELECT_ACCT_FOR_LOCK_SQL))
            .thenReturn(mockDebitLockStmt).thenReturn(mockCreditLockStmt);

        when(mockDebitLockStmt.executeQuery()).thenReturn(mockDebitRs);
        when(mockDebitRs.next()).thenReturn(true);
        when(mockDebitRs.getLong("CustomerId")).thenReturn(fromAccountInitial.getCustomerId());
        when(mockDebitRs.getLong("AccountId")).thenReturn(fromAccountInitial.getAccountNo());
        when(mockDebitRs.getBigDecimal("Balance")).thenReturn(fromAccountInitial.getBalance());
        when(mockDebitRs.getString("CurrencyCode")).thenReturn(fromAccountInitial.getCurrency());
        when(mockDebitRs.getBoolean("Status")).thenReturn(fromAccountInitial.getActive()); 

        when(mockCreditLockStmt.executeQuery()).thenReturn(mockCreditRs);
        when(mockCreditRs.next()).thenReturn(true);
        when(mockCreditRs.getLong("CustomerId")).thenReturn(toAccountInitial.getCustomerId());
        when(mockCreditRs.getLong("AccountId")).thenReturn(toAccountInitial.getAccountNo());
        when(mockCreditRs.getBigDecimal("Balance")).thenReturn(toAccountInitial.getBalance());
        when(mockCreditRs.getString("CurrencyCode")).thenReturn(toAccountInitial.getCurrency());
        when(mockCreditRs.getBoolean("Status")).thenReturn(toAccountInitial.getActive());
        
        APIException exception = assertThrows(APIException.class, () -> {
            accountRepository.fundTransfer(transfer);
        });
        
        assertEquals(APIConstants.ACCOUNT_NOT_ACTIVE, exception.getCode());
        verify(mockConnection).rollback();
        verify(mockConnection, Mockito.never()).commit();
        
        verify(mockDebitRs).close();
        verify(mockCreditRs).close();
        verify(mockDebitLockStmt).close();
        verify(mockCreditLockStmt).close();
        verify(mockConnection).close();
    }
}

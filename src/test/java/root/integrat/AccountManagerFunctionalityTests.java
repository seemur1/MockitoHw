package root.integrat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Assertions;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static root.integrat.AccountManagerTestsUtils.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AccountManagerFunctionalityTests {

    @Mock
    private static IServer mocker;

    @Spy
    private static AccountManager manager;

    @BeforeEach
    public void reinit() {
        manager = Mockito.spy(AccountManager.class);
        // Making the hash-function return the passed parameter (.makeSecure(password) = password).
        when(manager.makeSecure(anyString())).then(AdditionalAnswers.returnsFirstArg());

        mocker = Mockito.mock(IServer.class, Mockito.CALLS_REAL_METHODS);
        // Next stubbing settings make sense only in case of .makeSecure(password) being equal to password.
        when(mocker.login(anyString(), anyString())).thenReturn(UNDEFINED_ERROR_RESPONSE);
        when(mocker.login(anyString(), isNull())).thenReturn(UNDEFINED_ERROR_RESPONSE);
        when(mocker.login(eq(LOGGING_USER), anyString())).thenReturn(NO_USER_INCORRECT_PASSWORD_RESPONSE);
        when(mocker.login(eq(NOT_LOGGING_USER), anyString())).thenReturn(NO_USER_INCORRECT_PASSWORD_RESPONSE);
        when(mocker.login(LOGGING_USER, CORRECT_PASSWORD)).thenReturn(OK_RESPONSE);

        // when(mocker.logout(isNull())).thenReturn(UNDEFINED_ERROR_RESPONSE);
        when(mocker.logout(longThat(new NegativeLongMatcher()))).thenReturn(UNDEFINED_ERROR_RESPONSE);
        when(mocker.logout(NOT_LOGGING_SESSION)).thenReturn(NOT_LOGGED_RESPONSE);

        when(mocker.withdraw(anyLong(), doubleThat(new NegativeDoubleMatcher()))).thenReturn(UNDEFINED_ERROR_RESPONSE);
        when(mocker.withdraw(longThat(new NegativeLongMatcher()), anyDouble())).thenReturn(UNDEFINED_ERROR_RESPONSE);
        when(mocker.withdraw(eq(NOT_LOGGING_SESSION), anyDouble())).thenReturn(NOT_LOGGED_RESPONSE);

        when(mocker.deposit(anyLong(), doubleThat(new NegativeDoubleMatcher()))).thenReturn(UNDEFINED_ERROR_RESPONSE);
        when(mocker.deposit(longThat(new NegativeLongMatcher()), anyDouble())).thenReturn(UNDEFINED_ERROR_RESPONSE);
        when(mocker.deposit(eq(NOT_LOGGING_SESSION), anyDouble())).thenReturn(NOT_LOGGED_RESPONSE);

        // when(mocker.getBalance(isNull())).thenReturn(UNDEFINED_ERROR_RESPONSE);
        when(mocker.getBalance(longThat(new NegativeLongMatcher()))).thenReturn(UNDEFINED_ERROR_RESPONSE);
        when(mocker.getBalance(NOT_LOGGING_SESSION)).thenReturn(NOT_LOGGED_RESPONSE);

        manager.init(mocker);
    }

    @Test
    public void basicCallLoginTest() {
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);

        when(mocker.login(LOGGING_USER, CORRECT_PASSWORD)).thenReturn(ALREADY_LOGGED_RESPONSE);
        Assertions.assertEquals(AccountManagerResponse.ACCOUNT_MANAGER_RESPONSE, manager.callLogin(LOGGING_USER, CORRECT_PASSWORD));
    }

    @Test
    public void incorrectPasswordCallLoginTest() {
        Assertions.assertEquals(AccountManagerResponse.NO_USER_INCORRECT_PASSWORD_RESPONSE, manager.callLogin(LOGGING_USER, INCORRECT_PASSWORD));
    }

    @Test
    public void noUserCallLoginTest() {
        Assertions.assertEquals(AccountManagerResponse.NO_USER_INCORRECT_PASSWORD_RESPONSE, manager.callLogin(NOT_LOGGING_USER, CORRECT_PASSWORD));
    }

    @Test
    public void nullPasswordCallLoginTest() {
        Assertions.assertEquals(AccountManagerResponse.UNDEFINED_ERROR_RESPONSE, manager.callLogin(LOGGING_USER, null));
    }

    @Test
    public void basicCallLogoutTest() {
        when(mocker.logout(LOGGING_SESSION)).thenReturn(NOT_LOGGED_RESPONSE);
        Assertions.assertEquals(AccountManagerResponse.NOT_LOGGED_RESPONSE, manager.callLogout(LOGGING_USER, LOGGING_SESSION));

        when(mocker.logout(LOGGING_SESSION)).thenReturn(OK_RESPONSE);
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED_RESPONSE, manager.callLogout(LOGGING_USER, LOGGING_SESSION));
    }

    @Test
    public void notLoggedOnServerCallLogoutTest() {
        when(mocker.logout(LOGGING_SESSION)).thenReturn(NOT_LOGGED_RESPONSE);
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);
        Assertions.assertEquals(AccountManagerResponse.NOT_LOGGED_RESPONSE, manager.callLogout(LOGGING_USER, LOGGING_SESSION));
    }

    @Test
    public void nonEqualSessionCallLogoutTest() {
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);
        Assertions.assertEquals(AccountManagerResponse.INCORRECT_SESSION_RESPONSE, manager.callLogout(LOGGING_USER, NOT_LOGGING_SESSION));
    }

    @Test
    public void negativeSessionCallLogoutTest() {
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);
        Assertions.assertEquals(AccountManagerResponse.UNDEFINED_ERROR_RESPONSE, manager.callLogout(LOGGING_USER, -LOGGING_SESSION));
    }

    @Test
    public void basicWithdrawTest() {
        when(mocker.login(LOGGING_USER, CORRECT_PASSWORD)).thenReturn(NOT_LOGGED_RESPONSE);
        Assertions.assertEquals(AccountManagerResponse.NOT_LOGGED_RESPONSE, manager.withdraw(LOGGING_USER, LOGGING_SESSION, SMALL_DUMMY_VALUE));

        when(mocker.login(LOGGING_USER, CORRECT_PASSWORD)).thenReturn(OK_RESPONSE);
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);
        when(mocker.withdraw(LOGGING_SESSION, SMALL_DUMMY_VALUE)).thenReturn(DIFF_BALANCE_OK_RESPONSE);
        resp = manager.withdraw(LOGGING_USER, LOGGING_SESSION, SMALL_DUMMY_VALUE);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(BIG_DUMMY_VALUE - SMALL_DUMMY_VALUE, resp.response);
    }

    @Test
    public void notLoggedOnServerWithdrawTest() {
        when(mocker.withdraw(LOGGING_SESSION, SMALL_DUMMY_VALUE)).thenReturn(NOT_LOGGED_RESPONSE);
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);
        Assertions.assertEquals(AccountManagerResponse.NOT_LOGGED_RESPONSE, manager.withdraw(LOGGING_USER, LOGGING_SESSION, SMALL_DUMMY_VALUE));
    }

    @Test
    public void nonEqualSessionWithdrawTest() {
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);
        Assertions.assertEquals(AccountManagerResponse.INCORRECT_SESSION_RESPONSE, manager.withdraw(LOGGING_USER, NOT_LOGGING_SESSION, SMALL_DUMMY_VALUE));
    }

    @Test
    public void noMoneyWithdrawTest() {
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);
        when(mocker.withdraw(LOGGING_SESSION, BIG_DUMMY_VALUE)).thenReturn(SMALL_VALUE_NO_MONEY_RESPONSE);
        resp = manager.withdraw(LOGGING_USER, LOGGING_SESSION, BIG_DUMMY_VALUE);
        Assertions.assertEquals(AccountManagerResponse.NO_MONEY, resp.code);
        Assertions.assertEquals(SMALL_DUMMY_VALUE, resp.response);
    }

    @Test
    public void negativeSessionOrMoneyWithdrawTest() {
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);
        Assertions.assertEquals(AccountManagerResponse.UNDEFINED_ERROR_RESPONSE, manager.withdraw(LOGGING_USER, -LOGGING_SESSION, SMALL_DUMMY_VALUE));
        Assertions.assertEquals(AccountManagerResponse.UNDEFINED_ERROR_RESPONSE, manager.withdraw(LOGGING_USER, LOGGING_SESSION, -SMALL_DUMMY_VALUE));
    }

    @Test
    public void basicDepositTest() {
        when(mocker.login(LOGGING_USER, CORRECT_PASSWORD)).thenReturn(NOT_LOGGED_RESPONSE);
        Assertions.assertEquals(AccountManagerResponse.NOT_LOGGED_RESPONSE, manager.deposit(LOGGING_USER, LOGGING_SESSION, SMALL_DUMMY_VALUE));

        when(mocker.login(LOGGING_USER, CORRECT_PASSWORD)).thenReturn(OK_RESPONSE);
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);
        when(mocker.deposit(LOGGING_SESSION, SMALL_DUMMY_VALUE)).thenReturn(SUM_BALANCE_OK_RESPONSE);
        resp = manager.deposit(LOGGING_USER, LOGGING_SESSION, SMALL_DUMMY_VALUE);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(BIG_DUMMY_VALUE + SMALL_DUMMY_VALUE, resp.response);
    }

    @Test
    public void notLoggedOnServerDepositTest() {
        when(mocker.deposit(LOGGING_SESSION, SMALL_DUMMY_VALUE)).thenReturn(NOT_LOGGED_RESPONSE);
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);
        Assertions.assertEquals(AccountManagerResponse.NOT_LOGGED_RESPONSE, manager.deposit(LOGGING_USER, LOGGING_SESSION, SMALL_DUMMY_VALUE));
    }

    @Test
    public void nonEqualSessionDepositTest() {
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);
        Assertions.assertEquals(AccountManagerResponse.INCORRECT_SESSION_RESPONSE, manager.deposit(LOGGING_USER, NOT_LOGGING_SESSION, SMALL_DUMMY_VALUE));
    }

    @Test
    public void negativeSessionOrMoneyDepositTest() {
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);
        Assertions.assertEquals(AccountManagerResponse.UNDEFINED_ERROR_RESPONSE, manager.deposit(LOGGING_USER, -LOGGING_SESSION, SMALL_DUMMY_VALUE));
        Assertions.assertEquals(AccountManagerResponse.UNDEFINED_ERROR_RESPONSE, manager.deposit(LOGGING_USER, LOGGING_SESSION, -SMALL_DUMMY_VALUE));
    }

    @Test
    public void basicGetBalanceTest() {
        when(mocker.login(LOGGING_USER, CORRECT_PASSWORD)).thenReturn(NOT_LOGGED_RESPONSE);
        Assertions.assertEquals(AccountManagerResponse.NOT_LOGGED_RESPONSE, manager.getBalance(LOGGING_USER, LOGGING_SESSION));

        when(mocker.login(LOGGING_USER, CORRECT_PASSWORD)).thenReturn(OK_RESPONSE);
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);

        when(mocker.getBalance(LOGGING_SESSION)).thenReturn(BIG_VALUE_BALANCE_RESPONSE);
        resp = manager.getBalance(LOGGING_USER, LOGGING_SESSION);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(BIG_DUMMY_VALUE, resp.response);
    }

    @Test
    public void notLoggedOnServerBalanceTest() {
        when(mocker.getBalance(LOGGING_SESSION)).thenReturn(NOT_LOGGED_RESPONSE);
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);
        Assertions.assertEquals(AccountManagerResponse.NOT_LOGGED_RESPONSE, manager.getBalance(LOGGING_USER, LOGGING_SESSION));
    }

    @Test
    public void nonEqualSessionGetBalanceTest() {
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);
        Assertions.assertEquals(AccountManagerResponse.INCORRECT_SESSION_RESPONSE, manager.getBalance(LOGGING_USER, NOT_LOGGING_SESSION));
    }

    @Test
    public void negativeSessionGetBalanceTest() {
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);
        Assertions.assertEquals(AccountManagerResponse.UNDEFINED_ERROR_RESPONSE, manager.getBalance(LOGGING_USER, -LOGGING_SESSION));
    }

}

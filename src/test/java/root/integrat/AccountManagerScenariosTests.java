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
public class AccountManagerScenariosTests {

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
    public void firstScenarioTest() {
        Assertions.assertEquals(AccountManagerResponse.NO_USER_INCORRECT_PASSWORD_RESPONSE, manager.callLogin(NOT_LOGGING_USER, CORRECT_PASSWORD));
        Assertions.assertEquals(AccountManagerResponse.NO_USER_INCORRECT_PASSWORD_RESPONSE, manager.callLogin(LOGGING_USER, INCORRECT_PASSWORD));
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);

        // The next assertions imply that the user has a non-zero balance of "AccountManagerTestsUtils.BIG_DUMMY_VALUE".
        when(mocker.getBalance(LOGGING_SESSION)).thenReturn(BIG_VALUE_BALANCE_RESPONSE);
        resp = manager.getBalance(LOGGING_USER, LOGGING_SESSION);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(BIG_DUMMY_VALUE, resp.response);

        when(mocker.deposit(LOGGING_SESSION, SCENARIO_VALUE1)).thenReturn(SUM_BALANCE_SCENARIO1_RESPONSE);
        resp = manager.deposit(LOGGING_USER, LOGGING_SESSION, SCENARIO_VALUE1);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(BIG_DUMMY_VALUE + SCENARIO_VALUE1, resp.response);

        when(mocker.getBalance(LOGGING_SESSION)).thenReturn(SUM_BALANCE_SCENARIO1_RESPONSE);
        resp = manager.getBalance(LOGGING_USER, LOGGING_SESSION);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(BIG_DUMMY_VALUE + SCENARIO_VALUE1, resp.response);
    }

    @Test
    public void secondScenarioTest() {
        AccountManagerResponse resp = manager.callLogin(LOGGING_USER, CORRECT_PASSWORD);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(LOGGING_SESSION, resp.response);

        when(mocker.withdraw(LOGGING_SESSION, SCENARIO_VALUE2)).thenReturn(DEFAULT_BALANCE_NO_MONEY_RESPONSE);
        resp = manager.withdraw(LOGGING_USER, LOGGING_SESSION, SCENARIO_VALUE2);
        Assertions.assertEquals(AccountManagerResponse.NO_MONEY, resp.code);
        Assertions.assertEquals(DEFAULT_BALANCE, resp.response);

        when(mocker.getBalance(LOGGING_SESSION)).thenReturn(DEFAULT_BALANCE_RESPONSE);
        resp = manager.getBalance(LOGGING_USER, LOGGING_SESSION);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(DEFAULT_BALANCE, resp.response);

        when(mocker.deposit(LOGGING_SESSION, SCENARIO_VALUE1)).thenReturn(SCENARIO1_VAL_BALANCE_RESPONSE);
        resp = manager.deposit(LOGGING_USER, LOGGING_SESSION, SCENARIO_VALUE1);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(SCENARIO_VALUE1, resp.response);

        // when(mocker.withdraw(LOGGING_SESSION, SCENARIO_VALUE2)).thenReturn(DEFAULT_BALANCE_NO_MONEY_RESPONSE);
        Assertions.assertEquals(AccountManagerResponse.INCORRECT_SESSION_RESPONSE, manager.withdraw(LOGGING_USER, NOT_LOGGING_SESSION, SCENARIO_VALUE2));

        when(mocker.withdraw(LOGGING_SESSION, SCENARIO_VALUE2)).thenReturn(DIFF_BALANCE_SCENARIO2_RESPONSE);
        resp = manager.withdraw(LOGGING_USER, LOGGING_SESSION, SCENARIO_VALUE2);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(SCENARIO_VALUE1 - SCENARIO_VALUE2, resp.response);

        when(mocker.getBalance(LOGGING_SESSION)).thenReturn(DIFF_BALANCE_SCENARIO2_RESPONSE);
        resp = manager.getBalance(LOGGING_USER, LOGGING_SESSION);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED, resp.code);
        Assertions.assertEquals(SCENARIO_VALUE1 - SCENARIO_VALUE2, resp.response);

        when(mocker.logout(LOGGING_SESSION)).thenReturn(OK_RESPONSE);
        Assertions.assertEquals(AccountManagerResponse.SUCCEED_RESPONSE, manager.callLogout(LOGGING_USER, LOGGING_SESSION));
    }

}

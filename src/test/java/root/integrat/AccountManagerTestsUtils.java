package root.integrat;
import static root.integrat.ServerResponse.*;

public class AccountManagerTestsUtils {
    public static final String LOGGING_USER = "User1";
    public static final String CORRECT_PASSWORD = "1234567890";
    public static final String INCORRECT_PASSWORD = "0987654321";
    public static final long LOGGING_SESSION = 250L;
    public static final String NOT_LOGGING_USER = "User2";
    public static final long NOT_LOGGING_SESSION = 300L;
    public static final double SMALL_DUMMY_VALUE = 30.5;
    public static final double BIG_DUMMY_VALUE = 45.2;

    public static final double DEFAULT_BALANCE = 0.0;
    public static final double SCENARIO_VALUE1 = 100.0;
    public static final double SCENARIO_VALUE2 = 50.0;

    public static final ServerResponse OK_RESPONSE = new ServerResponse(SUCCESS, LOGGING_SESSION);
    public static final ServerResponse ALREADY_LOGGED_RESPONSE = new ServerResponse(ALREADY_LOGGED, null);
    public static final ServerResponse NO_USER_INCORRECT_PASSWORD_RESPONSE = new ServerResponse(NO_USER_INCORRECT_PASSWORD, null);
    public static final ServerResponse UNDEFINED_ERROR_RESPONSE = new ServerResponse(UNDEFINED_ERROR, null);
    public static final ServerResponse NOT_LOGGED_RESPONSE = new ServerResponse(NOT_LOGGED, null);

    public static final ServerResponse SMALL_VALUE_NO_MONEY_RESPONSE = new ServerResponse(NO_MONEY, SMALL_DUMMY_VALUE);
    public static final ServerResponse BIG_VALUE_NO_MONEY_RESPONSE = new ServerResponse(NO_MONEY, BIG_DUMMY_VALUE);
    public static final ServerResponse DIFF_BALANCE_OK_RESPONSE = new ServerResponse(SUCCESS, BIG_DUMMY_VALUE - SMALL_DUMMY_VALUE);
    public static final ServerResponse SUM_BALANCE_OK_RESPONSE = new ServerResponse(SUCCESS, BIG_DUMMY_VALUE + SMALL_DUMMY_VALUE);
    public static final ServerResponse BIG_VALUE_BALANCE_RESPONSE = new ServerResponse(SUCCESS, BIG_DUMMY_VALUE);
    public static final ServerResponse SMALL_VALUE_BALANCE_RESPONSE = new ServerResponse(SUCCESS, SMALL_DUMMY_VALUE);
    public static final ServerResponse SUM_BALANCE_SCENARIO1_RESPONSE = new ServerResponse(SUCCESS, BIG_DUMMY_VALUE + SCENARIO_VALUE1);
    public static final ServerResponse DEFAULT_BALANCE_RESPONSE = new ServerResponse(SUCCESS, DEFAULT_BALANCE);
    public static final ServerResponse DEFAULT_BALANCE_NO_MONEY_RESPONSE = new ServerResponse(NO_MONEY, DEFAULT_BALANCE);
    public static final ServerResponse SCENARIO1_VAL_BALANCE_RESPONSE = new ServerResponse(SUCCESS, SCENARIO_VALUE1);
    public static final ServerResponse DIFF_BALANCE_SCENARIO2_RESPONSE = new ServerResponse(SUCCESS, SCENARIO_VALUE1 - SCENARIO_VALUE2);
}

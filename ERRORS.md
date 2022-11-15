!!!Важные уточнения!!!:
1.	В качестве случаев непредвиденной ошибки ("AccountManagerResponse.UNDEFINED_ERROR_RESPONSE") рассматриваются случаи, когда
	конкретные параметры операции задаются значениями "null".
2.	Для операции "callLogin" рассматривается случай, когда пользователь не является авторизованным на уровне приложения,
	однако является авторизованным на уровне сервера - специфиция явным образом не объясняет, как реагировать на такие ситуации,
	и, в рамках тестирования, была предпринята попытка использовать меры реагирования, в рамках которых на уровне приложения в список
	авторизованных пользователей добавляется пользователь, авторизованный на сервере, однако с учетом предоставленного API нет возможности
	напрямую получить идентификатор сессии текущего пользователя с сервера, потому конкретно в рамках метода "callLogin" описанный случай
	не был протестирован.

[Ошибка №1]
1.	Местонахождение ошибки (и доп. условия ее возникновения):	файл "AccountManager.java",
	функция "public AccountManagerResponse callLogin(String login, String password)";
2.	Строка с проверкой корректности условий:	-";
3.	Описание ошибки:	с помощью хэш-словаря "activeAccounts" должны проверяться существующие сессии пользователей
	и ровно такая же проверка должна быть на уровне сервера, однако в случае успешного добавления сессии с новым пользователем
	на уровне сервера аналогичное действие не происходит на уровне "activeAccounts", при этом на уровне предоставленного API это не
	может быть успешным образом проверено, поскольку условие проверки на уровне сервера следует за условием проверки на уровне "activeAccounts",
	а следовательно, в случае "отпадающего" второго условия всегда может пройти первое;
4.	Способ исправления ошибки:	исправление следующих строк кода на форму далее -
	if(answ instanceof Long)
		return new AccountManagerResponse(AccountManagerResponse.SUCCEED, answ);
	на
	if (answ instanceof Long) {
		activeAccounts.put(login, (Long)answ);
		return new AccountManagerResponse(AccountManagerResponse.SUCCEED, answ);
    }	.

[Ошибка №2]
1.	Местонахождение ошибки (и доп. условия ее возникновения):	файл "AccountManager.java",
	функция "public AccountManagerResponse callLogin(String login, String password)", "password" = "null";
2.	Строка с проверкой корректности условий:	файл "AccountManagerFunctionalityTests.java", тест "nullPasswordCallLoginTest" -
	"Assertions.assertEquals(AccountManagerResponse.UNDEFINED_ERROR_RESPONSE, manager.callLogin(LOGGING_USER, null));";
3.	Описание ошибки:	для случая, когда возникает непредвиденная ошибка (в текущем случае, когда пароль представляется значением "null"),
	возвращается ответ, отличный от AccountManagerResponse UNDEFINED_ERROR_RESPONSE;
4.	Способ исправления ошибки:	исправление следующей строки кода на форму далее -
	return new AccountManagerResponse(AccountManagerResponse.INCORRECT_RESPONSE, ret);
	на
	return AccountManagerResponse.UNDEFINED_ERROR_RESPONSE;	.
	
	[Ошибка №3]
1.	Местонахождение ошибки (и доп. условия ее возникновения):	файл "AccountManager.java",
	функция "public AccountManagerResponse callLogout(String user, long session)", "session" < 0;
2.	Строка с проверкой корректности условий:	файл "AccountManagerFunctionalityTests.java", тест "negativeSessionCallLogoutTest" -
	"Assertions.assertEquals(AccountManagerResponse.UNDEFINED_ERROR_RESPONSE, manager.callLogout(LOGGING_USER, -LOGGING_SESSION));";
3.	Описание ошибки:	для случая, когда возникает непредвиденная ошибка (в текущем случае, когда ID сессии отрицателен),
	возвращается ответ, отличный от AccountManagerResponse UNDEFINED_ERROR_RESPONSE;
4.	Способ исправления ошибки:	исправление следующей строки кода на форму далее -
	return new AccountManagerResponse(AccountManagerResponse.INCORRECT_RESPONSE, ret);
	на
	return AccountManagerResponse.UNDEFINED_ERROR_RESPONSE;	.
	
	[Ошибка №4]
1.	Местонахождение ошибки (и доп. условия ее возникновения):	файл "AccountManager.java",
	функция "public AccountManagerResponse callLogout(String user, long session)", реальный ID сессии != "session";
2.	Строка с проверкой корректности условий:	файл "AccountManagerFunctionalityTests.java", тест "nonEqualSessionCallLogoutTest" -
	"Assertions.assertEquals(AccountManagerResponse.INCORRECT_SESSION_RESPONSE, manager.callLogout(LOGGING_USER, NOT_LOGGING_SESSION));";
3.	Описание ошибки:	для случая, когда реальный ID сессии пользователя отличается от переданного параметра "session",
	возвращается ответ, отличный от AccountManagerResponse INCORRECT_SESSION_RESPONSE;
4.	Способ исправления ошибки:	исправление следующих строк кода на форму далее -
	if(rem == null)
      return AccountManagerResponse.NOT_LOGGED_RESPONSE;
	на
	if(rem == null)
		return AccountManagerResponse.NOT_LOGGED_RESPONSE;
    if(rem != session && session >= 0)
		return AccountManagerResponse.INCORRECT_SESSION_RESPONSE;	.

	[Ошибка №5]
1.	Местонахождение ошибки (и доп. условия ее возникновения):	файл "AccountManager.java",
	функция "public AccountManagerResponse withdraw(String login, long session, double amount)", "session" < 0;
2.	Строка с проверкой корректности условий:	файл "AccountManagerFunctionalityTests.java", тест "negativeSessionOrMoneyWithdrawTest" -
	"Assertions.assertEquals(AccountManagerResponse.UNDEFINED_ERROR_RESPONSE, manager.withdraw(LOGGING_USER, -LOGGING_SESSION, SMALL_DUMMY_VALUE));";
3.	Описание ошибки:	для случая, когда возникает непредвиденная ошибка (в текущем случае, когда ID сессии отрицателен),
	возвращается ответ, отличный от AccountManagerResponse UNDEFINED_ERROR_RESPONSE;
4.	Способ исправления ошибки:	исправление следующих строк кода на форму далее -
	if(stored!=session)
		return AccountManagerResponse.INCORRECT_SESSION_RESPONSE;
	на
	if(stored!=session && session >= 0)
		return AccountManagerResponse.INCORRECT_SESSION_RESPONSE;	.
	
	[Ошибка №6]
1.	Местонахождение ошибки (и доп. условия ее возникновения):	файл "AccountManager.java",
	функция "public AccountManagerResponse withdraw(String login, long session, double amount)", "session" < 0 или "amount" < 0;
2.	Строка с проверкой корректности условий:	файл "AccountManagerFunctionalityTests.java", тест "negativeSessionOrMoneyWithdrawTest" -
	"Assertions.assertEquals(AccountManagerResponse.UNDEFINED_ERROR_RESPONSE, manager.withdraw(LOGGING_USER, -LOGGING_SESSION, SMALL_DUMMY_VALUE));";
3.	Описание ошибки:	для случая, когда возникает непредвиденная ошибка (в текущем случае, когда ID сессии отрицателен или когда сумма для вывода отрицательна),
	возвращается ответ, отличный от AccountManagerResponse UNDEFINED_ERROR_RESPONSE;
4.	Способ исправления ошибки:	исправление следующей строки кода на форму далее -
	return new AccountManagerResponse(AccountManagerResponse.INCORRECT_RESPONSE, resp)
	на
	return AccountManagerResponse.UNDEFINED_ERROR_RESPONSE;	.
	
	[Ошибка №7]
1.	Местонахождение ошибки (и доп. условия ее возникновения):	файл "AccountManager.java",
	функция "public AccountManagerResponse deposit(String login, long session, double amount)", "session" < 0;
2.	Строка с проверкой корректности условий:	файл "AccountManagerFunctionalityTests.java", тест "negativeSessionOrMoneyDepositTest" -
	"Assertions.assertEquals(AccountManagerResponse.UNDEFINED_ERROR_RESPONSE, manager.deposit(LOGGING_USER, -LOGGING_SESSION, SMALL_DUMMY_VALUE));";
3.	Описание ошибки:	для случая, когда возникает непредвиденная ошибка (в текущем случае, когда ID сессии отрицателен),
	возвращается ответ, отличный от AccountManagerResponse UNDEFINED_ERROR_RESPONSE;
4.	Способ исправления ошибки:	исправление следующих строк кода на форму далее -
	if(stored!=session)
		return AccountManagerResponse.INCORRECT_SESSION_RESPONSE;
	на
	if(stored!=session && session >= 0)
		return AccountManagerResponse.INCORRECT_SESSION_RESPONSE;	.
	
	[Ошибка №8]
1.	Местонахождение ошибки (и доп. условия ее возникновения):	файл "AccountManager.java",
	функция "public AccountManagerResponse deposit(String login, long session, double amount)", "session" < 0 или "amount" < 0;
2.	Строка с проверкой корректности условий:	файл "AccountManagerFunctionalityTests.java", тест "negativeSessionOrMoneyDepositTest" -
	"Assertions.assertEquals(AccountManagerResponse.UNDEFINED_ERROR_RESPONSE, manager.deposit(LOGGING_USER, -LOGGING_SESSION, SMALL_DUMMY_VALUE));";
3.	Описание ошибки:	для случая, когда возникает непредвиденная ошибка (в текущем случае, когда ID сессии отрицателен или когда сумма для ввода отрицательна),
	возвращается ответ, отличный от AccountManagerResponse UNDEFINED_ERROR_RESPONSE;
4.	Способ исправления ошибки:	исправление следующей строки кода на форму далее -
	return new AccountManagerResponse(AccountManagerResponse.INCORRECT_RESPONSE, resp)
	на
	return AccountManagerResponse.UNDEFINED_ERROR_RESPONSE;	.
	
	[Ошибка №9]
1.	Местонахождение ошибки (и доп. условия ее возникновения):	файл "AccountManager.java",
	функция "public AccountManagerResponse deposit(String login, long session, double amount)", "session" < 0 или "amount" < 0;
2.	Строка с проверкой корректности условий:	-;
3.	Описание ошибки:	в соответствие заданной спецификации, для данного метода не существует ситуации, результатом которой послужит
	вывод вида "AccountManagerResponse.NO_MONEY";
4.	Способ исправления ошибки:	удаление следующих строк кода далее -
	case ServerResponse.NO_MONEY:
		Object r = resp.response;
		if(r!=null && r instanceof Double)
			return new AccountManagerResponse(AccountManagerResponse.NO_MONEY, (Double)r);
		break;	
	, замена следующих строк на форму далее -
	case ServerResponse.SUCCESS:
		r = resp.response;
	на
	case ServerResponse.SUCCESS:
		Object r = resp.response;
	
	[Ошибка №10]
1.	Местонахождение ошибки (и доп. условия ее возникновения):	файл "AccountManager.java",
	функция "public AccountManagerResponse getBalance(String login, long session)", "session" < 0;
2.	Строка с проверкой корректности условий:	файл "AccountManagerFunctionalityTests.java", тест "negativeSessionGetBalanceTest" -
	"Assertions.assertEquals(AccountManagerResponse.UNDEFINED_ERROR_RESPONSE, manager.getBalance(LOGGING_USER, -LOGGING_SESSION));";
3.	Описание ошибки:	для случая, когда возникает непредвиденная ошибка (в текущем случае, когда ID сессии отрицателен),
	возвращается ответ, отличный от AccountManagerResponse UNDEFINED_ERROR_RESPONSE;
4.	Способ исправления ошибки:	исправление следующих строк кода на форму далее -
	if(stored!=session)
		return AccountManagerResponse.INCORRECT_SESSION_RESPONSE;
	на
	if(stored!=session && session >= 0)
		return AccountManagerResponse.INCORRECT_SESSION_RESPONSE;	.
	
	[Ошибка №11]
1.	Местонахождение ошибки (и доп. условия ее возникновения):	файл "AccountManager.java",
	функция "public AccountManagerResponse getBalance(String login, long session)", "session" < 0;
2.	Строка с проверкой корректности условий:	файл "AccountManagerFunctionalityTests.java", тест "negativeSessionGetBalanceTest" -
	"Assertions.assertEquals(AccountManagerResponse.UNDEFINED_ERROR_RESPONSE, manager.getBalance(LOGGING_USER, -LOGGING_SESSION));";
3.	Описание ошибки:	для случая, когда возникает непредвиденная ошибка (в текущем случае, когда ID сессии отрицателен),
	возвращается ответ, отличный от AccountManagerResponse UNDEFINED_ERROR_RESPONSE;
4.	Способ исправления ошибки:	исправление следующей строки кода на форму далее -
	return new AccountManagerResponse(AccountManagerResponse.INCORRECT_RESPONSE, resp)
	на
	return AccountManagerResponse.UNDEFINED_ERROR_RESPONSE;
	
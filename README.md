# StockMarket (HW-3)

Апи эмулятора биржы – [StockMarketController](stock_market/src/main/java/stock_market/controller/StockMarketController.java)

Апи личного кабинета пользователя – [UserController](user_account/src/main/java/user_account/controller/UserController.java)

Тесты с testcontainers – [UserAccountTest](user_account/src/test/java/user_account/UserAccountTest.java)

-------
Build app and add docker image with app to local docker registry:

```mvn -am -pl stock_market package```

Run integration test with docker:

```mvn -am -pl user_account test```
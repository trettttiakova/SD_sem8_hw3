package user_account;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import stock_market.controller.StockMarketController;
import stock_market.dto.NewStockDto;
import stock_market.entity.Share;
import stock_market.entity.Stock;
import stock_market.repository.InMemoryStockRepository;
import user_account.controller.UserController;
import user_account.dto.NewUserDto;
import user_account.entity.User;
import user_account.facade.UserFacade;
import user_account.repository.InMemoryUserRepository;

@Testcontainers
public class UserAccountTest {
    @Container
    public static GenericContainer stockMarketServer
        = new GenericContainer("stockMarket:1.0-SNAPSHOT")
        .withExposedPorts(8080);

    private StockMarketController stockMarketController;
    private InMemoryUserRepository userRepository;
    private UserController userController;
    private UserFacade userFacade;

    private Stock stock1, stock2, stock3;

    @Before
    public void before() {
        stockMarketController = new StockMarketController(new InMemoryStockRepository());

        userRepository = new InMemoryUserRepository();
        userFacade = new UserFacade(stockMarketController, userRepository);
        userController = new UserController(userRepository, userFacade);

        stock1 = stockMarketController.createStock(
            new NewStockDto("АЛРОСА", 100, 10)
        );
        stock2 = stockMarketController.createStock(
            new NewStockDto("Лукойл", 200, 5)
        );
        stock3 = stockMarketController.createStock(
            new NewStockDto("ВТБ", 300, 15)
        );
    }

    @Test
    public void singleUserBuyAndGetSharesTest() {
        User zoey = userController.createUser(
            new NewUserDto("Zoey")
        );

        userController.deposit(zoey.getId(), 100);
        userController.buySharesForUser(zoey.getId(), stock1.getId(), 2);

        assertThat(userController.getUserShares(zoey.getId()))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new Share(stock1, 10),
                new Share(stock1, 10)
            );

        assertThat(userController.getUSDBalance(zoey.getId())).isEqualTo(80);
        assertThat(userController.getOverallBalance(zoey.getId())).isEqualTo(100);
    }

    @Test
    public void multipleUserBuyAndGetSharesTest() {
        User zoey = userController.createUser(
            new NewUserDto("Zoey")
        );
        User donaldTrump = userController.createUser(
            new NewUserDto("Trump D.")
        );

        userController.deposit(zoey.getId(), 100);
        userController.buySharesForUser(zoey.getId(), stock1.getId(), 2);

        userController.deposit(donaldTrump.getId(), 10_000_000);
        userController.buySharesForUser(donaldTrump.getId(), stock1.getId(), 20);
        userController.buySharesForUser(donaldTrump.getId(), stock2.getId(), 30);
        userController.buySharesForUser(donaldTrump.getId(), stock3.getId(), 50);

        assertThat(userController.getUserShares(zoey.getId()).size())
            .isEqualTo(2);

        var trumpsShares = userController.getUserShares(donaldTrump.getId());

        assertThat(
            trumpsShares.stream()
                .filter(share -> share.getStock().getId() == stock1.getId())
                .toList()
                .size()
        ).isEqualTo(20);
        assertThat(
            trumpsShares.stream()
                .filter(share -> share.getStock().getId() == stock2.getId())
                .toList()
                .size()
        ).isEqualTo(30);
        assertThat(
            trumpsShares.stream()
                .filter(share -> share.getStock().getId() == stock3.getId())
                .toList()
                .size()
        ).isEqualTo(50);
    }

    @Test
    public void notEnoughMoneyTest() {
        User zoey = userController.createUser(
            new NewUserDto("Zoey")
        );

        userController.deposit(zoey.getId(), 100);
        boolean bought = userController.buySharesForUser(zoey.getId(), stock1.getId(), 100);

        assertThat(userController.getUserShares(zoey.getId()).size())
            .isEqualTo(0);

        assertThat(userController.getUSDBalance(zoey.getId())).isEqualTo(100);
        assertThat(userController.getOverallBalance(zoey.getId())).isEqualTo(100);

        assertThat(bought).isFalse();
    }

    @Test
    public void notEnoughSharesTest() {
        User zoey = userController.createUser(
            new NewUserDto("Zoey")
        );

        userController.deposit(zoey.getId(), 10_000);
        // успешная покупка
        boolean bought1 = userController.buySharesForUser(zoey.getId(), stock1.getId(), 80);
        // Акций АЛРОСЫ столько нет
        boolean bought2 = userController.buySharesForUser(zoey.getId(), stock1.getId(), 80);

        assertThat(bought1).isTrue();
        assertThat(bought2).isFalse();
    }

    @Test
    public void overallBalanceAfterPriceChange() {
        User zoey = userController.createUser(
            new NewUserDto("Zoey")
        );

        userController.deposit(zoey.getId(), 1000);

        // Куплено 10 акций по 10 долларов (начальная цена АЛРОСы)
        // На балансе осталось 900 USD
        userController.buySharesForUser(zoey.getId(), stock1.getId(), 10);

        stockMarketController.changeStockPrice(stock1.getId(), 1);

        // Куплено 20 акций по 1 доллару
        // На балансе осталось 880 USD
        userController.buySharesForUser(zoey.getId(), stock1.getId(), 20);

        // Итого общий баланс сейчас 880 + 30 * 1 = 910 USD
        assertThat(userController.getOverallBalance(zoey.getId())).isEqualTo(910);
    }

    @Test
    public void buyAndSellSharesTest() {
        User zoey = userController.createUser(
            new NewUserDto("Zoey")
        );

        userController.deposit(zoey.getId(), 100);
        // Куплено 3 акции stock1 по 10 долларов, на балансе 70 USD
        userController.buySharesForUser(zoey.getId(), stock1.getId(), 3);
        // Куплено 10 акций stock2 по 5 долларов, на балансе 20 USD
        userController.buySharesForUser(zoey.getId(), stock2.getId(), 10);

        // Цена за акцию stock1 выросла до 35 USD
        stockMarketController.changeStockPrice(stock1.getId(), 35);

        // Продаем 2 акции stock1 по 35 USD за штуку
        userController.sellSharesForUser(zoey.getId(), stock1.getId(), 2);

        // Итого на USD балансе: 100 - 30 - 50 + 70 = 90 USD
        // Итого на overall балансе: 90 + 35 + 50 = 175 USD: stonks

        assertThat(userController.getUSDBalance(zoey.getId())).isEqualTo(90);
        assertThat(userController.getOverallBalance(zoey.getId())).isEqualTo(175);
    }
}

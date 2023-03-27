package user_account;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import stock_market.entity.Share;
import stock_market.entity.Stock;
import user_account.controller.UserController;
import user_account.dto.NewUserDto;
import user_account.entity.User;
import user_account.facade.UserFacade;
import user_account.repository.InMemoryUserRepository;
import user_account.util.ParserUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;

public class UserAccountTest {
    @ClassRule
    public static GenericContainer stockMarketServer
    = new FixedHostPortGenericContainer("stockmarket:0.0.1-SNAPSHOT")
        .withFixedExposedPort(8080, 8080)
        .withExposedPorts(8080);

    private InMemoryUserRepository userRepository;
    private UserController userController;
    private UserFacade userFacade;
    private StockMarketClient stockMarketClient;

    private Stock stock1, stock2, stock3;

    @Before
    public void before() throws IOException {
        stockMarketServer.start();

        stockMarketClient = new StockMarketClient("localhost", 8080);
        userRepository = new InMemoryUserRepository(stockMarketClient);
        userFacade = new UserFacade(stockMarketClient, userRepository);
        userController = new UserController(userRepository, userFacade);

        stock1 = parseStockJson(stockMarketClient.post(
            "stock",
            Map.of(
                "companyName", "ALROSA",
                "overallSharesCount", "100",
                "currentPriceUSD", "10"
            )
        ).body());

        stock2 = parseStockJson(stockMarketClient.post(
            "stock",
            Map.of(
                "companyName", "Лукойл",
                "overallSharesCount", "200",
                "currentPriceUSD", "5"
            )
        ).body());

        stock3 = parseStockJson(stockMarketClient.post(
            "stock",
            Map.of(
                "companyName", "ВТБ",
                "overallSharesCount", "300",
                "currentPriceUSD", "15"
            )
        ).body());
    }

    private Stock parseStockJson(String json) {
        return ParserUtils.parseStockJson(json);
    }

    @After
    public void tearDown() {
        stockMarketServer.stop();
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
                new Share(
                    Stock.builder()
                        .id(stock1.getId())
                        .companyName(stock1.getCompanyName())
                        .overallSharesCount(stock1.getOverallSharesCount())
                        .availableSharesForSaleCount(98)
                        .currentPriceUSD(stock1.getCurrentPriceUSD())
                        .build(),
                    10
                ),
                new Share(
                    Stock.builder()
                        .id(stock1.getId())
                        .companyName(stock1.getCompanyName())
                        .overallSharesCount(stock1.getOverallSharesCount())
                        .availableSharesForSaleCount(98)
                        .currentPriceUSD(stock1.getCurrentPriceUSD())
                        .build(),
                    10
                )
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
                .collect(Collectors.toList())
                .size()
        ).isEqualTo(20);
        assertThat(
            trumpsShares.stream()
                .filter(share -> share.getStock().getId() == stock2.getId())
                .collect(Collectors.toList())
                .size()
        ).isEqualTo(30);
        assertThat(
            trumpsShares.stream()
                .filter(share -> share.getStock().getId() == stock3.getId())
                .collect(Collectors.toList())
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
    public void overallBalanceAfterPriceChange() {
        User zoey = userController.createUser(
            new NewUserDto("Zoey")
        );

        userController.deposit(zoey.getId(), 1000);

        // Куплено 10 акций по 10 долларов (начальная цена АЛРОСы)
        // На балансе осталось 900 USD
        userController.buySharesForUser(zoey.getId(), stock1.getId(), 10);

        stockMarketClient.changePrice(stock1.getId(), 1);

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
        stockMarketClient.changePrice(stock1.getId(), 35);

        // Продаем 2 акции stock1 по 35 USD за штуку
        userController.sellSharesForUser(zoey.getId(), stock1.getId(), 2);

        // Итого на USD балансе: 100 - 30 - 50 + 70 = 90 USD
        // Итого на overall балансе: 90 + 35 + 50 = 175 USD: stonks

        assertThat(userController.getUSDBalance(zoey.getId())).isEqualTo(90);
        assertThat(userController.getOverallBalance(zoey.getId())).isEqualTo(175);
    }
}

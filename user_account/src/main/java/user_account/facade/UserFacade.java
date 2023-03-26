package user_account.facade;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import stock_market.controller.StockMarketController;
import stock_market.entity.Share;
import stock_market.entity.Stock;
import user_account.entity.User;
import user_account.repository.UserRepository;

import java.util.Collection;

@Component
@AllArgsConstructor
public class UserFacade {
    private final StockMarketController stockMarketController;

    private final UserRepository userRepository;

    public boolean buyShares(long userId, long stockId, int count) {
        if (count <= 0) {
            return false;
        }

        User user = userRepository.getById(userId);
        Stock stock = stockMarketController.getStock(stockId);
        int price = stock.getCurrentPriceUSD();
        if (user.hasEnoughMoneyToSpend(price * count)) {
            Collection<Share> boughtShares = stockMarketController.buyShares(stockId, count, price);
            if (boughtShares == null) {
                return false;
            }
            int spentAmount = boughtShares.stream()
                .map(Share::getPaidPriceUSD)
                .mapToInt(Integer::intValue)
                .sum();
            user.withdraw(spentAmount);
            user.addShares(boughtShares);
            return true;
        }

        return false;
    }

    public Integer sellShares(long userId, long stockId, int count) {
        if (count <= 0) {
            return null;
        }

        User user = userRepository.getById(userId);
        return user.sellShares(stockId, count);
    }
}

package stock_market.repository;

import stock_market.entity.Share;
import stock_market.entity.Stock;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface StockRepository {
    Stock createNewStock(String companyName, int currentPriceUSD, int overallSharesCount);

    Stock getById(long id);

    Collection<Share> buyShares(long stockId, int count, int maxPrice);

    void changePrice(long stockId, int newPrice);
}

package stock_market.repository;

import org.springframework.stereotype.Component;
import stock_market.entity.Share;
import stock_market.entity.Stock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class InMemoryStockRepository implements StockRepository {
    private final List<Stock> stocks = new ArrayList<>();

    @Override
    public Stock createNewStock(String companyName, int currentPriceUSD, int overallSharesCount) {
        Stock newStock = new Stock(stocks.size(), companyName, currentPriceUSD, overallSharesCount);
        this.stocks.add(newStock);

        return newStock;
    }

    @Override
    public Stock getById(long id) {
        return this.stocks.stream()
            .filter(stock -> stock.getId() == id)
            .findFirst()
            .orElseThrow();
    }

    @Override
    public Collection<Share> buyShares(long stockId, int count, int maxPrice) {
        Stock stock = getById(stockId);
        return stock.buyShares(count, maxPrice);
    }

    @Override
    public void changePrice(long stockId, int newPrice) {
        Stock stock = getById(stockId);
        stock.changePrice(newPrice);
    }

}

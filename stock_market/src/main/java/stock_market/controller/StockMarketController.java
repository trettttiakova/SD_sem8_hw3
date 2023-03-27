package stock_market.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import stock_market.dto.NewStockDto;
import stock_market.entity.Share;
import stock_market.entity.Stock;
import stock_market.repository.InMemoryStockRepository;
import stock_market.repository.StockRepository;

import java.util.Collection;

@RestController
// P.S.: по-хорошему здесь надо передавать DTO, но для простоты передаю Stock и Share
public class StockMarketController {
    private final StockRepository stockRepository = new InMemoryStockRepository();

    // 1. Добавить новую компанию и ее акции
    @PostMapping(value = "stock", consumes = MediaType.APPLICATION_JSON_VALUE)
    public long createStock(
        @RequestBody NewStockDto newStockDto
    ) {
        return stockRepository.createNewStock(
            newStockDto.getCompanyName(),
            newStockDto.getOverallSharesCount(),
            newStockDto.getCurrentPriceUSD()
        );
    }

    // 2. Узнать текущую цену акции и их количество shares на бирже
    @RequestMapping("stocks/{id}/availableShareCount")
    public Stock getStock(@PathVariable("id") long id) {
        return stockRepository.getById(id);
    }

    // 3. Купить акции компаний по текущей цене, но не вышк maxPrice за акцию
    // Возвращает купленные акции
    @RequestMapping("stocks/{id}/buy/{count}/{maxPrice}")
    public Collection<Share> buyShares(
        @PathVariable("id") long stockId,
        @PathVariable("count") int count,
        @PathVariable("maxPrice") int maxPrice
    ) {
        return stockRepository.buyShares(stockId, count, maxPrice);
    }

    // 4. Динамически менять курс акций
    @RequestMapping("stocks/{id}/changePrice/{newPrice}")
    public void changeStockPrice(
        @PathVariable("id") long stockId,
        @PathVariable("newPrice") int newPrice
    ) {
        stockRepository.changePrice(stockId, newPrice);
    }

    @RequestMapping("stocks/{id}/sell/{count}")
    public void sellShares(
        @PathVariable("id") long stockId,
        @PathVariable("count") int count
    ) {
        stockRepository.sellShares(stockId, count);
    }
}

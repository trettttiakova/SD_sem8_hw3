package stock_market.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class Stock {
    private final long id;

    private final String companyName;

    /*
    Общее количество акций компании.
     */
    private int overallSharesCount;

    /*
    Остаток доступных к продаже акций.
     */
    private int availableSharesForSaleCount;

    private int currentPriceUSD;

    public Stock(long id, String companyName, int overallSharesCount, int currentPriceUSD) {
        this.id = id;
        this.companyName = companyName;
        this.overallSharesCount = overallSharesCount;
        this.currentPriceUSD = currentPriceUSD;
        this.availableSharesForSaleCount = overallSharesCount;
    }

    public boolean enoughShares(int count) {
        return count <= this.availableSharesForSaleCount;
    }

    public Collection<Share> buyShares(int count, int maxPrice) {
        if (count > this.availableSharesForSaleCount) {
            return null;
        }

        int fixedPrice = this.currentPriceUSD;
        if (fixedPrice > maxPrice) {
            return null;
        }

        List<Share> boughtShares = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            boughtShares.add(new Share(this, fixedPrice));
            this.availableSharesForSaleCount--;
        }

        return boughtShares;
    }

    public void changePrice(int newPriceUSD) {
        this.currentPriceUSD = newPriceUSD;

    }
}

package user_account.entity;

import lombok.Data;
import stock_market.entity.Share;
import stock_market.entity.Stock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class User {
    private final long id;
    private final String name;
    private int balanceUSD;

    private final Collection<Share> shares;

    public User(long id, String name) {
        this.id = id;
        this.name = name;
        this.shares = new ArrayList<>();
        this.balanceUSD = 0;
    }

    public void deposit(int depositUSD) {
        this.balanceUSD += depositUSD;
    }

    public Collection<Share> getShares() {
        return this.shares;
    }

    public int getOverallBalanceUSD() {
        return this.balanceUSD + this.shares.stream()
            .map(share -> share.getStock().getCurrentPriceUSD())
            .mapToInt(Integer::intValue)
            .sum();
    }

    public boolean hasEnoughMoneyToSpend(int amountUSD) {
        return amountUSD <= this.balanceUSD;
    }

    public boolean withdraw(int amountUSD) {
        this.balanceUSD -= amountUSD;
        return true;
    }

    public void addShares(Collection<Share> boughtShares) {
        this.shares.addAll(boughtShares);
    }

    public Integer sellShares(long stockId, int count) {
        List<Share> stockShares = this.getShares().stream()
            .filter(share -> share.getStock().getId() == stockId)
            .toList();
        if (stockShares.size() < count) { // Недостаточно акций для продажи
            return null;
        }
        int fixedPrice = stockShares.stream()
            .findFirst()
            .get()
            .getStock()
            .getCurrentPriceUSD();
        for (int i = 0; i < count; i++) {
            this.shares.remove(stockShares.get(i));
        }


        int soldAmount = fixedPrice * count;
        this.deposit(soldAmount);
        return soldAmount;
    }
}

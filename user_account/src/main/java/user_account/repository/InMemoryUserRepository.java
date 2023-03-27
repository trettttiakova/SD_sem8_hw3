package user_account.repository;

import org.springframework.stereotype.Repository;
import stock_market.entity.Share;
import user_account.StockMarketClient;
import user_account.entity.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private final List<User> users = new ArrayList<>();
    private final StockMarketClient client;

    public InMemoryUserRepository(StockMarketClient client) {
        this.client = client;
    }

    @Override
    public User getById(long id) {
        return this.users.stream()
            .filter(user -> user.getId() == id)
            .findFirst()
            .orElseThrow();
    }

    @Override
    public User createUser(String name) {
        User newUser = new User(users.size(), name);
        users.add(newUser);

        return newUser;
    }

    @Override
    public boolean deposit(long id, int depositBalanceUSD) {
        getById(id).deposit(depositBalanceUSD);
        return true;
    }

    @Override
    public Collection<Share> getUserShares(long id) {
        return getById(id).getShares();
    }

    @Override
    public int getUserOverallBalanceUSD(long id) {
        return getById(id).getOverallBalanceUSD(client);
    }

    @Override
    public int getUserBalanceUSD(long id) {
        return getById(id).getBalanceUSD();
    }
}

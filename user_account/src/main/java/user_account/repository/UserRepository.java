package user_account.repository;

import org.springframework.stereotype.Repository;
import stock_market.entity.Share;
import user_account.entity.User;

import java.util.Collection;

@Repository
public interface UserRepository {
    User getById(long id);

    User createUser(String name);

    boolean deposit(long id, int depositBalanceUSD);

    Collection<Share> getUserShares(long id);

    int getUserOverallBalanceUSD(long id);

    int getUserBalanceUSD(long id);
}

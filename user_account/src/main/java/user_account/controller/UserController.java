package user_account.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import stock_market.entity.Share;
import user_account.dto.NewUserDto;
import user_account.entity.User;
import user_account.facade.UserFacade;
import user_account.repository.UserRepository;

import java.util.Collection;

@RestController
@AllArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    private final UserFacade userFacade;

    // 1. Звести нового пользователя
    @PostMapping("stock")
    public User createUser(
        @RequestBody NewUserDto newUserDto
    ) {
        return userRepository.createUser(newUserDto.getName());
    }

    // 2. Добавить денежные средства на счет пользователя
    @RequestMapping("users/{id}/deposit/{depositAmount}")
    public boolean deposit(
        @PathVariable("id") long userId,
        @PathVariable("depositAmount") int depositAmount
    ) {
        return userRepository.deposit(userId, depositAmount);
    }

    // 3. Просматреть акции пользователя, их количество и текущую стоимость
    @RequestMapping("users/{id}/getShares")
    public Collection<Share> getUserShares(
        @PathVariable("id") long userId
    ) {
        return userRepository.getUserShares(userId);
    }

    // 4. Просмотреть, сколько сейчас суммарно у пользователя средств, если считать все акции
    // по текущей стоимости
    @RequestMapping("users/{id}/getOverallBalance")
    public int getOverallBalance(
        @PathVariable("id") long userId
    ) {
        return userRepository.getUserOverallBalanceUSD(userId);
    }

    // 4. Просмотреть баланс
    @RequestMapping("users/{id}/getUSDBalance")
    public int getUSDBalance(
        @PathVariable("id") long userId
    ) {
        return userRepository.getUserBalanceUSD(userId);
    }

    /*
    5. Купить акции для пользователя
    Фиксирует текущую стоимость акции и покупает только в том случае,
    если стоимость осталась такой или стала меньше.
    Возвращает флаг - удалось ли купить.
     */
    @RequestMapping("users/{id}/buyShares/{stockId}/{count}")
    public boolean buySharesForUser(
        @PathVariable("id") long userId,
        @PathVariable("stockId") long stockId,
        @PathVariable("count") int count
    ) {
        return userFacade.buyShares(userId, stockId, count);
    }

    // 6. Продать акцию пользователя
    // Возвращает цену, за которую удалось продать акцию, null если продать не удалось
    @RequestMapping("users/{id}/sellShare/{stockId}/{count}")
    public Integer sellSharesForUser(
        @PathVariable("id") long userId,
        @PathVariable("stockId") long stockId,
        @PathVariable("count") int count
    ) {
        return userFacade.sellShares(userId, stockId, count);
    }
}

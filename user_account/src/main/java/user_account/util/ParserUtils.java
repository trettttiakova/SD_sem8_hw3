package user_account.util;

import com.google.gson.Gson;
import stock_market.entity.Share;
import stock_market.entity.Stock;

import java.util.Collection;
import java.util.List;

public class ParserUtils {
    public static Stock parseStockJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Stock.class);
    }

    public static Collection<Share> parseSharesArray(String json) {
        Gson gson = new Gson();
        return List.of(gson.fromJson(json, Share[].class));
    }
}

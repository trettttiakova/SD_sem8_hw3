package stock_market.entity;

import lombok.Data;

@Data
public class Share {
    private final Stock stock;

    private final int paidPriceUSD;
}

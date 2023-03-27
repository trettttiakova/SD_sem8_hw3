package stock_market.dto;

import lombok.Data;

@Data
public class NewStockDto {
    private final String companyName;
    private final int overallSharesCount;
    private final int currentPriceUSD;
}

package user_account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import stock_market.entity.Share;
import stock_market.entity.Stock;
import user_account.util.ParserUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.Map;

public class StockMarketClient {
    private final String hostPort;

    public StockMarketClient(String host, int port) {
        this.hostPort = String.format("http://%s:%d", host, port);
    }

    public Stock getStock(long stockId) {
        return ParserUtils.parseStockJson(
            get(String.format("stocks/%d/availableShareCount", stockId))
                .body()
        );
    }

    public Collection<Share> buyShares(long stockId, int count, int maxPrice) {
        var response = get(String.format("stocks/%d/buy/%d/%d", stockId, count, maxPrice))
            .body();

        return ParserUtils.parseSharesArray(response);
    }

    public void changePrice(long stockId, int newPrice) {
        get(String.format("stocks/%d/changePrice/%d", stockId, newPrice));
    }

    public HttpResponse<String> get(String uri) {
        try {
            HttpRequest request;
            request = HttpRequest.newBuilder()
                .uri(new URI(this.hostPort + "/" + uri))
                .GET()
                .build();

            return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ignored) {
            return null;
        }
    }

    public HttpResponse<String> post(String uri, Map<String, String> body) throws JsonProcessingException {
        var objectMapper = new ObjectMapper();
        String requestBody = objectMapper
            .writeValueAsString(body);
        try {
            HttpRequest request;
            request = HttpRequest.newBuilder()
                .uri(new URI(this.hostPort + "/" + uri))
                .POST(
                    HttpRequest.BodyPublishers.ofString(requestBody)
                )
                .header("Content-Type", "application/json")
                .build();

            return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ignored) {
            return null;
        }
    }
}

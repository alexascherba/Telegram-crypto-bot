package innowice.java.hackathon.client;

import innowice.java.hackathon.exception.ServiceException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class CbrClient {
    @Autowired
    private OkHttpClient okHttpClient;

    @Value("${cbr.currency.rates.xml.url}")
    private String url;

    @Value("${cbr.currency.rates.symbol}")
    private String symbol;

    public String getCurrencyRatesXML() throws ServiceException {
        String fullUrl = url + "?symbol=" + symbol;

        var request = new Request.Builder()
                .url(fullUrl)
                .build();
        try (var response = okHttpClient.newCall(request).execute()){
            var body = response.body();
            return body == null ? null : body.string();
        } catch (IOException e) {
            throw new ServiceException("Ошибка получения курсов валют", e);
        }
    }
}

package innowice.java.hackathon.service;

import innowice.java.hackathon.entity.ExchangeRate;
import innowice.java.hackathon.entity.User;
import innowice.java.hackathon.exception.ServiceException;

public interface RateService {
    String getBitcoinExchangeRate () throws ServiceException;

    ExchangeRate save(ExchangeRate exchangeRate);

    ExchangeRate saveExchangeRate(Long chatId, String bitcoinRate, String formattedDateTime);
    ExchangeRate findByChartId(Long chartId);
}

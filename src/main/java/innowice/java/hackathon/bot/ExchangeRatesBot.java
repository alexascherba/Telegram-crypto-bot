package innowice.java.hackathon.bot;

import innowice.java.hackathon.entity.ExchangeRate;
import innowice.java.hackathon.entity.User;
import innowice.java.hackathon.exception.ServiceException;
import innowice.java.hackathon.service.RateService;
import innowice.java.hackathon.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

@Component
@EnableScheduling
public class ExchangeRatesBot extends TelegramLongPollingBot {
    private Long chatId;
    private String formattedText;
    private double previousBitcoinValue;
    private double percent;
    private static final int MAX_USERS = 1;
    private static final Logger LOG = LoggerFactory.getLogger(ExchangeRatesBot.class);
    private static final String START = "/start";
    private static final String BITCOIN = "/bitcoin";
    private static final String HELP = "/help";
    private static final String PUSH_MESSAGE = "/push_message";
    private static final String UP = "/up";
    private static final String UP_3 = "/up_3";
    private static final String UP_5 = "/up_5";
    private static final String UP_10 = "/up_10";
    private static final String UP_15 = "/up_15";
    private static final String DOWN = "/down";
    private static final String DOWN_3 = "/down_3";
    private static final String DOWN_5 = "/down_5";
    private static final String DOWN_10 = "/down_10";
    private static final String DOWN_15 = "/down_15";

    Timer timer = new Timer();

    @Autowired
    private RateService rateService;

    @Autowired
    private UserService userService;

    private String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return dateTime.format(formatter);
    }

    public ExchangeRatesBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }
    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        String massage = update.getMessage().getText();
        this.chatId = update.getMessage().getChatId();

        switch (massage){
            case START -> {
                String userName = update.getMessage().getFrom().getUserName();
                startCommand(chatId, userName);
            }
            case BITCOIN -> bitcoinCommand(chatId);
            case PUSH_MESSAGE -> pushMessageCommand(chatId);
            case HELP -> helpCommand(chatId);
            case UP -> upCommand(chatId);
            case UP_3 -> upCommandResult(chatId, percent = 3);
            case UP_5 -> upCommandResult(chatId, percent = 5);
            case UP_10 -> upCommandResult(chatId, percent = 10);
            case UP_15 -> upCommandResult(chatId, percent = 15);
            case DOWN -> downCommand(chatId);
            case DOWN_3 -> downCommandResult(chatId, percent = 3);
            case DOWN_5 -> downCommandResult(chatId, percent = 5);
            case DOWN_10 -> downCommandResult(chatId, percent = 10);
            case DOWN_15 -> downCommandResult(chatId, percent = 15);
            default -> unnoundCommand(chatId);
        }
    }

    @Override
    public String getBotUsername() {
        return "innowice_java_hackathon_bot";
    }

    private void startCommand(Long chatId, String userName) {

        User user = userService.findByUserName(userName);

        if (user == null) {
            userService.saveUser(chatId, userName);
        }

        String text = """
                Добро пожаловать в бот, %s!
                
                Здесь Вы сможете узнать курс валют для BITCOIN на сегодня и узнать о поовышении или понижении валюты.
                
                Для этого воспользуйтесь командами:
                /bitcoin - узнать текущий курс BITCOIN
                
                Для получения уведомлений о изменении курса воспользуйтесь командой:
                /push_message - получить уведомление о изменении курса
                
                Дополнительные команды:
                /help - получение справки
                """;
        var formattedText = String.format(text, userName);
        sendMessage(chatId, formattedText);
    }

    private void bitcoinCommand(Long chatId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String formattedDateTime = formatDateTime(now);

            String bitcoin = rateService.getBitcoinExchangeRate();
            String text = "Курс Bitcoin на %s составляет %s bitcoin";
            formattedText = String.format(text, formattedDateTime, bitcoin);

            ExchangeRate exchangeRate = rateService.findByChartId(chatId);

            if (exchangeRate == null) {
                rateService.saveExchangeRate(chatId, bitcoin, formattedDateTime);
            } else {
                exchangeRate.setPrice(bitcoin);
                exchangeRate.setDate(formattedDateTime);
                rateService.save(exchangeRate);
            }

        } catch (ServiceException e) {
            LOG.error("Ошибка получения курса Bitcoin", e);
            formattedText = "Не удалось получить курс Bitcoin, попробуйте позже";
        }
        sendMessage(chatId, formattedText);
    }
    @Scheduled(fixedRate = 20000)
    public void updateBitcoinExchangeRate() {
        if (this.chatId == null) {
            return;
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            String formattedDateTime = formatDateTime(now);

            String bitcoin = rateService.getBitcoinExchangeRate();
            ExchangeRate exchangeRate = rateService.findByChartId(this.chatId);

            if (exchangeRate == null) {
                previousBitcoinValue = Double.parseDouble(exchangeRate.getPrice());

                rateService.saveExchangeRate(this.chatId, bitcoin, formattedDateTime);
            } else {
                previousBitcoinValue = Double.parseDouble(exchangeRate.getPrice());

                exchangeRate.setPrice(bitcoin);
                exchangeRate.setDate(formattedDateTime);
                rateService.save(exchangeRate);
            }
            LOG.info("Данные Bitcoin обновлены: курс {}, время обновления {}", bitcoin, formattedDateTime);
        } catch (ServiceException e) {
            LOG.error("Ошибка обновления курса Bitcoin", e);
        }
    }
    private void pushMessageCommand(Long chatId) {
        String text = """
                Для получения уведомления о изменении курса валюты воспользуйтесь одной из следующих команд:
                
                /up - получить уведомление о повышении курса
                /down - получить уведомление о понижении курса
                
                Дополнительные команды:
                /help - получение справки
                """;
        sendMessage(chatId, text);
    }

    private void upCommand(Long chatId) {
        String text = """
                Выберете команду при повышении на сколько процентов вы хотите получить уведомление:
                
                /up_3 - получить уведомление при повышении на 3%
                
                /up_5 - получить уведомление при повышении на 5%
                
                /up_10 - получить уведомление при повышении на 10%
                
                /up_15 - получить уведомление при повышении на 15%
                
                Когда выбрана команда и происходит повышение стоимости монеты на заданное количество процентов,
                вам будет отправлено уведомление.
                
                Дополнительные команды:
                /help - получение справки
                """;
        sendMessage(chatId, text);
    }

    private void downCommand(Long chatId) {
        String text = """
                Выберете команду при повышении на сколько процентов вы хотите получить уведомление:
                
                /down_3 - получить уведомление при повышении на 3%
                
                /down_5 - получить уведомление при повышении на 5%
                
                /down_10 - получить уведомление при повышении на 10%
                
                /down_15 - получить уведомление при повышении на 15%
                
                Когда выбрана команда и происходит понижение стоимости монеты на заданное количество процентов,
                вам будет отправлено уведомление.
                
                Дополнительные команды:
                /help - получение справки
                """;
        sendMessage(chatId, text);
    }
    private void upCommandResult(Long chatId, double percent) {
        if (previousBitcoinValue == 0){
            String text = "Для получения некоторых данных нужно время. Повторите эту команду немного позже";
            sendMessage(chatId, text);
            return;
        }
        try {
            double bitcoin = Double.parseDouble(rateService.getBitcoinExchangeRate());
            double difference = bitcoin - previousBitcoinValue;
            double percentageDifference = (difference / previousBitcoinValue) * 100;

            if (percentageDifference >= percent) {
                String text = "Курс Bitcoin на данный момент составляет " + bitcoin + " bitcoin" +
                        " он подялся на " + percent + "% и более. Старая цена " + previousBitcoinValue + " bitcoin";
                sendMessage(chatId, text);
            } else {
                timer.schedule(new TimerTask() {
                    public void run() {
                        upCommandResult(chatId, percent);
                    }
                }, 20000);
            }

            LOG.info("Данные Bitcoin обновлены: percentageDifference {} >= percent {}", percentageDifference, percent);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }

    private void downCommandResult(Long chatId, double percent) {
        if (previousBitcoinValue == 0){
            String text = "Для получения некоторых данных нужно время. Повторите эту команду немного позже";
            sendMessage(chatId, text);
            return;
        }
        try {
            double bitcoin = Double.parseDouble(rateService.getBitcoinExchangeRate());
            double difference = bitcoin - previousBitcoinValue;
            double percentageDifference = (difference / previousBitcoinValue) * 100;

            if (percentageDifference <= -percent) {
                String text = "Курс Bitcoin на данный момент составляет " + bitcoin + " bitcoin" +
                        " он опустился на " + percent + "% и более. Старая цена " + previousBitcoinValue + " bitcoin";
                sendMessage(chatId, text);
            } else {
                timer.schedule(new TimerTask() {
                    public void run() {
                        downCommandResult(chatId, percent);
                    }
                }, 20000);
            }
            LOG.info("Данные Bitcoin обновлены: percentageDifference {} <= percent -{}", percentageDifference, percent);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }

    private void helpCommand(Long chatId) {
        String text =  """
                Справочная информация по боту
                
                Для получения текущего курса валют для BITCOIN воспользуйтесь командой:
                
                /bitcoin - курс BITCOIN
                
                Для получения уведомлений о повышении и понижении курса воспользуйтесь командами:
                
                /up - получить уведомление о повышении курса
                /down - получить уведомление о понижении курса
                """;
        sendMessage(chatId, text);
    }
    private void unnoundCommand(Long chatId) {
        String text = "Неизвестная команда";
        sendMessage(chatId, text);
    }
    private void sendMessage(Long chatId, String text) {
        var catIdStr = String.valueOf(chatId);
        var sendMessage = new SendMessage(catIdStr, text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOG.error("Ошибка отправки сообщения", e);
        }
    }
}

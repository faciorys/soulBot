package com.soulbot.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.soulbot.entities.Currency;
import com.soulbot.service.CurrencyConversionService;
import com.soulbot.service.CurrencyModeService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Bot {

    private final TelegramBot bot;
    private final CurrencyModeService currencyModeService = CurrencyModeService.getInstance();
    private final CurrencyConversionService currencyConversionService =
            CurrencyConversionService.getInstance();

    @EventListener(ApplicationReadyEvent.class)
    public void serve() {
        bot.setUpdatesListener(updates -> {
            updates.forEach(this::process);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void process(Update update) {
        CallbackQuery callbackQuery = update.callbackQuery();
        if (update.callbackQuery() != null) {
            handleCallback(callbackQuery);
        } else if (update.message() != null) {
            handleMessage(update.message());
        }
    }

    @SneakyThrows
    private void handleCallback(CallbackQuery callbackQuery) {
        Message message = callbackQuery.message();
        String[] param = callbackQuery.data().split(" ");
        String action = param[0];
        Currency newCurrency = Currency.valueOf(param[1]);
        switch (action) {
            case "ORIGINAL":
                currencyModeService.setOriginalCurrency(message.chat().id(), newCurrency);
                break;
            case "TARGET":
                currencyModeService.setTargetCurrency(message.chat().id(), newCurrency);
                break;
        }

        InlineKeyboardMarkup buttons = new InlineKeyboardMarkup();
        Currency originalCurrency = currencyModeService.getOriginalCurrency(message.chat().id());
        Currency targetCurrency = currencyModeService.getTargetCurrency(message.chat().id());
        for (Currency currency : Currency.values()) {
            buttons.addRow(new InlineKeyboardButton(getCurrencyButton(originalCurrency, currency))
                            .callbackData("ORIGINAL " + currency),
                    new InlineKeyboardButton(getCurrencyButton(targetCurrency, currency))
                            .callbackData("TARGET " + currency));


        }
        bot.execute(new EditMessageReplyMarkup(callbackQuery.from().id(), callbackQuery.message().messageId())
                .replyMarkup(buttons));
    }

    @SneakyThrows
    private void handleMessage(Message message) {
        if (message.text() != null && message.entities() != null) {
                switch (message.text()) {
                    case "/set_currency":
                        InlineKeyboardMarkup buttons = new InlineKeyboardMarkup();
                        Currency originalCurrency =
                                currencyModeService.getOriginalCurrency(message.chat().id());
                        Currency targetCurrency = currencyModeService.getTargetCurrency(message.chat().id());
                        for (Currency currency : Currency.values()) {
                            buttons.addRow(new InlineKeyboardButton(getCurrencyButton(originalCurrency, currency)).callbackData("ORIGINAL " + currency),
                                    new InlineKeyboardButton(getCurrencyButton(targetCurrency, currency)).callbackData("TARGET " + currency));

                        }
                        bot.execute(new SendMessage(message.chat().id(),"Change operation")
                                        .replyMarkup(buttons).replyMarkup(buttons));
                        return;
                }
            }
        if (message.text() != null) {
            String messageText = message.text();
            Optional<Double> value = parseDouble(messageText);
            Currency originalCurrency = currencyModeService.getOriginalCurrency(message.chat().id());
            Currency targetCurrency = currencyModeService.getTargetCurrency(message.chat().id());
            double ratio = currencyConversionService.getConversionRatio(originalCurrency, targetCurrency);
            value.ifPresent(aDouble -> bot.execute(new SendMessage(message.chat().id(), String.format(
                    "%4.2f %s is %4.2f %s",
                    aDouble, originalCurrency, (aDouble * ratio), targetCurrency))));
        }
    }

    private Optional<Double> parseDouble(String messageText) {
        try {
            return Optional.of(Double.parseDouble(messageText));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String getCurrencyButton(Currency saved, Currency current) {
        return saved == current ? current + " ✅            " : current.name();
    }
}



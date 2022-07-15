package com.soulbot.service.impl;

import com.soulbot.entities.Currency;
import com.soulbot.service.CurrencyConversionService;
import lombok.SneakyThrows;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NbrbCurrencyConversionService implements CurrencyConversionService {
    @Override
    public double getConversionRatio(Currency original, Currency target) {
        double originalRate = getRate(original);
        System.out.println(originalRate);
        double targetRate = getRate(target);
        System.out.println(targetRate);
        return originalRate / targetRate;
    }

    @SneakyThrows
    private double getRate(Currency currency) {
        if (currency == Currency.BYN) {
            return 1;
        }
        URL url = new URL("https://www.nbrb.by/api/exrates/rates/" + currency.getId());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JSONObject json = new JSONObject(response.toString());
        double rate = json.getDouble("Cur_OfficialRate");
        double scale = json.getDouble("Cur_Scale");
        return rate / scale;
    }
}
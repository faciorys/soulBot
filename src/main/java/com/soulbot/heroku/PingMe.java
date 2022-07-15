package com.soulbot.heroku;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;

@Service
@Slf4j
public class PingMe {
    @Scheduled(fixedRateString = "PT20M")
    @SneakyThrows
    public void ping() {
        try {
            URL url = new URL("https://soulmeowbot.herokuapp.com/");
            System.out.println(url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            log.info("Server is working...  Ping {} status: {}", url.getHost(), connection.getResponseCode());
            connection.disconnect();
        } catch (Exception e) {
            log.error("Error from pingMe {}", e.getMessage());
        }
    }
}
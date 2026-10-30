package com.ahorra.common.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Component
public class KeepAliveJob {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${base-url:http://localhost:2050}")
    private String baseUrl;

    @Value("${app.title}")
    private String apiTitle;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Scheduled(fixedRate = 180_000)
    public void ping() {
        URI uri = URI.create(baseUrl + "/" + apiTitle + "/keep-alive");
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            log.debug("Self-ping to {} returned {}", uri, response.statusCode());
        } catch (IOException e) {
            log.warn("Self-ping to {} failed", uri, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Self-ping to {} interrupted", uri);
        }
    }

    @Scheduled(fixedRate = 180_000)
    public void pingDatabase() {
        try {
            jdbcTemplate.execute("SELECT 1");
            log.debug("Database keep-alive query succeeded");
        } catch (DataAccessException e) {
            log.warn("Database keep-alive query failed", e);
        }
    }

}

package com.xolv.util;

import com.xolv.controller.RankingController;
import com.xolv.exception.SearchEngineException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;

public class HttpClientUtil {

    private static final Logger logger =
            Logger.getLogger(RankingController.class.getName());

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final int MAX_RETRIES = 3;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private HttpClientUtil() {
    }

    public static String get(String url) {

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {

            try {
                HttpRequest request = buildRequest(url);

                HttpResponse<String> response = CLIENT.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

                validateResponse(response);

                return response.body();

            } catch (Exception e) {

                if (attempt == MAX_RETRIES) {
                    throw new SearchEngineException(
                            "Failed to fetch URL after " + MAX_RETRIES + " attempts: " + url
                    );
                }
                sleep(attempt);
            }
        }
        throw new SearchEngineException("Unexpected error");
    }

    private static HttpRequest buildRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(REQUEST_TIMEOUT)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .build();
    }

    private static void validateResponse(HttpResponse<String> response) {
        int status = response.statusCode();

        if (status != 200) {
            throw new SearchEngineException("HTTP error: " + status);
        }

        String body = response.body();

        if (isBlocked(body)) {
            logger.info("Blocked by Google (captcha or JS required)");
            throw new SearchEngineException("Blocked by Google (captcha or JS required)");
        }
    }

    private static void sleep(int attempt) {
        try {
            Thread.sleep(1000L * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean isBlocked(String html) {
        return html.contains("detected unusual traffic")
                || html.contains("/httpservice/retry/enablejs")
                || html.contains("captcha")
                || html.contains("enablejs");
    }
}
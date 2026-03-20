package com.xolv.parser;

import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component("googleParser")
public class GoogleParser implements Parser {

    private static final String MARKER = "/url?q=";
    private static final int MAX_RESULTS = 100;

    @Override
    public List<String> parse(String html) {

        if (html == null || html.isBlank()) {
            return List.of();
        }

        Set<String> urls = new LinkedHashSet<>();
        int index = 0;

        while (urls.size() < MAX_RESULTS) {

            int start = html.indexOf(MARKER, index);
            if (start == -1) break;

            start += MARKER.length();

            int end = html.indexOf("&", start);
            if (end == -1) break;

            String rawUrl = html.substring(start, end);
            String url = URLDecoder.decode(rawUrl, StandardCharsets.UTF_8);
            if (isValidUrl(url)) {
                urls.add(url);
            }
            index = end;
        }

        return new ArrayList<>(urls);
    }

    private boolean isValidUrl(String url) {
        return url.startsWith("http")
                && !url.startsWith("https://www.google.")
                && !url.startsWith("http://www.google.");
    }
}
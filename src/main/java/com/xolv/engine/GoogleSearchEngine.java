package com.xolv.engine;

import com.xolv.exception.SearchEngineException;
import com.xolv.parser.Parser;
import com.xolv.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component("google")
public class GoogleSearchEngine implements SearchEngine {

    private static final String GOOGLE_URL =
            "https://www.google.com/search?q=%s&start=%d";

    private final Parser googleParser;

    public GoogleSearchEngine(@Qualifier("googleParser") Parser googleParser) {
        this.googleParser = googleParser;
    }

    @Override
    public List<String> search(String keyword) {

        List<String> urls = new ArrayList<>();
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

        for (int page = 0; page < 10 && urls.size() < 100; page++) {

            String url = String.format(GOOGLE_URL, encodedKeyword, page * 10);

            try {
                String html = HttpClientUtil.get(url);

                List<String> pageUrls = googleParser.parse(html);

                for (String u : pageUrls) {
                    if (urls.size() >= 100) break;

                    if (!urls.contains(u)) {
                        urls.add(u);
                    }
                }

            } catch (Exception e) {
                throw new SearchEngineException(
                        "Failed to fetch results from Google for keyword: " + keyword
                );
            }
        }

        return urls.size() > 100 ? urls.subList(0, 100) : urls;
    }
}
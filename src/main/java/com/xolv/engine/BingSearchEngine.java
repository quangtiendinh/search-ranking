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

@Component("bing")
public class BingSearchEngine implements SearchEngine {

    private static final String BING_URL =
            "https://www.bing.com/search?q=%s&first=%d";

    private final Parser bingParser;

    public BingSearchEngine(@Qualifier("bingParser") Parser bingParser) {
        this.bingParser = bingParser;
    }

    @Override
    public List<String> search(String keyword) {

        List<String> urls = new ArrayList<>();
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

        for (int page = 0; page < 10 && urls.size() < 100; page++) {

            String url = String.format(BING_URL, encodedKeyword, page * 10);

            try {
                String html = HttpClientUtil.get(url);
                urls.addAll(bingParser.parse(html));
            } catch (Exception e) {
                throw new SearchEngineException(
                        "Failed to fetch results from Bing for keyword: " + keyword
                );
            }
        }

        return urls.size() > 100 ? urls.subList(0, 100) : urls;
    }
}
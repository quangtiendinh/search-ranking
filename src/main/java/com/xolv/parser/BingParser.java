package com.xolv.parser;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BingParser implements Parser {
    @Override
    public List<String> parse(String html) {
        List<String> urls = new ArrayList<>();

        Pattern pattern = Pattern.compile("<li class=\"b_algo\".*?<a href=\"(https?://.*?)\"");
        Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
            urls.add(matcher.group(1));
        }
        return urls;
    }
}

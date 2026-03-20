package com.xolv.parser;

import java.util.List;

public interface Parser {
    List<String> parse(String html);
}
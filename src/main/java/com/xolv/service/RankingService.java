package com.xolv.service;

import com.xolv.model.RankingResult;

public interface RankingService {
    RankingResult getRankings(String engine, String keyword);
}

package com.xolv.controller;

import com.xolv.constant.ApiPath;
import com.xolv.model.RankingResult;
import com.xolv.service.RankingService;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@RestController
@RequestMapping(ApiPath.RANKINGS)
public class RankingController {

    private static final Logger logger =
            Logger.getLogger(RankingController.class.getName());

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping
    public RankingResult getRankings(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "google") String engine
    ) {

        logger.info("Received request: keyword=" + keyword + ", engine=" + engine);

        return rankingService.getRankings(engine.toLowerCase(), keyword);
    }
}
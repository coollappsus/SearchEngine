package main.controllers;

import main.dto.ResultDto;
import main.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatisticController {

    private final StatisticService statisticService;

    @Autowired
    public StatisticController(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> statistics() {
        try {
            return ResponseEntity.ok().body(statisticService.getStatistics());
        } catch (Exception e) {
            return ResponseEntity.ok().body(
                    new ResultDto("При построении статистики произошла неизвестная ошибка:\n" +
                            System.lineSeparator() + e.getMessage()));
        }
    }
}

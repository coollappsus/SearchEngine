package main.controllers;

import main.controllers.properties.SiteProperties;
import main.dto.ResultDto;
import main.dto.StatisticsDto;
import main.model.Site;
import main.service.SiteService;
import main.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class StatisticController {

    private final StatisticService statisticService;
    private final SiteService siteService;
    private final SiteProperties siteProperties;

    @Autowired
    public StatisticController(StatisticService statisticService, SiteService siteService, SiteProperties siteProperties) {
        this.statisticService = statisticService;
        this.siteService = siteService;
        this.siteProperties = siteProperties;
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> statistics() {
        try {
            ArrayList<Site> siteList = siteService.getAll();
            List<SiteProperties.SiteData> sites = siteProperties.getList();
            ArrayList<StatisticsDto.Detailed> detailedList = new ArrayList<>();
            siteService.checkOfExistenceAndDelete();
            if (siteList.size() != sites.size()) {
                siteService.create();
            }
            for (Site site : siteList) {
                StatisticsDto.Detailed detailed = statisticService.createDetailed(site);
                detailedList.add(detailed);
            }
            StatisticsDto.Statistics statistics = new StatisticsDto.Statistics(statisticService.createTotal(), detailedList);
            StatisticsDto statisticsDto = new StatisticsDto(statistics);
            return ResponseEntity.ok().body(statisticsDto);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new ResultDto("При построении статистики произошла неизвестная ошибка:\n" +
                            System.lineSeparator() + e.getMessage()));
        }
    }
}
